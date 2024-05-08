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
    public HashMap<String, String> string_declaration = new HashMap<>(); 
    
    
    public int code_pointer = 0;
    public int stack_pointer = -1; // Must be set when code gen is done
    public int heap_pointer = 255;
    public int temporary_value_counter = 0; 
    
    public int usable_bytes_remaining = 255; // start 255 because program will need to have an end instruction of 00 
    public int reserved_space = 0;

    public ExecutionEnvironment() throws CodeGenerationException {
        java.util.Arrays.fill(code_sequence, "0"); // populate code sequence array with "0" at start
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

    // Called after new instructions were inserted into the code sequence, this updates the remaining bytes accordingly
    public void updateRemainingSpace (String[] inserted_instructions, String location) {
        setRemainingBytes(getRemainingBytes() - inserted_instructions.length);
    }


    ////// String Declaration Map
    // Check to see if String with identical makeup has already been declared and inserted
    public boolean stringExists (String string_to_check) {
        return string_declaration.containsKey(string_to_check); 
    }

    // Return temporary location associated with identical String content
    public String retrieveStringAddress (String str) {
        return string_declaration.get(str);
    }

    ////// Static Table / Reversed Static Table
    public void performStaticTableInsertion (String variable_id, String scope_name) throws CodeGenerationException {
        if (static_table.isEmpty()) {
           String static_table_variable_name = variable_id + "@" + scope_name; 
           String temporary_address = "T0"; 
           static_table.put(temporary_address, static_table_variable_name); 
           reversed_static_table.put(static_table_variable_name, temporary_address); 
           incrementTemporaryValueCounter(); // increment counter used to form temp value addresses
        } else {
            String static_table_variable_name = variable_id + "@" + scope_name; 
            String temporary_address = "T" + getTemporaryValueCounter(); // only works until 9,
            static_table.put(temporary_address, static_table_variable_name); 
            reversed_static_table.put(static_table_variable_name, temporary_address); 
            incrementTemporaryValueCounter(); // increment counter used to form temp value addresses
            if (getTemporaryValueCounter() >= 10) throw new CodeGenerationException("ExecutionEnvironment, performStaticTableInsertion()", "You did it. You broke my compiler. Currently, temporary addresses are limited to the range T0-T9...");
        }
    }

    ////// Code Sequence 
    public boolean variableExistsInStaticTable (String variable_id, String scope_name) {
        String static_table_variable_name = variable_id + "@" + scope_name; 
        return reversed_static_table.containsKey(static_table_variable_name); // reversed table is mapped like, variable : address 
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
    public void insert (String[] instructions, String type, String location) throws CodeGenerationException {
        
        
        switch (location) {

            case "Code":
                if (codeInsertionPossible(instructions, "Code")) {
                    performCodeInsertion(instructions);
                } else throw new CodeGenerationException("ExecutionEnvironment, insert()", "Unable to insert into Code") ;

            case "Stack": 
                if (codeInsertionPossible(instructions, "Stack")) {
                    performStackInsertion(instructions);
                } else throw new CodeGenerationException("ExecutionEnvironment, insert()", "Unable to insert into Stack") ;

            case "Heap": 
                if (codeInsertionPossible(instructions, "Heap")) {
                    performHeapInsertion(instructions);
                } else throw new CodeGenerationException("ExecutionEnvironment, insert()", "Unable to insert into Heap") ;
        }

    }

    public void performCodeInsertion (String[] instructions) throws CodeGenerationException {
        System.arraycopy(instructions, 0, getCodeSequence(), getCodePointer(), instructions.length);
        setCodePointer(getCodePointer() + instructions.length);
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