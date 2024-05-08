import java.util.ArrayList;

public class CodeGeneration {
    public Toolkit toolkit;
    public ExecutionEnvironment execution_environment;
    
    public void traverseIntermediateRepresentation (ArrayList<Production> AST) throws CodeGenerationException {

    }
    
    public void performSemanticAnalysis (ArrayList<Production> AST, Toolkit tk ) throws CodeGenerationException {
            System.out.println("\n\nCODE GENERATION:");
            toolkit = tk;    
            execution_environment = new ExecutionEnvironment();  // not sure if necessary
            traverseIntermediateRepresentation(AST);
            
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
