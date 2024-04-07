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

public class SemanticAnalysis {


    ArrayList<Production> AST = new ArrayList<Production>();
    Production current_parent;
    Production prev_parent; 

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
        add("IntExpression");
        add("BooleanValue");
        add("CharacterList");
        add("Character");
        add("Digit");
        add("BoolOp");
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
       
        
       
        if (index >= 15) System.exit(0);

        //Production = 
        if (index == 0 && p.getName().equals("Program")) {
            System.out.println(stringOfCharacters(index * 2, " ") + index + stringOfCharacters(2, " ") + "   [" + p.getName() + "] ");
            index++;
        }

        for (int i = 0; i <= p.getChildren().size() - 1; i++ ) {
            Production c = p.getChild(i);
            String spaces = stringOfCharacters(index * 2, " ");
            Boolean is_terminal = (c.getClass().getSimpleName()).equals("Terminal");

            if (is_terminal) {
                TerminalsList.add(c.getName());

                Terminal terminal = (Terminal) c; 
                //System.out.println("Terminal: " + c.getName()); 
                if (valid_terminals.contains(terminal.getName())) {
                    System.out.println("*** " + terminal.getName() + ", Type: Terminal,   Children: NULL,   Action: Adding to Parent, " + current_parent.getName());
                    current_parent.addASTChild(terminal); // Add terminal to current parent
                } else {
                    System.out.println("*** " + terminal.getName() + ", Type: Terminal,   Children: NULL,   Action: Skipping");
                    //System.out.println("Skipping Invalid Terminal: " + terminal.getName()); 
                }

                
            } 
            
            else {
                
                NonTerminal nonterminal = (NonTerminal) c;
                String nonterminal_name = nonterminal.getName();  
                prev_parent = current_parent; 
                System.out.println("*** " + nonterminal_name + ", Type: NonTerminal,   Children: " + getChildrenNames(nonterminal));
                NonTerminalsList.add((NonTerminal) c);



                if (VALID_NONTERMINALS.contains(nonterminal_name)) {
                    //nonterminal.addASTParent(current_parent); // may not be needed
                    System.out.println("Adding NonTerminal: " + nonterminal_name + " to Parent: " + current_parent.getName());
                    current_parent.addASTChild(nonterminal);
                    
                    System.out.println("Updating Current Parent, " + current_parent.getName() + ",  to: " + nonterminal_name+ "\n\n");
                    current_parent = nonterminal; 
                    recursiveDescent(nonterminal, index + 1); // recurse on non-term
                    
                } else if (ACTIONABLE_NONTERMINALS.contains(nonterminal_name)) {
                    
                    if (nonterminal_name.equals("WhileStatement")) {
                        System.out.println("Adding NonTerminal: " + nonterminal_name + " to Parent: " + current_parent.getName());
                        NonTerminal while_node = new NonTerminal("While");
                        current_parent.addASTChild(while_node);
                        System.out.println("Updating Current Parent, " + current_parent.getName() + ",  to: " + while_node.getName()+ "\n\n");
                        current_parent = while_node;
                        recursiveDescent(nonterminal, index);
                    } 
                    
                    else if (nonterminal_name.equals("IfStatement")) {
                        System.out.println("Adding NonTerminal: " + nonterminal_name + " to Parent: " + current_parent.getName());
                        NonTerminal if_node = new NonTerminal("If");
                        current_parent.addASTChild(if_node);
                        System.out.println("Updating Current Parent, " + current_parent.getName() + ",  to: " + if_node.getName() + "\n\n");
                        current_parent = if_node;
                        recursiveDescent(nonterminal, index);
                    }
                    
                    else if (nonterminal_name.equals("BooleanExpression")) {
                        // Determine if NonTerm node that should be made should be called IsEqual or IsNotEqual OR if just true/false terminal-- which is not actionable
                        //System.out.println("NonTerminal Name - BooleanExpression");
                        if ( (nonterminal.getChild(0).getName().equals("BooleanValue")) ) {
                            System.out.println("BooleanExpression Type: VALUE - Letting recursion continue normally");
                            recursiveDescent(nonterminal, index + 1); // Let the terminal get added normally
                        } else {
                            // get Boolop, get Terminal, what is its name
                            String bool_op_value = ((Terminal) (nonterminal.getChild(2).getChild(0))).getName(); // SYMBOL_EQUIVALENCE OR SYMBOL_INEQUIVALENCE
                            System.out.println("BooleanExpression Type: " + bool_op_value);
                            
                            if (bool_op_value.equals("SYMBOL_EQUIVALENCE")) {
                                System.out.println("Adding NonTerminal: IsEqual to Parent: " + current_parent.getName());
                                
                                NonTerminal IsEqual = new NonTerminal("IsEqual"); // Becomes New Parent, remainder of children will be get added here ?? 
                                current_parent.addASTChild(IsEqual);
                                Production previous_parent = current_parent; System.out.println("Saved Parent Name: " + previous_parent.getName());
                                current_parent = IsEqual; // Update IsEqual to new parent
                                recursiveDescent(nonterminal, index);
                                current_parent = previous_parent; System.out.println("Reset Parent Name: " + previous_parent.getName());
                                System.out.println("Updating Current Parent, " + current_parent + ",  to: " + IsEqual.getName() + "\n\n");
                                //recursiveDescent(nonterminal, index + 1); // Continue Recursion
                                
                            } else if (bool_op_value.equals("SYMBOL_INEQUIVALENCE")) {
                                System.out.println("Adding NonTerminal: IsNotEqual to Parent: " + current_parent.getName());
                                NonTerminal IsNotEqual = new NonTerminal("IsNotEqual"); // Becomes New Parent, remainder of children will be get added here ?? 
                                
                                current_parent.addASTChild(IsNotEqual);
                                System.out.println("Updating Current Parent, " + current_parent.getName() + ",  to: " + IsNotEqual.getName() + "\n\n");
                                current_parent = IsNotEqual; 
                                //recursiveDescent(nonterminal, index + 1); // Continue Recursion
                            }
                            
                        }
                    }

                    else if (nonterminal_name.equals("StringExpression"))  {
                        System.out.println("STRING EXPR: ");
                        // /System.exit(0);
                        String string_expr_string = extractStringFromStringExpression((NonTerminal) nonterminal.getChild(1));  // Pass first CharacterList
                        Terminal terminal_for_string_expression = new Terminal("Character"); 
                        terminal_for_string_expression.setTokenName(nonterminal_name);
                        terminal_for_string_expression.setTokenAttribute(string_expr_string);
                        Token char_string_token = new Token(0, 0);
                        System.out.println("Adding Terminal FOR String Expression: " + terminal_for_string_expression.getName() + " to Parent: " + current_parent.getName());
                        //char_string.setName("CHARACTER");
                        //char_string.setAttribute(string_expr_string);
                        //terminal_for_string_expression.addChild(char_string_token);
                        
                        current_parent.addASTChild(terminal_for_string_expression); // Add Terminal for StringExpression String to expression's AST children
                        System.out.println("String: " + string_expr_string);
                    }

                    
                } else if ( invalid_nonterminals.contains(nonterminal_name)) {
                    System.out.println("Invalid NonTerminal... recursion");
                    recursiveDescent(c, index + 1);
                }
                
                else {

                    System.out.println("Something went horribly wrong");
                        //System.out.println("ERROR: NonTerminal not in ANY list: " + nonterminal_name); 
                    System.exit(0);
                    
                    //recursiveDescent(nonterminal, index + 1); // may need to incorporate third param for scope later
                }




                

               // NonTerminalsList.add((NonTerminal) c);

                //if (nonter)
                
                
                //System.out.println("NonTerminal: " + c.getName());
            }

            System.out.println("HEllo");
            //recursiveDescent(c, index + 1);
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
            if (!is_terminal) { System.out.println(spaces + index + stringOfCharacters(2, " ") + "   [" + c.getName() + "] AST Children: " + getASTChildrenNames(c)); } 
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