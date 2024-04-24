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
    
    public boolean within_vardecl = false;
    public boolean within_assignment = false;
    public Terminal found_vardecl_type;
    public Terminal found_vardecl_identifier;
    public Terminal found_assignment_identifier;
    public Terminal found_assignment_value;

    public boolean found_assignment_leftside = false; 

    public boolean within_addition = false; //prevent addition of digit + boolean/string, etc.... Know for sure, DIGIT + DIGIT + DIGIT + ... 
    // SYMBOL_CLOSEBLOCK, 
    public boolean within_booleanexpr = false; 
    public boolean found_booleanexpr_lhs = false;
    public boolean found_booleanexpr_rhs = false;
    public Terminal booleanexpr_lhs;
    public Terminal booleanexpr_rhs;


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
        
        add("StringExpression");
        add("WhileStatement"); // While 
        add("IfStatement");  // If
        add("BooleanExpression"); // IfEqual or IfNotEqual
        add("IntExpression");
    }};

    public ArrayList<String> types = new ArrayList<String>(){{
        add("KEYWORD_INT");
        add("KEYWORD_STRING");
        add("KEYWORD_BOOLEAN");
    }};

    public ArrayList<String> assignment_rhs = new ArrayList<String>(){{
        add("KEYWORD_TRUE");
        add("KEYWORD_FALSE");
        
        // Wait, we do not need the actual value right now... We just need make sure that it is being given the correct value, as it corresponds to its declaration...

        // What we do still need to do is make sure that comparisons (== / != ) are using correct types
        // e.g., while (b == true)
        //       while (true == true)
        //       while (true == b)
        //       where in either case, lhs and rhs are either boolval or a variable previously declared as being boolean 
        // IN ALL COMPILER HOFs... None of them incorporate whether or not variables need assignment prior to being used.............. Wtf. I guess that is not required? 
        // Seems cheap to leave that out, but also... I am not complaining, I guess. I would implement it myself, but it could be a my misunderstanding of theory and this just something
        // that is not a function of semantic analysis? 
   
        
        // NOTE: With the way that StringExpressions are combined into a single CHARACTER terminal, type checking for assignment is done where StringExpressions are handled
    }};
    

    // Thought process / Thoughts to self / What inspired this approach
    // Ignored Terminals: KEYWORD_PRINT, SYMBOL_OPENPAREN, SYMBOL_STRINGEXPRBOUNDARY, SYMBOL_CLOSEPAREN
    // PrintStatement is Parent, a nonterminal
    // Analyzed further is Expression
    // Think PrintStatement handling should reach down to Expression and then down to StringExpression because we know it is there
    // -> This would mean we would never have to handle StringExpression again
    // This would also deal with CharacterList
    // PrintStatement parent will be new sort of terminal 
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
    
    
    /**
     * Apparently, according to HoF compilers, the issue that I have been chasing down -- that which permits the likes of "x = 3 + j + j" where x and j are variables -- is
     * not an actual valid case within our grammar.. Oops. 
     */
    public void recursiveDescent (Production p, int index) throws SemanticAnalysisException {

        System.out.println(
            "\n@recursiveDescent\n" + 
                " -- within_assignment: " + within_assignment + 
                    " -- found_assignment_identifier: " + ( within_assignment ? (found_assignment_identifier == null ? "null" : (found_assignment_identifier.getTokenAttribute())) : "not within assignment") + 
                    " -- found_assignment_value: " + ( within_assignment ? (found_assignment_value == null ? "null" : (found_assignment_value.getTokenAttribute())) : "not within assignment") + 
                    " -- found_assignment_leftside: " + found_assignment_leftside + 
                    "\n" +
                " -- within_booleanexpr: " + within_booleanexpr + 
                    " -- found_booleanexpr_lhs: " + found_booleanexpr_lhs + 
                    " -- booleanexpr_lhs: [name: " + (booleanexpr_lhs == null ? "null" : booleanexpr_lhs.getName()) + ", value: " + (booleanexpr_lhs == null ? "null" : booleanexpr_lhs.getTokenAttribute())  + "] " +
                        "\n" + 
                    " -- found_booleanexpr_rhs: " + found_booleanexpr_rhs +  
                        " -- booleanexpr_rhs: [name: " + (booleanexpr_rhs == null ? "null" : booleanexpr_rhs.getName()) + ", value: " + (booleanexpr_rhs == null ? "null" : booleanexpr_rhs.getTokenAttribute())  + "] " +
                        "\n" +
                " -- within_addition: " + within_addition + "" );

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
                String terminal_name = terminal.getName();
                if (terminal_name.equals("IDENTIFIER")) terminal.setTokenAttribute( (terminal.getTokenAttribute()).replaceAll("[^a-z]", "")); // Lex fix


                if (within_addition) {

                    if ((terminal_name.equals("SYMBOL_INTOP")) || 
                        (terminal_name.equals("DIGIT")) || 
                        (terminal_name.equals("IDENTIFIER"))) {
                            if (terminal_name.equals("IDENTIFIER")) {
                                if (symbol_table.existsWithinAccessibleScopesAndValidAssignment(terminal, "int")) {
                                    System.out.println("We are solid! CONTINUE");
                                    symbol_table.setAsUsed(terminal);
                                } else {
                                    throw new SemanticAnalysisException("SemanticAnalysis, recursiveDescent()", "Variable incompatible with addition, " + terminal.getTokenAttribute());
                                }
                            }
                        }
                    
                    else {
                        throw new SemanticAnalysisException("SemanticAnalysis, recursiveDescent()", "Variable incompatible with addition, " + terminal_name + ", " + terminal.getTokenAttribute());
                    }
                }
               
           
                if (valid_terminals.contains(terminal_name)) {
                    

                    // Found VarDecl Type
                    if (within_vardecl && types.contains(terminal_name)) {
                        found_vardecl_type = terminal;    
                    }

                    // Found VarDecl Identifier
                    if (within_vardecl && terminal_name.equals("IDENTIFIER")) {
                        System.out.println("> VarDecl IDENTIFIER");
                        found_vardecl_identifier = terminal;
                        
                        boolean already_exists = symbol_table.existsWithinAccessibleScopes(found_vardecl_identifier);
                        
                        if (already_exists) {
                            throw new SemanticAnalysisException("SemanticAnalysis, recursiveDescent()", "Variable already declared with identifier: " + found_vardecl_identifier.getTokenAttribute() );                    
                        } else {
                            symbol_table.performEntry(found_vardecl_type, found_vardecl_identifier);
                            current_parent.addASTChild(found_vardecl_identifier); // Add VarDecl identifier, Could cause issue????
                            within_vardecl = false; //No longer in Variable Declaration, flip back to false
                            break;
                        }
                    
                    }


                    if (within_assignment && terminal_name.equals("IDENTIFIER") && !(found_assignment_leftside)) {
                        System.out.println(">within_assignment");
                        found_assignment_identifier = terminal;
                        found_assignment_leftside = true;
                        
                    } else if (within_assignment && terminal_name.equals("IDENTIFIER") && (found_assignment_leftside)) {
                        // does identifier exist in the table, if not ERROR(?), f so, is it same as left side type, get left side type, get current identifier type by looking it up
                        
                        System.out.println(">within_assignment");
                        boolean lhs_identifier_exists = symbol_table.existsWithinAccessibleScopes(found_assignment_identifier);
                        String lhs_identifier_type = symbol_table.getTypeFromAccessibleScopes(found_assignment_identifier);

                        boolean left_comports_with_right = symbol_table.existsWithinAccessibleScopesAndValidAssignment(terminal, lhs_identifier_type);

                        if (left_comports_with_right) {
                            System.out.println("LHS, " + found_assignment_identifier.getTokenAttribute() + " is of type: " + lhs_identifier_type);
                            System.out.println("RHS, " + terminal.getTokenAttribute() + " is of type: " + symbol_table.getTypeFromAccessibleScopes(terminal));
                            System.out.println("Left and Right Side Work");
                            symbol_table.setAsUsed(terminal);
                            symbol_table.setAsUsed(found_assignment_identifier);
                            within_assignment = false; 
                            found_assignment_leftside = false; 
                        } else {
                            throw new SemanticAnalysisException(
                                "SemanticAnalysis, recursiveDescent()", 
                                " Invalid Assignment Statement: \n " +
                                "" + "LHS, " + found_assignment_identifier.getTokenAttribute() + ", Scope: " + symbol_table.getScopeLocation(found_assignment_identifier).getName() + ", type: " + lhs_identifier_type + "\n"
                                   + " RHS, " + terminal.getTokenAttribute() + ", Scope: " + symbol_table.getScopeLocation(terminal).getName() + ", type: " + symbol_table.getTypeFromAccessibleScopes(terminal) + "\n"
                                   + " Incomptatible types for assignment.\n Fatal error"
                            );
                        }
                        
                    }

                    
                    /* Think this is for booleans...
                    System.out.println("Balls");
                    if (within_assignment && assignment_rhs.contains(terminal_name)) {
                       
                        boolean left_comports_with_right = symbol_table.existsWithinAccessibleScopesAndValidAssignment(found_assignment_identifier, terminal_name);
                        if (left_comports_with_right) {
                            System.out.println("Assignment complete for LHS: " + found_assignment_identifier.getName() + ", val: " + found_assignment_identifier.getTokenAttribute() );
                            System.out.println("with: " + terminal_name + ", val: " + terminal.getTokenAttribute() );
                            symbol_table.setAsUsed(found_assignment_identifier); // Left hand side
                            within_assignment = false; 
                            found_assignment_leftside = false; 
                        }
                        System.out.println("True");
                    }
                     */
                    if (within_booleanexpr) {
                        if ((terminal_name.equals("IDENTIFIER") || (terminal_name.equals("KEYWORD_TRUE") || (terminal_name.equals("KEYWORD_FALSE"))))) {
                            if (!found_booleanexpr_lhs ) {
                                booleanexpr_lhs = terminal;
                                found_booleanexpr_lhs = true;
                                
                            } else if (found_booleanexpr_lhs && !found_booleanexpr_rhs) {
                                booleanexpr_rhs = terminal;
                                boolean lhs_valid_in_scope = (booleanexpr_lhs.getName().equals("IDENTIFIER") ? symbol_table.existsWithinAccessibleScopes(booleanexpr_lhs) : true); // id, TRUE, FALSE
                                boolean rhs_valid_in_scope = (booleanexpr_rhs.getName().equals("IDENTIFIER") ? symbol_table.existsWithinAccessibleScopes(booleanexpr_rhs) : true) ; // id, TRUE, FALSE
                                if (lhs_valid_in_scope && rhs_valid_in_scope) {
                                    String lhs_type = symbol_table.getTypeFromAccessibleScopes(booleanexpr_lhs);
                                    String rhs_type = symbol_table.getTypeFromAccessibleScopes(booleanexpr_rhs);
                                    if (lhs_type.equals(rhs_type)) {
                                        System.out.println("Valid LHS & RHS for BooleanExpression. Between the following: " + booleanexpr_lhs.getTokenAttribute() + " and " + booleanexpr_rhs.getTokenAttribute());
                                        symbol_table.setAsUsed(booleanexpr_lhs);
                                        symbol_table.setAsUsed(booleanexpr_rhs);
                                        found_booleanexpr_lhs = false; found_booleanexpr_rhs = false; within_booleanexpr = false; 
                                    } else {
                                        System.out.println("LHS Type: " + lhs_type);
                                        System.out.println("RHS Type: " + rhs_type + ", Value: " + booleanexpr_rhs.getTokenAttribute());

                                        System.out.println("Invalid LHS & RHS values for Boolean Expr. "); 
                                        throw new SemanticAnalysisException("SemanticAnalysis, recursiveDescent()","Left Hand Side of BooleanExpression contains an invalid variable");
                                    }

                                } else {
                                    throw new SemanticAnalysisException("SemanticAnalysis, recursiveDescent()", "Left Hand Side of BooleanExpression contains an invalid variable");
                                }
                            }
                        }
                    }

                    System.out.println("*** " + terminal.getName() + ", Type: Terminal, Value: " + terminal.getTokenAttribute() + ", Children: NULL,   Action: Adding to Parent, " + current_parent.getName());
                    current_parent.addASTChild(terminal); // Add terminal to current parent
                
                } else System.out.println("*** " + terminal.getName() + ", Type: Terminal,   Children: NULL,   Action: Skipping");

                
            } 
            
            else {
                
                NonTerminal nonterminal = (NonTerminal) c;
                String nonterminal_name = nonterminal.getName();  
                Production prev_parent = current_parent; 
                NonTerminalsList.add((NonTerminal) c);

                System.out.println("*** " + nonterminal_name + ", Type: NonTerminal,   Children: " + getChildrenNames(nonterminal));

                if (nonterminal_name.equals("BLOCK")) {
                    System.out.println("\n\n\nENTERING BLOCK\n");
                    symbol_table.createNewScope();
                    System.out.println("Current Scope: " + symbol_table.getCurrentScopeName() + "\n\n");
                }
                
                if (VALID_NONTERMINALS.contains(nonterminal_name)) {                    
                    if (nonterminal_name.equals("VarDeclStatement")) within_vardecl = true;  // Within Variable Decl moving forward
                    if (nonterminal_name.equals("AssignmentStatement")) within_assignment = true;  // Within Variable Decl moving forward

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
                            within_booleanexpr = true; // Flip to true
                            // get Boolop, get Terminal, what is its name
                            String bool_op_value = ((Terminal) (nonterminal.getChild(2).getChild(0))).getName(); /* SYMBOL_EQUIVALENCE OR SYMBOL_INEQUIVALENCE */   System.out.println("** BooleanExpression Type: " + bool_op_value);
                            
                            if (bool_op_value.equals("SYMBOL_EQUIVALENCE")) {
                                                                                        System.out.println("** Adding NonTerminal: IsEqual to Parent: " + current_parent.getName());

                                NonTerminal IsEqual = new NonTerminal("IsEqual"); // Becomes New Parent, remainder of children will be get added here ?? 
                                current_parent.addASTChild(IsEqual);
                                Production previous_parent = current_parent;            System.out.println("* Saved Parent Name: " + previous_parent.getName());
                                current_parent = IsEqual; // Update IsEqual to new parent
                                recursiveDescent(nonterminal, index);
                                found_booleanexpr_lhs = false;
                                found_booleanexpr_rhs = false;
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
                                found_booleanexpr_lhs = false;
                                found_booleanexpr_rhs = false; 
                                current_parent = previous_parent; 
                                                                                        System.out.println("* Reset Parent Name: " + previous_parent.getName());
                                                                                        System.out.println("* e Updating Current Parent, " + current_parent.getName() + ",  to: " + IsNotEqual.getName() + "\n\n");
                                
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
                            
                            within_addition = true;
                            recursiveDescent(nonterminal, index); // Recurse over Expression, child at index 2
                            within_addition = false; 
                
                            current_parent = previous_parent;
                                                                                        System.out.println("* Reset Parent Name: " + previous_parent.getName());
                                                                                        System.out.println("* e Updating Current Parent, " + current_parent.getName() + ",  to: " + Addition.getName() + "\n\n");

                        } else {
                            Terminal digit = (Terminal) nonterminal.getChild(0).getChild(0);
                            current_parent.addASTChild(digit);
                        }
                    }

                      //char_string.setName("CHARACTER");
                        //char_string.setAttribute(string_expr_string);
                        //terminal_for_string_expression.addChild(char_string_token);

                    // "", "string", r=j should not work, where r and j are both strings
                    else if (nonterminal_name.equals("StringExpression"))  {
                                                                                        System.out.println("** STRING EXPR: ");

                        String string_expr_string = extractStringFromStringExpression((NonTerminal) nonterminal.getChild(1));  /* Pass first CharacterList */ System.out.println("* String: " + string_expr_string);
                        Terminal terminal_for_string_expression = new Terminal("CHARACTER"); 
                        terminal_for_string_expression.setTokenName(nonterminal_name);
                        terminal_for_string_expression.setTokenAttribute(string_expr_string);              
                        System.out.println("** Adding Terminal FOR String Expression: " + terminal_for_string_expression.getName() + " to Parent: " + current_parent.getName());
                      
                        if (within_assignment && current_parent.getName().equals("AssignmentStatement")) {
                            System.out.println("Within Assignment, checking that the variable it is being assigned to is of correct type...");
                            boolean is_valid = symbol_table.existsWithinAccessibleScopesAndValidAssignment(found_assignment_identifier, "CHARACTER");
                            if (is_valid) {
                                System.out.println("Valid String Assignment");
                                symbol_table.setAsUsed(found_assignment_identifier);
                                within_assignment = false; 
                                found_assignment_leftside = false;
                            }
                        } 

                        current_parent.addASTChild(terminal_for_string_expression); // Add Terminal for StringExpression String to expression's AST children
                        
                    }

                    
                } else if ( invalid_nonterminals.contains(nonterminal_name)) {
                    System.out.println("** Invalid NonTerminal... recursion");
                    recursiveDescent(c, index + 1);
                }
                
                else {
                    throw new SemanticAnalysisException("SemanticAnalysis, recursiveDescent()", "** Something went wrong **");
                }
                
                if (current_parent.getName().equals("PrintStatement") && nonterminal_name.equals("IDENTIFIER")) {
                    Terminal ps_identifier_terminal = (Terminal) nonterminal.getChild(0);
                    boolean valid_in_scope = symbol_table.existsWithinAccessibleScopes(ps_identifier_terminal);
                    if (valid_in_scope) symbol_table.setAsUsed(ps_identifier_terminal);
                        
                }

                if (nonterminal_name.equals("BLOCK")) {
                    symbol_table.exitScope();
                    System.out.println("\n\n\nEXIT BLOCK\n");
                    System.out.println("Backing Into Scope: " + symbol_table.getCurrentScopeName() + "\n");
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
            String spaces = stringOfCharacters(index * 4, " ");
            Boolean is_terminal = (c.getClass().getSimpleName()).equals("Terminal");
            //if (!is_terminal) { System.out.println(spaces + index + stringOfCharacters(2, " ") + "   [" + c.getName() + "] AST Children: " + getASTChildrenNames(c)); } 
            if (!is_terminal) { System.out.println(spaces + index + stringOfCharacters(5, " ") + "   [" + c.getName() + "]"); } 
            else { System.out.println(spaces + index + stringOfCharacters(5, " ") + " < " + ((Terminal) c).getTokenAttribute() + " >"); }
            recursivePrint(c, index + 1);
        }
    }
    
    public void performSemanticAnalysis (ArrayList<Production> derivation, Toolkit tk ) throws SemanticAnalysisException {
        System.out.println("\n\nSEMANTIC ANALYSIS:");
        
        
        NonTerminal ast_starting_block = new NonTerminal("Block");
        AST.add(ast_starting_block);
        current_parent = ast_starting_block;
        recursiveDescent(derivation.get(0).getChild(0), 1);
        
        System.out.println("\n┌--------------------------------------------------------------------------------------------------------------------┐");
        System.out.println("|--------------------------------------------------Symbol Table------------------------------------------------------|");
        System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
        System.out.println("\n\n  " + symbol_table.getScopeNames()) ;
        System.out.println("  Amount of Scopes: " + symbol_table.getScopeCount()) ;
        System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
        System.out.println("\nScopes and Entries: \n" + symbol_table.getScopesAndEntries()) ;
        System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
        System.out.println("└--------------------------------------------------------------------------------------------------------------------┘");
        System.out.println("\n\nAbstract Syntax Tree\n"); 
        recursivePrint(AST.get(0), 0);
        System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
        System.out.println("└--------------------------------------------------------------------------------------------------------------------┘");

    }




    
}
