import java.util.Arrays;

public class ExecutionEnvironment {
    
    // Each int/bool uses one memory address, this means that this space is reserved an cannot be used, e.g., two variables means two bytes of memory are reserved (in theory, not sure if I will put this into practice)
    // Each string is of undefined length, but it too will require at the very least at least one byte so that a reference can be made to its location within the heap

    // true false null
    // 74 72 75 65 00 66 61 6C 73 65 00 6E 75 6C 6C 00

    // Temorary variable insertion, T0 00, requires two bytes
    
    // When you are about to declare a variable,
    // ->  the accumulator must be set to 00, so A9 00
    // ->  store that value at the location of the temporary address


    // Reserved space the becomes necessary when variables are declared will be addressed within a create static table insertion method
    public String[] code_sequence = new String[256]; 
    public int code_pointer = 0;
    public int stack_pointer;

    public int heap_pointer = 255;
    
    
    public int usable_bytes_remaining = 255; // start 255 because program will need to have an end instruction of 00 
    public int reserved_space = 0;

    public ExecutionEnvironment() {
        java.util.Arrays.fill(code_sequence, "0"); // populate code sequence array with "0" at start
    }

    public int getRemainingBytes () { return this.usable_bytes_remaining; }
    public void setRemainingBytes (int rem_bytes) { this.usable_bytes_remaining = rem_bytes; }
    public void checkRemainingSpace () {}

    public void calculateRemainingSpace (String[] inserted_instructions, String location) {
        setRemainingBytes(getRemainingBytes() - inserted_instructions.length);
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
        
        // Safety check 
        switch (location) {
            case "Code":
                if (codeInsertionPossible(instructions, "Code")) {
                    
                } else throw new CodeGenerationException("ExecutionEnvironment, insert()", "Unable to insert into Code") ;
            case "Stack": 
                if (codeInsertionPossible(instructions, "Stack")) {}
                else throw new CodeGenerationException("ExecutionEnvironment, insert()", "Unable to insert into Stack") ;
            case "Heap": 
                if (codeInsertionPossible(instructions, "Heap")) {
                    performHeapInsertion(instructions, type, location);
                } else throw new CodeGenerationException("ExecutionEnvironment, insert()", "Unable to insert into Heap") ;
        }

        if (location.equals("Heap")) {
            String[] heap_instructions = Arrays.copyOf(instructions, instructions.length + 1); // copy instructions
            int instructions_length = heap_instructions.length; // instructions + "00" 
            int heap_insertion_location = heap_pointer - instructions_length; 

            heap_instructions[heap_instructions.length -1] = "00"; // add "00" in last position
            System.arraycopy(heap_instructions, 0, code_sequence, heap_pointer - heap_instructions.length, instructions_length);
            heap_pointer = heap_insertion_location - 1;

            calculateRemainingSpace(heap_instructions, "Heap"); // ensure local values are up to date
            //usable_bytes_remaining-= instructions.length - 1;  
        }


        if (location.equals("Code")) {codeInsertionPossible("Code")}
        System.arraycopy(instructions, 0, code_sequence, code_pointer, instructions.length);
        //usable_bytes_remaining-= instructions.length; 

    }

    public void performCodeInsertion (String[] instructions, String type, String location) throws CodeGenerationException {

    }

    public void performStackInsertion (String[] instructions, String type, String location) throws CodeGenerationException {
        
    }

    public void performHeapInsertion (String[] instructions, String type, String location) throws CodeGenerationException {
        String[] heap_instructions = Arrays.copyOf(instructions, instructions.length + 1); // copy instructions
        int instructions_length = heap_instructions.length; // instructions + "00" 
        int heap_insertion_location = heap_pointer - instructions_length; 

        heap_instructions[heap_instructions.length -1] = "00"; // add "00" in last position
        System.arraycopy(heap_instructions, 0, code_sequence, heap_pointer - heap_instructions.length, instructions_length);
        heap_pointer = heap_insertion_location - 1;

        calculateRemainingSpace(heap_instructions, "Heap"); // ensure local values are up to date
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