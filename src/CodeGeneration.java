import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CodeGeneration {
    public Toolkit toolkit;
    public ExecutionEnvironment execution_environment;
    public SymbolTable symbol_table;
    public String addition_temp_addr;

    public boolean within_nested_addition = false;


    public ArrayList<String> constants = new ArrayList<String>(Arrays.asList("KEYWORD_TRUE", "KEYWORD_FALSE", "DIGIT")); // TODO: Add DIGIT?

    // REMEMBER: Make it fluid, not rigid. 
    public void traverseIntermediateRepresentation ( Production p, int index ) throws CodeGenerationException {
        
        // Removed initial edge case catching code for BLOCK at index 0, when that was an issue but I do not think it is anymore

        for (int i = 0; i <= p.getASTChildren().size() - 1; i++ ) {
            Production c = p.getASTChild(i);
            Boolean is_terminal = (c.getClass().getSimpleName()).equals("Terminal");

            // Need to traverse down all and look for NT, 
            if (!is_terminal) {
                NonTerminal nt = (NonTerminal) c;     
                System.out.println("NonTerminal Name: " + nt.getName());
                
                nonterminalRouter(nt);
                
            }

            System.out.println("Traversing again...");
            traverseIntermediateRepresentation(c, index + 1);
        }
    }

    // *** Route NonTerminals to their respective NonTerminal Handlers 
    public void nonterminalRouter (NonTerminal nt ) throws CodeGenerationException{
        
        switch (nt.getName()) {
            
            case "VarDeclStatement": 
                processVariableDeclaration(nt);
                //String[] op_codes_array = op_codes.toArray(new String[0]); // convert to standard array
                //execution_environment.insert(op_codes_array, "int", "Code");
                break; 
               
            case "AssignmentStatement": 
                processAssignmentStatement(nt);
                break; 
            case "PrintStatement": 
                processPrintStatement(nt);
                break; 
            
            //case "ADDITION": 

                
        }
    }

    /*********************************************** NonTerminal Handlers ***********************************************/

    public void processVariableDeclaration (NonTerminal VarDeclStatement) throws CodeGenerationException{
        
        Terminal type = (Terminal) VarDeclStatement.getASTChild(0);
        Terminal identifier = (Terminal) VarDeclStatement.getASTChild(1);
        String identifier_type = getIdentifierType( (Production) VarDeclStatement, identifier);
        String default_value = ""; 
        
        
        if (identifier_type.equals("string")) default_value = "6E";  // null
        else if (identifier_type.equals("int")) default_value = "00";
        else if (identifier_type.equals("boolean")) default_value = "F5"; // false
        
        pout("processVarDecl - type: " + identifier_type); 

        String temp_addr = execution_environment.performStaticTableInsertion(identifier.getTokenAttribute(), VarDeclStatement.getScopeName()); // Maybe could be problematic with scope
        
        ArrayList<String> op_codes = new ArrayList<>(Arrays.asList("A9", default_value, "8D", temp_addr, "00"));
        execution_environment.insertImmediately(op_codes, "Code");
    }

    public void processAssignmentStatement (NonTerminal AssignmentStatement) throws CodeGenerationException {
        
        // Decls 
        ArrayList<String> op_codes = new ArrayList<>(); 
        ArrayList<String> assignment_children = getProductionChildrenArrayList(AssignmentStatement);
        Terminal identifier_terminal = (Terminal) AssignmentStatement.getASTChild(0);
        ArrayList<Production> identifier_nt_astchildren = AssignmentStatement.getASTChildren();
        
        String identifiers_temporary_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 0); // LHS, where value will be assigned

        // Output
        System.out.println("\n\nAssignment for " + identifier_terminal.getName() + ", " + identifier_terminal.getTokenAttribute());  System.out.println("Assignment Statement AST-Children: ");
        for (int i = 0; i <= identifier_nt_astchildren.size() - 1; i++) { System.out.println(AssignmentStatement.getASTChild(i).getName() + " : " + AssignmentStatement.getASTChild(i).getProdKind()); }
         
        // IsEqual Assignment
        if (assignment_children.contains("IsEqual")) {
            NonTerminal IsEqual = (NonTerminal) AssignmentStatement.getASTChild(1);
            processIsEqualAssignment(IsEqual, identifiers_temporary_location);
        }
        
        // Where single ID assignment goes
        if (assignment_children.contains("DIGIT") && assignment_children.size() == 2) {
            Terminal DIGIT_terminal = (Terminal) AssignmentStatement.getASTChild(1);
            processDIGITAssignment(DIGIT_terminal, identifiers_temporary_location); // Store digit in temp location
        }
        
        // Got screwed up when it found rhs character but saw lhs identifier
        else if (assignment_children.get(1).equals("IDENTIFIER")) {
            
            String lhs_id = ((Terminal) AssignmentStatement.getASTChild(0)).getTokenAttribute();
            String rhs_id = ((Terminal) AssignmentStatement.getASTChild(1)).getTokenAttribute();
            pout("ID LHS: " + lhs_id); pout("ID RHS: " + rhs_id);
            
            String lhs_id_temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 0); // left hand side
            String rhs_id_temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 1); // right side assignment id
            
            pout("LHS Location: " + lhs_id_temp_location); pout("RHS Location: " + rhs_id_temp_location);
            processIDENTIFIERAssignment(rhs_id_temp_location, lhs_id_temp_location);
        
        }


        // Single, non-complex, boolean assignment
        else if ( (assignment_children.contains("KEYWORD_TRUE") || (assignment_children.contains("KEYWORD_FALSE") ) && assignment_children.size() == 2)) {
            pout("Boolean Assignment");
            Terminal boolean_terminal = (Terminal) AssignmentStatement.getASTChild(1);
            processBOOLEANAssignment(boolean_terminal, identifiers_temporary_location);
        }

        //(String.format("%02X", ""+ (execution_environment.getHeapPointer() - hex_arraylist.length )))
        else if (assignment_children.contains("CHARACTER")) {
            
            int heap_pointer;
            Terminal CHARACTER_terminal = (Terminal) AssignmentStatement.getASTChild(1);
            String terminals_string_value = getStringFromCHARACTER(AssignmentStatement, 1); 
            String[] hex_arraylist = terminals_string_value.chars().mapToObj(c -> String.format("%02X", c)).toArray(String[]::new);
            boolean exists_in_strdecls = execution_environment.stringExistsWithStringDeclarations(terminals_string_value); System.out.println("\n\nExists within string decls: " + exists_in_strdecls);

            if (exists_in_strdecls) {
                heap_pointer = execution_environment.getAddressFromStringDeclarations(terminals_string_value); // If exists in string_decls already, get address
                System.out.println("Heap Pointer: " + heap_pointer);
            } else {
                heap_pointer = execution_environment.getHeapPointer() - hex_arraylist.length - 1; // otherwise get the current heap pointer, making sure to modify it for what it will be 
                System.out.println("Heap Pointer: " + heap_pointer);
                execution_environment.insertIntoStringDeclarations(terminals_string_value, heap_pointer); // Store in string_declarations hashmap 
                ArrayList<String> hex_arraylist_actually = new ArrayList<>(Arrays.asList(hex_arraylist));
                execution_environment.insertImmediately(hex_arraylist_actually, "Heap"); // insert it into code_sequence without waiting, so we can specify Heap insertion
            }
            
            // Need to update heap pointer after being done, need to full on return after being in this elif block, need to store pointer for string in string_declarations map 
            String heap_pointer_hex = String.format("%02X", heap_pointer); System.out.println("Heap Pointer Hex: " + heap_pointer_hex);

            processCHARACTERAssignment(CHARACTER_terminal, heap_pointer_hex, identifiers_temporary_location);  
        }

        else if (assignment_children.contains("ADDITION")) {            
            NonTerminal assignment_nonterminal = (NonTerminal) AssignmentStatement.getASTChild(1); 
            String new_addition_temp_addr = execution_environment.performStaticTableInsertion("addition", AssignmentStatement.getScopeName());  // Maybe... // Create location to store during addition
            processADDITIONAssignment(assignment_nonterminal, new_addition_temp_addr, identifiers_temporary_location, false); // Process addition
            //System.out.println("Assignment - Stored Value for Identifier: " + identifier_terminal.getName() + ", at location: " + id_temp_location + "\n");
            //op_codes.addAll(nonterminalRouter((NonTerminal) child));
        }
 
    }

    
    public void processPrintStatement (NonTerminal PrintStatement) throws CodeGenerationException {
        /*
        * Currently only prints IDENTIFIERS
        * If String is argument, e.g., print("string") --> It would fail
        */
        ArrayList<String> op_codes = new ArrayList<>(); 
        for (int i = 0; i <= PrintStatement.getASTChildren().size() - 1; i++) { System.out.println("PrintStatement Child: " + PrintStatement.getASTChild(i).getName() + " : " + PrintStatement.getASTChild(i).getProdKind()); }
        if (PrintStatement.getASTChildren().size() > 1) throw new CodeGenerationException("CodeGeneration, processPrintStatment", "I thought print statements could only have one child");
        
        Terminal print_child = (Terminal) PrintStatement.getASTChild(0); 

        // Can't there only be one child...? Why did I do this...
        
        //PrintStatement.getASTChild(0).getName().equals("IDENTIFIER")



        if (print_child.getName().equals("IDENTIFIER")) {
            //Terminal identifier = (Terminal) PrintStatement.getASTChild(0);
            String type = getIdentifierType(PrintStatement, print_child); System.out.println("PrintStatement - Terminal, type: " + type);
            String temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(PrintStatement, 0); // Location in static table for the Identifier the print statement is trying to print    
            gen_loadYRegisterFromMemory_AC_LDY(temp_location);
            gen_finishPrintStatment(type); 

        } else if (print_child.getName().equals("CHARACTER")) {
            String hex_location = handleCHARACTERterminalAndGetStringAddress(print_child); 
            gen_loadYRegisterFromConstant_AO_LDY(hex_location);
            gen_finishPrintStatment("string"); 
        }

    }

    
    // IsEqual
    public void processIsEqualAssignment ( NonTerminal IsEqual, String assignment_location ) throws CodeGenerationException {
        Production lhs = IsEqual.getASTChild(0);
        Production rhs = IsEqual.getASTChild(1);
        if (lhs.getProdKind().equals("Terminal") && rhs.getProdKind().equals("Terminal")) { 
            
            String temp_addr_1 = execution_environment.performStaticTableInsertion("ta1" + execution_environment.getTemporaryValueCounter(), IsEqual.getScopeName()); // Create first temp addr
            String temp_addr_2 = execution_environment.performStaticTableInsertion("ta2" + execution_environment.getTemporaryValueCounter(), IsEqual.getScopeName()); // Create first temp addr
            
            Terminal lhs_terminal = (Terminal) lhs; String lhs_terminal_name = lhs_terminal.getName();
            Terminal rhs_terminal = (Terminal) rhs; String rhs_terminal_name = rhs_terminal.getName();

            String lhs_terminal_addressing_component = getTerminalAddressingComponent(IsEqual, lhs_terminal, 0);
            String rhs_terminal_addressing_component = getTerminalAddressingComponent(IsEqual, rhs_terminal, 1);

            // LHS goes in X Register


            if (constants.contains(lhs_terminal_name)) {
                gen_loadXRegisterWithValue_A2_LDX(lhs_terminal_addressing_component); // Load X Register with LHS 
            } else {
                gen_loadXRegisterFromAddress_AE_LDX(lhs_terminal_addressing_component); // LOAD LHS
            }

            if (constants.contains(rhs_terminal_name)) {
                gen_loadAccumulatorWithConstant_A9_LDA(rhs_terminal_addressing_component); // Load RHS constant into Accumulator
            } else {
                gen_loadAccumulatorFromMemory_AD_LDA(rhs_terminal_addressing_component);
            }

            // IF CONSTANT, USE A2, e.g., DIGIT, BOOLEANS 
            //gen_loadXRegisterWithValue_A2_LDX(lhs_terminal_addressing_component); // Load X Register with LHS 
            //gen_loadXRegisterFromAddress_AE_LDX(lhs_terminal_addressing_component); // LOAD LHS
            //gen_loadAccumulatorWithConstant_A9_LDA(rhs_terminal_addressing_component); // Load RHS constant into Accumulator
            gen_storeAccumulatorIntoMemory_8D_STA(temp_addr_1); // Store into NEW Temp addr, the false pointer in Accumulator 
            gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(temp_addr_1); // Compare Temp Addr 1 with what is in X Register

            gen_branchNBytes_D0_BNE("0C"); // Branch 12 bytes If NOT Equal 

                // IF EQUAL (12 bytes)
                gen_loadAccumulatorWithConstant_A9_LDA(execution_environment.getTruePointer()); // Load TRUE constant into Accumulator
                gen_storeAccumulatorIntoMemory_8D_STA(temp_addr_2); // Store in NEW TEMP 2

                    // Force Flip
                    gen_loadXRegisterWithValue_A2_LDX("FF"); // 255 which is 00 in code_sequence
                    gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(execution_environment.getFalsePointer()); // Load FALSE constant as address (not sure if the constant as address is what guarantees flip or the if it does reference false in heap, nevertheless it is false)
                    gen_branchNBytes_D0_BNE("05"); // BRANCH FORWARD 5 Bytes -- which IT WILL DO because it is guaranteed false --> SKIPS "IF FALSE"

            //IF FALSE (Will be skipped if TRUE due to force flip ) (5 bytes)
            gen_loadAccumulatorWithConstant_A9_LDA(execution_environment.getFalsePointer()); // Load FALSE constant into Accumulator
            gen_storeAccumulatorIntoMemory_8D_STA(temp_addr_2); // Store in NEW TEMP 2

            gen_loadAccumulatorFromMemory_AD_LDA(temp_addr_2);// Temp Addr 2, at this point, contains answer to whether equal or not
            //gen_loadAccumulatorWithConstant_A9_LDA(temp_addr_2); 
            gen_storeAccumulatorIntoMemory_8D_STA(assignment_location); // Store answer in assignment location, the location of the variable in which the result of this comparison is being stored

        }
    

    }

    //public boolean processIsEqual (NonTerminal IsEqual, String assignment_locatio, boolean is_assignment) {

    //}

    // ADDITION
    
    public void processADDITIONAssignment (NonTerminal ADDITION, String temp_addition_addr, String assignment_location, boolean within_nested_addition) throws CodeGenerationException {
        
        ArrayList<String> op_codes = new ArrayList<>(); 
    
        for (int i = 0; i <= ADDITION.getASTChildren().size() - 1; i++) { System.out.println("Addition Child: " + ADDITION.getASTChild(i).getName() + " : " + ADDITION.getASTChild(i).getProdKind()); }
    
        
        if (ADDITION.getASTChild(0).getName().equals("DIGIT")) {
            int digit_value = Integer.parseInt(((Terminal) ADDITION.getASTChild(0)).getTokenAttribute());  System.out.println("Digit Value: " + digit_value);
            
            gen_loadAccumulatorWithConstant_A9_LDA(""+digit_value); // Load accum. with value in Digit

            if (within_nested_addition) gen_addWithCarryToAccum_6D_ADC(temp_addition_addr);  // If within recursive addition, add contents already in temp address into accumulator

            gen_storeAccumulatorIntoMemory_8D_STA(temp_addition_addr); // Store contents of the accumulator into memory, using the address of our temporary location
            
            if (ADDITION.getASTChildren().size() > 1) {
                
                // if of form: 3 + 1
                if (ADDITION.getASTChild(1).getName().equals("DIGIT")) {
                    int second_digit_value = Integer.parseInt(((Terminal) ADDITION.getASTChild(1)).getTokenAttribute()); System.out.println("Second Digit: " + second_digit_value);
                    
                    gen_loadAccumulatorWithConstant_A9_LDA(""+second_digit_value);  // Load accumulator with second value
                    gen_addWithCarryToAccum_6D_ADC(temp_addition_addr); // Add with carry into to the accum
                    gen_storeAccumulatorIntoMemory_8D_STA(temp_addition_addr); // Store in temp location
                    
                
                 // if of form 3+3 +a
                } else if (ADDITION.getASTChild(1).getName().equals("ADDITION")) {
                    within_nested_addition = true; 
                    processADDITIONAssignment((NonTerminal) ADDITION.getASTChild(1), temp_addition_addr, assignment_location, within_nested_addition);

                // = 3 + a
                } else if (ADDITION.getASTChild(1).getName().equals("IDENTIFIER")) {
                    String scope = ADDITION.getScopeName();
                    String id = ((Terminal) ADDITION.getASTChild(1)).getTokenAttribute(); 
                    String static_table_variable_name = id + "@" + scope; 
                    String static_table_temp_location = execution_environment.retrieveTempLocationFromStaticTable(static_table_variable_name);
                    System.out.println("Second Value is Identifier: " + id);
                
                    gen_loadAccumulatorFromMemory_AD_LDA(static_table_temp_location); // Load value for a into accumulator
                    gen_addWithCarryToAccum_6D_ADC(temp_addition_addr); // Add contents of temporary addition address to accumulator  
                    gen_storeAccumulatorIntoMemory_8D_STA(temp_addition_addr); // Store accumulator value in our temporary location for addition
                    
                    //gen_loadAccumulatorFromMemory_AD_LDA(op_codes, temp_addition_addr); // Load accumulator using the address of the value we just stored (redundant)
                    //gen_storeAccumulatorIntoMemory_8D_STA(op_codes, static_table_temp_location); // Store value of accumulator into location of a (wrong)

                }

            } else {
                within_nested_addition = false;
                System.out.println("Should be done with addition now");
            } 
        }  
        
        within_nested_addition = false;
        gen_loadAccumulatorFromMemory_AD_LDA(temp_addition_addr); // Load accumulator with value at address
        gen_storeAccumulatorIntoMemory_8D_STA(assignment_location); // Store in location for id
        System.out.println("Returning addition Final");
    }



    /*********************************************** Terminal Handlers ***********************************************/

    public void processDIGITAssignment (Terminal DIGIT, String identifiers_temporary_location) throws CodeGenerationException {
        int digit_value = Integer.parseInt(DIGIT.getTokenAttribute());
        ArrayList<String> op_codes = new ArrayList<>(); 
        gen_loadAccumulatorWithConstant_A9_LDA(""+ digit_value);
        gen_storeAccumulatorIntoMemory_8D_STA(identifiers_temporary_location);
    } 

    public void processCHARACTERAssignment (Terminal CHARACTER, String heap_pointer_hex, String identifiers_temporary_location) throws CodeGenerationException {
        String string_value = CHARACTER.getTokenAttribute(); 
        ArrayList<String> op_codes = new ArrayList<>(); 
        gen_loadAccumulatorWithConstant_A9_LDA(heap_pointer_hex); // Store pointer to location in the heap
        gen_storeAccumulatorIntoMemory_8D_STA(identifiers_temporary_location);
    }
    // Where LHS is identifier

    public void processIDENTIFIERAssignment(String rhs_id_temp_location, String lhs_id_temp_location) throws CodeGenerationException{
        ArrayList<String> op_codes = new ArrayList<>(); 
        gen_loadAccumulatorFromMemory_AD_LDA(rhs_id_temp_location); // load accumulator with value from identifiers location
        gen_storeAccumulatorIntoMemory_8D_STA(lhs_id_temp_location);
    }

    public void processBOOLEANAssignment (Terminal boolean_terminal, String identifiers_temporary_location) throws CodeGenerationException {
        String boolean_value = boolean_terminal.getTokenAttribute();
        ArrayList<String> op_codes = new ArrayList<>(); 
        String heap_boolean_location = (boolean_value.equals("KEYWORD_TRUE") ? execution_environment.getTruePointer() : execution_environment.getFalsePointer());
        gen_loadAccumulatorWithConstant_A9_LDA(heap_boolean_location);
        pout("processBOOLEANAssignment - loaded accumulator");
        gen_storeAccumulatorIntoMemory_8D_STA(identifiers_temporary_location);
        pout("processBOOLEANAssignment - stored accumulator into memory ");
    } 

    

    /*********************************************** OP Code Generators  ***********************************************/

    public void gen_loadXRegisterFromAddress_AE_LDX ( String address) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>();
        op_codes.add("AE"); 
        op_codes.add(address); op_codes.add("00"); 
        execution_environment.insertImmediately(op_codes, "Code");
    }

    public void gen_loadXRegisterWithValue_A2_LDX ( String value) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>();
        op_codes.add("A2"); 
        op_codes.add(value); //op_codes.add("00"); fk.
        execution_environment.insertImmediately(op_codes, "Code");
    }

    public void gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX ( String address) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>();
        op_codes.add("EC"); 
        op_codes.add(address); op_codes.add("00"); 
        execution_environment.insertImmediately(op_codes, "Code");
    }

    public void gen_loadAccumulatorWithConstant_A9_LDA ( String value) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>();
        op_codes.add("A9");
        if (value.length() == 2) op_codes.add(value); // Heap locations are loaded into the accumulator and they are of length 2, making adding the extra 0 unnecessary 
        else op_codes.add("0" + value);
        execution_environment.insertImmediately(op_codes, "Code");
    }

    public void gen_loadAccumulatorFromMemory_AD_LDA (  String location) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>();
        op_codes.add("AD");
        op_codes.add(location); op_codes.add("00");
        execution_environment.insertImmediately(op_codes, "Code");
    }

    public void gen_storeAccumulatorIntoMemory_8D_STA ( String location) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>();
        op_codes.add("8D");
        op_codes.add(location); op_codes.add("00");
        execution_environment.insertImmediately(op_codes, "Code");
    }

    public void gen_addWithCarryToAccum_6D_ADC ( String location) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>();
        op_codes.add("6D");
        op_codes.add(location); op_codes.add("00");
        execution_environment.insertImmediately(op_codes, "Code");
    }

    public void gen_loadYRegisterFromMemory_AC_LDY ( String location) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>();
        op_codes.add("AC");
        op_codes.add(location); op_codes.add("00");
        execution_environment.insertImmediately(op_codes, "Code");
    }

    public void gen_loadYRegisterFromConstant_AO_LDY ( String location) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>();
        op_codes.add("A0");
        op_codes.add(location);
        execution_environment.insertImmediately(op_codes, "Code");
    }

    public void gen_branchNBytes_D0_BNE(String byte_amount_in_hex) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>();
        op_codes.add("D0"); op_codes.add(byte_amount_in_hex); 
        execution_environment.insertImmediately(op_codes, "Code");
    }


    public void gen_finishPrintStatment( String type) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>();
        op_codes.add("A2");
        if (type.equals("string"))  op_codes.add("02");
        else if (type.equals("boolean")) op_codes.add("02");
        else if (type.equals("int")) op_codes.add("01");
        else throw new CodeGenerationException("CodeGeneration, gen_finishPrintStatment()", "Unknown type, " + type + ". Unable to continue. ");
        op_codes.add("FF");
        execution_environment.insertImmediately(op_codes, "Code");
    }


    public void gen_loadXRegister(NonTerminal parent, Terminal terminal, int index) throws CodeGenerationException {
        ArrayList<String> op_codes = new ArrayList<>(); 
        
        if (terminal.getName().equals("IDENTIFIER")) {
            String id_temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(parent, index); 
            gen_loadXRegisterFromAddress_AE_LDX(id_temp_location); // Load address of ID from static table
        } else if (terminal.getName().equals("DIGIT")) {
            String value = terminal.getTokenAttribute(); 
            gen_loadXRegisterWithValue_A2_LDX(value);
        } else if (terminal.getName().equals("CHARACTER")) {
            String string_location = handleCHARACTERterminalAndGetStringAddress(terminal);
            gen_loadXRegisterFromAddress_AE_LDX(string_location);
            
        } else if ( (terminal.getName().equals("KEYWORD_TRUE")) || (terminal.getName().equals("KEYWORD_FALSE"))) {
            if (terminal.getName().equals("KEYWORD_TRUE")) {
                gen_loadXRegisterWithValue_A2_LDX(execution_environment.getTruePointer());
            } else if (terminal.getName().equals("KEYWORD_FALSE")) {
                gen_loadXRegisterWithValue_A2_LDX(execution_environment.getFalsePointer());
            }
        }

        //execution_environment.insertImmediately(op_codes, "Code");

        // elif boolean, elif DIGIT, 
    }

    /*********************************************** Helpers ***********************************************/

    public String getTerminalAddressingComponent (NonTerminal parent, Terminal terminal, int index_if_identifier) throws CodeGenerationException {
        String addressing_component = ""; 
        
        if (terminal.getName().equals("IDENTIFIER")) { addressing_component = execution_environment.retrieveTempLocationFromChildOfNonTerminal(parent, index_if_identifier);    
        
        } else if (terminal.getName().equals("DIGIT")) { addressing_component= "0" + terminal.getTokenAttribute(); 

        } else if (terminal.getName().equals("CHARACTER")) { addressing_component = handleCHARACTERterminalAndGetStringAddress(terminal);
            
        } else if ( (terminal.getName().equals("KEYWORD_TRUE")) || (terminal.getName().equals("KEYWORD_FALSE"))) {
            if (terminal.getName().equals("KEYWORD_TRUE"))  addressing_component = execution_environment.getTruePointer(); 
            else if (terminal.getName().equals("KEYWORD_FALSE"))  addressing_component = execution_environment.getFalsePointer();

        } else throw new CodeGenerationException("CodeGeneration, getLocationOfTerminal()", "Terminal sent to getLocationOfTerminal() is not of type IDENTIFIER, CHARACTER, KEYWORD_TRUE, KEYWORD_FALSE. \nTerminal type received: " + terminal.getName());

        return addressing_component; 
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

    public static ArrayList<String> parseHexadecimalString(String hexString) {
        // Split the string into an array of strings using space as the delimiter
        String[] hexValues = hexString.split("\\s+");
        // Create an ArrayList from the array of hex values
        return new ArrayList<>(Arrays.asList(hexValues));
    }

    public ArrayList<String> getProductionChildrenArrayList (Production parent) {
        ArrayList<String> children_names = new ArrayList<>(); 
        ArrayList<Production> children = parent.getASTChildren();
        for ( int i = 0; i <= children.size() - 1; i++ ) {
            children_names.add(children.get(i).getName());
        } return children_names; 
    }

    public int getDigitFromDIGIT (Production parent_production, int index) {
        int digit = Integer.parseInt(((Terminal) parent_production.getASTChild(index)).getTokenAttribute());
        return digit; 
    }

    public String getStringFromCHARACTER (Production parent_production, int index) {
        String str = ((Terminal) parent_production.getASTChild(index)).getTokenAttribute();
        return str; 
    }

    ///
    
    public String stringOfCharacters(int amount, String character) { String s = ""; for (int j = 0; j <= amount-1; j++) { s = s + character; } return s; }
    
    public void pout(String s) {System.out.println(s);}

    /*********************************************** ******************** ***********************************************/
    public void performCodeGeneration (ArrayList<Production> AST, SymbolTable st, Toolkit tk ) throws CodeGenerationException {
            System.out.println("\n\nCODE GENERATION:");
            symbol_table = st; 
            toolkit = tk;    
            execution_environment = new ExecutionEnvironment();  // not sure if necessary
            
            
            //NonTerminal ast_starting_block = new NonTerminal("Block");
            //AST.add(ast_starting_block);
            traverseIntermediateRepresentation(AST.get(0), 1); // start on block
            execution_environment.updateStackPointerEOF();

            
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

            String readableOpcodes = execution_environment.translateOpcodesToEnglish(new ArrayList<>(Arrays.asList(execution_environment.getCodeSequence())));
            System.out.println("\n┌--------------------------------------------------------------------------------------------------------------------┐");
            System.out.println("|-------------------------------------------READABLE INSTRUCTION OUTPUT----------------------------------------------|");
            System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
            System.out.println(readableOpcodes);
            
            
            
            System.out.println("\n┌--------------------------------------------------------------------------------------------------------------------┐");
            System.out.println("|-------------------------------------------READABLE INSTRUCTION OUTPUT----------------------------------------------|");
            System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
            ArrayList<String> proper_ops = parseHexadecimalString("A9 F5 8D 44 00 A9 F5 8D 45 00 A9 F5 8D 46 00 A9 F5 8D 44 00 A9 F5 8D 45 00 AE 44 00 A9 F5 8D 47 00 EC 47 00 D0 0C A9 F0 8D 48 00 A2 FF EC F5 00 D0 05 A9 F5 8D 48 00 AD 48 00 8D 46 00 AC 46 00 A2 02 FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 74 72 75 65 00 66 61 6C 73 65 00 6E 75 6C 6C 00");
            String proper_readable_ops = execution_environment.translateOpcodesToEnglishUnrestricted(proper_ops);
            //System.out.println(proper_readable_ops);
            
            
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