import java.util.ArrayList;


/**
 

while, if, print, (, ), {, }

type: int, string, boolean

space? 

boolval: true, false

intop: + 

boolop: ==, != 

 */


public class OldParse {

    // TODO: Add error handling? How to decide when it failed?

    //ArrayList<Token> token_stream;
    public  ArrayList<Production> derivation = new ArrayList<>();
    public  ArrayList<Token> token_stream;
    public  Toolkit toolkit;
    public int token_pointer = 0;

    public OldParse () { }

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

    public String getCurrentTokenName() {
        return (token_stream.get(token_pointer)).getName(); 
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
        NonTerminal block = new NonTerminal("BLOCK");
        
        Terminal open_block = match("SYMBOL_OPENBLOCK", token_pointer); { token_pointer++; } 
        NonTerminal new_statement_list = new NonTerminal("STATEMENT_LIST");
        parseStatementList(new_statement_list);
        Terminal close_block = match("SYMBOL_CLOSEBLOCK", token_pointer); token_pointer++;
        System.out.println("After close block");
        if (open_block.getSuccess() && new_statement_list.getSuccess() && close_block.getSuccess()) {
            System.out.println("Full Block Match: SYMBOL_OPENBLOCK, STATEMENTLIST, SYMBOL_CLOSEBLOCK" );
            block.addChild(open_block); // Terminal
            block.addChild(new_statement_list); // NonTerminal
            block.addChild(close_block); // Terminal
            block.setSuccess(true);
            program.addChild(block);
            program.setSuccess(true);
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
        if (statement.getSuccess()) {
            System.out.println("@parseStatement finished: " + statement.getChild(0).getName()); 
            return statement;
        }

        parseAssignmentStatement(statement);
        if (statement.getSuccess()) {
            System.out.println("@parseStatement finished: " + statement.getChild(0).getName()); 
            return statement;
        }

        parseVariableDeclarationStatement(statement);
        if (statement.getSuccess()) {
            System.out.println("@parseStatement finished: " + statement.getChild(0).getName()); 
            return statement;
        }

        parseWhileStatement(statement);
        if (statement.getSuccess()) {
            System.out.println("@parseStatement finished: " + statement.getChild(0).getName()); 
            return statement;
        }
        //parseIfStatement

        if (statement.getSuccess()) {
            System.out.println("@parseStatement finished: " + statement.getChild(0).getName()); 
            return statement;
        }

        System.out.println("@parseStatement - Failed");
        return statement;

    }


    // Modifies the parameter being passed to it
    public  void parsePrintStatement (NonTerminal statement) {
        System.out.println("@parsePrintStatement, token_pointer: " + token_pointer);
        int starting_token_pointer = token_pointer;
        NonTerminal print_statement = new NonTerminal("PRINT_STATEMENT"); // Added as child if all prove to be successful

        Terminal keyword_print = match("KEYWORD_PRINT", token_pointer); if (keyword_print.getSuccess()) { token_pointer++; } else {token_pointer = starting_token_pointer; return;} 
        
        Terminal symbol_openparen = match("SYMBOL_OPENPAREN", token_pointer); if (symbol_openparen.getSuccess()) { token_pointer++; } else {token_pointer = starting_token_pointer; return;} 

        NonTerminal expression = parseExpression();

        Terminal symbol_closeparen = match("SYMBOL_CLOSEPAREN", token_pointer); if (symbol_closeparen.getSuccess()) { token_pointer++; } else {token_pointer = starting_token_pointer; return;} 

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

        Terminal identifier = match("IDENTIFIER", token_pointer); if (identifier.getSuccess()) { { token_pointer++; } } else {token_pointer = starting_token_pointer; return;} 
        Terminal symbol_assignment = match("SYMBOL_ASSIGNMENT", token_pointer); if (symbol_assignment.getSuccess()) { { token_pointer++; } } else {token_pointer = starting_token_pointer; return;} 
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
        NonTerminal variable_declaration_statement = new NonTerminal("VARDECL_STATEMENT");
        String token_name = (token_stream.get(token_pointer)).getName();
        
        if (token_name.equals("KEYWORD_INT") || token_name.equals("KEYWORD_STRING") || token_name.equals("KEWYORD_BOOLEAN")) {
            Terminal type = match(token_name, token_pointer); if (type.getSuccess()) { { type.setName("Type"); token_pointer++; } } else {token_pointer = starting_token_pointer; return;} 

            if ((token_stream.get(token_pointer).getName()).equals("IDENTIFIER")) {
                Terminal identifier = match("IDENTIFIER", token_pointer); if (identifier.getSuccess()) { { identifier.setName("Identifier"); token_pointer++; } } else {token_pointer = starting_token_pointer; return;} 
                
                variable_declaration_statement.addChild(type);
                variable_declaration_statement.addChild(identifier);
                statement.addChild(variable_declaration_statement);
                statement.setSuccess(true);
                return;
            }
        }
        
    }

    public void parseWhileStatement (NonTerminal statement) {
        System.out.println("@parseWhileStatement, token_pointer: " + token_pointer);
        int starting_token_pointer = token_pointer;
        NonTerminal while_statement = new NonTerminal("WHILE_STATEMENT");
        String token_name = (token_stream.get(token_pointer)).getName();
        
        if (token_name.equals("KEYWORD_WHILE")) {
            Terminal keyword_while = match(token_name, token_pointer); if (keyword_while.getSuccess()) { { token_pointer++; } } else {token_pointer = starting_token_pointer; return;} 
            
            // For expression, if after parseBooleanExpression -> getSuccess() == true, the child will be the boolean expression
            NonTerminal expression = new NonTerminal("BOOLEAN_EXPRESSION"); 
            parseBooleanExpression(expression); 
            expression = expression.getSuccess() ? (NonTerminal) expression.getChild(0) : expression; // Boolean expression
            
            // For nt, if after parseBlock -> getSuccess() == true, the child will be BLOCK
            NonTerminal nt = new NonTerminal("BLOCK"); 
            parseBlock(nt);
            nt = nt.getSuccess() ? (NonTerminal) nt.getChild(0) : nt; 

            if (expression.getSuccess() && nt.getSuccess()) {
                while_statement.addChild(keyword_while);
                while_statement.addChild(expression);
                while_statement.addChild(nt); 
                while_statement.setSuccess(true);
                statement.addChild(while_statement);
                statement.setSuccess(true); // Success, full while statement
            }
        }

    }

    // Creates NonTerminal "EXPRESSION" and returns it
    public NonTerminal parseExpression () {
        System.out.println("@parseExpression, token_pointer: " + token_pointer);
        NonTerminal expression = new NonTerminal("EXPRESSION");  
        
        parseIntExpression(expression);
        parseStringExpression(expression); //Should be checking to see if done here
        parseBooleanExpression(expression);
        parseIdentifierExpresison(expression);
        
        System.out.println(".... (location, after expression checks) ....");
        if (expression.getSuccess()) {
            System.out.println("@parseExpression - Expression Recognized, " + expression.getChild(0).getName());
            return expression;
        }  // Done

        else {
            System.out.println("@parseExpression - Failed");
        }
        /**
         * Remaining
         */

        //if (expression.getChildren().size() > 0) { System.out.println("Production: Expression Recognized... This child was added: " + expression.getChild(0).getName()); }

        return expression;
    }

    public void parseBooleanExpression (NonTerminal expression) {
        System.out.println("@parseBooleanExpression, token_pointer: " + token_pointer);
        int starting_token_pointer = token_pointer;
        NonTerminal boolean_expression = new NonTerminal("BOOLEAN_EXPRESSION");
        
        if (getCurrentTokenName().equals("SYMBOL_OPENPAREN")) {
            Terminal keyword_openparen = match("SYMBOL_OPENPAREN", token_pointer); if (keyword_openparen.getSuccess()) { token_pointer++; } else {token_pointer = starting_token_pointer; return; } 
            NonTerminal expression_first = parseExpression();
            
            if (getCurrentTokenName().equals("SYMBOL_EQUIVALENCE") || getCurrentTokenName().equals("SYMBOL_INEQUIVALENCE") ) {
                Terminal symbol_boolop = match(getCurrentTokenName(), token_pointer); if (symbol_boolop.getSuccess()) { token_pointer++; } else {token_pointer = starting_token_pointer; return; } 
                NonTerminal expression_second = parseExpression();

                if (getCurrentTokenName().equals("SYMBOL_CLOSEPAREN")) {
                    Terminal keyword_closeparen = match(getCurrentTokenName(), token_pointer); if (keyword_closeparen.getSuccess()) { token_pointer++; } else {token_pointer = starting_token_pointer; return; } 
                    
                    if (expression_first.getSuccess() && expression_second.getSuccess()) {
                        boolean_expression.addChild(keyword_openparen);
                        boolean_expression.addChild(expression_first);
                        boolean_expression.addChild(symbol_boolop);
                        boolean_expression.addChild(expression_second);
                        boolean_expression.addChild(keyword_closeparen);
                        boolean_expression.setSuccess(true);
                        expression.addChild(boolean_expression);
                        expression.setSuccess(true);
                        return; // Success, boolean_expression is child of NonTerminal object "expression" passed to the func
                    }
                }
            }
        } 
        
        // true / false
        else if ( getCurrentTokenName().equals("KEYWORD_TRUE") || getCurrentTokenName().equals("KEYWORD_FALSE") ) {
            Terminal keyword_boolval = match(getCurrentTokenName(), token_pointer); if (keyword_boolval.getSuccess()) { token_pointer++; } else {token_pointer = starting_token_pointer; return; } 
            boolean_expression.addChild(keyword_boolval);
            boolean_expression.setSuccess(true);
            expression.addChild(boolean_expression);
            expression.setSuccess(true);
            return; 
        }

        return; // success = false
    }

    public void parseIntExpression (NonTerminal expression) {
        System.out.println("@parseBooleanExpression, token_pointer: " + token_pointer);
        int starting_token_pointer = token_pointer;
        NonTerminal int_expression = new NonTerminal("INT_EXPRESSION");

        if (getCurrentTokenName().equals("DIGIT")) {
            Terminal digit_first = match("DIGIT", token_pointer); if (digit_first.getSuccess()) { token_pointer++; } else {token_pointer = starting_token_pointer; return; } 

            if (getCurrentTokenName().equals("SYMBOL_INTOP")) {
                Terminal intop = match("SYMBOL_INTOP", token_pointer); if (intop.getSuccess()) { token_pointer++; } else {token_pointer = starting_token_pointer; return; }
                intop.setName("INTOP");
                NonTerminal child_expression = parseExpression();


                if (child_expression.getSuccess()) {
                    //Terminal digit_second = match("DIGIT", token_pointer); if (digit_second.getSuccess()) { token_pointer++; } else {token_pointer = starting_token_pointer; return; }

                    int_expression.addChild(digit_first);
                    int_expression.addChild(intop);
                    int_expression.addChild(child_expression);
                    int_expression.setSuccess(true);
                    expression.addChild(int_expression);
                    expression.setSuccess(true);
                    return;
                }

            } else {
                // Single digit IntExpr
                int_expression.addChild(digit_first);
                int_expression.setSuccess(true);
                expression.addChild(int_expression);
                expression.setSuccess(true);
                return;
            }
        
        } 

    }


    public void parseIdentifierExpresison (NonTerminal expression) {
        System.out.println("@parseIdentifierExpression, token_pointer: " + token_pointer);
        Token token = token_stream.get(token_pointer);
        if (token.getName().equals("IDENTIFIER")) {
            Terminal identifier = match("IDENTIFIER", token_pointer); { token_pointer++; } // Assured
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
       
        Terminal keyword_opening_stringexprboundary = match("SYMBOL_STRINGEXPRBOUNDARY", token_pointer); if (keyword_opening_stringexprboundary.getSuccess()) { token_pointer++; } else {token_pointer = starting_token_pointer; return;} 
        NonTerminal character_list = parseCharacterList(new NonTerminal("CHARACTER_LIST"));
        Terminal keyword_closing_stringexprboundary = match("SYMBOL_STRINGEXPRBOUNDARY", token_pointer); if (keyword_closing_stringexprboundary.getSuccess()) { token_pointer++; } else {token_pointer = starting_token_pointer; return;} 

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
                { token_pointer++; } // Increase pointer
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


    public String stringOfCharacters(int amount, String character) {
        String s = "";
        for (int j = 0; j <= amount-1; j++) {
            s = s + character;
        } return s;
    }

    public void recursivePrint (Production p, int index) {
        //System.out.println("Rec: " + p.getChildren().size());
        for (int i = 0; i <= p.getChildren().size() - 1; i++ ) {
            Production c = p.getChild(i);
            String spaces = stringOfCharacters(index * 2, " ");
            String x = String.valueOf(index); 

            //System.out.println(spaces + index + ". " + c.getName() + " " + ( (c.getClass().getSimpleName()).equals("Terminal") ? "\n" + stringOfSpaces(index) + stringOfSpaces((x + ". " ).length() / 2) + stringOfDashes(  ( ((x + ". " + c.getName()).length()) / 2) - (" -> [" + ( (Terminal) c).getTokenAttribute() + "]").length()  ) + " -> [" + ( (Terminal) c).getTokenAttribute() + "]": ""));
            System.out.println(spaces + index + "   [" + c.getName() + "] " + ( (c.getClass().getSimpleName()).equals("Terminal") ? "\n" + spaces + stringOfCharacters((x + ". " ).length(), " ") + " " + stringOfCharacters(  ( ( ((x + ". " + c.getName()).length() - 2)) - ("--- < " + ( (Terminal) c).getTokenAttribute() + " >").length() ), "-") + "--- < " + ( (Terminal) c).getTokenAttribute() + " >": ""));
            recursivePrint(c, index + 1);
        }

    };

    public void recursivePrintImproved (Production p, int index) {
        //System.out.println("Rec: " + p.getChildren().size());
        for (int i = 0; i <= p.getChildren().size() - 1; i++ ) {
            Production c = p.getChild(i);
            String spaces = stringOfCharacters(index * 2, " ");
            String x = String.valueOf(index); 

            Boolean is_terminal = (c.getClass().getSimpleName()).equals("Terminal");

            if (!is_terminal) {
                System.out.println(spaces + index + "   [" + c.getName() + "] "); 
            } else {
                System.out.println(spaces + stringOfCharacters((x + ". " ).length(), " ") + " " + stringOfCharacters(  ( ( ((x + ". " + c.getName()).length() - 2)) - ("--- < " + ( (Terminal) c).getTokenAttribute() + " >").length() ), "-") + "--- < " + ( (Terminal) c).getTokenAttribute() + " >");
            }

            //System.out.println(spaces + index + ". " + c.getName() + " " + ( (c.getClass().getSimpleName()).equals("Terminal") ? "\n" + stringOfSpaces(index) + stringOfSpaces((x + ". " ).length() / 2) + stringOfDashes(  ( ((x + ". " + c.getName()).length()) / 2) - (" -> [" + ( (Terminal) c).getTokenAttribute() + "]").length()  ) + " -> [" + ( (Terminal) c).getTokenAttribute() + "]": ""));
            //System.out.println(spaces + index + "   [" + c.getName() + "] " + ( (c.getClass().getSimpleName()).equals("Terminal") ? "\n" + spaces + stringOfCharacters((x + ". " ).length(), " ") + " " + stringOfCharacters(  ( ( ((x + ". " + c.getName()).length() - 2)) - ("--- < " + ( (Terminal) c).getTokenAttribute() + " >").length() ), "-") + "--- < " + ( (Terminal) c).getTokenAttribute() + " >": ""));
            recursivePrintImproved(c, index + 1);
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
            recursivePrintImproved(i, 1);
        }

        return derivation;

    }


}
