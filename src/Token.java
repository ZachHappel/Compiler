import java.util.ArrayList;
import java.util.Arrays;
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

    public boolean isPotentiallyKeyword = true;

    public boolean isWithinComment;
    public boolean isWithinStringExpression; 
    
    public boolean windowIsFullyExpanded;

    public boolean thereExistsSharedLongestMatch;
    public boolean thereExistsUniqueLongestMatch;

    public String longest_match_name;
    public int longest_match_length = 0;
    public ArrayList<String> shared_longest_match_array;

    
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


    public String getName() { return this.name; }
    public String getAttribute() { return this.attribute; }
    public int getStartPos() { return this.start_pos; }
    public int getEndPos() { return this.end_pos; }
    public int getStartLineNumber() { return this.start_line_number; }
    public int getEndLineNumber() { return this.end_line_number; }
    public int getLineIndex() { return this.line_index; }
    public boolean getIsPotentiallyKeyword() { return this.isPotentiallyKeyword; }
    public boolean getIsWithinComment() { return this.isWithinComment; }
    public boolean getIsWithinStringExpression() { return this.isWithinStringExpression; }
    public boolean getWindowIsFullyExpanded() { return this.windowIsFullyExpanded; }
    public Map<String, int[]> getPossibilities () { return this.LexemePossibilities; }
    public String getLongestMatchName() { return this.longest_match_name;}
    public boolean getThereExistsSharedLongestMatch() { return this.thereExistsSharedLongestMatch;}
    public boolean getThereExistsUniqueLongestMatch() { return this.thereExistsUniqueLongestMatch;}
    public int getLongestMatchLength() { return this.longest_match_length;}
    public ArrayList<String> getSharedLongestMatchArray() { return this.shared_longest_match_array;}
    public Map<String, int[]> getLexemePossibilities() { return this.LexemePossibilities;}

    public void setName(String i) { this.name = i; }
    public void setAttribute(String i) { this.attribute = i; }
    public void setStartPos(int i) { this.start_pos = i; }
    public void setEndPos(int i) { this.end_pos = i; }
    public void setStartLineNumber(int i) { this.start_line_number = i; }
    public void setEndLineNumber(int i) { this.end_line_number = i; }
    public void setLineIndex(int i) { this.line_index = i; }
    public void setIsPotentiallyKeyword(boolean i) {this.isPotentiallyKeyword = i; }
    public void setIsWithinComment(boolean i) {this.isWithinComment = i; }
    public void setIsWithinStringExpression(boolean i) {this.isWithinStringExpression = i; }
    public void setWindowIsFullyExpanded(boolean i) {this.windowIsFullyExpanded = i; }
    


    public void removePossiblity(String lexeme_name) {
        this.LexemePossibilities.remove(lexeme_name);
    }

    public void updatePossibility(String lexeme_name, int[] indices) {
        this.LexemePossibilities.put(lexeme_name, indices);
    }

    public void printRemainingPossibilities(Token token) {
        //Map<String, int[]> remaining = new

        for (Map.Entry<String, int[]> entry : this.LexemePossibilities.entrySet()) {
            String name = entry.getKey();
            int[] i = entry.getValue();
            System.out.println("Name: " + name + " Indices: " + i + " Length of match: " + (i[1] - i[0]));    

        }
    }


    public void processLongestMatch () {
        

        ArrayList<String> matches_of_same_length = new ArrayList<String>();

        for (Map.Entry<String, int[]> entry : this.LexemePossibilities.entrySet()) {
            String name = entry.getKey();
            int[] i = entry.getValue();
            int difference = i[1] - i[0];
            
            System.out.println("Keyword: " + name + " Indices: " + Arrays.toString(i) + " Length of match: " + difference);    
            
            if (difference == this.longest_match_length && this.longest_match_length > 0) {
                matches_of_same_length.add(name);
            }

            if (difference > this.longest_match_length) {
                this.longest_match_length = difference; // Update longest match length
                this.longest_match_name = name; // Update name of longest match
                matches_of_same_length = new ArrayList<String>(); // Reset similarities because new longest found
            }

            //if (diff)
        }
        // Will only be greater than zero if there is not a unique longest match
        if (matches_of_same_length.size() > 0) {
            this.thereExistsUniqueLongestMatch = false;
            this.thereExistsSharedLongestMatch = true;
            this.shared_longest_match_array = matches_of_same_length; 
        }
    }


    public void printRemainingPossibilities(boolean with_indices) {
        System.out.println("\n(...) Remaining Probabilities");
        for (Map.Entry<String, int[]> entry : (this.LexemePossibilities).entrySet()) {
            if (with_indices) System.out.println(entry.getKey() + ", " + Arrays.toString(entry.getValue()));
            else System.out.println(entry.getKey());
        }
    }

    public int[] getIndices() {
        return new int[]{this.start_pos, this.end_pos};
    }

    


}
