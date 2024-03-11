import java.util.ArrayList;

public class Parse {

    // TODO: Add error handling? How to decide when it failed?

    //ArrayList<Token> token_stream;
    public static ArrayList<Production> derivation = new ArrayList<>();
    public static ArrayList<Token> token_stream;
    public static Toolkit toolkit;
    public int token_pointer = 0;

    public Parse () { }

    public Terminal match (String seq, int token_pointer_local) {
        Terminal t = new Terminal(seq);
        
        
        if (token_stream.get(token_pointer_local).getName() == seq) {
            t.setSuccess(true); // Important
            t.setToken(token_stream.get(token_pointer_local));
            t.setTokenName(seq);
            t.setTokenAttribute(token_stream.get(token_pointer_local).getAttribute());
            return t;
        }

        return t;
    }

    public void parseProgram () {
        Production program = new Production("Program");
        parseBlock(program);
        Terminal eop = match("EOP", token_pointer);
        
        if (eop.getSuccess()) {
            derivation.add(program);
            derivation.add(eop);
        }
    }


    /**
     * Potential Error Checking Approach:
     * if (open_block.getSuccess()) {
            Production statement_list = parseStatementList();
            Terminal close_block = match("KEYWORD_CLOSEBLOCK", token_pointer);
        } else {
            System.out.println("Error - No Open Block");
        }
     * 
     * Messy and possibly avoidable, although avoidance strategy may provide less useful error messages
     */
    public void parseBlock(Production program) {
        
        NonTerminal block = new NonTerminal("Block");
        Terminal open_block = match("KEYWORD_OPENBLOCK", token_pointer); token_pointer++; 
        NonTerminal new_statement_list = new NonTerminal("STATEMENT_LIST");
        parseStatementList(new_statement_list);
        Terminal close_block = match("KEYWORD_CLOSEBLOCK", token_pointer); token_pointer++;
        
        if (open_block.getSuccess() && new_statement_list.getSuccess() && close_block.getSuccess()) {
            block.addChild(open_block);
            block.addChild(new_statement_list);
            block.addChild(close_block);
        } else {
            System.out.println("Error matching block");
        }
        

    }

    public NonTerminal parseStatementList(Production statement_list) {
        // Builds off of STATEMENT_LIST
        //Production statement_list = new Production("STATEMENT_LIST");
        
        NonTerminal statement = parseStatement();
        if (statement.getSuccess()) {
            statement_list.addChild(statement);
            Production child_statement_list = new Production("STATEMENT_LIST");
            return parseStatementList(child_statement_list); // recursively call
        }

        // If EMPTY (E)
        return new NonTerminal("STATEMENT_LIST");
    
    }

    /**
     * = PrintStatement
    ::== AssignmentStatement
    ::== VarDecl
    ::== WhileStatement
    ::== IfStatement
    ::== Block
     */

     // Creates NonTerminal "STATEMENT" and returns it
    public NonTerminal parseStatement () {
        NonTerminal statement = new NonTerminal("STATEMENT");
        parsePrintStatement(statement);
        
        if (statement.getSuccess()) {
            System.out.println("Production: Statement Recognized"); 
        }

        return statement;

    }


    // Modifies the parameter being passed to it
    public void parsePrintStatement (Production statement) {
        int starting_token_pointer = token_pointer;
       // NonTerminal print_statement = new NonTerminal("PRINT_STATEMENT");

        Terminal keyword_print = match("KEYWORD_PRINT", token_pointer); if (keyword_print.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 
        
        Terminal symbol_openparen = match("SYMBOL_OPENPAREN", token_pointer); if (symbol_openparen.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 

        NonTerminal expression = parseExpression();

        Terminal symbol_closeparen = match("SYMBOL_CLOSEPAREN", token_pointer); if (symbol_closeparen.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 


    }

    // Creates NonTerminal "EXPRESSION" and returns it
    public NonTerminal parseExpression () {
        NonTerminal expression = new NonTerminal("EXPRESSION");
        
        parseStringExpression(expression);

        if (expression.getSuccess()) { System.out.println("Production: Expression Recognized, " + expression.getName()); }

        return expression;
    }
    

    public void parseStringExpression (NonTerminal expression) {
        //SYMBOL_STRINGEXPRBOUNDARY
        int starting_token_pointer = token_pointer;

        Terminal keyword_opening_stringexprboundary = match("SYMBOL_STRINGEXPRBOUNDARY", token_pointer); if (keyword_opening_stringexprboundary.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 
     
        NonTerminal character_list = parseCharacterList();
        
        Terminal keyword_closing_stringexprboundary = match("SYMBOL_STRINGEXPRBOUNDARY", token_pointer); if (keyword_closing_stringexprboundary.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 

    }

    public NonTerminal parseCharacterList () {
        int starting_token_pointer = token_pointer;
        NonTerminal character_list = new NonTerminal("CHARACTER_LIST");
        String first_token_name = token_stream.get(token_pointer).getName(); 

        if (first_token_name == "SPACE" || first_token_name == "CHARACTER") {
            Terminal space_or_char = match(first_token_name, token_pointer); 
            
            if (space_or_char.getSuccess()) {
                token_pointer++; 
            } 
            
            else {
                token_pointer = starting_token_pointer; 
                return character_list;
            } 
        }

        return character_list; 
    }


    public static ArrayList<Production> ParseTokens ( ArrayList<Token> ts, Toolkit tk ) {
        
        token_stream = ts; 
        toolkit = tk; 

        return derivation;

    }


}
