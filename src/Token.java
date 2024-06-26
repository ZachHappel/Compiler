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
    public boolean remaining_possibilites_are_unset;
    public boolean remaining_possibilities_are_impossible;
    public String f_possibility_name;
    public int[] f_possibility_indices;

    public int symbol_semi_matches; 
    public int keyword_semi_matches;

    public boolean has_matched_assginment; 
    public String matched_assignment_attribute;
    
    public static ArrayList<String> ungrouped_tks_of_length_one = new ArrayList<>(){{
        add("IDENTIFIER");
        add("CHARACTER");
        add("DIGIT");
        add("EOP");
    }};
    
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
    public boolean getRemainingPossibilitiesAreImpossible() { return this.remaining_possibilities_are_impossible;}
    public int getSymbolSemiMatchesAmount( ) { return this.symbol_semi_matches;}
    public int getKeywordSemiMatchesAmount( ) { return this.keyword_semi_matches; }
    public boolean getHasMatchedAssignment () { return this.has_matched_assginment;  }
    public String getMatchedAssignmentValue () { return this.matched_assignment_attribute;  }

    
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
    public void setRemainingPossibilitiesAreImpossible(boolean i) { this.remaining_possibilities_are_impossible = i;}
    public void setSymbolSemiMatchesAmount( int i ) { this.symbol_semi_matches = i; }
    public void setKeywordSemiMatchesAmount( int i ) { this.keyword_semi_matches = i; }
    public void setHasMatchedAssignment (boolean i) { this.has_matched_assginment = i; }
    public void setMatchedAssignmentValue(String i) { this.matched_assignment_attribute = i; }
    
    

    public int[] getIndices() {
        return new int[]{this.start_pos, this.end_pos};
    }


    public void removePossiblity(String lexeme_name) {
        this.LexemePossibilities.remove(lexeme_name);
    }
    
    public void updatePossibility(String lexeme_name, int[] indices) {
        
        //System.out.println("Update Possibilities - name: " + lexeme_name);
        //System.out.println("Update Possibilities - indices: " + Arrays.toString(indices));
        //System.out.println("LexemePossibilities.get(lexeme_name)[0]: " + (LexemePossibilities.get(lexeme_name) == null));
        
        if (ungrouped_tks_of_length_one.contains(lexeme_name)) {

            int[] current_indices = this.LexemePossibilities.get(lexeme_name);
            int ci_0 = current_indices[0];
            if (ci_0 < 0) {
                this.LexemePossibilities.put(lexeme_name, indices);
            } 
            return;
        }

        this.LexemePossibilities.put(lexeme_name, indices);


        /**
        // If the lexeme in question is among those of one character length, and its indices have NOT been accounted for, update them (if they have been accounted for already, e.g., not being -1, then it will be ignored)
        if ((LexemePossibilities.get(lexeme_name) == null) && ungrouped_tks_of_length_one.contains(lexeme_name)) {
            this.LexemePossibilities.put(lexeme_name, indices);
        
        // If it is anything other than those four non-grouped lexemes of length one, update the possibility with the position
        } else if (!ungrouped_tks_of_length_one.contains(lexeme_name)) {
            this.LexemePossibilities.put(lexeme_name, indices);
        } **/
    }

    public String getFinalRemainingPossibilityName () {
        for (Map.Entry<String, int[]> entry : this.LexemePossibilities.entrySet()) {
            this.f_possibility_name = entry.getKey();
        } return this.f_possibility_name; 
    }

    public int[] getFinalRemainingPossibilityIndices () {
        for (Map.Entry<String, int[]> entry : this.LexemePossibilities.entrySet()) {
           this.f_possibility_indices= entry.getValue();
        } return this.f_possibility_indices;
    }


    public void printShortTokenSummary (boolean is_verbose, ArrayList<Token> ts) {
        if (is_verbose) System.out.println("(?) Current Token Name: " + this.getName() + " / Amount of Lexeme Possibilities: " + (this.getPossibilities()).size() + " / Current Window Size: " + (this.getEndPos() - this.getStartPos()) + "/ Token Stream Length:" + ts.size());
    }

    public void printRemainingPossibilities(Token token, boolean is_verbose) {
        //Map<String, int[]> remaining = new

        for (Map.Entry<String, int[]> entry : this.LexemePossibilities.entrySet()) {
            String name = entry.getKey();
            int[] i = entry.getValue();
            if (is_verbose) System.out.println("Name: " + name + " Indices: " + i + " Length of match: " + (i[1] - i[0]));    

        }
    }


    public void processLongestMatch () {

        int count_of_remaining_possibilies_completely_unmatched = 0;
        ArrayList<String> matches_of_same_length = new ArrayList<String>();

        for (Map.Entry<String, int[]> entry : this.LexemePossibilities.entrySet()) {
            String name = entry.getKey();
            int[] i = entry.getValue();
            int difference = i[1] - i[0];
            
            System.out.println("Keyword: " + name + " Indices: " + Arrays.toString(i) + " Length of match: " + difference);    
            
            if ( ( i[0] == - 1 ) && ( i[1] == -1) ) count_of_remaining_possibilies_completely_unmatched = count_of_remaining_possibilies_completely_unmatched + 1; 

            if (difference == this.longest_match_length && this.longest_match_length > 0) {
                matches_of_same_length.add(name);
            }

            // Excluding keyword should work for all cases
            if (difference > this.longest_match_length) {
                if (!name.contains("KEYWORD")) { // Because partial matches do not count
                    this.thereExistsSharedLongestMatch = false; 
                    this.thereExistsUniqueLongestMatch = true; 
                    this.longest_match_length = difference; // Update longest match length
                    this.longest_match_name = name; // Update name of longest match
                    matches_of_same_length = new ArrayList<String>(); // Reset similarities because new longest found
                }
            }

            //if (diff)
        }
        // Will only be greater than zero if there is not a unique longest match
        if (matches_of_same_length.size() > 0) {
            this.thereExistsUniqueLongestMatch = false;
            this.thereExistsSharedLongestMatch = true;
            this.shared_longest_match_array = matches_of_same_length; 
        }

        if (count_of_remaining_possibilies_completely_unmatched == this.getLexemePossibilities().size()) {
            System.out.println("Entirely unmatching == same size");
            setRemainingPossibilitiesAreImpossible(true);
        }

    }


    public void printRemainingPossibilities(boolean with_indices, boolean is_verbose) {
        if (is_verbose) System.out.println("\n(...) Remaining Probabilities");
        for (Map.Entry<String, int[]> entry : (this.LexemePossibilities).entrySet()) {
            if (with_indices && is_verbose) System.out.println(entry.getKey() + ", " + Arrays.toString(entry.getValue()));
            else if (is_verbose) System.out.println(entry.getKey());
        }
    }

    

   


    // Tired.... I apologize to any future employer who sees this abomination
    // Needs to be only used in the event that there is one and only one possibility remaining=
    public void updateTokenWithRemainingNameAndIndices () {
        for (Map.Entry<String, int[]> entry : (this.getPossibilities()).entrySet()) {
            this.setName(entry.getKey());
            this.setStartPos(entry.getValue()[0]);
            this.setEndPos(entry.getValue()[1]);
            break;
        }
    }


    public void removePossibilitiesOfSpecifiedType(String type, boolean remove_all_but) {

        String type_uppercase = type.toUpperCase();
        Map<String, int[]> token_lexeme_possibilities_local = new HashMap<>(this.getPossibilities());
        
        // Remove all but specified type
        if ( remove_all_but ) {
            for (Map.Entry<String, int[]> entry : (token_lexeme_possibilities_local).entrySet()) {
                String possibility_name = entry.getKey();
                if (!possibility_name.contains(type_uppercase)) this.removePossiblity(possibility_name);
            }
        } else {
            for (Map.Entry<String, int[]> entry : (token_lexeme_possibilities_local).entrySet()) {
                String possibility_name = entry.getKey();
                if (possibility_name.contains(type_uppercase)) this.removePossiblity(possibility_name);
            }
        }
        
    }
    


}
