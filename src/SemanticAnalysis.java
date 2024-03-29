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
    }};

    public ArrayList<String> invalid_terminals = new ArrayList<String>(){{
        //add("KEYWORD_PRINT");
        add("SYMBOL_OPENBLOCK");
        add("SYMBOL_CLOSEBLOCK");
        add("SYMBOL_OPENPAREN");
        add("SYMBOL_CLOSEPAREN");
        add("SYMBOL_STRINGEXPRBOUNDARY");
        
    }};

    
    // No
    public ArrayList<String> valid_nonterminals = new ArrayList<String>(){{
        add("SYMBOL_EQUIVALENCE");
        add("IDENTIFIER");
        add("BLOCK");
    }};

    public ArrayList<String> invalid_nonterminals = new ArrayList<String>(){{
        add("StatementList");
        add("Statement");
        add("BLOCK");
    }};
    
    public ArrayList<String> ACTIONABLE_NONTERMINALS = new ArrayList<String>(){{
        add("VarDeclStatement");
        
        add("PrintStatement"); // Summation of CharacterList within the Expression which is contained within print(" EXPR ")
        // Ignored Terminals: KEYWORD_PRINT, SYMBOL_OPENPAREN, SYMBOL_STRINGEXPRBOUNDARY, SYMBOL_CLOSEPAREN
        // PrintStatement is Parent, a nonterminal
        // Analyzed further is Expression
        // Think PrintStatement handling should reach down to Expression and then down to StringExpression because we know it is there
        // -> This would mean we would never have to handle StringExpression again
        // This would also deal with CharacterList
        // PrintStatement parent will be new sort of terminal 


        add("AssignmentStatement");
        add("WhileStatement");
        add("IfStatement"); 
        add("BooleanExpression");
        //add("StringExpression");
        add("CharacterList");
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

    

    
    
    public void recursiveDescent (Production p, int index) {

        // These terminals all are child-most and their values are what is added to the AST, symbol table, etc. 
       
        
       

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
                
                if (valid_terminals.contains(terminal.getName())) {
                    current_parent.addChild(terminal);
                    // could cause issues for top scope... being that the terminal would already be there 
                }

                //System.out.println("Terminal: " + c.getName()); 
            } else {
                NonTerminal nonterminal = (NonTerminal) c;
                String nonterminal_name = nonterminal.getName();
                
                if ( nonterminal_name.equals("WhileStatment")) {

                }

                NonTerminalsList.add((NonTerminal) c);

                //if (nonter)
                
                
                //System.out.println("NonTerminal: " + c.getName());
            }

            recursiveDescent(c, index + 1);
        }
    }

    public String getChildrenNames (NonTerminal nt) {
        String children_names = "[";
        for (int x = 0; x <= nt.getChildren().size() - 1; x++ ) {
            children_names = children_names +  nt.getChild(x).getName() + "," ;
        }
        return children_names + "]";
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
        

        for (int x = 0; x <= TerminalsList.size() - 1; x++) {
            System.out.println("Terminal: " + TerminalsList.get(x));
        }

        System.out.println("\n\n");

        for (int y = 0; y <= NonTerminalsList.size() - 1; y++) {
            NonTerminal nonterminal = NonTerminalsList.get(y);
            System.out.print("NonTerminal: " + nonterminal.getName());
            System.out.print(nonterminal.getChildren().size() > 0 ? "         Children: " + getChildrenNames(nonterminal) + "\n": "0\n");

        }    

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