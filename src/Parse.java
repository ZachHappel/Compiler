import java.util.ArrayList;

public class Parse {

    // TODO: Add error handling? How to decide when it failed?

    //ArrayList<Token> token_stream;
    public  ArrayList<Production> derivation = new ArrayList<>();
    public  ArrayList<Token> token_stream;
    public  Toolkit toolkit;
    public int token_pointer = 0;

    public Parse () { }

    public Terminal match (String seq, int token_pointer_local) {
        Terminal t = new Terminal(seq);
        System.out.println("@match - Attempting to match pattern: " + seq + " || Got: " + token_stream.get(token_pointer_local).getName());
        if (token_stream.get(token_pointer_local).getName().equals(seq)) {
            t.setSuccess(true); // Important
            t.setToken(token_stream.get(token_pointer_local));
            t.setTokenName(seq);
            t.setTokenAttribute(token_stream.get(token_pointer_local).getAttribute());
            System.out.println("Matched: " + seq);
            return t;
        }
        System.out.println("-- Match Failed");
        return t;
    }

    public  void parseProgram () {
        System.out.println("@parseProgram");
        NonTerminal program = new NonTerminal("Program");
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
    public void parseBlock(NonTerminal program) {
        System.out.println("@parseBlock");
        NonTerminal block = new NonTerminal("Block");
        
        Terminal open_block = match("SYMBOL_OPENBLOCK", token_pointer); token_pointer++; 
        NonTerminal new_statement_list = new NonTerminal("STATEMENT_LIST");
        parseStatementList(new_statement_list);
        Terminal close_block = match("SYMBOL_CLOSEBLOCK", token_pointer); token_pointer++;
        
        if (open_block.getSuccess() && new_statement_list.getSuccess() && close_block.getSuccess()) {
            block.addChild(open_block); // Terminal
            block.addChild(new_statement_list); // NonTerminal
            block.addChild(close_block); // Terminal
            program.addChild(block);
        } else {
            System.out.println("Error matching block");
        }
        

    }

    public  NonTerminal parseStatementList(NonTerminal statement_list) {
        System.out.println("@parseStatementList");
        // Builds off of STATEMENT_LIST
        //Production statement_list = new Production("STATEMENT_LIST");
        
        NonTerminal statement = parseStatement();
        if (statement.getSuccess()) {
            statement_list.addChild(statement);
            statement_list.addChild(new Production("STATEMENT_LIST"));
            parseStatementList((NonTerminal) statement_list.getChild(1));
            return statement_list; 
            //NonTerminal child_statement_list = new NonTerminal("STATEMENT_LIST");
            //return parseStatementList(child_statement_list); // recursively call
        }

        // If EMPTY (E)
        System.out.println("Empty Statement List Added");
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
    public  NonTerminal parseStatement () {
        System.out.println("@parseStatement");
        NonTerminal statement = new NonTerminal("STATEMENT");
        parsePrintStatement(statement);
        
        if (statement.getSuccess()) {
            System.out.println("NonTerminal: Statement Recognized, " + statement.getChild(0).getName()); 
        }

        return statement;

    }


    // Modifies the parameter being passed to it
    public  void parsePrintStatement (NonTerminal statement) {
        System.out.println("@parsePrintStatement");
        int starting_token_pointer = token_pointer;
       // NonTerminal print_statement = new NonTerminal("PRINT_STATEMENT");

        Terminal keyword_print = match("KEYWORD_PRINT", token_pointer); if (keyword_print.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 
        
        Terminal symbol_openparen = match("SYMBOL_OPENPAREN", token_pointer); if (symbol_openparen.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 

        NonTerminal expression = parseExpression();

        Terminal symbol_closeparen = match("SYMBOL_CLOSEPAREN", token_pointer); if (symbol_closeparen.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 

        if (keyword_print.getSuccess() && symbol_openparen.getSuccess() && expression.getSuccess() && symbol_closeparen.getSuccess()) {
            System.out.println("NonTerminal: Print Statement Recognized");
        }

    }

    // Creates NonTerminal "EXPRESSION" and returns it
    public  NonTerminal parseExpression () {
        System.out.println("@parseExpression");
        NonTerminal expression = new NonTerminal("EXPRESSION");  
        
        parseStringExpression(expression); 
        if (expression.getSuccess()) {
            System.out.println("NonTerminal: Expression Recognized, " + expression.getChild(0).getName());
            return expression;
        }  // Done

        /**
         * Remaining
         */

        //if (expression.getChildren().size() > 0) { System.out.println("Production: Expression Recognized... This child was added: " + expression.getChild(0).getName()); }

        return expression;
    }
    

    public  void parseStringExpression (NonTerminal expression) {
        System.out.println("@parseStringExpression");

        int starting_token_pointer = token_pointer;

        NonTerminal string_expression = new NonTerminal("STRING_EXPRESSION");
       
        Terminal keyword_opening_stringexprboundary = match("SYMBOL_STRINGEXPRBOUNDARY", token_pointer); if (keyword_opening_stringexprboundary.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 
        NonTerminal character_list = parseCharacterList(new NonTerminal("CHARACTER_LIST"));
        Terminal keyword_closing_stringexprboundary = match("SYMBOL_STRINGEXPRBOUNDARY", token_pointer); if (keyword_closing_stringexprboundary.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 

        if (keyword_opening_stringexprboundary.getSuccess() && character_list.getSuccess() && keyword_closing_stringexprboundary.getSuccess()) {
            string_expression.addChild(keyword_opening_stringexprboundary);     
            string_expression.addChild(character_list);
            string_expression.addChild(keyword_closing_stringexprboundary);
            string_expression.setSuccess(true);
            expression.addChild(string_expression); 
            return; 
            //return expression;
        } else {
            token_pointer = starting_token_pointer; // Reset pointer to where it was so next expression attempt starts at same spot
        }
    }

    // Accepts CharacterList nonterminal
    // Returns CharacterList nonterminal
    public  NonTerminal parseCharacterList (NonTerminal cl) {
        System.out.println("@parseCharacterList");
        
        int starting_token_pointer = token_pointer;
        String first_token_name = token_stream.get(token_pointer).getName(); 

        if (first_token_name == "SPACE" || first_token_name == "CHARACTER") {
            
            Terminal space_or_char = match(first_token_name, token_pointer); 
            
            if (space_or_char.getSuccess()) {
                token_pointer++; // Increase pointer
                cl.addChild(space_or_char); // Add matched terminal to cl's children
                cl.addChild(new NonTerminal("CHARACTER_LIST")); // Add new cl to cl's children
                parseCharacterList((NonTerminal) cl.getChild(1)); // call parseCharacter again cl which was add to children of original cl
                cl.setSuccess(true);
                return cl;
            }
            else {
                token_pointer = starting_token_pointer; 
                cl.setSuccess(false);
                return cl; // Return current state of cl
            } 
        }

        System.out.println("Returning Character List with Success = False");
        return cl; // cl would 
    }


    public  ArrayList<Production> ParseTokens ( ArrayList<Token> ts, Toolkit tk ) {
        System.out.println("Parse Tokens: \n\n");
        token_stream = ts; 
        toolkit = tk; 
        parseProgram();

        return derivation;

    }


}
