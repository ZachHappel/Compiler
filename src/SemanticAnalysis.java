// Creating an AST from the CST

import java.util.ArrayList;

public class SemanticAnalysis {

    public String stringOfCharacters(int amount, String character) { String s = ""; for (int j = 0; j <= amount-1; j++) { s = s + character; } return s; }

    public void recursiveDescent (Production p, int index) {

        // These terminals all are child-most and their values are what is added to the CST, symbol table, etc. 
        ArrayList<String> valid_terminals = new ArrayList<String>(){{
            add("Identifier");
            add("Character");
            add("Digit");
            add("KEYWORD_TRUE");
            add("KEYWORD_FALSE");
        }};

        ArrayList<String> valid_nonterminals = new ArrayList<String>(){{
            add("SYMBOL_EQUIVALENCE");
            add("CHARACTER");
            add("DIGIT");
            add("IDENTIFIER");
            add("KEYWORD_TRUE");
            add("KEYWORD_FALSE");
        }};

        // These NonTerminals will never be added to the CST, as they provide unnecessary abstraction and not pertinent concrete detail
        ArrayList<String> ignore = new ArrayList<String>(){{
            add("StatementList");
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
        }};

        if (index == 0 && p.getName().equals("Program")) {
            System.out.println(stringOfCharacters(index * 2, " ") + index + stringOfCharacters(2, " ") + "   [" + p.getName() + "] ");
            index++;
        }

        for (int i = 0; i <= p.getChildren().size() - 1; i++ ) {
            Production c = p.getChild(i);
            String spaces = stringOfCharacters(index * 2, " ");
            Boolean is_terminal = (c.getClass().getSimpleName()).equals("Terminal");
            if (!is_terminal) { 
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
            recursiveDescent(c, index + 1);
        }
    }

    public void performSemanticAnalysis (ArrayList<Production> derivation, Toolkit tk ) {
        System.out.println("\n\nSEMANTIC ANALYSIS:");
        recursiveDescent(derivation.get(0), 0);

    }




    
}
