import java.util.ArrayList;
import java.util.Arrays;

public class CodeGeneration {
    public Toolkit toolkit;
    public ExecutionEnvironment execution_environment;
    public String addition_temp_addr;

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
                ArrayList<String> op_codes = nonterminalRouter(nt);
                String[] op_codes_array = op_codes.toArray(new String[0]); // convert to standard array
                
                // Stuff to the heap will be added by itself
                if (op_codes_array.length > 0) {
                    execution_environment.insert(op_codes_array, "int", "Code");
                }

            }
            traverseIntermediateRepresentation(c, index + 1);
        }
    }


    public ArrayList<String> terminalRouter (Terminal t ) throws CodeGenerationException{
        ArrayList<String> op_codes = new ArrayList<>(); 
        
        switch (t.getName()) {  
            case "IDENTIFIER": 
                break; 
            
        }

        return op_codes; 
    }

    public ArrayList<String> nonterminalRouter (NonTerminal nt ) throws CodeGenerationException{
        ArrayList<String> op_codes = new ArrayList<>(); 
        switch (nt.getName()) {
            
            case "VarDeclStatement": 
                op_codes = processVariableDeclaration(nt);
                //String[] op_codes_array = op_codes.toArray(new String[0]); // convert to standard array
                //execution_environment.insert(op_codes_array, "int", "Code");
                break; 
               
            case "AssignmentStatement": 
                op_codes = processAssignmentStatement(nt);
                break; 
            case "PrintStatement": 
                op_codes = processPrintStatement(nt);
                break; 
            //case "ADDITION":
                //execution_environment.loadAccumulator(0); // reset accumulator;
                //String temp_addr = execution_environment.createAndRetrieveNewTemporaryAddress(); 
                //addition_temp_addr = temp_addr;
                //op_codes = processADDITION(op_codes, nt, temp_addr, false);
                
           
        }

        return op_codes; 
    }


    

    public ArrayList<String> processVariableDeclaration (NonTerminal VarDeclStatement) throws CodeGenerationException{
        Terminal type = (Terminal) VarDeclStatement.getASTChild(0);
        Terminal identifier = (Terminal) VarDeclStatement.getASTChild(1);
        String temp_addr = execution_environment.performStaticTableInsertion(identifier.getTokenAttribute(), VarDeclStatement.getScopeName()); // Maybe could be problematic with scope
        ArrayList<String> op_codes = new ArrayList<>(Arrays.asList("A9", "00", "8D", temp_addr, "00"));
        return op_codes; 
    }

    // a = 1
    // a = b
    // a = 3 + 1 : store 0 at memory address T0, load accumulator with 3, store 3 at memory address T1, load accumulator with 1, add value of T1(3) to accumulator with carry (1), store accumulator (4) in T1, load value from T1 now into accumulator, store in T0
    // a = 3 + 1 + a
    public ArrayList<String> processAssignmentStatement (NonTerminal AssignmentStatement) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>(); 
        Terminal identifier_t = (Terminal) AssignmentStatement.getASTChild(0);
        System.out.println("Assignment Statement AST-Children: ");
        ArrayList<Production> identifier_nt_astchildren = AssignmentStatement.getASTChildren();
        
        for (int i = 0; i <= identifier_nt_astchildren.size() - 1; i++) {
            System.out.println(AssignmentStatement.getASTChild(i).getName() + " : " + AssignmentStatement.getASTChild(i).getProdKind()); 
        }

        if (AssignmentStatement.getASTChild(1).getName().equals("ADDITION")) {
            System.out.println("Addition");
            String identifier_value = identifier_t.getTokenAttribute();
            String identifier_scope = AssignmentStatement.getScopeName(); // Maybe change could be problematic
            String static_table_variable_name = identifier_value + "@" + identifier_scope; 
            String id_temp_location = execution_environment.retrieveTempLocationFromStaticTable(static_table_variable_name);
            System.out.println("Assignment, using addition, for ID: " + static_table_variable_name + " at temp location: " + id_temp_location);


            execution_environment.loadAccumulator(0); // reset accumulator;
            String temp_addr = execution_environment.createAndRetrieveNewTemporaryAddress(); 
            addition_temp_addr = temp_addr;
            op_codes = processADDITION(op_codes, (NonTerminal) AssignmentStatement.getASTChild(1), addition_temp_addr, false);



            op_codes.add("AD"); // load accumulator from memory
            op_codes.add(addition_temp_addr); op_codes.add("00"); // using address we have been using for addition

            op_codes.add("8D"); // store accumulator
            op_codes.add(id_temp_location); op_codes.add("00"); // with the address of the variable in which is being assigned

            
            //op_codes.addAll(nonterminalRouter((NonTerminal) child));
            //op_codes.addAll(
        }
        

        /*
        for (int i = 0; i <= identifier_nt_astchildren.size() - 1; i++) {
           System.out.println(AssignmentStatement.getASTChild(i).getName() + " : " + AssignmentStatement.getASTChild(i).getProdKind()); 
           Production child = AssignmentStatement.getASTChild(i);
           String child_prod_kind = child.getProdKind(); 
           String child_name = child.getName();
           
           if (child_prod_kind.equals("NonTerminal")) {
                op_codes.addAll(nonterminalRouter((NonTerminal) child)); // Add resultant op_codes to op_codes arraylist
            } else if (child_prod_kind.equals("Terminal")) {
                op_codes.addAll(terminalRouter((Terminal) child)); // Add resultant op_codes to op_codes arraylist
           }

        }
        **/
        //String identifier_nt = ((Terminal) ().getASTChild(0)).getTokenAttribute() ; // Identifier is NonTerminal
        //System.out.println("Assignment Identifier: " + identifier_nt);
        return op_codes; 
    }
    
    public ArrayList<String> processADDITION (ArrayList<String> op_codes, NonTerminal ADDITION, String temp_addr, boolean within_nested_addition) {
        //ArrayList<String> op_codes = new ArrayList<>(); 
        //String temp_addr = execution_environment.createAndRetrieveNewTemporaryAddress(); 

        // if child 0 is digit
        // if child 1 is ADDITION
        System.out.println("Process ADDITION ");

        for (int i = 0; i <= ADDITION.getASTChildren().size() - 1; i++) {
            System.out.println("Addition Child: " + ADDITION.getASTChild(i).getName() + " : " + ADDITION.getASTChild(i).getProdKind()); 
        }
        if (ADDITION.getASTChild(0).getName().equals("DIGIT")) {
            int digit_value = Integer.parseInt(((Terminal) ADDITION.getASTChild(0)).getTokenAttribute());
            System.out.println("Digit Value: " + digit_value);
            // Load accumulator
            op_codes.add("A9"); // load accumulator
            execution_environment.loadAccumulator(digit_value); // load execution env's accumulator with value
            op_codes.add(execution_environment.getAccumulatorAsString()); // get value as a string and add it to the op_codes
            
            // if within nested addition, via recursion
            if (within_nested_addition) {
                System.out.println("Within nested addition");
                 // Add with carry
                 op_codes.add("6D"); // add with carry
                 op_codes.add(temp_addr); op_codes.add("00"); // in temp location 
            }

            // Store in temp address
            op_codes.add("8D"); // store accumulator
            op_codes.add(temp_addr); op_codes.add("00"); // in temp location
            
            if (ADDITION.getASTChildren().size() > 1) {
                System.out.println("ADDITION.getASTChildren().size() > 1");
                // if of form: 3 + 1
                if (ADDITION.getASTChild(1).getName().equals("DIGIT")) {
                    System.out.println("Second Child is DIGIT");
                    int second_digit_value = Integer.parseInt(((Terminal) ADDITION.getASTChild(0)).getTokenAttribute());
                    System.out.println("Second Digit Value: " + digit_value);
                    // Load accumulator with second value
                    op_codes.add("A9"); // load accumulator
                    execution_environment.loadAccumulator(second_digit_value); // load execution env's accumulator with value
                    op_codes.add(execution_environment.getAccumulatorAsString());
    
                    // Add with carry
                    op_codes.add("6D"); // add with carry
                    op_codes.add(temp_addr); op_codes.add("00"); // in temp location 
                    
                    // Store in temp location
                    op_codes.add("8D"); // store
                    op_codes.add(temp_addr); op_codes.add("00"); // in temp location
                
                // if of form 3+3 +a
                } else if (ADDITION.getASTChild(1).getName().equals("ADDITION")) {
                    op_codes = processADDITION(op_codes, (NonTerminal) ADDITION.getASTChild(1), temp_addr, true);
                    System.out.println("Returning addition");
                    //return op_codes; 
                }
            } else {
                System.out.println("Should be done with addition now");
            }
            
        } //else if (ADDITION.getASTChild(0).getName().equals("IDENTIFIER"))

        //for (int addition_children = 0; addition_children <= ADDITION.getASTChildren().size() - 1; addition_children++) {
        //    System.out.println("Addition Children: " + ADDITION.getASTChild(addition_children).getName() + " : " + ADDITION.getASTChild(addition_children).getProdKind());

        
        System.out.println("Returning addition Final");
        return op_codes; 
    }


    public ArrayList<String> processPrintStatement (NonTerminal PrintStatement) {
        return new ArrayList<String>(); 
    }
    
    public void processAssignmentRightHandSide () {

    }


    //public ArrayList<String> processIDENTIFIER (Terminal IDENTIFIER) {

    //}



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
            execution_environment.backpatch();
            System.out.println(Arrays.toString(execution_environment.getCodeSequence()));
            //recursivePrint(AST.get(0), 0);
            System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
            System.out.println("└--------------------------------------------------------------------------------------------------------------------┘");

        }

}
