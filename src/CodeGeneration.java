import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CodeGeneration {
    public Toolkit toolkit;
    public ExecutionEnvironment execution_environment;
    public SymbolTable symbol_table;
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
                new_op_codes = processIsEqual(new_op_codes, nt, lhs_location);
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
        else if (identifier_type.equals("boolean")) default_value = "FA"; // false
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
            processIsEqual(op_codes, AssignmentStatement, lhs_id_location);
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
        op_codes.add(value); op_codes.add("00"); 
        return op_codes; 
    }

    public ArrayList<String> gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX (ArrayList<String> op_codes, String address) throws CodeGenerationException {
        op_codes.add("EC"); 
        op_codes.add(address); op_codes.add("00"); 
        return op_codes; 
    }

    public ArrayList<String> gen_branchIfNotEqual(ArrayList<String> op_codes, String lhs_location) throws CodeGenerationException {
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

    
    public ArrayList<String> processIsEqual (ArrayList<String> op_codes, NonTerminal IsEqual, String lhs_location) throws CodeGenerationException {
        Production lhs = IsEqual.getASTChild(0);
        Production rhs = IsEqual.getASTChild(1);

        // TODO: Is Boolean reference to Heap or is it just set 0 and 1 -- think just set 0 and 1, fuck
        // Need to finish NonTerminals, but thinking load X register with LHS, save in temp value, then load X register with RHS, and compare X register with temp value

        // I think only NonTerminal it could be is IsEqual or IsNotEqual 
        if (lhs.getProdKind().equals("NonTerminal")) {
            nonterminalCallableRouter(op_codes, ((NonTerminal) lhs), lhs_location); 
        }
        
        if (rhs.getProdKind().equals("NonTerminal")) {
            nonterminalCallableRouter(op_codes, ((NonTerminal) rhs), lhs_location); 
        }

        if (lhs.getProdKind().equals("Terminal")) {
            gen_loadXRegister(op_codes, IsEqual, ( (Terminal) lhs) , 0); 
            //if ( !(((Terminal) lhs).getName().equals("DIGIT"))) {
             //  
            //} else {
                // need to load value into register
            //}
        }

        if (rhs.getProdKind().equals("Terminal")) {
            String terminal_location = getLocationOfTerminal( IsEqual, ((Terminal) rhs), 1);
            if ( !(((Terminal) lhs).getName().equals("DIGIT"))) {
                gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(op_codes, terminal_location);
                gen_branchIfNotEqual(op_codes, lhs_location);
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
            //execution_environment.
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