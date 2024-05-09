import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
public class ExecutionEnvironment {
    
    /** 
    
    Each int/bool uses one memory address, this means that this space is reserved an cannot be used, e.g., two variables means two bytes of memory are reserved (in theory, not sure if I will put this into practice)
    Each string is of undefined length, but it too will require at the very least at least one byte so that a reference can be made to its location within the heap
    
    Strings content will be stored in the heap but just like any other variable, we must wait to provide it with an actual permanent location. The String will receive a static table entry
    The Strings' temporary values will be used in the string_declaration hashmap as well. Associating identical strings with the same temporary value is all the collation that we need.

    **/ 


    // true false null
    // 74 72 75 65 00 66 61 6C 73 65 00 6E 75 6C 6C 00

    // Temorary variable insertion, T0 00, requires two bytes
    
    // When you are about to declare a variable,
    // ->  the accumulator must be set to 00, so A9 00
    // ->  store that value at the location of the temporary address


    // Reserved space the becomes necessary when variables are declared will be addressed within a create static table insertion method
    public String[] code_sequence = new String[256]; 

    // Maps temporary variable ID value @ scope to address locations
    // Stored will be the first portion of the temporary address value, for example: T0 00 would just be stored as T0
    public Map<String, String> static_table = new LinkedHashMap<>(); // address : variable
    public Map<String, String> reversed_static_table = new LinkedHashMap<>();  // variable : address
    
    public Map<String, String> jump_table = new HashMap<>(); 

    // Hashmap which maps String content to an address location ([a-z]* : Tn)
    public HashMap<String, Integer> string_declaration = new HashMap<>(); 

    ////////////////////////////////////////////////////////////////////
    
    public int code_pointer = 0;
    public int stack_pointer = -1; // Must be set when code gen is done
    public int heap_pointer = 240;
    public int temporary_value_counter = 0; 
    public int accumulator = 0;

    // Starting index in hexadecimal for each of the constant string values stored in Heap 
    public String null_pointer = "F0";
    public String false_pointer = "FA";
    public String true_pointer = "F5"; 

    ///////////////////////////////////////////////////////////////////
    
    public int usable_bytes_remaining = 240; // start 255 because program will need to have an end instruction of 00 
    public int reserved_space = 0;

    public ExecutionEnvironment() throws CodeGenerationException {
        java.util.Arrays.fill(code_sequence, "0"); // populate code sequence array with "0" at start
        
        System.arraycopy(new String[]{"6E", "75", "6C", "6C", "00", "74", "72", "75", "65", "00", "66", "61", "6C", "73", "65", "00"}, 0, getCodeSequence(), code_sequence.length - 16, 16);
    }


    public String[] getCodeSequence () { return this.code_sequence; }
    public Map<String, String> getStaticTable () { return this.static_table; }
    public int getTemporaryValueCounter () { return this.temporary_value_counter; }

    public int getCodePointer () { return this.code_pointer; }
    public int getStackPointer () throws CodeGenerationException { if  (this.stack_pointer > 0) { return this.stack_pointer; } else throw new CodeGenerationException("ExecutionEnvironment, getStackPointer", "Stack Pointer was never set"); }
    public int getHeapPointer () { return this.heap_pointer; }
    public int getRemainingBytes () { return this.usable_bytes_remaining; }
    
    public void setCodePointer (int i) { this.code_pointer = i; }
    public void setStackPointer (int i) { this.stack_pointer = i; }
    public void setHeapPointer (int i) { this.heap_pointer = i; }
    public void setRemainingBytes (int rem_bytes) { this.usable_bytes_remaining = rem_bytes; }
    public void setTemporaryVariableCounter (int i) { this.temporary_value_counter = i;}
    public void incrementTemporaryValueCounter () { this.temporary_value_counter = this.temporary_value_counter + 1; }

  
    public String getValueFromCodeSequence (int index) { return code_sequence[index]; }

    public String getFalsePointer () { return this.false_pointer; }
    public String getFalsePointerHex () {  String hex_value = String.format("%02X", this.false_pointer); return hex_value;   }
    public String getTruePointer () { return this.true_pointer; }
    public String getTruePointerHex () {  String hex_value = String.format("%02X", this.true_pointer); return hex_value;   }
    

    
    // Called after new instructions were inserted into the code sequence, this updates the remaining bytes accordingly
    public void updateRemainingSpace (String[] inserted_instructions, String location) {
        setRemainingBytes(getRemainingBytes() - inserted_instructions.length);
    }

    ////// String Declaration Map
    // Check to see if String with identical makeup has already been declared and inserted
    public boolean stringExistsWithStringDeclarations (String string_to_check) {
        return string_declaration.containsKey(string_to_check); 
    }

    // Return temporary location associated with identical String content
    public int getAddressFromStringDeclarations (String str) {
        return string_declaration.get(str);
    }

    // location being the non-hex index value within the code sequence
    public void insertIntoStringDeclarations (String str, int location) {
        string_declaration.put(str, location);
    }

    ////// Static Table / Reversed Static Table
    
    // Returns String name of the new temporary address
    public String performStaticTableInsertion (String variable_id, String scope_name) throws CodeGenerationException {
        String temp_addr;
        if (static_table.isEmpty()) {
           String static_table_variable_name = variable_id + "@" + scope_name; 
           temp_addr = "T0"; 
           System.out.println("StaticTableInsertion: ID: " + static_table_variable_name + ", Temp Address Created: " + temp_addr);
           static_table.put(temp_addr, static_table_variable_name); 
           reversed_static_table.put(static_table_variable_name, temp_addr); 
           incrementTemporaryValueCounter(); // increment counter used to form temp value addresses
        } else {
            String static_table_variable_name = variable_id + "@" + scope_name; 
            temp_addr = "T" + getTemporaryValueCounter(); // only works until 9,
            System.out.println("StaticTableInsertion: ID: " + static_table_variable_name + ", Temp Address Created: " + temp_addr);
            static_table.put(temp_addr, static_table_variable_name); 
            reversed_static_table.put(static_table_variable_name, temp_addr); 
            incrementTemporaryValueCounter(); // increment counter used to form temp value addresses
            if (getTemporaryValueCounter() >= 99) throw new CodeGenerationException("ExecutionEnvironment, performStaticTableInsertion()", "You did it. You broke my compiler. Currently, temporary addresses are limited to the range T0-T9...");
        }

        return temp_addr; 
    }

    
    public void backpatch() throws CodeGenerationException {
        
        for (int i = 0; i <= code_sequence.length - 1; i++) {
            if (code_sequence[i] == "0") {
                code_sequence[i] = "00";
            }
        }

        int stack_addressing_index = getStackPointer(); // + 1 because program ends with 00

        for (Map.Entry<String, String> entry: static_table.entrySet()) {
            String temporary_location = entry.getKey();
            String memory_location = String.format("%02X", stack_addressing_index);
            System.out.println("Backpatching Temp Location, " + temporary_location + ", with Address: " + memory_location);
            stack_addressing_index+=1; 

            for (int i = 0; i <= code_sequence.length - 1; i++) if (code_sequence[i].equals(temporary_location)) code_sequence[i] = memory_location; 
        }

        //String[] updatedArray = Arrays.stream(code_sequence).map(s -> s.equals("0") ? "00" : s).toArray(String[]::new);


    }


    public String createAndRetrieveNewTemporaryAddress () {
        String temp_addr = "T" + getTemporaryValueCounter();
        incrementTemporaryValueCounter();
        return temp_addr; 

    }

    ////// Code Sequence 
    public boolean variableExistsInStaticTable (String variable_id, String scope_name) {
        String static_table_variable_name = variable_id + "@" + scope_name; 
        return reversed_static_table.containsKey(static_table_variable_name); // reversed table is mapped like, variable : address 
    }

    public String retrieveTempLocationFromStaticTable (String static_table_variable_name) {
        return reversed_static_table.get(static_table_variable_name);
    }

    public String retrieveTempLocationFromStaticTable (Terminal identifier, Production prod) {
        
        String identifier_value = identifier.getTokenAttribute();
        String identifier_scope = (prod.getProdKind().equals("NonTerminal") ? ((NonTerminal) prod).getScopeName() :  ((Terminal) prod).getScopeName()); // Maybe change could be problematic
        String static_table_variable_name = identifier_value + "@" + identifier_scope; 
        String id_temp_location = retrieveTempLocationFromStaticTable(static_table_variable_name);
        System.out.println("Retrieved From Static Table the Location for, " + static_table_variable_name + ": " + id_temp_location);
        return reversed_static_table.get(id_temp_location);
    }

    public String retrieveTempLocationFromChildOfNonTerminal (NonTerminal Production, int index) {
        String scope = Production.getScopeName();
        String id = ((Terminal) Production.getASTChild(index)).getTokenAttribute(); 
        String static_table_variable_name = id + "@" + scope; 
        String temp_location = retrieveTempLocationFromStaticTable(static_table_variable_name);
        //System.out.println("Retrieved From Static Table the Location for, " + static_table_variable_name + ": " + id_temp_location);
        return temp_location;
    }



    public boolean codeInsertionPossible (String[] instructions, String location) throws CodeGenerationException {
        // If the remaining space is greater than or equal to zero after adding the instructions (+ 1) then true
        // + 1 is just to keep good pace right now and I do not want to get too hung up on anything
        boolean insertion_possible = (getRemainingBytes() - (instructions.length + 1) >= 0 ? true : false); 
        System.out.println("Insertion Possible: " + insertion_possible);
        return insertion_possible; 
    }

    /* instruction: the bytes, either 
     * type: either int, boolean, string
     */
    public void insert (String[] instructions, String location) throws CodeGenerationException {
        
        
        switch (location) {

            case "Code":
                if (codeInsertionPossible(instructions, "Code")) {
                    performCodeInsertion(instructions);
                    break;
                } else throw new CodeGenerationException("ExecutionEnvironment, insert()", "Unable to insert into Code") ;

            case "Stack": 
                if (codeInsertionPossible(instructions, "Stack")) {
                    performStackInsertion(instructions);
                    break;
                } else throw new CodeGenerationException("ExecutionEnvironment, insert()", "Unable to insert into Stack") ;

            case "Heap": 
                if (codeInsertionPossible(instructions, "Heap")) {
                    performHeapInsertion(instructions);
                    break;
                } else throw new CodeGenerationException("ExecutionEnvironment, insert()", "Unable to insert into Heap") ;
        }

    }

    public void performCodeInsertion (String[] instructions) throws CodeGenerationException {
        System.arraycopy(instructions, 0, getCodeSequence(), getCodePointer(), instructions.length);
        setCodePointer(getCodePointer() + instructions.length);
        setStackPointer(getCodePointer() + 1);
        updateRemainingSpace(instructions, "Code");
    }

    public void performStackInsertion (String[] instructions) throws CodeGenerationException {
        System.arraycopy(instructions, 0, getCodeSequence(), getStackPointer(), instructions.length);
        setStackPointer(getStackPointer() + instructions.length);
        updateRemainingSpace(instructions, "Stack");
    }

    public void performHeapInsertion (String[] instructions) throws CodeGenerationException {
        String[] heap_instructions = Arrays.copyOf(instructions, instructions.length + 1); // copy instructions
        int instructions_length = heap_instructions.length; // instructions + "00" 
        int heap_insertion_location = getHeapPointer() - instructions_length; 
        heap_instructions[heap_instructions.length -1] = "00"; // add "00" in last position
        
        // Update code_sequence with new instructions
        System.arraycopy(heap_instructions, 0, code_sequence, heap_pointer - heap_instructions.length, instructions_length);
        
        setHeapPointer(heap_insertion_location - 1);
        updateRemainingSpace(heap_instructions, "Heap"); // ensure local values are up to date
    }

    public void printCodeString () {
        String code = "";
        for (int i = 0; i <= code_sequence.length - 1; i++) {
            code = code + code_sequence[i] + " ";
        }
        System.out.println(code);
    } 

   


}




//System.arraycopy( (Arrays.copyOf(instructions, instructions.length + 1)), 0, code_sequence, heap_pointer-(instructions.length + 1), instructions.length + 1);
            //code_sequence[heap_pointer] = "00"; // set end of string
            //heap_pointer = heap_pointer - instructions.length - 1; // now we update new heap pointer -- where the muinus one accounts for the "00"



/*
 * 
 *  public void insertNextByte (String instruction, String type, String location) {
        code_sequence[code_pointer] = instruction;   
    }

    public void insertNextByte (String[] instructions, String type, String location) {
        
        
    }
 */