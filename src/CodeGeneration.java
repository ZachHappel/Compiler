import java.util.ArrayList;

public class CodeGeneration {
    public Toolkit toolkit;
    public ExecutionEnvironment execution_environment;
    

    // REMEMBER: Make it fluid, not rigid. 
    public void traverseIntermediateRepresentation ( Production p, int index ) throws CodeGenerationException {
        
        // Removed initial edge case catching code for BLOCK at index 0, when that was an issue but I do not think it is anymore

        for (int i = 0; i <= p.getASTChildren().size() - 1; i++ ) {
            Production c = p.getASTChild(i);
            String spaces = stringOfCharacters(index * 4, " ");
            Boolean is_terminal = (c.getClass().getSimpleName()).equals("Terminal");
            //if (!is_terminal) { System.out.println(spaces + index + stringOfCharacters(2, " ") + "   [" + c.getName() + "] AST Children: " + getASTChildrenNames(c)); } 
            //if (!is_terminal) { System.out.println(spaces + index + stringOfCharacters(5, " ") + "   [" + c.getName() + "]"); } 
            //else { System.out.println(spaces + index + stringOfCharacters(5, " ") + " < " + ((Terminal) c).getTokenAttribute() + " >"); }


            // Need to traverse down all and look for NT, 
            if (!is_terminal) {
                NonTerminal nt = (NonTerminal) c;     
                System.out.println("NonTerminal Name: " + nt.getName());
            }
            traverseIntermediateRepresentation(c, index + 1);
        }
    }

    public void nonterminalRouter (NonTerminal nt ) {
        switch (nt.getName()) {
            
            case "VarDeclStatement": 
                processVariableDeclaration(nt);
            case "AssignmentStatement": 
            
            case "PrintStatement": 


        }
    }


    public void processVariableDeclaration (NonTerminal VarDeclStatement) {
        //ArrayList<Production> ast_children = VarDeclStatement.getASTChildren(); 
        Terminal type = (Terminal) VarDeclStatement.getASTChild(0);
        Terminal identifier = (Terminal) VarDeclStatement.getASTChild(1);
        //String scope = VarDeclStatement.get

    }
    



    public String stringOfCharacters(int amount, String character) { String s = ""; for (int j = 0; j <= amount-1; j++) { s = s + character; } return s; }

    public void performCodeGeneration (ArrayList<Production> AST, Toolkit tk ) throws CodeGenerationException {
            System.out.println("\n\nCODE GENERATION:");
            toolkit = tk;    
            execution_environment = new ExecutionEnvironment();  // not sure if necessary
            
            //NonTerminal ast_starting_block = new NonTerminal("Block");
            //AST.add(ast_starting_block);
            traverseIntermediateRepresentation(AST.get(0), 1); // start on block
            
            System.out.println("\n┌--------------------------------------------------------------------------------------------------------------------┐");
            System.out.println("|----------------------------------------------------Code Gen--------------------------------------------------------|");
            System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
            //System.out.println("\n\n  " + symbol_table.getScopeNames()) ;
            //System.out.println("  Amount of Scopes: " + symbol_table.getScopeCount()) ;
            System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
            //System.out.println("\nScopes and Entries: \n" + symbol_table.getScopesAndEntries()) ;
            System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
            System.out.println("└--------------------------------------------------------------------------------------------------------------------┘");
            System.out.println("\n\nAbstract Syntax Tree\n"); 
            //recursivePrint(AST.get(0), 0);
            System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
            System.out.println("└--------------------------------------------------------------------------------------------------------------------┘");

        }

}
