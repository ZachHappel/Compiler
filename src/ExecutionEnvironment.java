import java.util.Arrays;

public class ExecutionEnvironment {
    
    // Each int/bool uses one memory address, this means that this space is reserved an cannot be used, e.g., two variables means two bytes of memory are reserved (in theory, not sure if I will put this into practice)
    // Each string is of undefined length, but it too will require at the very least at least one byte so that a reference can be made to its location within the heap

    String[] code_sequence = new String[256]; 
    int code_pointer = 0;
    int usable_bytes_remaining = 255; // start 255 because program will need to have an end instruction of 00 

    public ExecutionEnvironment() {
        java.util.Arrays.fill(code_sequence, "0"); // populate code sequence array with "0" to begin
    }

    public boolean codeInsertionPossible () {
        return true;
    }

    
    public void insert (String instruction, String type, String location) {

    }

    public void insertNextByte (String instruction, String type) {
        code_sequence[code_pointer] = instruction;   
    }

    public void insertNextByte (String[] instructions, String type) {
        if ()
        for (int i = 0; i <= instructions.length - 1; i++) {

        }
    }



}
