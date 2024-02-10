public class Token {
    
    public String name; // Token name, Prefixes: KEYWORD, IDENTIFIER, SYMBOL, DIGIT, CHARACTER
    public String attribute; // Value, "chars_matched" previously
    
    public int start_pos; // Index within byte array
    public int end_byte_pos; // Index within byte array
    
    // Line number, as found within the source input file
    public int start_line_number; // Line where the lexeme match begins
    public int end_line_number; // Line where the lexeme match ends
    public int line_index; // Where on the line, with first index on line being 0

    

}
