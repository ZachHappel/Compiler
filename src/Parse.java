import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Parse {

    // TODO: Add error handling? How to decide when it failed?

    //ArrayList<Token> token_stream;
    public  ArrayList<Production> derivation = new ArrayList<>();
    public  ArrayList<Token> token_stream;
    public  Toolkit toolkit;
    public int token_pointer = 0;
    public int stashed_pointer = 0; // used if pointer ever needs to get back


    // Crude fix for recursively added empty CharacterLists... Just pretend that this doesn't exist
    public int charListCounter = 0;
    public int charListFailState = 1000000;
    public boolean characterListHasBeenRemoved = false;

    public ArrayList<String> type = new ArrayList<>() {{
                add("KEYWORD_INT");
                add("KEYWORD_STRING");
                add("KEYWORD_BOOLEAN");
    }};

    
    public Parse () { }

    public void stashPointer() { stashed_pointer = token_pointer;} // Use at beginning of method, to know where token stream position was at the beginning
    public void restorePointer() {token_pointer = stashed_pointer;} // Used to restore pointer to where it was at the beginning of a method
    public void incrementPointer() {token_pointer = token_pointer + 1;}
    public String tokenName() { return (token_stream.get(token_pointer)).getName();  }
    public Token currentToken() { return token_stream.get(token_pointer);  }


    // Returns Terminal of sequence provided if there is a match
    public Terminal match (String seq, int token_pointer_local) throws ParsingException {
        Terminal t = new Terminal(seq);
        if (token_stream.get(token_pointer_local).getName().equals(seq)) {
            t.setSuccess(true); // Important
            t.setToken(token_stream.get(token_pointer_local));
            t.setTokenName(seq);
            t.setTokenAttribute(token_stream.get(token_pointer_local).getAttribute());
            
            System.out.println("INFO - MATCHED: <" + seq + ", " + t.getTokenAttribute() + ">");
            return t;
        } return t;
    }

    public void parseProgram () throws ParsingException {
        System.out.println("INFO - Parsing Program..."); String expected_production = "BLOCK, EOP"; String location = "Parse Program";
        NonTerminal program = new NonTerminal("Program");
        parseBlock(program); if (!program.success()) throw new ParsingException(location, "Improper Sequence", expected_production, "[?, ? ]", currentToken());
        Terminal eop = match("EOP", token_pointer);
        if (eop.success()) { 
            program.addChild(eop);
            eop.addParent(program);
            derivation.add(program);
        } else throw new ParsingException(location, "Improper Sequence", expected_production, "[BLOCK, ? ]", currentToken()); 
    }

    // Modifies NonTerminal which it is passed 
    public void parseBlock(NonTerminal nt) throws ParsingException {
        System.out.println("INFO - Parsing Block...");
        stashPointer();
        Terminal open_block = match("SYMBOL_OPENBLOCK", token_pointer); if (open_block.success()) incrementPointer(); else restorePointer();
       
        NonTerminal new_statement_list = new NonTerminal("StatementList"); 
        parseStatementList(new_statement_list); if (!new_statement_list.success()) return; 
        
        Terminal close_block = match("SYMBOL_CLOSEBLOCK", token_pointer); if (close_block.success()) incrementPointer(); else restorePointer();
        
        NonTerminal block = new NonTerminal("BLOCK");
        block.addChild(open_block); block.addChild(new_statement_list); block.addChild(close_block); block.addParent(nt); block.setSuccess(true);
        nt.addChild(block); nt.setSuccess(true);
        System.out.println("INFO - [Block] Recognized" );
        return;
    }

    // Returns NonTerminal 
    public NonTerminal parseStatementList(NonTerminal statement_list) throws ParsingException {
        System.out.println("INFO - Parsing StatementList");
        
        // In the case of Block, we want empty to return as valid // Empty StatementList 
        if (tokenName().equals("SYMBOL_CLOSEBLOCK")) {
            statement_list.setSuccess(true); 
            System.out.println("INFO - [StatementList] Recognized - Production: ε");
            return statement_list;   
        }

        NonTerminal statement = parseStatement();
        
        if (statement.success()) {
            //System.out.println("INFO - [Statement] Recognized - Production: Statement ~" + statement.getName());
            statement.addParent(statement_list);
            statement_list.addChild(statement);
            statement_list.setSuccess(true);
            if (!tokenName().equals("SYMBOL_CLOSEBLOCK")) {
                System.out.println("INFO - [StatementList] Recognized - Production: Statement, StatementList");
                statement_list.addChild(new NonTerminal("StatementList"));
                parseStatementList((NonTerminal) statement_list.getChild(1));
            } 
            return statement_list; 
        }        
        return new NonTerminal("StatementList"); // If EMPTY (E)
    }


    // Returns NonTerminal "STATEMENT" // Parse Statement Methods:  All modify the NonTerminal "expression" which they are passed
    public NonTerminal parseStatement () throws ParsingException {
        System.out.println("INFO - Parsing Statement...");
        NonTerminal statement = new NonTerminal("Statement");
        parsePrintStatement(statement); if (statement.success()) return statement;  // Access via "Statement Matched: " + statement.getChild(0).getName()
        parseAssignmentStatement(statement); if (statement.success()) return statement;
        parseVariableDeclarationStatement(statement); if (statement.success()) return statement;
        parseWhileStatement(statement); if (statement.success()) return statement;
        parseIfStatement(statement); if (statement.success()) return statement;
        if (tokenName().equals("SYMBOL_OPENBLOCK")) {parseBlock(statement); if (statement.success()) return statement;}
        return statement;
    }

    // Returns NonTerminal "EXPRESSION" // Parse Expression Methods: All modify the NonTerminal "expression" which they are passed
    public NonTerminal parseExpression () throws ParsingException {
        System.out.println("INFO - Parsing Expression...");
        NonTerminal expression = new NonTerminal("Expression");  
        parseIntExpression(expression); if (expression.success()) { return expression; }
        parseStringExpression(expression); if (expression.success()) { return expression;}
        parseBooleanExpression(expression); if (expression.success()) { return expression; }
        parseIdentifierExpresison(expression); if (expression.success()) { return expression; }
        return expression;
    }


    // Modified NonTerminal passed to it
    public void parsePrintStatement (NonTerminal statement) throws ParsingException {
        System.out.println("INFO - Parsing PrintStatement...");
        stashPointer();
        String expected_production = "KEYWORD_PRINT, SYMBOL_OPENPAREN, EXPRESSION, SYMBOL_CLOSEPAREN"; String location = "Parse PrintStatement";

        Terminal keyword_print = match("KEYWORD_PRINT", token_pointer);  if (keyword_print.success()) { incrementPointer(); }   else {restorePointer(); return;} 


        Terminal symbol_openparen = match("SYMBOL_OPENPAREN", token_pointer); 
        if (symbol_openparen.success()) { incrementPointer(); } 
        else throw new ParsingException(location, "Improper Sequence", expected_production, "[KEYWORD_PRINT, ?, ... ]", currentToken()); 
        
        NonTerminal expression = parseExpression(); 
        if (!expression.success()) throw new ParsingException(location, "Improper Sequence", expected_production, "[KEYWORD_PRINT, SYMBOL_OPENPAREN, ... ]", currentToken()); 


        Terminal symbol_closeparen = match("SYMBOL_CLOSEPAREN", token_pointer); if (symbol_closeparen.success()) { incrementPointer(); } 
        else throw new ParsingException(location, "Improper Sequence", expected_production, "[KEYWORD_PRINT, SYMBOL_OPENPAREN, EXPRESSION, ... ]", currentToken()); 
        
        NonTerminal print_statement = new NonTerminal("PrintStatement"); // Added as child if all prove to be successful
        print_statement.addChild(keyword_print); print_statement.addChild(symbol_openparen); print_statement.addChild(expression); print_statement.addChild(symbol_closeparen); print_statement.addParent(statement);
        statement.addChild(print_statement); statement.setSuccess(true);
        System.out.println("INFO - [PrintStatement] Recognized - Production: KEYWORD_PRINT, SYMBOL_OPENPAREN, EXPRESSION, SYMBOL_CLOSEPAREN");
        return;
    }


    // Modifies NonTerminal 
    // TODO: SHOULD THERE BE ERROR HANDLING FOR IDENTIFIER?? 
    public void parseAssignmentStatement (NonTerminal statement) throws ParsingException {
        System.out.println("INFO - Parsing AssignmentStatement...");

        if (!tokenName().equals("IDENTIFIER")) return; // Cannot be assignment statement

        NonTerminal identifier = new NonTerminal("Identifier");
        Terminal identifier_terminal = match("IDENTIFIER", token_pointer); if (identifier_terminal.success()) { { identifier_terminal.addParent(identifier); identifier.addChild(identifier_terminal); identifier.setSuccess(true); incrementPointer(); } } else {restorePointer(); return;} 
        Terminal symbol_assignment = match("SYMBOL_ASSIGNMENT", token_pointer); if (symbol_assignment.success()) {  incrementPointer(); } else {restorePointer(); return;} 
        NonTerminal expression = parseExpression(); if (!expression.success()) { restorePointer(); return; } 
        
        NonTerminal assignment_statement = new NonTerminal("AssignmentStatement");
        assignment_statement.addChild(identifier);
        assignment_statement.addChild(symbol_assignment);
        assignment_statement.addChild(expression);
        assignment_statement.addParent(statement);
        statement.addChild(assignment_statement);
        statement.setSuccess(true);
        System.out.println("INFO - [AssignmentStatement] Recognized - Production: IDENTIFIER, SYMBOL_ASSIGNMENT, EXPRESSION");
        return; 
    }


    // Modifies NonTerminal
    public void parseVariableDeclarationStatement (NonTerminal statement) throws ParsingException {
        System.out.println("INFO - Parsing VarDeclStatement...");
        stashPointer();
        
        NonTerminal variable_declaration_statement = new NonTerminal("VarDeclStatement");
        String expected_production = "Type, Identifier"; String location = "Parse VariableDeclarationStatement";
        
        if (type.contains(tokenName())) {
            NonTerminal type = new NonTerminal("Type");
            Terminal type_terminal = match(tokenName(), token_pointer); if (type_terminal.success()) { { type_terminal.addParent(type); type.addChild(type_terminal); type.setSuccess(true); incrementPointer(); } } else {restorePointer(); return;} 

            if (tokenName().equals("IDENTIFIER")) {
                NonTerminal identifier = new NonTerminal("Identifier"); 
                Terminal identifier_terminal = match("IDENTIFIER", token_pointer); if (identifier_terminal.success()) { { identifier_terminal.addParent(identifier); identifier.addChild(identifier_terminal); identifier.setSuccess(true); incrementPointer(); } } else {restorePointer(); return;} 
                
                variable_declaration_statement.addChild(type);
                variable_declaration_statement.addChild(identifier);
                variable_declaration_statement.addParent(statement);
                statement.addChild(variable_declaration_statement);
                statement.setSuccess(true);
                System.out.println("INFO - [VarDeclStatement] Recognized - Production: TYPE, IDENTIFIER");
                return;
            } else throw new ParsingException(location, "Improper Sequence", expected_production, "[Type, ?]", currentToken());
        }
    }

    // Modifies NonTerminal
    public void parseWhileStatement (NonTerminal statement) throws ParsingException {
        System.out.println("INFO - Parsing WhileStatement...");
        
        stashPointer();
        NonTerminal while_statement = new NonTerminal("WhileStatement");
        String expected_production = "KEYWORD_WHILE, BOOLEXPR, BLOCK"; String location = "Parse WhileStatement";
        
        if (tokenName().equals("KEYWORD_WHILE")) {
            Terminal keyword_while = match(tokenName(), token_pointer); if (keyword_while.success()) { incrementPointer(); } else { restorePointer(); return;} 
            
            // For expression, if after parseBooleanExpression -> success() == true, the child will be the boolean expression
            NonTerminal boolexpr = new NonTerminal("");  
            parseBooleanExpression(boolexpr); if (!boolexpr.success()) throw new ParsingException(location, "Improper Sequence", expected_production, "[KEYWORD_WHILE, ?, ... ]", currentToken());
            boolexpr = boolexpr.success() ? (NonTerminal) boolexpr.getChild(0) : boolexpr; // Boolean expression
            
            // For block, if after parseBlock -> success() == true, the child will be BLOCK
            NonTerminal block = new NonTerminal(""); 
            parseBlock(block); if (!block.success()) throw new ParsingException(location, "Improper Sequence", expected_production, "[KEYWORD_WHILE, BOOLEXPR, ... ]", currentToken());
            block = block.success() ? (NonTerminal) block.getChild(0) : block; 

            if (boolexpr.success() && block.success()) {
                while_statement.addChild(keyword_while);
                while_statement.addChild(boolexpr);
                while_statement.addChild(block); 
                while_statement.addParent(statement);
                while_statement.setSuccess(true);
                statement.addChild(while_statement);
                statement.setSuccess(true); // Success, full while statement
                System.out.println("INFO - [WhileStatement] Recognized - Production: KEYWORD_WHILE, BOOLEXPR, BLOCK");
                return;
            }
        }
    }

    public void parseIfStatement (NonTerminal statement) throws ParsingException {
        System.out.println("INFO - Parsing IfStatement...");
        stashPointer();

        NonTerminal if_statement = new NonTerminal("IfStatement");
        String expected_production = "KEYWORD_IF, BOOLEXPR, BLOCK"; String location = "Parse IfStatement";

        if (tokenName().equals("KEYWORD_IF")) {
            Terminal keyword_if = match(tokenName(), token_pointer); if (keyword_if.success()) { incrementPointer(); } else {restorePointer(); return; } 
            
            NonTerminal boolexpr = new NonTerminal("");  
            parseBooleanExpression(boolexpr); if (!boolexpr.success()) throw new ParsingException(location, "Improper Sequence", expected_production, "[KEYWORD_IF, ?, ... ]", currentToken());
            boolexpr = boolexpr.success() ? (NonTerminal) boolexpr.getChild(0) : boolexpr; // Boolean expression
            
            NonTerminal block = new NonTerminal("");  
            parseBlock(block); if (!block.success()) throw new ParsingException(location, "Improper Sequence", expected_production, "[KEYWORD_IF, BOOLEXPR, ... ]", currentToken());
            block = block.success() ? (NonTerminal) block.getChild(0) : block; 
            
            
            if (boolexpr.success() && block.success()) {
                if_statement.addChild(keyword_if);
                if_statement.addChild(boolexpr);
                if_statement.addChild(block); 
                if_statement.addParent(statement);
                if_statement.setSuccess(true);
                statement.addChild(if_statement);
                statement.setSuccess(true); // Success, full if statement
                System.out.println("INFO - [IfStatement] Recognized - Production: KEYWORD_IF, BOOLEXPR, BLOCK");
                return;
            }   
        }
    }

    
    public void parseBooleanExpression (NonTerminal expression) throws ParsingException {
        System.out.println("INFO - Parsing BooleanExpression...");
        stashPointer();
        NonTerminal boolean_expression = new NonTerminal("BooleanExpression");
        String expected_production = "SYMBOL_OPENPAREN, EXPRESSION, BOOLOP, EXPRESSION, SYMBOL_CLOSEPAREN (OR) BOOLVAL"; String location = "Parse BooleanExpression";
        
        if (tokenName().equals("SYMBOL_OPENPAREN")) { 
            

            Terminal keyword_openparen = match("SYMBOL_OPENPAREN", token_pointer); incrementPointer(); //if (keyword_openparen.success()) { incrementPointer(); } else {restorePointer(); return; } 
            
            NonTerminal expression_first = parseExpression(); if (!expression_first.success()) throw new ParsingException(location, "Improper Sequence", expected_production, "[SYMBOL_OPENPAREN, ?, ... ]", currentToken());

            if (tokenName().equals("SYMBOL_EQUIVALENCE") || tokenName().equals("SYMBOL_INEQUIVALENCE") ) {
                
                NonTerminal boolop = new NonTerminal("BoolOp");
                Terminal boolop_terminal = match(tokenName(), token_pointer); if (boolop_terminal.success()) { boolop_terminal.addParent(boolop); boolop.addChild(boolop_terminal); incrementPointer(); } else {restorePointer(); return; } 
                

                NonTerminal expression_second = parseExpression(); 

                if (tokenName().equals("SYMBOL_CLOSEPAREN")) {
                    Terminal keyword_closeparen = match(tokenName(), token_pointer); if (keyword_closeparen.success()) { incrementPointer(); } else {restorePointer(); return; } 
                    
                    if (expression_first.success() && expression_second.success()) {
                        boolean_expression.addChild(keyword_openparen);
                        boolean_expression.addChild(expression_first);
                        boolean_expression.addChild(boolop);
                        boolean_expression.addChild(expression_second);
                        boolean_expression.addChild(keyword_closeparen);
                        boolean_expression.addParent(expression);
                        boolean_expression.setSuccess(true);
                        expression.addChild(boolean_expression);
                        expression.setSuccess(true);
                        System.out.println("INFO - [BooleanExpression] Recognized - Production: SYMBOL_OPENPAREN, EXPRESSION, BOOLOP, EXPRESSION, SYMBOL_CLOSEPAREN");
                        return; // Success, boolean_expression is child of NonTerminal object "expression" passed to the func
                    }
                } else throw new ParsingException(location, "Improper Sequence", expected_production, "[SYMBOL_OPENPAREN, EXPRESSION, BoolOp, EXPRESSION, ? ]", currentToken());
            } else throw new ParsingException(location, "Improper Sequence", expected_production, "[SYMBOL_OPENPAREN, EXPRESSION, ?, ... ]", currentToken());
        } 

        if ( tokenName().equals("KEYWORD_TRUE") || tokenName().equals("KEYWORD_FALSE") ) {
            NonTerminal boolval = new NonTerminal("BooleanValue");
            Terminal boolval_terminal = match(tokenName(), token_pointer); if (boolval_terminal.success()) {  boolval_terminal.addParent(boolval); boolval.addChild(boolval_terminal); boolval.addParent(boolean_expression); boolval.setSuccess(true); incrementPointer(); } else {restorePointer(); return; } 
            boolean_expression.addChild(boolval); 
            boolean_expression.setSuccess(true);
            boolean_expression.addParent(expression);
            expression.addChild(boolean_expression); expression.setSuccess(true);
            System.out.println("INFO - [BooleanExpression] Recognized - Production: BOOLVAL");
            return; 
        }
        
        return; // success = false
    }

    public void parseIntExpression (NonTerminal expression) throws ParsingException {
        System.out.println("INFO - Parsing IntExpression...");
        stashPointer();
        NonTerminal int_expression = new NonTerminal("IntExpression");

        if (tokenName().equals("DIGIT")) {
            NonTerminal digit = new NonTerminal("Digit");
            Terminal digit_terminal = match("DIGIT", token_pointer); if (digit_terminal.success()) { digit_terminal.addParent(digit); digit.addChild(digit_terminal); digit.setSuccess(true); incrementPointer(); } else {restorePointer(); return; } 
            
            if (tokenName().equals("SYMBOL_INTOP")) {    
                NonTerminal intop = new NonTerminal("INTOP");
                Terminal intop_terminal = match("SYMBOL_INTOP", token_pointer); if (intop_terminal.success()) { intop_terminal.addParent(intop); intop.addChild(intop_terminal); intop.setSuccess(true); incrementPointer(); } else {restorePointer(); return; }
                NonTerminal child_expression = parseExpression(); if (!child_expression.success()) return; // error

                int_expression.addChild(digit); int_expression.addChild(intop); int_expression.addChild(child_expression); 
                int_expression.addParent(expression); 
                int_expression.setSuccess(true);

                expression.addChild(int_expression); 
                expression.setSuccess(true);
                
                System.out.println("INFO - [IntExpression] Recognized - Production: DIGIT, INTOP, EXPRESSION");
                return;
                

            } else {
                // Single digit IntExpr
                int_expression.addChild(digit); 
                int_expression.addParent(expression); 
                int_expression.setSuccess(true);
                
                expression.addChild(int_expression); 
                expression.setSuccess(true);
                
                System.out.println("INFO - [IntExpression] Recognized - Production: DIGIT");
                return;
            }
        } 
    }

    public void parseIdentifierExpresison (NonTerminal expression) throws ParsingException {
        System.out.println("INFO - Parsing IdentifierExpression..."); 
        stashPointer();
        if (tokenName().equals("IDENTIFIER")) {
            NonTerminal identifier = new NonTerminal("IDENTIFIER"); 
            Terminal identifier_terminal = match("IDENTIFIER", token_pointer); if (identifier_terminal.success()) { identifier_terminal.addParent(identifier); identifier.addChild(identifier_terminal); identifier.addParent(expression); identifier.setSuccess(true); incrementPointer(); } else {restorePointer(); return;} 
            expression.addChild(identifier);
            expression.setSuccess(true);
            System.out.println("INFO - [IdentifierExpression] Recognized - Production: IDENTIFIER");
            return;
        }
    }    

    public void parseStringExpression (NonTerminal expression) throws ParsingException {
        System.out.println("INFO - Parsing StringExpression...");
        stashPointer();
        
        NonTerminal string_expression = new NonTerminal("StringExpression");
        Terminal keyword_opening_stringexprboundary = match("SYMBOL_STRINGEXPRBOUNDARY", token_pointer); if (keyword_opening_stringexprboundary.success()) incrementPointer(); else {restorePointer(); return;} 
        NonTerminal character_list = parseCharacterList(new NonTerminal("CharacterList")); if (!character_list.success()) { return;} else {charListCounter = 0; charListFailState = 1000000; characterListHasBeenRemoved = false; }
        Terminal keyword_closing_stringexprboundary = match("SYMBOL_STRINGEXPRBOUNDARY", token_pointer); if (keyword_closing_stringexprboundary.success()) incrementPointer(); else {restorePointer(); return;} 
        // Error handling of unmatched StringExprBoundarries is done via Lex already 

        string_expression.addChild(keyword_opening_stringexprboundary);     
        string_expression.addChild(character_list);
        string_expression.addChild(keyword_closing_stringexprboundary);
        string_expression.addParent(expression);
        string_expression.setSuccess(true);
        expression.addChild(string_expression); 
        expression.setSuccess(true);
        System.out.println("INFO - [StringExpression] Recognized - Production: StringExprBoundary, CharacterList, StringExprBoundary");
        return; 
         
    }

    // Accepts CharacterList nonterminal, Returns CharacterList nonterminal
    public NonTerminal parseCharacterList (NonTerminal character_list) throws ParsingException {
        System.out.println("INFO - Parsing CharacterList...");
        stashPointer();
        charListCounter++;
        
        if (tokenName() == "SPACE" || tokenName() == "CHARACTER") {
            NonTerminal space_or_character = new NonTerminal( (tokenName().equals("CHARACTER") ? "Character" : "Space"));
            Terminal space_or_character_terminal = match(tokenName(), token_pointer);  
            
            if (space_or_character_terminal.success()) {
                //toolkit.debugoutput("Char List Counter: " + charListCounter);
                space_or_character_terminal.addParent(space_or_character);
                space_or_character.addChild(space_or_character_terminal); space_or_character.addParent(character_list); space_or_character.setSuccess(true); incrementPointer();
                character_list.addChild(space_or_character);
                
                System.out.println("INFO - [CharacterList] Recognized - Production: Character, CharacterList");
                character_list.addChild(new NonTerminal("CharacterList"));
                parseCharacterList((NonTerminal) character_list.getChild(1));

                if ( (charListCounter >= charListFailState) && !characterListHasBeenRemoved) {
                    character_list.removeChild(1);
                    characterListHasBeenRemoved = true; 
                }

                character_list.setSuccess(true);
                return character_list;
            } 
            
            else {
                //System.out.println("Space Or Char Failed, Count: " + charListCounter);
                charListFailState = charListCounter;
                restorePointer();
                return character_list;
            }      
        }

        character_list.setSuccess(true); // Maybe?
        charListFailState = charListCounter;
        //System.out.println("Returning Character List with Success = False, CLC: " + charListCounter);
        return character_list; // cl would 
    }


    public String stringOfCharacters(int amount, String character) { String s = ""; for (int j = 0; j <= amount-1; j++) { s = s + character; } return s; }

    public void recursivePrint (Production p, int index) {

        if (index == 0 && p.getName().equals("Program")) {
            System.out.println(stringOfCharacters(index * 2, " ") + index + stringOfCharacters(2, " ") + "   [" + p.getName() + "] ");
            index++;
        }

        for (int i = 0; i <= p.getChildren().size() - 1; i++ ) {
            Production c = p.getChild(i);
            String spaces = stringOfCharacters(index * 2, " ");
            Boolean is_terminal = (c.getClass().getSimpleName()).equals("Terminal");
            if (!is_terminal) { System.out.println(spaces + index + stringOfCharacters(2, " ") + "   [" + c.getName() + "] "); } 
            else { System.out.println(spaces + index + stringOfCharacters(2, " ") + " < " + ((Terminal) c).getTokenAttribute() + " >"); }
            recursivePrint(c, index + 1);
        }
    }

    public ArrayList<Production> ParseTokens ( ArrayList<Token> ts, Toolkit tk ) throws ParsingException {

        System.out.println("Parse Tokens: \n\n");
        token_stream = ts; 
        toolkit = tk; 
        parseProgram();
       
        System.out.println("\n\nCST: " );

        recursivePrint(derivation.get(0), 0);

        TreeDraw treeDraw = new TreeDraw(); 
        treeDraw.draw(derivation);

        return derivation;
    }
}
/**

toolkit.debugoutput("@parseBooleanExpression, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName());
toolkit.debugoutput("@parseWhileStatement, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName());
System.out.println("@parseCharacterList, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName());
System.out.println("@parseStringExpression, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName());
System.out.println("@parseIdentifierExpression, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName());
System.out.println("@parseIntExpression, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName());
toolkit.debugoutput("@parseIfStatement, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName());
toolkit.debugoutput("\n@parseStatementList, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName());
toolkit.debugoutput("@parseVariableDeclarationStatement, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName());
//System.out.println("@parsePrintStatement, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName());
toolkit.debugoutput("@parseBlock, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName());
toolkit.debugoutput("@parseAssignmentStatement, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName()); stashPointer();
System.out.println("@parseExpression, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName());
System.out.println("\n>@parseStatement, token_pointer: " + token_pointer + " token_name: " + token_stream.get(token_pointer).getName());


NonTerminals: Identifier 

all terminals that are not part of any 
while, if, print, (, ), {, }, = 

type: int, string, boolean {

    type - NT 
    int - T

    type - NT
    string - T

    type - NT
    boolean - T
}


boolop: ==, != {
    
    boolop - NT
    == - T

    boolop - NT
    != - T

}

boolval: true, false {
    boolval - NT
    true - T

    boolval - NT
    false - T
}

intop: + {
    intop - NT
    +  - T
}



character
space
digit


 */

/**
 * public int getWidestLevelWidth() {
        int max = 0;
        for (Map.Entry<Integer, ArrayList<String>> entry : tiers.entrySet()) {
            if ( (entry.getValue()).size() > max ) max = (entry.getValue()).size();
            //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }
        return max;
    }

    public void printFullWidthTree(Map<Integer, ArrayList<String>> tiers, int maxLevelWidth) {
        int tree_width = getWidestLevelWidth() * getWidestLevelWidth(); 
        for (int level = 0; level < tiers.size(); level++) {
            ArrayList<String> nodes = tiers.get(level);

            int space_between_nodes = (tree_width - nodes.size()) / (nodes.size() + 1);
    
            System.out.print(" ".repeat(space_between_nodes));
    
            // Print nodes and spaces
            for (String node_name : nodes) {
                System.out.print(node_name);
                // Print the spaces between nodes
                System.out.print(" ".repeat(space_between_nodes));
            }
            System.out.println(); // Move to the next line after each level
            System.out.println(); // Move to the next line after each level
        }
    }
 */