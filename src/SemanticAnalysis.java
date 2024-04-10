// Creating an AST from the CST

/**
 *       add("StatementList");
        add("Statement");
        add("Expression");
        add("VarDeclStatement");
        add("PrintStatement");
        add("AssignmentStatement");
        add("WhileStatement");
        add("IfStatement");
        add("BooleanExpression");
        add("StringExpression");
        add("CharacterList");
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SemanticAnalysis {


    public ArrayList<Production> AST = new ArrayList<Production>();
    public Production current_parent;
    public int block_index = 0;
    public ArrayList<String> block_indexes = new ArrayList<>(); 
    //public Map<String, Map<String, String>> symbol_table = new HashMap<>();
    public SymbolTable symbol_table = new SymbolTable();
    public boolean in_variabledeclaration = false;

    //Production prev_parent; 

    /**
     * There is a Character NonTerminal and there is a CHARACTER Terminal. The NonTerminal, Character, is always the parent to the Terminal, CHARACTER. Within the CHARACTER, is the value. 
     * 
     */


    

    public String stringOfCharacters(int amount, String character) { String s = ""; for (int j = 0; j <= amount-1; j++) { s = s + character; } return s; }

    public ArrayList<String> TerminalsList = new ArrayList<>();
    public ArrayList<NonTerminal> NonTerminalsList = new ArrayList<>();

    public ArrayList<String> valid_terminals = new ArrayList<String>(){{
        add("Identifier");
        add("Character");
        add("Digit");
        add("KEYWORD_TRUE");
        add("KEYWORD_FALSE");
        add("KEYWORD_INT");
        add("KEYWORD_BOOLEAN");
        add("KEYWORD_STRING");
        add("IDENTIFIER");
        add("DIGIT");
        //add("KEYWORD_")
    }};
    
    public ArrayList<String> invalid_terminals = new ArrayList<String>(){{
        //add("KEYWORD_PRINT");
        add("SYMBOL_OPENBLOCK");
        add("SYMBOL_CLOSEBLOCK");
        add("SYMBOL_OPENPAREN");
        add("SYMBOL_CLOSEPAREN");
        add("SYMBOL_STRINGEXPRBOUNDARY");
        add("SYMBOL_EQUIVALENCE");
        add("SYMBOL_INEQUIVALENCE");
        add("SYMBOL_INTOP");
        
    }};

    
    // Get added as is
    public ArrayList<String> VALID_NONTERMINALS = new ArrayList<String>(){{
        //add("SYMBOL_EQUIVALENCE");
        //add("IDENTIFIER");
        add("BLOCK");
        add("VarDeclStatement");
        add("PrintStatement");
        add("AssignmentStatement");
        //add("WhileStatement");
        //add("IfStatement"); 
    }};
    
    public ArrayList<String> invalid_nonterminals = new ArrayList<String>(){{
        add("StatementList");
        add("Statement");
        add("Type");
        add("Identifier");
        add("Expression");
        add("BooleanValue");
        add("CharacterList");
        add("Character");
        add("Digit");
        add("BoolOp");
        add("IDENTIFIER");
        add("INTOP");
        //add("BLOCK");
    }};
    
    public ArrayList<String> ACTIONABLE_NONTERMINALS = new ArrayList<String>(){{
        //add("VarDeclStatement");
        
        //add("PrintStatement"); // Summation of CharacterList within the Expression which is contained within print(" EXPR ")
        // Ignored Terminals: KEYWORD_PRINT, SYMBOL_OPENPAREN, SYMBOL_STRINGEXPRBOUNDARY, SYMBOL_CLOSEPAREN
        // PrintStatement is Parent, a nonterminal
        // Analyzed further is Expression
        // Think PrintStatement handling should reach down to Expression and then down to StringExpression because we know it is there
        // -> This would mean we would never have to handle StringExpression again
        // This would also deal with CharacterList
        // PrintStatement parent will be new sort of terminal 


        //add("AssignmentStatement");
        add("StringExpression");
        add("WhileStatement"); // While 
        add("IfStatement");  // If
        add("BooleanExpression"); // IfEqual or IfNotEqual
        add("IntExpression");
        //add("StringExpression");
        //add("CharacterList");
    }};


    // These NonTerminals will never be added to the CST, as they provide unnecessary concrete detail and not pertinent to the future steps, which include code gen
    public ArrayList<String> ignore = new ArrayList<String>(){{
        add("StatementList");
        add("Statement");
        add("Expression");
        add("VarDeclStatement");
        add("PrintStatement");
        add("AssignmentStatement");
        add("WhileStatement");
        add("IfStatement");
        
        //add("BooleanExpression"); 
        // Get its own weird thing

        add("StringExpression");
        add("CharacterList");
    }};

    

    public String extractStringFromStringExpression (NonTerminal str_expr) {
        String str = ""; 
        System.out.println("Str Expr Children: " + getChildrenNames(str_expr));
        if (str_expr.getChildren().size() == 2) {
            NonTerminal character_nt = (NonTerminal) str_expr.getChild(0); // Character NT
            NonTerminal starting_character_list = (NonTerminal) str_expr.getChild(1); // CharacterList NT
            String letter = ( (Terminal) character_nt.getChild(0)).getTokenAttribute(); // Attribute value w/in Character's token
            System.out.println("Letter: " + letter);
            str = str + letter;  // Add letter to string
            str = str + extractStringFromStringExpression(starting_character_list); // Add recursive result to the string
        } else if (str_expr.getChildren().size() == 1) {
            str = str + ((Terminal) ((NonTerminal) str_expr.getChild(0)).getChild(0)).getTokenAttribute(); // Get last letter
        }

        return str; 

    }
    
    
    public void recursiveDescent (Production p, int index) {

        // These terminals all are child-most and their values are what is added to the AST, symbol table, etc. 
       
        
       
        

        //Production = 
        if (index == 0 && p.getName().equals("Program")) {
                                                                                        System.out.println(stringOfCharacters(index * 2, " ") + index + stringOfCharacters(2, " ") + "   [" + p.getName() + "] ");
            index++;
        }

        for (int i = 0; i <= p.getChildren().size() - 1; i++ ) {
            Production c = p.getChild(i);
            String spaces = stringOfCharacters(index * 2, " ");
            System.out.println("\n\n[index: " + index + "]\n**** Type Checking: " + c.getClass() + ", CST Children: " + (c.getClass().getSimpleName().equals("NonTerminal") ? getChildrenNames((NonTerminal) c) : "Not NonTerminal, Cannot Get Children"));
            Boolean is_terminal = (c.getClass().getSimpleName()).equals("Terminal");

            if (is_terminal) {
               TerminalsList.add(c.getName());

                Terminal terminal = (Terminal) c; 
                //System.out.println("Terminal: " + c.getName()); 
                if (valid_terminals.contains(terminal.getName())) {

                    System.out.println("*** " + terminal.getName() + ", Type: Terminal,   Children: NULL,   Action: Adding to Parent, " + current_parent.getName());
                    current_parent.addASTChild(terminal); // Add terminal to current parent
                
                } else System.out.println("*** " + terminal.getName() + ", Type: Terminal,   Children: NULL,   Action: Skipping");

                
            } 
            
            else {
                
                NonTerminal nonterminal = (NonTerminal) c;
                String nonterminal_name = nonterminal.getName();  
                Production prev_parent = current_parent; 
                System.out.println("*** " + nonterminal_name + ", Type: NonTerminal,   Children: " + getChildrenNames(nonterminal));

                if (nonterminal_name.equals("BLOCK")) {
                    System.out.println("\n\n\nENTERING BLOCK\n\n");
                    symbol_table.createNewScope();
                }
                block_index++;


                NonTerminalsList.add((NonTerminal) c);
                
                
                
                if (VALID_NONTERMINALS.contains(nonterminal_name)) {
                    
                                                                                        System.out.println("** Adding NonTerminal: " + nonterminal_name + " to Parent: " + current_parent.getName());
                    current_parent.addASTChild(nonterminal);                            System.out.println("* a Updating Current Parent, " + current_parent.getName() + ",  to: " + nonterminal_name+ "\n\n");
                    current_parent = nonterminal; 
                    recursiveDescent(nonterminal, index + 1); /* recurse on non-term */ System.out.println("* Resetting Current Parent: " + current_parent.getName() + ", to Previous Parent: " + prev_parent.getName());
                    current_parent = prev_parent;
                    
                } else if (ACTIONABLE_NONTERMINALS.contains(nonterminal_name)) {
                    
                    if (nonterminal_name.equals("WhileStatement")) {
                        
                        NonTerminal while_node = new NonTerminal("While");              System.out.println("** Adding NonTerminal: " + nonterminal_name + " to Parent: " + current_parent.getName());
                        current_parent.addASTChild(while_node);                         System.out.println("* b Updating Current Parent, " + current_parent.getName() + ",  to: " + while_node.getName()+ "\n\n");
                        current_parent = while_node;
                        recursiveDescent(nonterminal, index);                           System.out.println("** Resetting Current Parent: " + current_parent.getName() + ", to Previous Parent: " + prev_parent.getName());
                        current_parent = prev_parent;
                    } 
                    
                    else if (nonterminal_name.equals("IfStatement")) {
                                                                                        System.out.println("** Adding NonTerminal: " + nonterminal_name + " to Parent: " + current_parent.getName());
                        NonTerminal if_node = new NonTerminal("If");
                        current_parent.addASTChild(if_node);                            System.out.println("* c Updating Current Parent, " + current_parent.getName() + ",  to: " + if_node.getName() + "\n\n");
                        current_parent = if_node;
                        recursiveDescent(nonterminal, index);                           System.out.println("* Resetting Current Parent: " + current_parent.getName() + ", to Previous Parent: " + prev_parent.getName());
                        current_parent = prev_parent;
                    }
                    
                    else if (nonterminal_name.equals("BooleanExpression")) {
                        // Determine if NonTerm node that should be made should be called IsEqual or IsNotEqual OR if just true/false terminal-- which is not actionable
                        //System.out.println("NonTerminal Name - BooleanExpression");
                        if ( (nonterminal.getChild(0).getName().equals("BooleanValue")) ) {
                            System.out.println("** BooleanExpression Type: VALUE - Letting recursion continue normally");
                            recursiveDescent(nonterminal, index + 1); // Let the terminal get added normally
                        
                        } else {
                            // get Boolop, get Terminal, what is its name
                            String bool_op_value = ((Terminal) (nonterminal.getChild(2).getChild(0))).getName(); /* SYMBOL_EQUIVALENCE OR SYMBOL_INEQUIVALENCE */   System.out.println("** BooleanExpression Type: " + bool_op_value);
                            
                            if (bool_op_value.equals("SYMBOL_EQUIVALENCE")) {
                                                                                        System.out.println("** Adding NonTerminal: IsEqual to Parent: " + current_parent.getName());

                                NonTerminal IsEqual = new NonTerminal("IsEqual"); // Becomes New Parent, remainder of children will be get added here ?? 
                                current_parent.addASTChild(IsEqual);
                                Production previous_parent = current_parent;            System.out.println("* Saved Parent Name: " + previous_parent.getName());
                                current_parent = IsEqual; // Update IsEqual to new parent
                                recursiveDescent(nonterminal, index);
                                current_parent = previous_parent;                       System.out.println("* Reset Parent Name: " + previous_parent.getName());
                                                                                        System.out.println("* d Updating Current Parent, " + current_parent + ",  to: " + IsEqual.getName() + "\n\n");
                                
                                
                            } else if (bool_op_value.equals("SYMBOL_INEQUIVALENCE")) {
                                                                                        System.out.println("** Adding NonTerminal: IsNotEqual to Parent: " + current_parent.getName());
                                NonTerminal IsNotEqual = new NonTerminal("IsNotEqual"); // Becomes New Parent, remainder of children will be get added here ?? 
                                current_parent.addASTChild(IsNotEqual);
                                Production previous_parent = current_parent; 
                                                                                        System.out.println("* Saved Parent Name: " + previous_parent.getName());
                                current_parent = IsNotEqual; // Update IsEqual to new parent
                                recursiveDescent(nonterminal, index);
                                current_parent = previous_parent; 
                                                                                        System.out.println("* Reset Parent Name: " + previous_parent.getName());
                                                                                        System.out.println("* e Updating Current Parent, " + current_parent.getName() + ",  to: " + IsNotEqual.getName() + "\n\n");
                                
                                //recursiveDescent(nonterminal, index + 1); // Continue Recursion
                            }
                            
                        }
                    }

                    else if (nonterminal_name.equals("IntExpression")) {
                                                                                        System.out.println("** INT EXPR: ");
                        if (nonterminal.getChildren().size() == 3) {
                            NonTerminal Addition = new NonTerminal("ADDITION");         System.out.println("** Adding NonTerminal: Addition to Parent: " + current_parent.getName());
                            current_parent.addASTChild(Addition); // Add Addition to AST Children of current Parent
                            Production previous_parent = current_parent; /* Store current parent */ System.out.println("* Saved Parent Name: " + previous_parent.getName());
                            current_parent = Addition; // Update current parent to Addition
                            recursiveDescent(nonterminal, index); // Recurse over Expression, child at index 2
                            current_parent = previous_parent;
                                                                                        System.out.println("* Reset Parent Name: " + previous_parent.getName());
                                                                                        System.out.println("* e Updating Current Parent, " + current_parent.getName() + ",  to: " + Addition.getName() + "\n\n");

                        } else {
                            Terminal digit = (Terminal) nonterminal.getChild(0).getChild(0);
                            current_parent.addASTChild(digit);
                        }
                    }

                    else if (nonterminal_name.equals("StringExpression"))  {
                                                                                        System.out.println("** STRING EXPR: ");

                        String string_expr_string = extractStringFromStringExpression((NonTerminal) nonterminal.getChild(1));  /* Pass first CharacterList */ System.out.println("* String: " + string_expr_string);
                        Terminal terminal_for_string_expression = new Terminal("Character"); 
                        terminal_for_string_expression.setTokenName(nonterminal_name);
                        terminal_for_string_expression.setTokenAttribute(string_expr_string);
                        Token char_string_token = new Token(0, 0);                      System.out.println("** Adding Terminal FOR String Expression: " + terminal_for_string_expression.getName() + " to Parent: " + current_parent.getName());
                        //char_string.setName("CHARACTER");
                        //char_string.setAttribute(string_expr_string);
                        //terminal_for_string_expression.addChild(char_string_token);
                        
                        current_parent.addASTChild(terminal_for_string_expression); // Add Terminal for StringExpression String to expression's AST children
                        
                    }

                    
                } else if ( invalid_nonterminals.contains(nonterminal_name)) {
                    System.out.println("** Invalid NonTerminal... recursion");
                    recursiveDescent(c, index + 1);
                }
                
                else {
                    System.out.println("** Something went horribly wrong");
                    System.exit(0);
                }

                if (nonterminal_name.equals("BLOCK")) {
                    symbol_table.exitScope();
                    System.out.println("\n\n\n EXIT BLOCK\n\n");
                }
            }
            //System.out.println("HEllo");
        }
    }

    public String getChildrenNames (NonTerminal nt) {
        String children_names = "[";
        for (int x = 0; x <= nt.getChildren().size() - 1; x++ ) {
            children_names = children_names +  nt.getChild(x).getName() + "," ;
        }
        return children_names + "]";
    }

    public String getASTChildrenNames (Production p) {
        String children_names = "[";
        for (int x = 0; x <= p.getASTChildren().size() - 1; x++ ) {
            children_names = children_names +  p.getASTChild(x).getName() + "," ;
        }
        return children_names + "]";
    }
    public void recursivePrint (Production p, int index) {

        if (index == 0 && p.getName().equals("Block")) {
            System.out.println(stringOfCharacters(index * 2, " ") + index + stringOfCharacters(2, " ") + "   [" + p.getName() + "] ");
            index++;
        }

        for (int i = 0; i <= p.getASTChildren().size() - 1; i++ ) {
            Production c = p.getASTChild(i);
            String spaces = stringOfCharacters(index * 2, " ");
            Boolean is_terminal = (c.getClass().getSimpleName()).equals("Terminal");
            //if (!is_terminal) { System.out.println(spaces + index + stringOfCharacters(2, " ") + "   [" + c.getName() + "] AST Children: " + getASTChildrenNames(c)); } 
            if (!is_terminal) { System.out.println(spaces + index + stringOfCharacters(2, " ") + "   [" + c.getName() + "]"); } 
            else { System.out.println(spaces + index + stringOfCharacters(2, " ") + " < " + ((Terminal) c).getTokenAttribute() + " >"); }
            recursivePrint(c, index + 1);
        }
    }
    
    public void performSemanticAnalysis (ArrayList<Production> derivation, Toolkit tk ) {
        System.out.println("\n\nSEMANTIC ANALYSIS:");
        Production topscope_block = derivation.get(0).getChild(0); 
        //Production topscope_copy = topscope_block.clone()
        //AST.add(derivation.get(0).getChild(0)); // Block
        //current_parent = derivation.get(0).getChild(0);
        NonTerminal ast_starting_block = new NonTerminal("Block");
        AST.add(ast_starting_block);
        current_parent = ast_starting_block;
        recursiveDescent(derivation.get(0).getChild(0), 1);
        
        System.out.println("Symbol Table Scopes: " + symbol_table.getScopeNames()) ;
        System.out.println("Amount of Scopes: " + symbol_table.getScopeCount()) ;
        System.out.println("\n\nAbstract Syntax Tree\n"); 
        recursivePrint(AST.get(0), 0);
        /**
        for (int x = 0; x <= TerminalsList.size() - 1; x++) {
            System.out.println("Terminal: " + TerminalsList.get(x));
        }

        System.out.println("\n\n");

        for (int y = 0; y <= NonTerminalsList.size() - 1; y++) {
            NonTerminal nonterminal = NonTerminalsList.get(y);
            System.out.print("NonTerminal: " + nonterminal.getName());
            System.out.print(nonterminal.getChildren().size() > 0 ? "         Children: " + getChildrenNames(nonterminal) + "\n": "0\n");

        } 
        
        **/

    }




    
}


/**
 *  if (!is_terminal) { 
                String nonterminal_name = c.getName(); 
                if ( !(nonterminal_name.contains("List")) ) {
                    System.out.println(spaces + index + stringOfCharacters(2, " ") + "   [" + c.getName() + "] "); 
                }
            } 
            else { 
                Terminal terminal = (Terminal) c; 
                if ( 
                    (terminal.getName().equals("IDENTIFIER")) || 
                    (terminal.getName().equals("CHARACTER")) || 
                    (terminal.getName().equals("DIGIT")) ||
                    (terminal.getName().equals("KEYWORD_TRUE")) ||
                    (terminal.getName().equals("KEYWORD_FALSE"))


                ) 
                
                {
                    System.out.println(spaces + index + stringOfCharacters(2, " ") + " < " + ((Terminal) c).getTokenAttribute() + " >"); 
                } else if ((terminal.getName().equals("SYMBOL_EQUIVALENCE")) || (terminal.getName().equals("SYMBOL_INEQUIVALENCE"))) {
                        
                    System.out.println(spaces + index + stringOfCharacters(2, " ") + " < ||||||||" + ((Terminal) c).getTokenAttribute() + " >"); 
                    
                } else {
                    System.out.println("SKIPPING: " + terminal.getName()); 
                }
            } 
 */