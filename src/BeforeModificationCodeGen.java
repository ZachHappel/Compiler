import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class BeforeModificationCodeGen {
    public Toolkit toolkit;
    public ExecutionEnvironment execution_environment;
    public SymbolTable symbol_table;
    public String addition_temp_addr;

    public ArrayList<String> constants = new ArrayList<String>(Arrays.asList("KEYWORD_TRUE", "KEYWORD_FALSE")); // TODO: Add DIGIT?

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
                    System.out.println("\n\n\n\nINSERTIONNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
                    execution_environment.insert(op_codes_array, "Code");
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

    public ArrayList<String> nonterminalCallableRouter (ArrayList<String> op_codes, NonTerminal nt, String lhs_location ) throws CodeGenerationException{
        ArrayList<String> new_op_codes = new ArrayList<String>(); 
        switch (nt.getName()) {
            
            case "IsEqual": 
                new_op_codes = og_processIsEqual(new_op_codes, nt, lhs_location);
                op_codes.addAll(new_op_codes); // add result of IsEqual to op_codes 
                break; 
            
            
            //case "ADDITION": 
                
        }

        return op_codes; 
    }


    public String handleCHARACTERterminalAndGetStringAddress (Terminal terminal) throws CodeGenerationException {
        String terminals_string_value = terminal.getTokenAttribute(); 
        boolean exists_in_strdecls = execution_environment.stringExistsWithStringDeclarations(terminals_string_value);
        System.out.println("\n\nExists within string decls: " + exists_in_strdecls);
        
        int heap_pointer;
        String[] hex_arraylist = terminals_string_value.chars().mapToObj(c -> String.format("%02X", c)).toArray(String[]::new);
        if (exists_in_strdecls) {
            heap_pointer = execution_environment.getAddressFromStringDeclarations(terminals_string_value); // If exists in string_decls already, get address
            System.out.println("Heap Pointer: " + heap_pointer);
            
        } else {
            heap_pointer = execution_environment.getHeapPointer() - hex_arraylist.length - 1; // otherwise get the current heap pointer, making sure to modify it for what it will be 
            System.out.println("Heap Pointer: " + heap_pointer);
            execution_environment.insertIntoStringDeclarations(terminals_string_value, heap_pointer); // Store in string_declarations hashmap 
            execution_environment.insert(hex_arraylist, "Heap"); // insert it into code_sequence without waiting, so we can specify Heap insertion

            
        }
        
        // Need to update heap pointer after being done, need to full on return after being in this elif block, need to store pointer for string in string_declarations map 
        String heap_pointer_hex = String.format("%02X", heap_pointer);
        System.out.println("Heap Pointer Hex: " + heap_pointer_hex);
        return heap_pointer_hex;
        
    }
    

    // If string, set A9 FB
    public ArrayList<String> processVariableDeclaration (NonTerminal VarDeclStatement) throws CodeGenerationException{
        Terminal type = (Terminal) VarDeclStatement.getASTChild(0);
        Terminal identifier = (Terminal) VarDeclStatement.getASTChild(1);
        String identifier_type = getIdentifierType( (Production) VarDeclStatement, identifier);
        String default_value = ""; 
        System.out.println("Value at... " + execution_environment.getValueFromCodeSequence(240));
        if (identifier_type.equals("string")) default_value = "6E"; 
        else if (identifier_type.equals("int")) default_value = "00";
        else if (identifier_type.equals("boolean")) default_value = "F5"; // false
        pout("processVarDecl - type: " + identifier_type); 
        String temp_addr = execution_environment.performStaticTableInsertion(identifier.getTokenAttribute(), VarDeclStatement.getScopeName()); // Maybe could be problematic with scope
        ArrayList<String> op_codes = new ArrayList<>(Arrays.asList("A9", default_value, "8D", temp_addr, "00"));
        return op_codes; 
    }

    // a = 1
    // a = b
    // a = 3 + 1 : store 0 at memory address T0, load accumulator with 3, store 3 at memory address T1, load accumulator with 1, add value of T1(3) to accumulator with carry (1), store accumulator (4) in T1, load value from T1 now into accumulator, store in T0
    // a = 3 + 1 + a
    public ArrayList<String> processAssignmentStatement (NonTerminal AssignmentStatement) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>(); 
        ArrayList<String> assignment_children = getProductionChildrenArrayList(AssignmentStatement);
        Terminal identifier_terminal = (Terminal) AssignmentStatement.getASTChild(0);
        ArrayList<Production> identifier_nt_astchildren = AssignmentStatement.getASTChildren();
        
        System.out.println("\n\nAssignment for " + identifier_terminal.getName() + ", " + identifier_terminal.getTokenAttribute());
        System.out.println("Assignment Statement AST-Children: ");
        for (int i = 0; i <= identifier_nt_astchildren.size() - 1; i++) { System.out.println(AssignmentStatement.getASTChild(i).getName() + " : " + AssignmentStatement.getASTChild(i).getProdKind()); }


        if (assignment_children.contains("IsEqual")) {
            String lhs_id_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 0); // left hand side
            //processIsEqual(op_codes, AssignmentStatement, lhs_id_location);
            processIsEqual(op_codes, (NonTerminal) AssignmentStatement.getASTChild(1), lhs_id_location);
        }
        // Where single ID assignment goes
        if (assignment_children.contains("DIGIT") && assignment_children.size() == 2) {
            int terminals_digit_value = getDigitFromDIGIT(AssignmentStatement, 1); 
            String identifiers_temporary_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 0);
            gen_loadAccumulatorWithConstant_A9_LDA(op_codes, ""+terminals_digit_value);
            gen_storeAccumulatorIntoMemory_8D_STA(op_codes, identifiers_temporary_location);
        }
        
        // Got screwed up when it found rhs character but saw lhs identifier
        else if (assignment_children.get(1).equals("IDENTIFIER")) {
        //else if (assignment_children.contains("IDENTIFIER") && assignment_children.size() == 2) {
            String lhs_id = ((Terminal) AssignmentStatement.getASTChild(0)).getTokenAttribute();
            String rhs_id = ((Terminal) AssignmentStatement.getASTChild(1)).getTokenAttribute();
            pout("ID LHS: " + lhs_id);
            pout("ID RHS: " + rhs_id);
            String lhs_id_temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 0); // left hand side
            String rhs_id_temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 1); // right side assignment id
            pout("LHS Location: " + lhs_id_temp_location);
            pout("RHS Location: " + rhs_id_temp_location);
            gen_loadAccumulatorFromMemory_AD_LDA(op_codes, rhs_id_temp_location); // load accumulator with value from identifiers location
            gen_storeAccumulatorIntoMemory_8D_STA(op_codes, lhs_id_temp_location);
        }

        else if ( (assignment_children.contains("KEYWORD_TRUE") || (assignment_children.contains("KEYWORD_FALSE") ) && assignment_children.size() == 2)) {
            System.out.println("Location At 250: " + execution_environment.getValueFromCodeSequence(250));
            String id_temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 0);
            String heap_boolean_location = (assignment_children.contains("KEYWORD_TRUE") ? execution_environment.getTruePointer() : execution_environment.getFalsePointer()); 
            gen_loadAccumulatorWithConstant_A9_LDA(op_codes, heap_boolean_location);
            gen_storeAccumulatorIntoMemory_8D_STA(op_codes, id_temp_location);
            //String boolean_value = getStringFromCHARACTER(identifier_terminal, 1); //  should work
            //System.out.println("Value at : " + execution_environment.getValueFromCodeSequence(244));

        }

        //(String.format("%02X", ""+ (execution_environment.getHeapPointer() - hex_arraylist.length )))
        else if (assignment_children.contains("CHARACTER")) {
            String terminals_string_value = getStringFromCHARACTER(AssignmentStatement, 1); 
            String identifiers_temporary_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 0);
            String[] hex_arraylist = terminals_string_value.chars().mapToObj(c -> String.format("%02X", c)).toArray(String[]::new);
            
            boolean exists_in_strdecls = execution_environment.stringExistsWithStringDeclarations(terminals_string_value);
            System.out.println("\n\nExists within string decls: " + exists_in_strdecls);
            
            int heap_pointer;
            if (exists_in_strdecls) {
                heap_pointer = execution_environment.getAddressFromStringDeclarations(terminals_string_value); // If exists in string_decls already, get address
                System.out.println("Heap Pointer: " + heap_pointer);
            } else {
                heap_pointer = execution_environment.getHeapPointer() - hex_arraylist.length - 1; // otherwise get the current heap pointer, making sure to modify it for what it will be 
                System.out.println("Heap Pointer: " + heap_pointer);
                execution_environment.insertIntoStringDeclarations(terminals_string_value, heap_pointer); // Store in string_declarations hashmap 
                execution_environment.insert(hex_arraylist, "Heap"); // insert it into code_sequence without waiting, so we can specify Heap insertion
            }
            
            // Need to update heap pointer after being done, need to full on return after being in this elif block, need to store pointer for string in string_declarations map 
            String heap_pointer_hex = String.format("%02X", heap_pointer);
            System.out.println("Heap Pointer Hex: " + heap_pointer_hex);
            gen_loadAccumulatorWithConstant_A9_LDA(op_codes, heap_pointer_hex); // Store pointer to location in the heap
            gen_storeAccumulatorIntoMemory_8D_STA(op_codes, identifiers_temporary_location);
             // Empty list because we already added
        }

        else if (assignment_children.contains("ADDITION")) {            
            String id_temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 0); // Left side, assignment id
            String new_addition_temp_addr = execution_environment.performStaticTableInsertion("addition", AssignmentStatement.getScopeName());  // Maybe... // Create location to store during addition
            op_codes = processADDITION(op_codes, (NonTerminal) AssignmentStatement.getASTChild(1), new_addition_temp_addr, false); // Process addition
            gen_loadAccumulatorFromMemory_AD_LDA(op_codes, new_addition_temp_addr); // Load accumulator with value at address
            gen_storeAccumulatorIntoMemory_8D_STA(op_codes, id_temp_location); // Store in location for id
            System.out.println("Assignment - Stored Value for Identifier: " + identifier_terminal.getName() + ", at location: " + id_temp_location + "\n");
            //op_codes.addAll(nonterminalRouter((NonTerminal) child));
        }

        return op_codes; 
    }

    /*
    public ArrayList<String> processTerminalA9 (ArrayList<String> op_codes, NonTerminal parent, Terminal terminal, int index) throws CodeGenerationException {
        if (terminal.getName().equals("IDENTIFIER")) {
            // Load address of ID from static table
            String id_temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(parent, index); // Left side, assignment id
            gen_loadAccumulatorWithConstant_A9_LDA(op_codes, id_temp_location);
        }
    } */


    public ArrayList<String> gen_loadXRegisterFromAddress_AE_LDX (ArrayList<String> op_codes, String address) throws CodeGenerationException {
        op_codes.add("AE"); 
        op_codes.add(address); op_codes.add("00"); 
        return op_codes; 
    }

    public ArrayList<String> gen_loadXRegisterWithValue_A2_LDX (ArrayList<String> op_codes, String value) throws CodeGenerationException {
        op_codes.add("A2"); 
        op_codes.add(value); //op_codes.add("00"); fk.
        return op_codes; 
    }

    public ArrayList<String> gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX (ArrayList<String> op_codes, String address) throws CodeGenerationException {
        op_codes.add("EC"); 
        op_codes.add(address); op_codes.add("00"); 
        return op_codes; 
    }

    // Backup before modifying -- workds for (x == y) where x and y are booleans
    
    public ArrayList<String> old_gen_branchIfNotEqual(ArrayList<String> op_codes, String lhs_location) throws CodeGenerationException {
        op_codes.add("D0");
        
        String true_location = execution_environment.getTruePointer();
        ArrayList<String> set_lhs_true_op_codes = new ArrayList<String>();
        gen_loadAccumulatorWithConstant_A9_LDA(set_lhs_true_op_codes, true_location); // Load true
        gen_storeAccumulatorIntoMemory_8D_STA(set_lhs_true_op_codes, lhs_location);
        int set_true_ops_length = set_lhs_true_op_codes.size(); 
        
        
        String set_true_ops_length_hex = (set_true_ops_length >= 10) ? String.format("%02X", set_lhs_true_op_codes) : ("0" + set_true_ops_length) ; // Maybe
        
        System.out.println("Set True Ops Length Hex: " + set_true_ops_length_hex);
        
        op_codes.add(set_true_ops_length_hex); // Add length of op codes to skip if not equal
        op_codes.addAll(set_lhs_true_op_codes); // Add the op codes that are processed if true

        op_codes.add("A2"); op_codes.add("FF"); // load x register with FF
        gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(op_codes, execution_environment.getFalsePointer());

        return op_codes; 
    }
    

    public ArrayList<String> gen_branchNBytes_D0_BNE(ArrayList<String> op_codes, String byte_amount_in_hex) throws CodeGenerationException {
        op_codes.add("D0"); op_codes.add(byte_amount_in_hex); 
        return op_codes;
    }


    // maybe have return be an object with both op_codes and result value location
    /*
     * TODO: 
     * Update nonterminalCallableRouter to use newProcessIsEqual
     */
    public ArrayList<String> newProcessIsEqual (ArrayList<String> op_codes, NonTerminal IsEqual, String assignment_location) throws CodeGenerationException {
        Production lhs = IsEqual.getASTChild(0);
        Production rhs = IsEqual.getASTChild(1);
        

        if (lhs.getProdKind().equals("Terminal")) {
            //gen_loadXRegister(op_codes, IsEqual, ( (Terminal) lhs) , 0); 
            String lhs_terminal_location = getLocationOfTerminal(IsEqual, (Terminal) IsEqual.getASTChild(0), 0);
            //gen_loadAccumulatorFromMemory_AD_LDA(op_codes, lhs_terminal_location); // Load first value into accumulator 
            
            
            if (rhs.getProdKind().equals("Terminal")) {
                String rhs_terminal_location = getLocationOfTerminal(IsEqual, (Terminal) IsEqual.getASTChild(1), 1);
                gen_loadXRegisterWithValue_A2_LDX(op_codes, rhs_terminal_location); // Load X Register with second value
                
                gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(op_codes, lhs_terminal_location); // Compare first value which we 

                
                ArrayList<String> op_codes_if_true = new ArrayList<String>();  /// if true
                gen_loadAccumulatorWithConstant_A9_LDA(op_codes_if_true, execution_environment.getTruePointer()); // Load true in accumulator
                gen_storeAccumulatorIntoMemory_8D_STA(op_codes_if_true, assignment_location); // Store in assignment location
                
                
                //ArrayList<String> op_codes_if_false = new ArrayList<String>(); /// if false
                //gen_loadAccumulatorWithConstant_A9_LDA(op_codes_if_false, execution_environment.getFalsePointer()); // Load true in accumulator
                //gen_storeAccumulatorIntoMemory_8D_STA(op_codes_if_false, assignment_location); // Store in assignment location
                
                
                int branch_ops_length = op_codes_if_true.size(); // both are same amount of ops so can use either one
                String branch_ops_length_hex = (branch_ops_length >= 10) ? String.format("%02X", branch_ops_length) : ("0" + branch_ops_length) ; // Maybe
                System.out.println("Branch ops length: " + branch_ops_length);
                
                gen_branchNBytes_D0_BNE(op_codes, branch_ops_length_hex);
                op_codes.addAll(op_codes_if_true); // add op codes to be processed if true

                //gen_branchNBytes_D0_BNE(op_codes, branch_ops_length_hex);
                //op_codes.addAll(op_codes_if_false); // add op codes to be processed if false



                //int set_true_ops_length = set_lhs_true_op_codes.size(); 
                //System.out.println("Set True Ops Length Hex: " + set_true_ops_length_hex);

            } else if ( rhs.getProdKind().equals("NonTerminal")) {
                NonTerminal rhs_nt = (NonTerminal) rhs;
                String rhs_name = rhs_nt.getName(); 
                if (rhs_name.equals("IsEqual") || rhs_name.equals("IsNotEqual")) {
                    System.out.println("newProcessIsEqual() - rhs is " + rhs_name + "...");
                    System.exit(0);
                } else {
                    System.out.println("newProcessIsEqual() - rhs is " + rhs_name + "... Not good?");
                    System.exit(0);
                }
            }



            
            //gen_load
        }

        return op_codes; 

    }


    public String getConstantsPointer (String terminal_attribute_value) throws CodeGenerationException {

        switch (terminal_attribute_value) {
            case ("KEYWORD_TRUE"):
                return execution_environment.getTruePointer(); 
                 
            case ("KEYWORD_FALSE"):
                return execution_environment.getFalsePointer();
                
            default:
                return "ERROR"; 
                
        }
        
    }


    public ArrayList<String> processIsEqual (ArrayList<String> op_codes, NonTerminal IsEqual, String assignment_location) throws CodeGenerationException { 
        Production lhs = IsEqual.getASTChild(0);
        Production rhs = IsEqual.getASTChild(1);
        System.out.println("Assignment Location Address: " + assignment_location);

        //gen_loadXRegisterWithValue_A2_LDX(op_codes, execution_environment.getTruePointer());

        // TempValue Counter used to make variable names unique, because after insertion the counter will increase itself and having duplicate "lhsT1" for example will never be a problem
        //String lhs_value_location = execution_environment.performStaticTableInsertion("lhs" + execution_environment.getTemporaryValueCounter(), IsEqual.getScopeName()); // where we will store temp value
        //String rhs_value_location = execution_environment.performStaticTableInsertion("rhs" + execution_environment.getTemporaryValueCounter(), IsEqual.getScopeName()); // where we will store temp value
        //String false_pointer_location = execution_environment.performStaticTableInsertion(execution_environment.createAndRetrieveNewTemporaryAddress(), rhs_value_location)
       
        String lhs_value = "";//getLocationOfTerminal(IsEqual, (Terminal) IsEqual.getASTChild(0), 0);
        String rhs_value = "";//getLocationOfTerminal(IsEqual, (Terminal) IsEqual.getASTChild(1), 1);

        if (lhs.getProdKind().equals("Terminal") && rhs.getProdKind().equals("Terminal")) { 

            Terminal lhs_terminal = (Terminal) lhs; String lhs_terminal_name = lhs_terminal.getName();
            Terminal rhs_terminal = (Terminal) rhs; String rhs_terminal_name = rhs_terminal.getName();
            
            String rhs_value_location = execution_environment.performStaticTableInsertion("rhs" + execution_environment.getTemporaryValueCounter(), IsEqual.getScopeName());

            //String lhs_terminal_value = getLocationOfTerminal(IsEqual, lhs_terminal, 0); // Get value associated with terminal in format that can be saved * Careful zach*
            //String rhs_terminal_value = getLocationOfTerminal(IsEqual, rhs_terminal, 1); // Get value associated with terminal in format that can be saved * Careful zach*
            
            if (constants.contains(lhs_terminal_name)) {
                lhs_value = getConstantsPointer(lhs_terminal_name); // Constant (KEYWORD_TRUE or KEYWORD_FALSE)
                gen_loadXRegisterWithValue_A2_LDX(op_codes, lhs_value);
                //gen_loadAccumulatorWithConstant_A9_LDA(op_codes, lhs_value);
            } else {
                lhs_value = getLocationOfTerminal(IsEqual, (Terminal) IsEqual.getASTChild(0), 0); // Address of assignment
                gen_loadXRegisterFromAddress_AE_LDX(op_codes, lhs_value);
                //gen_loadAccumulatorFromMemory_AD_LDA(op_codes, lhs_value);
            }

            if (constants.contains(rhs_terminal_name)) {
                rhs_value = getConstantsPointer(rhs_terminal_name); // Constant (KEYWORD_TRUE or KEYWORD_FALSE)
                gen_loadAccumulatorWithConstant_A9_LDA(op_codes, rhs_value);
                gen_storeAccumulatorIntoMemory_8D_STA(op_codes, rhs_value_location);
            } else {
                rhs_value = getLocationOfTerminal(IsEqual, (Terminal) IsEqual.getASTChild(1), 1); // Address of assignment
                gen_loadAccumulatorFromMemory_AD_LDA(op_codes, rhs_value);
                gen_storeAccumulatorIntoMemory_8D_STA(op_codes, rhs_value_location);
            }

            /*/
            gen_loadAccumulatorWithConstant_A9_LDA(op_codes, lhs_terminal_value);
            gen_storeAccumulatorIntoMemory_8D_STA(op_codes, lhs_value_location);

            gen_loadAccumulatorWithConstant_A9_LDA(op_codes, rhs_terminal_value);
            gen_storeAccumulatorIntoMemory_8D_STA(op_codes, rhs_value_location);

            gen_loadXRegisterWithValue_A2_LDX(op_codes, lhs_terminal_value); // Store in X Register
            */
            gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(op_codes, rhs_value_location); // CHECK HERE IF PROBLEM

            ArrayList<String> op_codes_if_false = new ArrayList<String>(); /// if false
            gen_loadAccumulatorWithConstant_A9_LDA(op_codes_if_false, execution_environment.getFalsePointer()); // Load false into accumulator 
            gen_storeAccumulatorIntoMemory_8D_STA(op_codes_if_false, assignment_location); // Store in assignment location address


            int branch_ops_length = op_codes_if_false.size(); // both are same amount of ops so can use either one
            String branch_ops_length_hex = (branch_ops_length >= 10) ? String.format("%02X", branch_ops_length) : ("0" + branch_ops_length) ; // Maybe
            System.out.println("Branch ops length: " + branch_ops_length);


            /*  Z Flag = 1, if last operation was equal... Using D0, it checks if the value of the Z Flag. "Not Set" means the value is 0. When the value is 0, it means that the last operation 
                (if it occurred, i think idk default) was not equal. 

                D0 branches to another part of the code if the Z Flag is "not set", meaning the last operation was not equal. 

                Operations such as EC (CPX) flip the Z flag depending on their evaluation 

                The byte value next assigned after D0 is the distance to jump if the result of the previous calculations were "not set"/0/"not equal"


                Suggested online:  If equal: no branch, set c = true and jump over setting c = false
                                   If noteq: branch to ----------------------------> c = false


                                ----> set false set true
                                branch 

                                   

                WAIT!!!!!!!!!!!! 
                                 if equal: 
                                        D0 -----------> 
                                 if noteq                                             
                                        D0 00 set false, set true
                FOR JUMPING

                Branch if Not equal... Okay

                Typically that means

                if not equal --> branch to setting c = false, 
                what would run if equal --> c = true
                but since we don't have JMP...
                we cam do

                if not equal--> branch to c = true
                c = false 
            
            */
            gen_branchNBytes_D0_BNE(op_codes, branch_ops_length_hex);

            // true case 
            gen_loadAccumulatorWithConstant_A9_LDA(op_codes, execution_environment.getTruePointer());
            gen_storeAccumulatorIntoMemory_8D_STA(op_codes, assignment_location); // Store true in assignment location

            /*
            String lhs_terminal_location = getLocationOfTerminal(IsEqual, (Terminal) IsEqual.getASTChild(0), 0);
            String rhs_terminal_location = getLocationOfTerminal(IsEqual, (Terminal) IsEqual.getASTChild(1), 1);
            gen_loadXRegisterWithValue_A2_LDX(op_codes, rhs_terminal_location);
            gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(op_codes, lhs_terminal_location);

            ArrayList<String> op_codes_if_true = new ArrayList<String>();  /// if true
            gen_loadAccumulatorWithConstant_A9_LDA(op_codes_if_true, "F5"); // Load true in accumulator
            gen_storeAccumulatorIntoMemory_8D_STA(op_codes_if_true, assignment_location); // Store in assignment location

            ArrayList<String> op_codes_if_false = new ArrayList<String>(); /// if false
            gen_loadAccumulatorWithConstant_A9_LDA(op_codes_if_false, "FA"); // Load true in accumulator
            gen_storeAccumulatorIntoMemory_8D_STA(op_codes_if_false, assignment_location); // Store in assignment location

            int branch_ops_length = op_codes_if_true.size(); // both are same amount of ops so can use either one
            String branch_ops_length_hex = (branch_ops_length >= 10) ? String.format("%02X", branch_ops_length) : ("0" + branch_ops_length) ; // Maybe
            System.out.println("Branch ops length: " + branch_ops_length);

            gen_branchNBytes_D0_BNE(op_codes, branch_ops_length_hex);
            op_codes.addAll(op_codes_if_true);
            op_codes.addAll(op_codes_if_false);*/
        }

        return op_codes;
    }

    // lhs location refers to the location in memory of the lhs of the IsEqual children
    public ArrayList<String> gen_branchIfNotEqual(ArrayList<String> op_codes, String lhs_location) throws CodeGenerationException {
        
        String true_heap_location = execution_environment.getTruePointer();
        ArrayList<String> set_lhs_true_op_codes = new ArrayList<String>();
        
        String new_addr = execution_environment.createAndRetrieveNewTemporaryAddress();
        gen_loadAccumulatorWithConstant_A9_LDA(op_codes, true_heap_location); // Load true
        gen_storeAccumulatorIntoMemory_8D_STA(op_codes, new_addr);
        gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(op_codes, new_addr); // LHS x terminal should be loaded into accumulator => Compare it to the value of true which we have stored in new location `new_addr`
        

        //----------
        // If true, stiore true_heap_location in a new secondary temp address
        String second_new_addr = execution_environment.createAndRetrieveNewTemporaryAddress(); 
        gen_loadAccumulatorWithConstant_A9_LDA(set_lhs_true_op_codes, true_heap_location); // Load true
        gen_storeAccumulatorIntoMemory_8D_STA(set_lhs_true_op_codes, second_new_addr);
        set_lhs_true_op_codes.add("A2"); set_lhs_true_op_codes.add("FF");
        gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(set_lhs_true_op_codes, execution_environment.getFalsePointer()); // Compare value at X register to false_pointer_location


        int set_true_ops_length = set_lhs_true_op_codes.size(); 
        String set_true_ops_length_hex = (set_true_ops_length >= 10) ? String.format("%02X", set_true_ops_length) : ("0" + set_true_ops_length) ; // Maybe
        System.out.println("Set True Ops Length Hex: " + set_true_ops_length_hex);
        //----------
        
        op_codes.add("D0"); op_codes.add(set_true_ops_length_hex); // Branch length of instructions that would have been processed if true, if it is not true
        
        op_codes.addAll(set_lhs_true_op_codes); // Add the op codes that are processed if true


        ArrayList<String> second_jump_set_of_op_codes = new ArrayList<String>();
        gen_loadAccumulatorWithConstant_A9_LDA(second_jump_set_of_op_codes, execution_environment.getFalsePointer()); // load accumulator with false heap pointer
        gen_storeAccumulatorIntoMemory_8D_STA(second_jump_set_of_op_codes, second_new_addr); // store the value of false heap pointer into the second address
        int set_false_ops_length = second_jump_set_of_op_codes.size(); 
        String set_false_ops_length_hex = (set_false_ops_length >= 10) ? String.format("%02X", set_false_ops_length) : ("0" + set_false_ops_length) ; // Maybe
        System.out.println("Set True Ops Length Hex: " + set_false_ops_length);

        op_codes.add("D0"); op_codes.add(set_false_ops_length_hex); // Branch length of instructions that would have been processed if true, if it is not true
        op_codes.addAll(second_jump_set_of_op_codes); // Add the op codes that are processed if true

        gen_loadAccumulatorFromMemory_AD_LDA(op_codes, second_new_addr); // get value from new 2nd addr and load into accum
        gen_storeAccumulatorIntoMemory_8D_STA(second_jump_set_of_op_codes, lhs_location); // sets LHS location as the destination for the FINAL VALUE.... *****

        
        //op_codes.add("A2"); op_codes.add("FF"); // load x register with FF
        //gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(op_codes, execution_environment.getFalsePointer()); // Compare value at X register to false_pointer_location



        return op_codes; 
    }

    // TODO: REMOVE CONSTANTS FROM HERE
    public String getLocationOfTerminal (NonTerminal parent, Terminal terminal, int index_if_identifier) throws CodeGenerationException {
        String location = ""; 
        
        if (terminal.getName().equals("IDENTIFIER")) {
            location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(parent, index_if_identifier);    
        /*
        } else if (terminal.getName().equals("DIGIT")) {
            String value = terminal.getTokenAttribute(); 
            gen_loadXRegisterWithValue_A2_LDX(op_codes, value); */

        } else if (terminal.getName().equals("CHARACTER")) {
            location = handleCHARACTERterminalAndGetStringAddress(terminal);
            
        } else if ( (terminal.getName().equals("KEYWORD_TRUE")) || (terminal.getName().equals("KEYWORD_FALSE"))) {
            if (terminal.getName().equals("KEYWORD_TRUE")) {
                location = execution_environment.getTruePointer(); 

            } else if (terminal.getName().equals("KEYWORD_FALSE")) {
                location = execution_environment.getFalsePointer();
            }

        } else throw new CodeGenerationException("CodeGeneration, getLocationOfTerminal()", "Terminal sent to getLocationOfTerminal() is not of type IDENTIFIER, CHARACTER, KEYWORD_TRUE, KEYWORD_FALSE. \nTerminal type received: " + terminal.getName());

        return location; 
    }
    
    public ArrayList<String> gen_loadXRegister(ArrayList<String> op_codes, NonTerminal parent, Terminal terminal, int index) throws CodeGenerationException {

        
        if (terminal.getName().equals("IDENTIFIER")) {
            String id_temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(parent, index); 
            gen_loadXRegisterFromAddress_AE_LDX(op_codes, id_temp_location); // Load address of ID from static table
        } else if (terminal.getName().equals("DIGIT")) {
            String value = terminal.getTokenAttribute(); 
            gen_loadXRegisterWithValue_A2_LDX(op_codes, value);
        } else if (terminal.getName().equals("CHARACTER")) {
            String string_location = handleCHARACTERterminalAndGetStringAddress(terminal);
            gen_loadXRegisterFromAddress_AE_LDX(op_codes, string_location);
            
        } else if ( (terminal.getName().equals("KEYWORD_TRUE")) || (terminal.getName().equals("KEYWORD_FALSE"))) {
            if (terminal.getName().equals("KEYWORD_TRUE")) {
                gen_loadXRegisterWithValue_A2_LDX(op_codes, execution_environment.getTruePointer());
            } else if (terminal.getName().equals("KEYWORD_FALSE")) {
                gen_loadXRegisterWithValue_A2_LDX(op_codes, execution_environment.getFalsePointer());
            }
        }

        
        
        return op_codes; 
        
        // elif boolean, elif DIGIT, 
    }


    // Recursive approach ??
    // What if processIsEqualLeft processIsEqualRight called by processIsEqual 
    
    public ArrayList<String> og_processIsEqual (ArrayList<String> op_codes, NonTerminal IsEqual, String lhs_location) throws CodeGenerationException {
        Production lhs = IsEqual.getASTChild(0);
        Production rhs = IsEqual.getASTChild(1);

        // TODO: Is Boolean reference to Heap or is it just set 0 and 1 -- think just set 0 and 1, fuck
        // Need to finish NonTerminals, but thinking load X register with LHS, save in temp value, then load X register with RHS, and compare X register with temp value

        // I think only NonTerminal it could be is IsEqual or IsNotEqual 
        if (lhs.getProdKind().equals("NonTerminal")) {
            System.out.println("processIsEqual() lhs is NonTerminal: " + ((NonTerminal) lhs).getName());
            nonterminalCallableRouter(op_codes, ((NonTerminal) lhs), lhs_location); 
        }

        if (lhs.getProdKind().equals("Terminal")) {
            gen_loadXRegister(op_codes, IsEqual, ( (Terminal) lhs) , 0); 
            //if ( !(((Terminal) lhs).getName().equals("DIGIT"))) {
             //  
            //} else {
                // need to load value into register
            //}
        }

        if (rhs.getProdKind().equals("NonTerminal")) {
            nonterminalCallableRouter(op_codes, ((NonTerminal) rhs), lhs_location); 
        }

        if (rhs.getProdKind().equals("Terminal")) {
            String terminal_location = getLocationOfTerminal( IsEqual, ((Terminal) rhs), 1);
            if ( !(((Terminal) lhs).getName().equals("DIGIT"))) {
                
                // X Register will be loaded with value of lhs rerminal if lhs is Terminal -- currently
                gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(op_codes, terminal_location);
                old_gen_branchIfNotEqual(op_codes, lhs_location);
                // need to find first declaration of assignments lhs
            } else {
                // need to compare to value somehow
            }
        }


        //execution_environment.retrieveTempLocationFromChildOfNonTerminal(IsEqual, 0);
        //execution_environment.retrieveTempLocationFromChildOfNonTerminal(IsEqual, 0);
        
        return op_codes;
    }
    
    public ArrayList<String> processADDITION (ArrayList<String> op_codes, NonTerminal ADDITION, String temp_addition_addr, boolean within_nested_addition) throws CodeGenerationException {
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

            gen_storeAccumulatorIntoMemory_8D_STA(op_codes, temp_addition_addr); // Store contents of the accumulator into memory, using the address of our temporary location
            
            if (ADDITION.getASTChildren().size() > 1) {
                // if of form: 3 + 1
                if (ADDITION.getASTChild(1).getName().equals("DIGIT")) {
                    int second_digit_value = Integer.parseInt(((Terminal) ADDITION.getASTChild(0)).getTokenAttribute());
                    System.out.println("Second Digit: " + second_digit_value);
                    
                    gen_loadAccumulatorWithConstant_A9_LDA(op_codes, ""+second_digit_value);  // Load accumulator with second value
            
                    gen_addWithCarryToAccum_6D_ADC(op_codes, temp_addition_addr); // Add with carry into to the accum
            
                    gen_storeAccumulatorIntoMemory_8D_STA(op_codes, temp_addition_addr); // Store in temp location
                    
                
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
                    gen_storeAccumulatorIntoMemory_8D_STA(op_codes, temp_addition_addr); // Store accumulator value in our temporary location for addition
                    
                    //gen_loadAccumulatorFromMemory_AD_LDA(op_codes, temp_addition_addr); // Load accumulator using the address of the value we just stored (redundant)
                    //gen_storeAccumulatorIntoMemory_8D_STA(op_codes, static_table_temp_location); // Store value of accumulator into location of a (wrong)

                }

            } else {
                System.out.println("Should be done with addition now");
            } 
        }  
        
        System.out.println("Returning addition Final");
        return op_codes; 
    }


    public ArrayList<String> processPrintStatement (NonTerminal PrintStatement) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>(); 
        for (int i = 0; i <= PrintStatement.getASTChildren().size() - 1; i++) {
            System.out.println("PrintStatement Child: " + PrintStatement.getASTChild(i).getName() + " : " + PrintStatement.getASTChild(i).getProdKind()); 
        }
        //System.out.println();
        if (PrintStatement.getASTChild(0).getName().equals("IDENTIFIER")) {
            Terminal identifier = (Terminal) PrintStatement.getASTChild(0);
            System.out.println("PrintStatement - Terminal");
            //String type =  ((Terminal) PrintStatement.getASTChild(0)).getTokenAttribute();
            String type = getIdentifierType(PrintStatement, identifier);
            System.out.println("Print Type: " + type);
            String temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(PrintStatement, 0); // Location in static table for the Identifier the print statement is trying to print    
            gen_loadYRegisterFromMemory_AC_LDY(op_codes, temp_location);
            gen_finishPrintStatment(op_codes, type); 
        }

        return op_codes;
    }
    
    public void processAssignmentRightHandSide () {

    }
    

    // Did not code myself
    public String translateOpcodesToEnglish(ArrayList<String> opcodes, ExecutionEnvironment env) throws CodeGenerationException{
        StringBuilder sb = new StringBuilder();
    
        for (int i = 0; i < opcodes.size(); i++) {
            String code = opcodes.get(i);
            String nextValue = (i + 1 < opcodes.size()) ? opcodes.get(i + 1) : "";
            
            switch (code) {
                case "A9":
                    // Load accumulator with constant
                    sb.append(String.format("   LDA #$%s - Load the accumulator with constant %s", nextValue, nextValue));
                    i++;
                    break;
                case "AD":
                    // Load accumulator from memory
                    sb.append(String.format("   LDA $%s00 - Load the accumulator from memory at address $%s00, which contains %s", nextValue, nextValue, "address pointer [hex: " + env.getValueFromCodeSequence(i+1) + ", decimal: " + Integer.parseInt(nextValue, 16) + "]"));
                    i++;
                    break;
                case "8D":
                    // Store accumulator in memory
                    sb.append(String.format("   STA $%s00 - Store the accumulator into memory at address $%s00", nextValue, nextValue));
                    i++;
                    break;
                case "A2":
                    // Load X with constant
                    sb.append(String.format("   LDX #$%s - Load the X register with constant %s", nextValue, nextValue));
                    i++;
                    break;
                case "AE":
                    // Load X from memory
                    sb.append(String.format("   LDX $%s00 - Load the X register from memory at address $%s00, which contains %s", nextValue, nextValue, "address pointer [hex: " + env.getValueFromCodeSequence(i+1) + ", decimal: " + Integer.parseInt(nextValue, 16) + "]"));
                    i++;
                    break;
                case "A0":
                    // Load Y with constant
                    sb.append(String.format("   LDY #$%s - Load the Y register with constant %s", nextValue, nextValue));
                    i++;
                    break;
                case "AC":
                    // Load Y from memory
                    sb.append(String.format("   LDY $%s00 - Load the Y register from memory at address $%s00, which contains %s", nextValue, nextValue, "address pointer [hex: " + env.getValueFromCodeSequence(i+1) + ", decimal: " + Integer.parseInt(nextValue, 16) + "]"));
                    i++;
                    break;
                case "EA":
                    sb.append("   NOP - No operation\n");
                    break;
                case "00":
                    if ( (i <= execution_environment.getStackPointer()) || (i >= execution_environment.getHeapPointer()) ) {
                        sb.append("   BRK - Break\n");
                    }
                    break;
                case "EC":
                    // Compare X with memory
                    sb.append(String.format("   CPX $%s00 - Compare the X register with memory at address $%s00, which contains %s", nextValue, nextValue, "address pointer [hex: " + env.getValueFromCodeSequence(i+1) + ", decimal: " + Integer.parseInt(nextValue, 16) + "]"));
                    i++;
                    break;
                case "D0":
                    sb.append(String.format("   BNE $%s - Branch n bytes if Z flag is 0, to skip %s bytes", nextValue, nextValue));
                    i++;
                    break;
                case "EE":
                    // Increment memory
                    sb.append(String.format("   INC $%s00 - Increment the value at memory location $%s00", nextValue, nextValue));
                    i++;
                    break;
                case "FF":
                    sb.append("   SYS - System Call\n");
                    break;
                default:
                    sb.append(String.format("   Unknown opcode: %s\n", code));
                    break;
            }
            if ( ((i <= execution_environment.getStackPointer()) || (i >= execution_environment.getHeapPointer())) ) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    // Did not code myself
    public String translateOpcodesToEnglishUnrestricted(ArrayList<String> opcodes, ExecutionEnvironment env) throws CodeGenerationException{
        StringBuilder sb = new StringBuilder();
    
        for (int i = 0; i < opcodes.size(); i++) {
            String code = opcodes.get(i);
            String nextValue = (i + 1 < opcodes.size()) ? opcodes.get(i + 1) : "";
            
            switch (code) {
                case "A9":
                    // Load accumulator with constant
                    sb.append(String.format("   LDA #$%s - Load the accumulator with constant %s", nextValue, nextValue));
                    i++;
                    break;
                case "AD":
                    // Load accumulator from memory
                    sb.append(String.format("   LDA $%s00 - Load the accumulator from memory at address $%s00, which contains %s", nextValue, nextValue, "address pointer [hex: " + env.getValueFromCodeSequence(i+1) + ", decimal: " + Integer.parseInt(nextValue, 16) + "]"));
                    i++;
                    break;
                case "8D":
                    // Store accumulator in memory
                    sb.append(String.format("   STA $%s00 - Store the accumulator into memory at address $%s00", nextValue, nextValue));
                    i++;
                    break;
                case "A2":
                    // Load X with constant
                    sb.append(String.format("   LDX #$%s - Load the X register with constant %s", nextValue, nextValue));
                    i++;
                    break;
                case "AE":
                    // Load X from memory
                    sb.append(String.format("   LDX $%s00 - Load the X register from memory at address $%s00, which contains %s", nextValue, nextValue, "address pointer [hex: " + env.getValueFromCodeSequence(i+1) + ", decimal: " + Integer.parseInt(nextValue, 16) + "]"));
                    i++;
                    break;
                case "A0":
                    // Load Y with constant
                    sb.append(String.format("   LDY #$%s - Load the Y register with constant %s", nextValue, nextValue));
                    i++;
                    break;
                case "AC":
                    // Load Y from memory
                    sb.append(String.format("   LDY $%s00 - Load the Y register from memory at address $%s00, which contains %s", nextValue, nextValue, "address pointer [hex: " + env.getValueFromCodeSequence(i+1) + ", decimal: " + Integer.parseInt(nextValue, 16) + "]"));
                    i++;
                    break;
                case "EA":
                    sb.append("   NOP - No operation\n");
                    break;
                case "00":
                    
                    sb.append("   BRK - Break\n");
                    
                    break;
                case "EC":
                    // Compare X with memory
                    sb.append(String.format("   CPX $%s00 - Compare the X register with memory at address $%s00, which contains %s", nextValue, nextValue, "address pointer [hex: " + env.getValueFromCodeSequence(i+1) + ", decimal: " + Integer.parseInt(nextValue, 16) + "]"));
                    i++;
                    break;
                case "D0":
                    sb.append(String.format("   BNE $%s - Branch n bytes if Z flag is 0, to skip %s bytes", nextValue, nextValue));
                    i++;
                    break;
                case "EE":
                    // Increment memory
                    sb.append(String.format("   INC $%s00 - Increment the value at memory location $%s00", nextValue, nextValue));
                    i++;
                    break;
                case "FF":
                    sb.append("   SYS - System Call\n");
                    break;
                default:
                    sb.append(String.format("   Unknown opcode: %s\n", code));
                    break;
            }
            
                sb.append("\n");
        }
        return sb.toString();
    }


    public static ArrayList<String> parseHexadecimalString(String hexString) {
        // Split the string into an array of strings using space as the delimiter
        String[] hexValues = hexString.split("\\s+");
        // Create an ArrayList from the array of hex values
        return new ArrayList<>(Arrays.asList(hexValues));
    }
    
    public ArrayList<String> gen_loadAccumulatorWithConstant_A9_LDA (ArrayList<String> op_codes, String value) throws CodeGenerationException {
        
        op_codes.add("A9");
        if (value.length() == 2) op_codes.add(value); // Heap locations are loaded into the accumulator and they are of length 2, making adding the extra 0 unnecessary 
        else op_codes.add("0" + value);
        return op_codes;
    }

    public ArrayList<String> gen_loadAccumulatorFromMemory_AD_LDA (ArrayList<String> op_codes, String location) throws CodeGenerationException {
        op_codes.add("AD");
        op_codes.add(location); op_codes.add("00");
        return op_codes;
    }

    public ArrayList<String> gen_storeAccumulatorIntoMemory_8D_STA (ArrayList<String> op_codes, String location) throws CodeGenerationException {
        op_codes.add("8D");
        op_codes.add(location); op_codes.add("00");
        return op_codes;
    }

    public ArrayList<String> gen_addWithCarryToAccum_6D_ADC (ArrayList<String> op_codes, String location) throws CodeGenerationException {
        op_codes.add("6D");
        op_codes.add(location); op_codes.add("00");
        return op_codes;
    }

    public ArrayList<String> gen_loadYRegisterFromMemory_AC_LDY (ArrayList<String> op_codes, String location) throws CodeGenerationException {
        op_codes.add("AC");
        op_codes.add(location); op_codes.add("00");
        return op_codes;
    }

    public ArrayList<String> gen_finishPrintStatment(ArrayList<String> op_codes, String type) throws CodeGenerationException {
        op_codes.add("A2");
        if (type.equals("string"))  op_codes.add("02");
        else if (type.equals("boolean")) op_codes.add("02");
        else if (type.equals("int")) op_codes.add("01");
        else throw new CodeGenerationException("CodeGeneration, gen_finishPrintStatment()", "Unknown type, " + type + ". Unable to continue. ");
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

    public String getStringFromCHARACTER (Production parent_production, int index) {
        String str = ((Terminal) parent_production.getASTChild(index)).getTokenAttribute();
        return str; 
    }

    

    public ArrayList<String> getProductionChildrenArrayList (Production parent) {
        ArrayList<String> children_names = new ArrayList<>(); 
        ArrayList<Production> children = parent.getASTChildren();
        for ( int i = 0; i <= children.size() - 1; i++ ) {
            children_names.add(children.get(i).getName());
        } return children_names; 
    }

    public String getIdentifierType (Production parent, Terminal identifier) {
        String scope = parent.getScopeName(); 
        String name = identifier.getName(); 
        String id = identifier.getTokenAttribute(); 
        System.out.println("GetIdentifierType - Scope: " + scope + ", Name: " + name + " Id: " + id); 
        pout("Scopes:  " + symbol_table.getScopeNames());
        SymbolTableScope residing_scope = symbol_table.getScope(scope);
        boolean entry_exists = residing_scope.entryExists(id);
        pout("Entry Exists: " + entry_exists);
        SymbolTableEntry specific_entry = residing_scope.retrieveEntry(id);
        String type = specific_entry.getType();
        pout("Type: " + type);
        return type; 
    }

    public String stringOfCharacters(int amount, String character) { String s = ""; for (int j = 0; j <= amount-1; j++) { s = s + character; } return s; }
    
    public void pout(String s) {System.out.println(s);}

    public void performCodeGeneration (ArrayList<Production> AST, SymbolTable st, Toolkit tk ) throws CodeGenerationException {
            System.out.println("\n\nCODE GENERATION:");
            symbol_table = st; 
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

            String readableOpcodes = translateOpcodesToEnglish(new ArrayList<>(Arrays.asList(execution_environment.getCodeSequence())), execution_environment);
            System.out.println("\n┌--------------------------------------------------------------------------------------------------------------------┐");
            System.out.println("|-------------------------------------------READABLE INSTRUCTION OUTPUT----------------------------------------------|");
            System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
            System.out.println(readableOpcodes);
            
            
            
            System.out.println("\n┌--------------------------------------------------------------------------------------------------------------------┐");
            System.out.println("|-------------------------------------------READABLE INSTRUCTION OUTPUT----------------------------------------------|");
            System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
            ArrayList<String> proper_ops = parseHexadecimalString("A9 F5 8D 44 00 A9 F5 8D 45 00 A9 F5 8D 46 00 A9 F5 8D 44 00 A9 F5 8D 45 00 AE 44 00 A9 F5 8D 47 00 EC 47 00 D0 0C A9 F0 8D 48 00 A2 FF EC F5 00 D0 05 A9 F5 8D 48 00 AD 48 00 8D 46 00 AC 46 00 A2 02 FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 74 72 75 65 00 66 61 6C 73 65 00 6E 75 6C 6C 00");
            String proper_readable_ops = translateOpcodesToEnglishUnrestricted(proper_ops, execution_environment);
            System.out.println(proper_readable_ops);
            
            
            System.out.println("└--------------------------------------------------------------------------------------------------------------------┘");
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