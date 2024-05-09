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
        Terminal identifier_terminal = (Terminal) AssignmentStatement.getASTChild(0);
        ArrayList<Production> identifier_nt_astchildren = AssignmentStatement.getASTChildren();
        
        System.out.println("Assignment Statement AST-Children: ");
        for (int i = 0; i <= identifier_nt_astchildren.size() - 1; i++) { System.out.println(AssignmentStatement.getASTChild(i).getName() + " : " + AssignmentStatement.getASTChild(i).getProdKind()); }

        // Where single ID assignment goes

        if (AssignmentStatement.getASTChildren().size() == 2 && AssignmentStatement.getASTChild(1).getName().equals("DIGIT")) {
            System.out.println("\n\nAssignment for " + identifier_terminal.getName() + ", " + identifier_terminal.getTokenAttribute());
            int terminals_digit_value = getDigitFromDIGIT(AssignmentStatement, 1); 
            String identifiers_temporary_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 0);
            gen_loadAccumulatorWithConstant_A9_LDA(op_codes, ""+terminals_digit_value);
            gen_storeAccumulatorInMemory_8D_STA(op_codes, identifiers_temporary_location);
        }

        else if (AssignmentStatement.getASTChild(1).getName().equals("ADDITION")) {
            System.out.println("\n\nAssignment for " + identifier_terminal.getName() + ", " + identifier_terminal.getTokenAttribute());
            
            String id_temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 0); // Left side, assignment id
            String new_addition_temp_addr = execution_environment.performStaticTableInsertion("addition", AssignmentStatement.getScopeName());  // Maybe... // Create location to store during addition
            op_codes = processADDITION(op_codes, (NonTerminal) AssignmentStatement.getASTChild(1), new_addition_temp_addr, false); // Process addition
            gen_loadAccumulatorFromMemory_AD_LDA(op_codes, new_addition_temp_addr); // Load accumulator with value at address
            gen_storeAccumulatorInMemory_8D_STA(op_codes, id_temp_location); // Store in location for id
            System.out.println("Assignment - Stored Value for Identifier: " + identifier_terminal.getName() + ", at location: " + id_temp_location + "\n");
            //op_codes.addAll(nonterminalRouter((NonTerminal) child));
        }
        return op_codes; 
    }
    
    public ArrayList<String> processADDITION (ArrayList<String> op_codes, NonTerminal ADDITION, String temp_addition_addr, boolean within_nested_addition) {
        //ArrayList<String> op_codes = new ArrayList<>(); 
        //String temp_addr = execution_environment.createAndRetrieveNewTemporaryAddress(); 

        

        for (int i = 0; i <= ADDITION.getASTChildren().size() - 1; i++) { System.out.println("Addition Child: " + ADDITION.getASTChild(i).getName() + " : " + ADDITION.getASTChild(i).getProdKind()); }
        
        if (ADDITION.getASTChild(0).getName().equals("DIGIT")) {
            int digit_value = Integer.parseInt(((Terminal) ADDITION.getASTChild(0)).getTokenAttribute());
            System.out.println("Digit Value: " + digit_value);
            

            gen_loadAccumulatorWithConstant_A9_LDA(op_codes, ""+digit_value); // Load accum. with value in Digit

            if (within_nested_addition) {
                gen_addWithCarryToAccum_6D_ADC(op_codes, temp_addition_addr);  // If within recursive addition, add contents already in temp address into accumulator
            }

            gen_storeAccumulatorInMemory_8D_STA(op_codes, temp_addition_addr); // Store contents of the accumulator into memory, using the address of our temporary location
            
            if (ADDITION.getASTChildren().size() > 1) {
                // if of form: 3 + 1
                if (ADDITION.getASTChild(1).getName().equals("DIGIT")) {
                    int second_digit_value = Integer.parseInt(((Terminal) ADDITION.getASTChild(0)).getTokenAttribute());
                    System.out.println("Second Digit: " + second_digit_value);
                    
                    gen_loadAccumulatorWithConstant_A9_LDA(op_codes, ""+second_digit_value);  // Load accumulator with second value
            
                    gen_addWithCarryToAccum_6D_ADC(op_codes, temp_addition_addr); // Add with carry into to the accum
            
                    gen_storeAccumulatorInMemory_8D_STA(op_codes, temp_addition_addr); // Store in temp location
                    
                
                 // if of form 3+3 +a
                } else if (ADDITION.getASTChild(1).getName().equals("ADDITION")) {
                    op_codes = processADDITION(op_codes, (NonTerminal) ADDITION.getASTChild(1), temp_addition_addr, true);

                // = 3 + a
                } else if (ADDITION.getASTChild(1).getName().equals("IDENTIFIER")) {
                    String scope = ADDITION.getScopeName();
                    String id = ((Terminal) ADDITION.getASTChild(1)).getTokenAttribute(); 
                    String static_table_variable_name = id + "@" + scope; 
                    String static_table_temp_location = execution_environment.retrieveTempLocationFromStaticTable(static_table_variable_name);
                    System.out.println("Second Value is Identifier: " + id);
                
                    gen_loadAccumulatorFromMemory_AD_LDA(op_codes, static_table_temp_location); // Load value for a into accumulator
                    gen_addWithCarryToAccum_6D_ADC(op_codes, temp_addition_addr); // Add contents of temporary addition address to accumulator  
                    gen_storeAccumulatorInMemory_8D_STA(op_codes, temp_addition_addr); // Store accumulator value in our temporary location for addition
                    
                    //gen_loadAccumulatorFromMemory_AD_LDA(op_codes, temp_addition_addr); // Load accumulator using the address of the value we just stored (redundant)
                    //gen_storeAccumulatorInMemory_8D_STA(op_codes, static_table_temp_location); // Store value of accumulator into location of a (wrong)

                }

            } else {
                System.out.println("Should be done with addition now");
            } 
        }  
        
        System.out.println("Returning addition Final");
        return op_codes; 
    }


    public ArrayList<String> processPrintStatement (NonTerminal PrintStatement) {
        ArrayList<String> op_codes = new ArrayList<>(); 
        for (int i = 0; i <= PrintStatement.getASTChildren().size() - 1; i++) {
            System.out.println("PrintStatement Child: " + PrintStatement.getASTChild(i).getName() + " : " + PrintStatement.getASTChild(i).getProdKind()); 
        }
        //System.out.println();
        if (PrintStatement.getASTChild(0).getName().equals("IDENTIFIER")) {
            System.out.println("PrintStatement - Terminal");
            String temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(PrintStatement, 0); // Location in static table for the Identifier the print statement is trying to print
            gen_loadYRegisterFromMemory_AC_LDY(op_codes, temp_location);
            gen_finishPrintStatment(op_codes); 
        }

        return op_codes;
    }
    
    public void processAssignmentRightHandSide () {

    }


    
    public ArrayList<String> gen_loadAccumulatorWithConstant_A9_LDA (ArrayList<String> op_codes, String value) {
        op_codes.add("A9");
        op_codes.add("0" + value);
        return op_codes;
    }

    public ArrayList<String> gen_loadAccumulatorFromMemory_AD_LDA (ArrayList<String> op_codes, String location) {
        op_codes.add("AD");
        op_codes.add(location); op_codes.add("00");
        return op_codes;
    }

    public ArrayList<String> gen_storeAccumulatorInMemory_8D_STA (ArrayList<String> op_codes, String location) {
        op_codes.add("8D");
        op_codes.add(location); op_codes.add("00");
        return op_codes;
    }

    public ArrayList<String> gen_addWithCarryToAccum_6D_ADC (ArrayList<String> op_codes, String location) {
        op_codes.add("6D");
        op_codes.add(location); op_codes.add("00");
        return op_codes;
    }

    public ArrayList<String> gen_loadYRegisterFromMemory_AC_LDY (ArrayList<String> op_codes, String location) {
        op_codes.add("AC");
        op_codes.add(location); op_codes.add("00");
        return op_codes;
    }

    public ArrayList<String> gen_finishPrintStatment(ArrayList<String> op_codes) {
        op_codes.add("A2");
        op_codes.add("01");
        op_codes.add("FF");
        return op_codes;
    }
    //public ArrayList<String> processIDENTIFIER (Terminal IDENTIFIER) {

    //}

    public void printOps(ArrayList<String> op_codes) {
        String x = ""; 
        for (int i = 0; i<=op_codes.size() - 1; i++) {
            x = x + op_codes.get(i);
        } System.out.println("New Ops: " + x);
    }

    public int getDigitFromDIGIT (Production parent_production, int index) {
        int digit = Integer.parseInt(((Terminal) parent_production.getASTChild(index)).getTokenAttribute());
        return digit; 
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
            execution_environment.backpatch();
            System.out.println(Arrays.toString(execution_environment.getCodeSequence()));
            execution_environment.printCodeString();
            //recursivePrint(AST.get(0), 0);
            System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
            System.out.println("└--------------------------------------------------------------------------------------------------------------------┘");

        }

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