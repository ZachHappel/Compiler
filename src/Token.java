import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Token {
    
    public String name; // Token name, Prefixes: KEYWORD, IDENTIFIER, SYMBOL, DIGIT, CHARACTER
    public String attribute; // Value, "chars_matched" previously
    
    public int start_pos; // Index within byte array
    public int end_pos; // Index within byte array
    
    // Line number, as found within the source input file
    public int start_line_number; // Line where the lexeme match begins
    public int end_line_number; // Line where the lexeme match ends
    public int line_index; // Where on the line, with first index on line being 0
    
    public Map<String, int[]> LexemePossibilities = new HashMap<String, int[]>() {{
        put("KEYWORD_INT", new int[]{-1, -1});
        put("KEYWORD_STRING", new int[]{-1, -1});
        put("KEYWORD_BOOLEAN", new int[]{-1, -1});
        put("KEYWORD_PRINT", new int[]{-1, -1});
        put("KEYWORD_WHILE", new int[]{-1, -1});
        put("KEYWORD_TRUE", new int[]{-1, -1});
        put("KEYWORD_FALSE", new int[]{-1, -1});
        put("KEYWORD_IF", new int[]{-1, -1});
        
        put("IDENTIFIER", new int[]{-1, -1});

        put("SYMBOL_OPENBLOCK", new int[]{-1, -1});
        put("SYMBOL_CLOSEBLOCK", new int[]{-1, -1});
        put("SYMBOL_OPENPAREN", new int[]{-1, -1});
        put("SYMBOL_CLOSEPAREN", new int[]{-1, -1});
        put("SYMBOL_STRINGEXPRBOUNDARY", new int[]{-1, -1});
        put("SYMBOL_ASSIGNMENT", new int[]{-1, -1});
        put("SYMBOL_EQUIVALENCE", new int[]{-1, -1});
        put("SYMBOL_INEQUIVALENCE", new int[]{-1, -1});
        put("SYMBOL_OPENCOMMENT", new int[]{-1, -1});
        put("SYMBOL_CLOSECOMMENT", new int[]{-1, -1});
        
        put("DIGIT", new int[]{-1, -1});
        put("CHARACTER", new int[]{-1, -1});
        
        put("EOP", new int[]{-1, -1});
    }};

    public Token (int start_pos, int end_pos) {
        this.start_pos = start_pos;
        this.end_pos = end_pos;
    }

    public void setName(String i) { this.name = i; }
    public void setAttribute(String i) { this.attribute = i; }
    public void setStartPos(int i) { this.start_pos = i; }
    public void setEndPos(int i) { this.end_pos = i; }
    public void setStartLineNumber(int i) { this.start_line_number = i; }
    public void setEndLineNumber(int i) { this.end_line_number = i; }
    public void setLineIndex(int i) { this.line_index = i; }

    public void removePossiblity(String lexeme_name) {
        this.LexemePossibilities.remove(lexeme_name);
    }

    public void updatePossibility(String lexeme_name, int[] indices) {
        this.LexemePossibilities.put(lexeme_name, indices);
    }
}
