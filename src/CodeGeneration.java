import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CodeGeneration {
    public Toolkit toolkit;
    public ExecutionEnvironment execution_environment;
    public SymbolTable symbol_table;
    public String addition_temp_addr;

    public boolean within_nested_addition = false;

    String [] deep_copy_code_sequence = new String[256];
    int stored_code_pointer = 0; 
    int stored_stack_pointer = 0; 
    int stored_heap_pointer = 0; 
    boolean within_jump = false;
    
    String addr_for_while_comparison = ""; 
    ArrayList<String> to_insert_after_block = new ArrayList<String>();
    boolean should_insert_after_block = false; 
    int code_pointer_before_block = 0; 
    int code_pointer_after_block = 0; 

    int current_max_jump = 0; 
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
                System.out.println("NonTerminal Name: " + nt.getName() + ", Kids: " + childrenAsString(nt));
                nonterminalRouter(nt);
                
            }
            
            System.out.println("Traversing again...");
            
            if (c.getProdKind().equals("NonTerminal")) { 
                if (((NonTerminal) c).getName().equals("BLOCK")) {
                    code_pointer_before_block = execution_environment.getCodePointer();
                }
            }
            traverseIntermediateRepresentation(c, index + 1);

            if (c.getProdKind().equals("NonTerminal")) {
                if ( ((NonTerminal) c).getName().equals("BLOCK")) {
                    pout("Just Exitted Block");
                    if ( should_insert_after_block ) {
                        code_pointer_after_block = execution_environment.getCodePointer();  pout("Pointer Before Block: " + code_pointer_before_block + ",  Pointer After Block: " + code_pointer_after_block);
                        execution_environment.insertImmediately(to_insert_after_block, "Code"); // Insert ArrayList into code_sequence at location of "Code"
                        should_insert_after_block = false;  // Flip it so it can be used again without issue
                        to_insert_after_block.clear();     // Empty arraylist
                    }
                }
            }
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

            case "If":
                processIf(nt);
                break;

            case "While":
                processWhile(nt);
                pout("\n\n\n\n\nDONE WITH WHILE\n\n\n\n");
                break;
            // Keep under those which use IsEqual
            case "IsEqual":
                // do something
                //processIsEqual(nt); 
                break;
            
            case "IsNotEqual":
                // do something
                //processIsEqual(nt); 
                break;

        /*  case "Block":
        *       Maybe, actually... no.
             */
            
            //case "ADDITION": 

                
        }
    }



    // Store current contents of code_sequence in new array,  store current pointers, clear code_sequence, process NT, new code_pointer after + 1 is jump distance, restore all values, return jump distance
    public int nonterminalRouterWithJumpWatcher (NonTerminal nt ) throws CodeGenerationException{
        
        pout("Performing Stores and Resets");
        stored_code_pointer = execution_environment.getCodePointer(); // Store so that it can be restored later 
        stored_stack_pointer = execution_environment.getStackPointerSafe(); 
        stored_heap_pointer = execution_environment.getHeapPointer(); 

        String[] code_sequence = execution_environment.getCodeSequence();
        deepCopyStringArray(code_sequence, deep_copy_code_sequence); // Store contents of current code_sequence into new array
        
        execution_environment.clearCodeSequence();
        execution_environment.setCodePointer(0);
        execution_environment.setStackPointer(1);
        execution_environment.setHeapPointer(240);

            
      
        // Store and clear code sequence so that overflows dont happen
        

        int jump_distance = 0; 
    
        switch (nt.getName()) {
            
            
            case "VarDeclStatement": 
                processVariableDeclaration(nt);
                //String[] op_codes_array = op_codes.toArray(new String[0]); // convert to standard array
                //execution_environment.insert(op_codes_array, "int", "Code");
                jump_distance = execution_environment.getCodePointer();
                
                break; 
               
            case "AssignmentStatement": 
                processAssignmentStatement(nt);
                jump_distance = execution_environment.getCodePointer();
                //performRestoreOfPreviousValuesToExecutionEnvironment (deep_copy_code_sequence, stored_code_pointer, stored_stack_pointer, stored_heap_pointer);
                
                break; 

            case "PrintStatement": 
            pout("Current Code Pointer: " + execution_environment.getCodePointer());
                jump_distance = execution_environment.getCodePointer();
                //performRestoreOfPreviousValuesToExecutionEnvironment (deep_copy_code_sequence, stored_code_pointer, stored_stack_pointer, stored_heap_pointer);
                break;  
                
            case "If":
                processIf(nt);
                jump_distance = execution_environment.getCodePointer();
              
                //performRestoreOfPreviousValuesToExecutionEnvironment (deep_copy_code_sequence, stored_code_pointer, stored_stack_pointer, stored_heap_pointer);
                break; 
                
            case "While":
                processWhile(nt);
                jump_distance = execution_environment.getCodePointer();
                // Keep under those which use IsEqual
            case "IsEqual":
                processIsEqual(nt);
                jump_distance = execution_environment.getCodePointer();
                
                //performRestoreOfPreviousValuesToExecutionEnvironment (deep_copy_code_sequence, stored_code_pointer, stored_stack_pointer, stored_heap_pointer);
                break;  
                
            case "IsNotEqual":
                processIsNotEqual(nt);
                jump_distance = execution_environment.getCodePointer();
                
                //performRestoreOfPreviousValuesToExecutionEnvironment (deep_copy_code_sequence, stored_code_pointer, stored_stack_pointer, stored_heap_pointer);
                break;  
                
            case "BLOCK": 
                //.
                processBlock(nt);
                jump_distance = execution_environment.getCodePointer();
                pout("within switch jump distance :" + jump_distance);
                //performRestoreOfPreviousValuesToExecutionEnvironment (deep_copy_code_sequence, stored_code_pointer, stored_stack_pointer, stored_heap_pointer);
                break; 


        /*  case "Block":
        *       Maybe, actually... no.
             */
            
            //case "ADDITION": 

                
        }
        
        
        System.out.println("Jump Distance Before returning: " + jump_distance); 
        return execution_environment.getCodePointer(); 
    }


    /*********************************************** NonTerminal Handlers ***********************************************/


    public void processBlock (NonTerminal Block) throws CodeGenerationException {
        Production block_production = (Production) Block; 
        traverseIntermediateRepresentation(block_production, 0);
        pout("Could add now...");
        /*
        ArrayList<Production> block_children = Block.getASTChildren(); 
        for (int i = 0; i <= block_children.size(); i++) {
            Production block_child = Block.getASTChild(i);
            String block_child_type = block_child.getProdKind();
            if (block_child_type.equals("NonTerminal")) {

            }
        }
        */
    }

    public void processVariableDeclaration (NonTerminal VarDeclStatement) throws CodeGenerationException{
        
        Terminal type = (Terminal) VarDeclStatement.getASTChild(0);
        Terminal identifier = (Terminal) VarDeclStatement.getASTChild(1);
        String identifier_type = getIdentifierType(  VarDeclStatement, identifier);
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
        ArrayList<String> assignment_children = getProductionChildrenArrayList(AssignmentStatement);
        Terminal identifier_terminal = (Terminal) AssignmentStatement.getASTChild(0);
        ArrayList<Production> identifier_nt_astchildren = AssignmentStatement.getASTChildren();
        
        //String identifiers_temporary_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 0); // LHS, where value will be assigned
        //String identifiers_temporary_location = execution_environment.retrieveTemporaryLocation(getIdentifierScopeName(AssignmentStatement, identifier_terminal), identifier_terminal.getTokenAttribute()); //execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 0); // LHS, where value will be assigned
        String identifiers_scope_location = getLocation(AssignmentStatement, identifier_terminal);
        String identifiers_temporary_location = execution_environment.retrieveTemporaryLocation(identifier_terminal.getTokenAttribute(), identifiers_scope_location);
        pout("Assignment Location: " + identifiers_temporary_location);
        // Output
        //System.out.println("\n\nAssignment for " + identifier_terminal.getName() + ", " + identifier_terminal.getTokenAttribute());  System.out.println("Assignment Statement AST-Children: ");
        for (int i = 0; i <= identifier_nt_astchildren.size() - 1; i++) { System.out.println(AssignmentStatement.getASTChild(i).getName() + " : " + AssignmentStatement.getASTChild(i).getProdKind()); }
         
        // IsEqual Assignment
        if (assignment_children.contains("IsEqual")) {
            NonTerminal IsEqual = (NonTerminal) AssignmentStatement.getASTChild(1);
            processIsEqualAssignment(IsEqual, identifiers_temporary_location);
        }
        
        // Where single ID assignment goes
        if (assignment_children.contains("DIGIT") && assignment_children.size() == 2) {
            Terminal DIGIT_terminal = (Terminal) AssignmentStatement.getASTChild(1);
            System.out.println("Temp Address"  + identifiers_temporary_location);
            processDIGITAssignment(DIGIT_terminal, identifiers_temporary_location); // Store digit in temp location
        }
        
        // Got screwed up when it found rhs character but saw lhs identifier
        else if (assignment_children.get(1).equals("IDENTIFIER")) {
            
            String lhs_id = ((Terminal) AssignmentStatement.getASTChild(0)).getTokenAttribute();
            String rhs_id = ((Terminal) AssignmentStatement.getASTChild(1)).getTokenAttribute();
            //pout("ID LHS: " + lhs_id); pout("ID RHS: " + rhs_id);
            
            String lhs_id_temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 0); // left hand side
            String rhs_id_temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(AssignmentStatement, 1); // right side assignment id
            
            //pout("LHS Location: " + lhs_id_temp_location); pout("RHS Location: " + rhs_id_temp_location);
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
            boolean exists_in_strdecls = execution_environment.stringExistsWithStringDeclarations(terminals_string_value); //System.out.println("\n\nExists within string decls: " + exists_in_strdecls);

            if (exists_in_strdecls) {
                heap_pointer = execution_environment.getAddressFromStringDeclarations(terminals_string_value); // If exists in string_decls already, get address
                //System.out.println("Heap Pointer: " + heap_pointer);
            } else {
                heap_pointer = execution_environment.getHeapPointer() - hex_arraylist.length - 1; // otherwise get the current heap pointer, making sure to modify it for what it will be 
                //System.out.println("Heap Pointer: " + heap_pointer);
                execution_environment.insertIntoStringDeclarations(terminals_string_value, heap_pointer); // Store in string_declarations hashmap 
                ArrayList<String> hex_arraylist_actually = new ArrayList<>(Arrays.asList(hex_arraylist));
                characterTerminalHandler(CHARACTER_terminal);
                //execution_environment.insertImmediately(hex_arraylist_actually, "Heap"); // insert it into code_sequence without waiting, so we can specify Heap insertion
            }
            
            // Need to update heap pointer after being done, need to full on return after being in this elif block, need to store pointer for string in string_declarations map 
            String heap_pointer_hex = String.format("%02X", heap_pointer); System.out.println("Heap Pointer Hex: " + heap_pointer_hex);

            //characterTerminalHandler(CHARACTER_terminal);//(CHARACTER_terminal, heap_pointer_hex, identifiers_temporary_location);  
        }

        else if (assignment_children.contains("ADDITION")) {            
            NonTerminal assignment_nonterminal = (NonTerminal) AssignmentStatement.getASTChild(1); 
            String new_addition_temp_addr = execution_environment.performStaticTableInsertion("addition", AssignmentStatement.getScopeName());  // Maybe... // Create location to store during addition
            processADDITIONAssignment(assignment_nonterminal, new_addition_temp_addr, identifiers_temporary_location, false); // Process addition
            //System.out.println("Assignment - Stored Value for Identifier: " + identifier_terminal.getName() + ", at location: " + id_temp_location + "\n");
            //op_codes.addAll(nonterminalRouter((NonTerminal) child));
        }
 
    }

    public void processWhile (NonTerminal While) throws CodeGenerationException {
         
        System.out.println("processWhile");
        Production lhs = While.getASTChild(0);
        Production rhs = While.getASTChild(1);
        int starting_pointer = -1;
        if (lhs.getProdKind().equals("NonTerminal")) {
            NonTerminal lhs_nonterminal = (NonTerminal) lhs; String lhs_nonterminal_name = lhs_nonterminal.getName(); 
            
            if (!within_jump) starting_pointer = execution_environment.getCodePointer(); 
        

            if ( (lhs_nonterminal_name.equals("IsEqual")) || (lhs_nonterminal_name.equals("IsNotEqual")) )  {
                

                pout("While - IsEqual/IsNotEqual");
                if (lhs_nonterminal_name.equals("IsEqual"))  processIsEqual(lhs_nonterminal);  
                else if (lhs_nonterminal_name.equals("IsNotEqual")) processIsNotEqual(lhs_nonterminal);
                              

                if (!within_jump) {
                    
                    within_jump = true; 
                    NonTerminal block = (NonTerminal) rhs; // Block 
                    int jump_distance = nonterminalRouterWithJumpWatcher(block) + 7; // get jump distance
                    String jump_distance_hex = String.format("%02X", jump_distance);
                    performRestoreOfPreviousValuesToExecutionEnvironment (deep_copy_code_sequence, stored_code_pointer, stored_stack_pointer, stored_heap_pointer);
                    pout("Setting Jump Distance: " + jump_distance + ", and in hex: " + jump_distance_hex); 
                    gen_branchNBytes_D0_BNE(jump_distance_hex); // Branch 12 bytes If NOT Equal 
                    within_jump = false; 
                    System.out.println("End of While Loop, starting pos: " + starting_pointer);
                    System.out.println("End of While Loop, code pointer: " + execution_environment.getCodePointer());
                    int jumpback_distance = starting_pointer - execution_environment.getCodePointer(); 
                    String jumpback_distance_hex = String.format("%02X", jumpback_distance & 0xFF);
                    
                    to_insert_after_block.add("A9"); to_insert_after_block.add(execution_environment.getFalsePointer());
                    to_insert_after_block.add("EC"); to_insert_after_block.add(addr_for_while_comparison); to_insert_after_block.add("00");
                    to_insert_after_block.add("D0"); to_insert_after_block.add(jumpback_distance_hex);
                    should_insert_after_block = true; // Will be triggered by main loop when recursion exits the NonTerminal for BLOCK
                    System.out.println("End of While Loop, jumpback_distance: " + jumpback_distance);
                    //gen_loadAccumulatorWithConstant_A9_LDA(execution_environment.getFalsePointer());
                    //gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(addr_for_while_comparison);
                    //gen_branchNBytes_D0_BNE(jumpback_distance_hex);
                    
                } // Recursion will continue normally from here
            } 

        /*  
        } else if (lhs.getProdKind().equals("Terminal")) {
            Terminal lhs_terminal = (Terminal) lhs; String lhs_terminal_name = lhs_terminal.getName();
            if (lhs_terminal_name.equals("KEYWORD_TRUE") || lhs_terminal_name.equals("KEYWORD_FALSE")) {
                String terminal_addressing_component = getTerminalAddressingComponent(While, if_statement_lhs_terminal, 0); 
                String temporary_address = execution_environment.performStaticTableInsertion("while" + execution_environment.getTemporaryValueCounter(), While.getScopeName()); // Create first temp addr

                gen_loadAccumulatorWithConstant_A9_LDA(terminal_addressing_component); 
                gen_storeAccumulatorIntoMemory_8D_STA(temporary_address);
                gen_loadXRegisterWithValue_A2_LDX(execution_environment.getTruePointer());
                gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(temporary_address);
                
                if (!within_jump) {
                    within_jump = true; 
                    NonTerminal block = (NonTerminal) while_statement_rhs; // Block 
                    int jump_distance = nonterminalRouterWithJumpWatcher(block); // get jump distance
                    String jump_distance_hex = String.format("%02X", jump_distance);
                    performRestoreOfPreviousValuesToExecutionEnvironment (deep_copy_code_sequence, stored_code_pointer, stored_stack_pointer, stored_heap_pointer);
                    pout("Setting Jump Distance: " + jump_distance + ", and in hex: " + jump_distance_hex); 
                    gen_branchNBytes_D0_BNE(jump_distance_hex); // Branch 12 bytes If NOT Equal 
                    within_jump = false; 
                }
            }
        } */

            
          
            
        }
    }

    public void processIf (NonTerminal If) throws CodeGenerationException {
        /** 
         * Something 
         * Block
         * 
         * Something: IsEqual, IsNotEqual, KEYWORD_TRUE, KEYWORD_FALSE
         * 
         * IsEqual -> Process IsEqual? 
         * IsNotEqual -> ProcessIsNotEqual? 
         * KEYWORD_TRUE/KEYWORD_FALSE -> hmmm
         * 
         * Maybe create nonTerminalOrTerminalRouter...
         *      here we "process if", here we know it exists
         *      - store current code pointer locally
         *      - call NTorT router
         *          - can call NT router
         *          - maybe 
         *              instead of NTorT, we have NTorSingleBoolExpr,
         *              need similar 
         * 
         *              or what if we say "fkit" and if its an If, and we see "false", just skip everything until after the block lol... dead code anyways, never will be touched
         *                      if true, just process it normally
         *              
         *              however, this wouldn't work for "while true", but then again, i think we can think of these things entirely separately... 
         *                      if true in that case would always have a recursive branch back to the start of the while.. dead code there on after that
         
         * 
         * */

         
        Production if_statement_lhs = If.getASTChild(0);
        Production if_statement_rhs = If.getASTChild(1);

        /* 
        **  RHS is nonterminal always
            Current if conditions handled:
            - LHS is NonTerminal IsEqual
        */
        if (if_statement_lhs.getProdKind().equals("NonTerminal")) {
            NonTerminal if_statement_lhs_nonterminal = (NonTerminal) if_statement_lhs; String lhs_nonterminal_name = if_statement_lhs_nonterminal.getName(); 

            if (lhs_nonterminal_name.equals("IsEqual")) {
                /**
                NonTerminal block = (NonTerminal) if_statement_rhs; // Block 
                int jump_distance = nonterminalRouterWithJumpWatcher(block); // get jump distance
                String jump_distance_hex = String.format("%02X", jump_distance);
                pout("Jump Distance Calculated: " + jump_distance + ", hex: " + jump_distance_hex);
                processIsEqual(if_statement_lhs_nonterminal, jump_distance_hex);                
                **/
                processIsEqual(if_statement_lhs_nonterminal);                

                if (!within_jump) {
                    within_jump = true; 
                    NonTerminal block = (NonTerminal) if_statement_rhs; // Block 
                    int jump_distance = nonterminalRouterWithJumpWatcher(block); // get jump distance
                    String jump_distance_hex = String.format("%02X", jump_distance);
                    performRestoreOfPreviousValuesToExecutionEnvironment (deep_copy_code_sequence, stored_code_pointer, stored_stack_pointer, stored_heap_pointer);
                    pout("Setting Jump Distance: " + jump_distance + ", and in hex: " + jump_distance_hex); 
                    gen_branchNBytes_D0_BNE(jump_distance_hex); // Branch 12 bytes If NOT Equal 
                    within_jump = false; 
                }
                
                // Recursion will continue normally from here
            } else if (lhs_nonterminal_name.equals("IsNotEqual")) {
                /**
                NonTerminal block = (NonTerminal) if_statement_rhs; // Block 
                int jump_distance = nonterminalRouterWithJumpWatcher(block); // get jump distance
                String jump_distance_hex = String.format("%02X", jump_distance);
                pout("Jump Distance Calculated: " + jump_distance + ", hex: " + jump_distance_hex);
                processIsEqual(if_statement_lhs_nonterminal, jump_distance_hex);                
                **/
                processIsNotEqual(if_statement_lhs_nonterminal);                

                if (!within_jump) {
                    within_jump = true; 
                    NonTerminal block = (NonTerminal) if_statement_rhs; // Block 
                    int jump_distance = nonterminalRouterWithJumpWatcher(block); // get jump distance
                    String jump_distance_hex = String.format("%02X", jump_distance);
                    performRestoreOfPreviousValuesToExecutionEnvironment (deep_copy_code_sequence, stored_code_pointer, stored_stack_pointer, stored_heap_pointer);
                    pout("Setting Jump Distance: " + jump_distance + ", and in hex: " + jump_distance_hex); 
                    gen_branchNBytes_D0_BNE(jump_distance_hex); // Branch 12 bytes If NOT Equal 
                    within_jump = false; 
                }
                
                // Recursion will continue normally from here
            }

        
        } else {
            Terminal if_statement_lhs_terminal = (Terminal) if_statement_lhs; String lhs_terminal_name = if_statement_lhs_terminal.getName();
            if (lhs_terminal_name.equals("KEYWORD_TRUE") || lhs_terminal_name.equals("KEYWORD_FALSE")) {
                String terminal_addressing_component = getTerminalAddressingComponent(If, if_statement_lhs_terminal, 0); 
                String temporary_address = execution_environment.performStaticTableInsertion("if" + execution_environment.getTemporaryValueCounter(), If.getScopeName()); // Create first temp addr
                
                gen_loadAccumulatorWithConstant_A9_LDA(terminal_addressing_component); 
                gen_storeAccumulatorIntoMemory_8D_STA(temporary_address);
                gen_loadXRegisterWithValue_A2_LDX(execution_environment.getTruePointer());
                gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(temporary_address);
                
                if (!within_jump) {
                    within_jump = true; 
                    NonTerminal block = (NonTerminal) if_statement_rhs; // Block 
                    int jump_distance = nonterminalRouterWithJumpWatcher(block); // get jump distance
                    String jump_distance_hex = String.format("%02X", jump_distance);
                    performRestoreOfPreviousValuesToExecutionEnvironment (deep_copy_code_sequence, stored_code_pointer, stored_stack_pointer, stored_heap_pointer);
                    pout("Setting Jump Distance: " + jump_distance + ", and in hex: " + jump_distance_hex); 
                    gen_branchNBytes_D0_BNE(jump_distance_hex); // Branch 12 bytes If NOT Equal 
                    within_jump = false; 
                }
            }

        }

    }
    
    public void processPrintStatement (NonTerminal PrintStatement) throws CodeGenerationException {
        pout("processPrintStatement");
        /*
        * Currently only prints IDENTIFIERS
        * If String is argument, e.g., print("string") --> It would fail
        */
        ArrayList<String> op_codes = new ArrayList<>(); 
        //for (int i = 0; i <= PrintStatement.getASTChildren().size() - 1; i++) { System.out.println("PrintStatement Child: " + PrintStatement.getASTChild(i).getName() + " : " + PrintStatement.getASTChild(i).getProdKind()); }
        if (PrintStatement.getASTChildren().size() > 1) throw new CodeGenerationException("CodeGeneration, processPrintStatment", "I thought print statements could only have one child");
        
        Terminal print_child = (Terminal) PrintStatement.getASTChild(0);
        //pout("Print Child: " + print_child.getTokenAttribute()); 
        //pout("Print Child Type: " + print_child.getName()); 

        // Can't there only be one child...? Why did I do this...
        
        //PrintStatement.getASTChild(0).getName().equals("IDENTIFIER")



        if (print_child.getName().equals("IDENTIFIER")) {
            //Terminal identifier = (Terminal) PrintStatement.getASTChild(0);
            
            /**
            String type = getIdentifierType(PrintStatement, print_child); //System.out.println("PrintStatement - Terminal, type: " + type);
            String temp_location = execution_environment.retrieveTempLocationFromChildOfNonTerminal(PrintStatement, 0); // Location in static table for the Identifier the print statement is trying to print    
            gen_loadYRegisterFromMemory_AC_LDY(temp_location);
            gen_finishPrintStatment(type); **/
            Terminal identifier_terminal = (Terminal) PrintStatement.getASTChild(0); // Cast to terminal
            String type = getIdentifierType(PrintStatement, identifier_terminal); // So proper ops can be chosen 
            String identifier_scope_name = getIdentifierScopeName(PrintStatement, identifier_terminal); // Get scope location of identifier
            String identifier_variable = identifier_terminal.getTokenAttribute(); // Get variable/value/id assigned to identifier
            String static_location = execution_environment.retrieveTemporaryLocation(identifier_variable, identifier_scope_name); // Get location from static table
            gen_loadYRegisterFromMemory_AC_LDY(static_location);
            gen_finishPrintStatment(type); 

        } else if (print_child.getName().equals("CHARACTER")) {
            // unchecked if imporvement
            Terminal character_terminal = (Terminal) PrintStatement.getASTChild(0);  
            String hex_location = characterTerminalHandler(character_terminal);             pout("hex_location: " + hex_location);
            gen_loadYRegisterFromConstant_AO_LDY(hex_location);
            characterTerminalHandler(character_terminal);  
            gen_finishPrintStatment("string"); 
        } else if (print_child.getName().equals("DIGIT")) {
            Terminal digit_terminal = (Terminal) PrintStatement.getASTChild(0);
            int digit_value = Integer.parseInt(digit_terminal.getTokenAttribute());
            gen_loadYRegisterFromConstant_AO_LDY("0"+digit_value);
            gen_finishPrintStatment("int"); 
        }

    }

    
    // IsEqual Assignment
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

            if (constants.contains(lhs_terminal_name)) gen_loadXRegisterWithValue_A2_LDX(lhs_terminal_addressing_component); // Load X Register with LHS 
            else gen_loadXRegisterFromAddress_AE_LDX(lhs_terminal_addressing_component); // LOAD LHS
        
            if (constants.contains(rhs_terminal_name)) gen_loadAccumulatorWithConstant_A9_LDA(rhs_terminal_addressing_component); // Load RHS constant into Accumulator
            else gen_loadAccumulatorFromMemory_AD_LDA(rhs_terminal_addressing_component);
            
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
            gen_loadAccumulatorWithConstant_A9_LDA(execution_environment.getFalsePointer()); // Load FALSE constant into Accumulator
            gen_storeAccumulatorIntoMemory_8D_STA(temp_addr_2); 

            gen_loadAccumulatorFromMemory_AD_LDA(temp_addr_2);// Temp Addr 2, at this point, contains answer to whether equal or not
            //gen_loadAccumulatorWithConstant_A9_LDA(temp_addr_2); 
            gen_storeAccumulatorIntoMemory_8D_STA(assignment_location); // Store answer in assignment location, the location of the variable in which the result of this comparison is being stored
        }
    }


    public void processIsNotEqualAssignment ( NonTerminal IsNotEqual, String assignment_location ) throws CodeGenerationException {
        Production lhs = IsNotEqual.getASTChild(0);
        Production rhs = IsNotEqual.getASTChild(1);
        if (lhs.getProdKind().equals("Terminal") && rhs.getProdKind().equals("Terminal")) { 
            String temp_addr_1 = execution_environment.performStaticTableInsertion("ta1" + execution_environment.getTemporaryValueCounter(), IsNotEqual.getScopeName()); // Create first temp addr
            String temp_addr_2 = execution_environment.performStaticTableInsertion("ta2" + execution_environment.getTemporaryValueCounter(), IsNotEqual.getScopeName()); // Create first temp addr
            Terminal lhs_terminal = (Terminal) lhs; String lhs_terminal_name = lhs_terminal.getName();
            Terminal rhs_terminal = (Terminal) rhs; String rhs_terminal_name = rhs_terminal.getName();
            String lhs_terminal_addressing_component = getTerminalAddressingComponent(IsNotEqual, lhs_terminal, 0);
            String rhs_terminal_addressing_component = getTerminalAddressingComponent(IsNotEqual, rhs_terminal, 1);

            if (constants.contains(lhs_terminal_name)) gen_loadXRegisterWithValue_A2_LDX(lhs_terminal_addressing_component); // Load X Register with LHS 
            else gen_loadXRegisterFromAddress_AE_LDX(lhs_terminal_addressing_component); // LOAD LHS

            if (constants.contains(rhs_terminal_name)) gen_loadAccumulatorWithConstant_A9_LDA(rhs_terminal_addressing_component); // Load RHS constant into Accumulator
            else gen_loadAccumulatorFromMemory_AD_LDA(rhs_terminal_addressing_component);
            
            // IF CONSTANT, USE A2, e.g., DIGIT, BOOLEANS 
            //gen_loadXRegisterWithValue_A2_LDX(lhs_terminal_addressing_component); // Load X Register with LHS 
            //gen_loadXRegisterFromAddress_AE_LDX(lhs_terminal_addressing_component); // LOAD LHS
            //gen_loadAccumulatorWithConstant_A9_LDA(rhs_terminal_addressing_component); // Load RHS constant into Accumulator
             
            gen_storeAccumulatorIntoMemory_8D_STA(temp_addr_1); // Store into NEW Temp addr, the false pointer in Accumulator 
            gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(temp_addr_1); // Compare Temp Addr 1 with what is in X Register
            gen_branchNBytes_D0_BNE("0C"); // Branch 12 bytes If NOT Equal 
                gen_loadAccumulatorWithConstant_A9_LDA(execution_environment.getFalsePointer()); // Load TRUE constant into Accumulator
                gen_storeAccumulatorIntoMemory_8D_STA(temp_addr_2); // Store in NEW TEMP 2
                    gen_loadXRegisterWithValue_A2_LDX("FF"); // 255 which is 00 in code_sequence
                    gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(execution_environment.getFalsePointer());
                    gen_branchNBytes_D0_BNE("05"); // BRANCH FORWARD 5 Bytes -- which IT WILL DO because it is guaranteed false --> SKIPS "IF FALSE"
            gen_loadAccumulatorWithConstant_A9_LDA(execution_environment.getFalsePointer()); // Load FALSE constant into Accumulator
            gen_storeAccumulatorIntoMemory_8D_STA(temp_addr_2); // Store in NEW TEMP 2
            gen_loadAccumulatorFromMemory_AD_LDA(temp_addr_2);// Temp Addr 2, at this point, contains answer to whether equal or not
            gen_storeAccumulatorIntoMemory_8D_STA(assignment_location); // Store answer in assignment location, the location of the variable in which the result of this comparison is being stored
        }
    }

    
    public void processIsEqual( NonTerminal IsEqual) throws CodeGenerationException {
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


            // Load LHS into X Register
            if (constants.contains(lhs_terminal_name)) gen_loadXRegisterWithValue_A2_LDX(lhs_terminal_addressing_component); // Load X Register with LHS 
            else gen_loadXRegisterFromAddress_AE_LDX(lhs_terminal_addressing_component); // LOAD LHS
            
            // Load RHS into Accumulator
            if (constants.contains(rhs_terminal_name)) gen_loadAccumulatorWithConstant_A9_LDA(rhs_terminal_addressing_component); // Load RHS constant into Accumulator
            else gen_loadAccumulatorFromMemory_AD_LDA(rhs_terminal_addressing_component);


            gen_storeAccumulatorIntoMemory_8D_STA(temp_addr_1); // Store into NEW Temp addr, the false pointer in Accumulator 
            gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(temp_addr_1); // Compare Temp Addr 1 with what is in X Register  // THE COMPARISON=


            gen_branchNBytes_D0_BNE("0C"); // Branch 12 bytes If NOT Equal 

                // IF EQUAL (12 bytes)
                gen_loadAccumulatorWithConstant_A9_LDA(execution_environment.getTruePointer()); // Load TRUE constant into Accumulator
                gen_storeAccumulatorIntoMemory_8D_STA(temp_addr_2); // Store in NEW TEMP 2
                addr_for_while_comparison = temp_addr_2; 
                    // Force Flip
                    gen_loadXRegisterWithValue_A2_LDX("FF"); // 255 which is 00 in code_sequence
                    gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(execution_environment.getFalsePointer()); // Load FALSE constant as address (not sure if the constant as address is what guarantees flip or the if it does reference false in heap, nevertheless it is false)
                    gen_branchNBytes_D0_BNE("05"); // BRANCH FORWARD 5 Bytes -- which IT WILL DO because it is guaranteed false --> SKIPS "IF FALSE"

            //IF FALSE (Will be skipped if TRUE due to force flip ) (5 bytes)
            gen_loadAccumulatorWithConstant_A9_LDA(execution_environment.getFalsePointer()); // Load FALSE constant into Accumulator
            gen_storeAccumulatorIntoMemory_8D_STA(temp_addr_2); // Store in NEW TEMP 2

            gen_loadXRegisterWithValue_A2_LDX(execution_environment.getTruePointer());
            gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(temp_addr_2);

            //gen_branchNBytes_D0_BNE(jump_distance_hex); // Branch 12 bytes If NOT Equal 

        }
    

    }

    public void processIsNotEqual( NonTerminal IsNotEqual) throws CodeGenerationException {
        Production lhs = IsNotEqual.getASTChild(0);
        Production rhs = IsNotEqual.getASTChild(1);

        if (lhs.getProdKind().equals("Terminal") && rhs.getProdKind().equals("Terminal")) {     
            String temp_addr_1 = execution_environment.performStaticTableInsertion("ta1" + execution_environment.getTemporaryValueCounter(), IsNotEqual.getScopeName()); // Create first temp addr
            String temp_addr_2 = execution_environment.performStaticTableInsertion("ta2" + execution_environment.getTemporaryValueCounter(), IsNotEqual.getScopeName()); // Create first temp addr
            Terminal lhs_terminal = (Terminal) lhs; String lhs_terminal_name = lhs_terminal.getName();
            Terminal rhs_terminal = (Terminal) rhs; String rhs_terminal_name = rhs_terminal.getName();
            String lhs_terminal_addressing_component = getTerminalAddressingComponent(IsNotEqual, lhs_terminal, 0);
            String rhs_terminal_addressing_component = getTerminalAddressingComponent(IsNotEqual, rhs_terminal, 1);
            // Load LHS into X Register
            if (constants.contains(lhs_terminal_name)) gen_loadXRegisterWithValue_A2_LDX(lhs_terminal_addressing_component); // Load X Register with LHS 
            else gen_loadXRegisterFromAddress_AE_LDX(lhs_terminal_addressing_component); // LOAD LHS
            // Load RHS into Accumulator
            if (constants.contains(rhs_terminal_name)) gen_loadAccumulatorWithConstant_A9_LDA(rhs_terminal_addressing_component); // Load RHS constant into Accumulator
            else gen_loadAccumulatorFromMemory_AD_LDA(rhs_terminal_addressing_component);

            gen_storeAccumulatorIntoMemory_8D_STA(temp_addr_1); // Store into NEW Temp addr, the false pointer in Accumulator 
            gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(temp_addr_1); // Compare Temp Addr 1 with what is in X Register  // THE COMPARISON=
            gen_branchNBytes_D0_BNE("0C"); // Branch 12 bytes If NOT Equal 
                gen_loadAccumulatorWithConstant_A9_LDA(execution_environment.getFalsePointer()); // ONLY CHANGE IN NOT EQUAL
                gen_storeAccumulatorIntoMemory_8D_STA(temp_addr_2); // Store in NEW TEMP 2
                addr_for_while_comparison = temp_addr_2; 
                    gen_loadXRegisterWithValue_A2_LDX("FF"); // 255 which is 00 in code_sequence
                    gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(execution_environment.getFalsePointer()); // Load FALSE constant as address (not sure if the constant as address is what guarantees flip or the if it does reference false in heap, nevertheless it is false)
                    gen_branchNBytes_D0_BNE("05"); // BRANCH FORWARD 5 Bytes -- which IT WILL DO because it is guaranteed false --> SKIPS "IF FALSE"
            gen_loadAccumulatorWithConstant_A9_LDA(execution_environment.getTruePointer()); // Load FALSE constant into Accumulator
            gen_storeAccumulatorIntoMemory_8D_STA(temp_addr_2); // Store in NEW TEMP 2
            gen_loadXRegisterWithValue_A2_LDX(execution_environment.getTruePointer());
            gen_compareValueAtAddressWithXRegister_EC_ArrayList_CPX(temp_addr_2);
        }
    }

   


    // ADDITION
    public void processADDITIONAssignment (NonTerminal ADDITION, String temp_addition_addr, String assignment_location, boolean within_nested_addition) throws CodeGenerationException {
            
        //for (int i = 0; i <= ADDITION.getASTChildren().size() - 1; i++) { System.out.println("Addition Child: " + ADDITION.getASTChild(i).getName() + " : " + ADDITION.getASTChild(i).getProdKind()); }
    
        
        if (ADDITION.getASTChild(0).getName().equals("DIGIT")) {
            int digit_value = Integer.parseInt(((Terminal) ADDITION.getASTChild(0)).getTokenAttribute()); // System.out.println("Digit Value: " + digit_value);
            
            gen_loadAccumulatorWithConstant_A9_LDA(""+digit_value); // Load accum. with value in Digit

            if (within_nested_addition) gen_addWithCarryToAccum_6D_ADC(temp_addition_addr);  // If within recursive addition, add contents already in temp address into accumulator

            gen_storeAccumulatorIntoMemory_8D_STA(temp_addition_addr); // Store contents of the accumulator into memory, using the address of our temporary location
            
            if (ADDITION.getASTChildren().size() > 1) {
                
                // if of form: 3 + 1
                if (ADDITION.getASTChild(1).getName().equals("DIGIT")) {
                    int second_digit_value = Integer.parseInt(((Terminal) ADDITION.getASTChild(1)).getTokenAttribute()); //System.out.println("Second Digit: " + second_digit_value);
                    
                    gen_loadAccumulatorWithConstant_A9_LDA(""+second_digit_value);  // Load accumulator with second value
                    gen_addWithCarryToAccum_6D_ADC(temp_addition_addr); // Add with carry into to the accum
                    gen_storeAccumulatorIntoMemory_8D_STA(temp_addition_addr); // Store in temp location
                    
                
                 // if of form 3+3 +a
                } else if (ADDITION.getASTChild(1).getName().equals("ADDITION")) {
                    within_nested_addition = true; 
                    processADDITIONAssignment((NonTerminal) ADDITION.getASTChild(1), temp_addition_addr, assignment_location, within_nested_addition);

                // = 3 + a
                } else if (ADDITION.getASTChild(1).getName().equals("IDENTIFIER")) {
                    //String scope = ADDITION.getScopeName();
                    

                    Terminal identifier_terminal = (Terminal) ADDITION.getASTChild(1);
                    String type = getIdentifierType(ADDITION, identifier_terminal); 
                    pout("Addition AST Children Before Messing Around: " + type);
                    String scope_location = getLocation(ADDITION, identifier_terminal);
                    String identifiers_temporary_location = execution_environment.retrieveTemporaryLocation(identifier_terminal.getTokenAttribute(), scope_location);


                    //System.out.println("Second Value is Identifier: " + id);
                    pout("Assignment static_table_temp_location: " + identifiers_temporary_location);
                    gen_loadAccumulatorFromMemory_AD_LDA(identifiers_temporary_location); // Load value for a into accumulator
                    gen_addWithCarryToAccum_6D_ADC(temp_addition_addr); // Add contents of temporary addition address to accumulator  
                    gen_storeAccumulatorIntoMemory_8D_STA(temp_addition_addr); // Store accumulator value in our temporary location for addition
                    
                    //gen_loadAccumulatorFromMemory_AD_LDA(op_codes, temp_addition_addr); // Load accumulator using the address of the value we just stored (redundant)
                    //gen_storeAccumulatorIntoMemory_8D_STA(op_codes, static_table_temp_location); // Store value of accumulator into location of a (wrong)

                }

            } else {
                within_nested_addition = false;
                //System.out.println("Should be done with addition now");
            } 
        }  
        
        within_nested_addition = false;
        pout("Assignment Location: " + assignment_location);
        gen_loadAccumulatorFromMemory_AD_LDA(temp_addition_addr); // Load accumulator with value at address
        gen_storeAccumulatorIntoMemory_8D_STA(assignment_location); // Store in location for id
        //System.out.println("Returning addition Final");
    }



    /*********************************************** Terminal Handlers ***********************************************/

    public void processDIGITAssignment (Terminal DIGIT, String identifiers_temporary_location) throws CodeGenerationException {
        int digit_value = Integer.parseInt(DIGIT.getTokenAttribute());
        gen_loadAccumulatorWithConstant_A9_LDA(""+ digit_value);
        gen_storeAccumulatorIntoMemory_8D_STA(identifiers_temporary_location);
    } 

    public void processCHARACTERAssignment (Terminal CHARACTER, String heap_pointer_hex, String identifiers_temporary_location) throws CodeGenerationException {
        gen_loadAccumulatorWithConstant_A9_LDA(heap_pointer_hex); // Store pointer to location in the heap
        gen_storeAccumulatorIntoMemory_8D_STA(identifiers_temporary_location);
    }
    // Where LHS is identifier

    public void processIDENTIFIERAssignment(String rhs_id_temp_location, String lhs_id_temp_location) throws CodeGenerationException{
        gen_loadAccumulatorFromMemory_AD_LDA(rhs_id_temp_location); // load accumulator with value from identifiers location
        gen_storeAccumulatorIntoMemory_8D_STA(lhs_id_temp_location);
    }

    public void processBOOLEANAssignment (Terminal boolean_terminal, String identifiers_temporary_location) throws CodeGenerationException {
        String boolean_value = boolean_terminal.getTokenAttribute();
        String heap_boolean_location = (boolean_value.equals("KEYWORD_TRUE") ? execution_environment.getTruePointer() : execution_environment.getFalsePointer());
        gen_loadAccumulatorWithConstant_A9_LDA(heap_boolean_location);
        //pout("processBOOLEANAssignment - loaded accumulator");
        gen_storeAccumulatorIntoMemory_8D_STA(identifiers_temporary_location);
        //pout("processBOOLEANAssignment - stored accumulator into memory ");
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

    public void clearArray (String[] strarr) {
        for (int i = 0; i <= strarr.length - 1; i++) {
            strarr[i]  = "00";
        }
    }

    public void performRestoreOfPreviousValuesToExecutionEnvironment (String[] deep_copy_code_sequence, int stored_code_pointer, int stored_stack_pointer, int stored_heap_pointer) {
        

        pout("Performing Restore");
        execution_environment.restoreCodeSequenceValues(deep_copy_code_sequence); // Fill array back with the stored values
        execution_environment.setCodePointer(stored_code_pointer);
        execution_environment.setStackPointer(stored_stack_pointer);
        execution_environment.setHeapPointer(stored_heap_pointer);
        
        pout("Clearing array");
        clearArray(deep_copy_code_sequence);
    }

    public void deepCopyStringArray (String[] main_array, String[] copy_of_array) {
        //pout("main arr len: " + main_array.length); 
        //pout("copy arr len: " + copy_of_array.length); 
        for (int i = 0; i <= main_array.length - 1; i++) {
            copy_of_array[i] = main_array[i]; 
            //pout("i:" + i + "copy i:  " + copy_of_array[i]);
        }
    }

    public SymbolTableScope getScopeOfTerminal (NonTerminal parent, Terminal child) throws CodeGenerationException {
        String parent_scope_name = parent.getScopeName(); 
        String id = child.getTokenAttribute();
        SymbolTableScope residing_scope = symbol_table.getScope(parent_scope_name);
        if (residing_scope.entryExists(id)) {
            return residing_scope; 
        } else {
            //SymbolTableEntry specific_entry = symbol_table.retrieveEntryFromAccessibleScopes(residing_scope, child);
            SymbolTableScope scope_of_terminal = symbol_table.retrieveScopeWithNearestDeclaration(parent, child);
            return scope_of_terminal;
        }
    }

    public String getIdentifierType (NonTerminal parent, Terminal child) throws CodeGenerationException {
        SymbolTableScope scope_of_identifier = getScopeOfTerminal(parent, child);
        pout("Child Name: " + child.getName());
        try {
            SymbolTableEntry entry = symbol_table.retrieveEntryFromAccessibleScopes(scope_of_identifier, child);
            System.out.println( " ( " + child.getTokenAttribute() + " )  Found Entry: " + entry.getDetailsString() ); //entry.getDetailsString());
            System.out.println( " Scope Name: " + scope_of_identifier.getName());
            return entry.getType();

        } catch (Error err) { 
            throw new CodeGenerationException("CodeGeneration, getIdentifierType()", "Unable to locate entry within scope");
        }
    }



    public String getLocation (NonTerminal parent, Terminal child) throws CodeGenerationException {
        SymbolTableScope scope_of_identifier = getScopeOfTerminal(parent, child);
        pout("Child Name: " + child.getName());
        try {
            SymbolTableEntry entry = symbol_table.retrieveEntryFromAccessibleScopes(scope_of_identifier, child);
            System.out.println( " ( " + child.getTokenAttribute() + " )  Found Entry: " + entry.getDetailsString());
            System.out.println( " Scope Name: " + scope_of_identifier.getName());
            return scope_of_identifier.getName();

        } catch (Error err) { 
            throw new CodeGenerationException("CodeGeneration, getIdentifierType()", "Unable to locate entry within scope");
        }
    }

    public String characterTerminalHandler (Terminal character_terminal) throws CodeGenerationException {
        String string_stored_terminal = character_terminal.getTokenAttribute(); 
        boolean exists_in_string_declarations = execution_environment.stringExistsWithStringDeclarations(string_stored_terminal);
        String[] hexadecimal_array_of_string = string_stored_terminal.chars().mapToObj(c -> String.format("%02X", c)).toArray(String[]::new);
        String hexadecimal_location_in_heap; 
        int int_location_in_heap; // needed for storage into string decls if required
        if (exists_in_string_declarations) hexadecimal_location_in_heap = String.format("%02X", (execution_environment.getAddressFromStringDeclarations(string_stored_terminal)));
        else if (within_jump) { hexadecimal_location_in_heap = String.format("%02X", execution_environment.getHeapPointer() - hexadecimal_array_of_string.length - 1); } // Not actually going to store, but will provide location where it will be stored
        else { 
            //execution_environment.insertImmediately(new ArrayList<>(Arrays.asList(hexadecimal_array_of_string)), "Heap");
            int_location_in_heap = execution_environment.getHeapPointer() - hexadecimal_array_of_string.length - 1;
            execution_environment.insertIntoStringDeclarations(string_stored_terminal, int_location_in_heap);
            int retrievd_location_from_static_table = execution_environment.getAddressFromStringDeclarations(string_stored_terminal);
            hexadecimal_location_in_heap = String.format("%02X", int_location_in_heap); 
            pout("retrievd_location_from_static_table: " + retrievd_location_from_static_table);
            pout("hexadecimal location in string: " + hexadecimal_location_in_heap);
            pout(execution_environment.getValueFromCodeSequence(int_location_in_heap + 6));
            pout(execution_environment.getCodeSequenceString());
            execution_environment.performHeapInsertion(hexadecimal_array_of_string);
        }   
        pout("hexadecimal location in string: " + hexadecimal_location_in_heap);
        pout("getHeapPointer: " + execution_environment.getHeapPointer());

        //pout(execution_environment.getValueFromCodeSequence(int_location_in_heap));
        return hexadecimal_location_in_heap; 
    }

    public String getIdentifierScopeName (NonTerminal parent, Terminal child) throws CodeGenerationException {
        SymbolTableScope scope_of_identifier = getScopeOfTerminal(parent, child);
        return scope_of_identifier.getName();
    }

    public String getTerminalAddressingComponent (NonTerminal parent, Terminal terminal, int index_of_identifier) throws CodeGenerationException {
        String addressing_component = ""; 

        
        if (terminal.getName().equals("IDENTIFIER")) { 
            String id_scope_name = getIdentifierScopeName(parent, terminal);
            //pout("ID Scope Name: " + id_scope_name);
            //pout("Token Attribute: " + terminal.getTokenAttribute()); 
            addressing_component = execution_environment.retrieveTemporaryLocation(terminal.getTokenAttribute(), id_scope_name); 
            //pout("Addressing COmpontnet: " + addressing_component );
        } else if (terminal.getName().equals("DIGIT")) { addressing_component= "0" + terminal.getTokenAttribute(); // Digit formatted properly

        } else if (terminal.getName().equals("CHARACTER")) { addressing_component = characterTerminalHandler(terminal); // Hexadecima location within heap 
            
        } else if ( (terminal.getName().equals("KEYWORD_TRUE")) || (terminal.getName().equals("KEYWORD_FALSE"))) {
            if (terminal.getName().equals("KEYWORD_TRUE"))  addressing_component = execution_environment.getTruePointer(); 
            else if (terminal.getName().equals("KEYWORD_FALSE"))  addressing_component = execution_environment.getFalsePointer();

        } else throw new CodeGenerationException("CodeGeneration, getLocationOfTerminal()", "Terminal sent to getLocationOfTerminal() is not of type IDENTIFIER, CHARACTER, KEYWORD_TRUE, KEYWORD_FALSE. \nTerminal type received: " + terminal.getName());

        return addressing_component; 
    }


    public String handleCHARACTERterminalAndGetStringAddress (Terminal terminal) throws CodeGenerationException {
        String terminals_string_value = terminal.getTokenAttribute(); 
        boolean exists_in_strdecls = execution_environment.stringExistsWithStringDeclarations(terminals_string_value);
        //System.out.println("\n\nExists within string decls: " + exists_in_strdecls);
        
        int heap_pointer;
        String[] hex_arraylist = terminals_string_value.chars().mapToObj(c -> String.format("%02X", c)).toArray(String[]::new);
        
        if (exists_in_strdecls) {
            heap_pointer = execution_environment.getAddressFromStringDeclarations(terminals_string_value); // If exists in string_decls already, get address
            pout("`" + terminals_string_value + "`" + " exists within String Declaration. [Address: " + heap_pointer + " ]");
            
        } else {
            heap_pointer = execution_environment.getHeapPointer() - hex_arraylist.length - 1; // new insertion location
           // String heap_pointer_hex = String.format("%02X", heap_pointer);
            if (!within_jump) {
                execution_environment.performHeapInsertion(hex_arraylist);
                execution_environment.insertIntoStringDeclarations(terminals_string_value, heap_pointer); // Store in string_declarations hashmap 
                // Checking proper insertion
                heap_pointer = execution_environment.getAddressFromStringDeclarations(terminals_string_value); // If exists in string_decls already, get address
                pout("Checking.. `" + terminals_string_value + "`" + " exists within String Declaration. [Address: " + heap_pointer + " ]");
            }
            //execution_environment.insertImmediately(new ArrayList<>(Arrays.asList(hex_arraylist)), "Heap"); // insert it into code_sequence without waiting, so we can specify Heap insertion
            //execution_environment.insertImmediatelyHeap(hex_arraylist_actually, "Heap", ); // insert it into code_sequence without waiting, so we can specify Heap insertion
        }
        
        // Need to update heap pointer after being done, need to full on return after being in this elif block, need to store pointer for string in string_declarations map 
        String heap_pointer_hex = String.format("%02X", heap_pointer);
        //System.out.println("Heap Pointer Hex: " + heap_pointer_hex);
        return heap_pointer_hex;
        
    }

    /*
    public String getIdentifierType (Production parent, Terminal identifier) {
        String scope = parent.getScopeName(); 
        String name = identifier.getName(); 
        String id = identifier.getTokenAttribute(); 
        System.out.println("GetIdentifierType - Scope: " + scope + ", Name: " + name + " Id: " + id); 
        //pout("Scopes:  " + symbol_table.getScopeNames());
        SymbolTableScope residing_scope = symbol_table.getScope(scope);
        boolean entry_exists = residing_scope.entryExists(id);
        pout("Entry Exists: " + entry_exists);
        SymbolTableEntry specific_entry = residing_scope.retrieveEntry(id);
        String type = specific_entry.getType();
        pout("Type: " + type);
        return type; 
    } */

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

    public String childrenAsString (NonTerminal nt) { 
        String children_string = "";
        for (int i = 0; i<= nt.getASTChildren().size() - 1; i++) children_string = children_string + ", " +  nt.getASTChild(i).getName();
        return children_string;
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