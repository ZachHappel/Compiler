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
            System.out.println("!!! Matched: " + seq);
            return t;
        }
        System.out.println("-- Match Failed");
        return t;
    }

    public void parseProgram () {
        System.out.println("@parseProgram, token_pointer: " + token_pointer);

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
        System.out.println("@parseBlock, token_pointer: " + token_pointer);
        NonTerminal block = new NonTerminal("Block");
        
        Terminal open_block = match("SYMBOL_OPENBLOCK", token_pointer); token_pointer++; 
        NonTerminal new_statement_list = new NonTerminal("STATEMENT_LIST");
        parseStatementList(new_statement_list);
        Terminal close_block = match("SYMBOL_CLOSEBLOCK", token_pointer); token_pointer++;
        System.out.println("After close block");
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
        System.out.println("@parseStatementList, token_pointer: " + token_pointer);
        // Builds off of STATEMENT_LIST
        //Production statement_list = new Production("STATEMENT_LIST");
        
        NonTerminal statement = parseStatement();
        if (statement.getSuccess()) {
            System.out.println("First Statement Success");
            statement_list.addChild(statement);
            statement_list.setSuccess(true);
            statement_list.addChild(new NonTerminal("STATEMENT_LIST"));
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
        System.out.println("@parseStatement, token_pointer: " + token_pointer);
        NonTerminal statement = new NonTerminal("STATEMENT");
        
        parsePrintStatement(statement);
        parseAssignmentStatement(statement);
        parseVariableDeclarationStatement(statement);
        //parseWhileStatement
        //parseIfStatement

        if (statement.getSuccess()) {
            System.out.println("@parseStat finished: " + statement.getChild(0).getName()); 
        }

        return statement;

    }


    // Modifies the parameter being passed to it
    public  void parsePrintStatement (NonTerminal statement) {
        System.out.println("@parsePrintStatement, token_pointer: " + token_pointer);
        int starting_token_pointer = token_pointer;
        NonTerminal print_statement = new NonTerminal("PRINT_STATEMENT"); // Added as child if all prove to be successful

        Terminal keyword_print = match("KEYWORD_PRINT", token_pointer); if (keyword_print.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 
        
        Terminal symbol_openparen = match("SYMBOL_OPENPAREN", token_pointer); if (symbol_openparen.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 

        NonTerminal expression = parseExpression();

        Terminal symbol_closeparen = match("SYMBOL_CLOSEPAREN", token_pointer); if (symbol_closeparen.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 

        if (keyword_print.getSuccess() && symbol_openparen.getSuccess() && expression.getSuccess() && symbol_closeparen.getSuccess()) {
            
            System.out.println("NonTerminal: Print Statement Recognized");
            print_statement.addChild(keyword_print);
            print_statement.addChild(symbol_openparen);
            print_statement.addChild(expression);
            print_statement.addChild(symbol_closeparen);
            statement.addChild(print_statement);
            statement.setSuccess(true);
        }

    }

    public void parseAssignmentStatement (NonTerminal statement) {
        System.out.println("@parseAssignmentStatement, token_pointer: " + token_pointer);
        int starting_token_pointer = token_pointer;
        NonTerminal assignment_statement = new NonTerminal("ASSIGNMENT_STATEMENT");

        Terminal identifier = match("IDENTIFIER", token_pointer); if (identifier.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 
        Terminal symbol_assignment = match("SYMBOL_ASSIGNMENT", token_pointer); if (symbol_assignment.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 
        NonTerminal expression = parseExpression(); 

        if (identifier.getSuccess() && symbol_assignment.getSuccess() && expression.getSuccess()) {
            System.out.println("NonTerminal: Assignment Statement Recognized");
            assignment_statement.addChild(identifier);
            assignment_statement.addChild(symbol_assignment);
            assignment_statement.addChild(expression);
            statement.addChild(assignment_statement);
            statement.setSuccess(true);
        }
    }

    public void parseVariableDeclarationStatement (NonTerminal statement) {
        System.out.println("@parseVariableDeclarationStatement, token_pointer: " + token_pointer);
        int starting_token_pointer = token_pointer;
        NonTerminal variable_declaration_statement = new NonTerminal("VARIABLE_DECLARATION");
        String token_name = (token_stream.get(token_pointer)).getName();
        
        if (token_name.equals("KEYWORD_INT") || token_name.equals("KEYWORD_STRING") || token_name.equals("KEWYORD_BOOLEAN")) {
            Terminal type = match(token_name, token_pointer); if (type.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 
            
            if ((token_stream.get(token_pointer).getName()).equals("IDENTIFIER")) {
                Terminal identifier = match("IDENTIFIER", token_pointer); if (identifier.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 
            
                variable_declaration_statement.addChild(type);
                variable_declaration_statement.addChild(identifier);
                statement.addChild(variable_declaration_statement);
                statement.setSuccess(true);
                return;
            }
        }
        
    }

    // Creates NonTerminal "EXPRESSION" and returns it
    public NonTerminal parseExpression () {
        System.out.println("@parseExpression, token_pointer: " + token_pointer);
        NonTerminal expression = new NonTerminal("EXPRESSION");  
        
        parseStringExpression(expression); //Should be checking to see if done here
        //IntExpression
        //StringExpression
        //BooleanExpression
        parseIdentifierExpresison(expression);



        System.out.println(".... (location, after expression checks) ....");
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

    public void parseIdentifierExpresison (NonTerminal expression) {
        System.out.println("@parseIdentifierExpression, token_pointer: " + token_pointer);
        Token token = token_stream.get(token_pointer);
        if (token.getName().equals("IDENTIFIER")) {
            Terminal identifier = match("IDENTIFIER", token_pointer); token_pointer++; // Assured
            if (identifier.getSuccess()) {
                expression.addChild(identifier);
                expression.setSuccess(true);
                return;
            }
        }
    }    

    public void parseStringExpression (NonTerminal expression) {
        System.out.println("@parseStringExpression, token_pointer: " + token_pointer);

        int starting_token_pointer = token_pointer;

        NonTerminal string_expression = new NonTerminal("STRING_EXPRESSION");
       
        Terminal keyword_opening_stringexprboundary = match("SYMBOL_STRINGEXPRBOUNDARY", token_pointer); if (keyword_opening_stringexprboundary.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 
        NonTerminal character_list = parseCharacterList(new NonTerminal("CHARACTER_LIST"));
        Terminal keyword_closing_stringexprboundary = match("SYMBOL_STRINGEXPRBOUNDARY", token_pointer); if (keyword_closing_stringexprboundary.getSuccess()) token_pointer++; else {token_pointer = starting_token_pointer; return;} 

        if (keyword_opening_stringexprboundary.getSuccess() && character_list.getSuccess() && keyword_closing_stringexprboundary.getSuccess()) {
            System.out.println("> Matched STRING_EXPRESSION");
            string_expression.addChild(keyword_opening_stringexprboundary);     
            string_expression.addChild(character_list);
            string_expression.addChild(keyword_closing_stringexprboundary);
            string_expression.setSuccess(true);
            expression.addChild(string_expression); 
            expression.setSuccess(true);
            return; 
            //return expression;
        } else {
            token_pointer = starting_token_pointer; // Reset pointer to where it was so next expression attempt starts at same spot
        }
    }

    // Accepts CharacterList nonterminal
    // Returns CharacterList nonterminal
    public  NonTerminal parseCharacterList (NonTerminal cl) {
        System.out.println("@parseCharacterList, token_pointer: " + token_pointer);
        
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
        cl.setSuccess(true); // Maybe?
        System.out.println("Returning Character List with Success = False");
        return cl; // cl would 
    }


    public String stringOfSpaces (int amount) {
        String s = "";
        for (int j = 0; j <= amount-1; j++) {
            s = s + "  ";
        } return s;
    }

    public void recursivePrint (Production p, int index) {
        //System.out.println("Rec: " + p.getChildren().size());
        for (int i = 0; i <= p.getChildren().size() - 1; i++ ) {
            Production c = p.getChild(i);
            String spaces = stringOfSpaces(index);
            System.out.println(spaces + index + ". " + c.getName() + " " + ( (c.getClass().getSimpleName()).equals("Terminal") ? "[" + ( (Terminal) c).getTokenAttribute() + "]": ""));
            recursivePrint(c, index + 1);
        }

    };


    public  ArrayList<Production> ParseTokens ( ArrayList<Token> ts, Toolkit tk ) {
        System.out.println("Parse Tokens: \n\n");
        token_stream = ts; 
        toolkit = tk; 
        parseProgram();
        System.out.println("Derivation: " );
        for (Production i : derivation) {
            System.out.println("Type: " + i.getName());
            recursivePrint(i, 1);
        }

        return derivation;

    }


}
