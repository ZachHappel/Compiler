import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Toolkit {
    public Boolean is_verbose;
    public int[] indices;

    public Toolkit() {
        this.is_verbose = true; 
    }

    public Toolkit( Boolean is_verbose ) {
        this.is_verbose = is_verbose; 
    }

    public void setIsVerbose( Boolean is_verbose ) {
        this.is_verbose = is_verbose;
    }

    public boolean getIsVerbose() {
        return this.is_verbose;
    }

    public void printTokenStream (ArrayList<Token> token_stream) {
        for (Token t : token_stream) {
            System.out.println("Token: " + t.getName() + " Value/Attribute: " + t.getAttribute() + ", Indices: " + Arrays.toString(t.getIndices()) + ", Start Pos: " + t.getStartPos() + ", End Pos: " + t.getEndPos());
        }
    }

    public void setIndices (int[] indices) {
        this.indices = indices; 
    }

    public boolean hashmapEqualityExists (Map<String, int[]> h1, Map<String, int[]> h2) {

        boolean areEqual = true;
        
        if (h1.size() != h2.size()) {
            areEqual = false; 
        } else {    
            for (Map.Entry<String, int[]> entry: h1.entrySet()) {
                String name = entry.getKey();
                int[] indices = entry.getValue();
                if (!h2.containsKey(name) || !h2.get(name).equals(indices)) {
                    areEqual = false;
                    break;
                }
            }
        }
        return areEqual;

    }

    //public boolean countComplimentingCommentSymbols(ArrayList<Token> token_stream) {}
 
    public boolean isWithinComment (ArrayList<Token> token_stream) {
        return (countCommentSymbols(token_stream) % 2 == 0 ? false : true);
    }
    
    // I am not a fan, but this works because we do not allow in our language something like this: /*  */ */
    // Instead it has to be like this: /*  */ /* */
    public int countCommentSymbols (ArrayList<Token> token_stream) {
        int count = 0;
        for (int i = 0; i <= token_stream.size() - 1; i ++) { 
            String tk_name = (token_stream.get(i)).getName(); 
            if ( tk_name == "SYMBOL_OPENCOMMENT" || tk_name == "SYMBOL_CLOSECOMMENT") {
                System.out.println("Comment Token: " + tk_name);
                count = count + 1;
            }
        }
        System.out.println("Count of Comment Symbols: " + count);
        return count;
    }

    public boolean isWithinStringExpression(ArrayList<Token> token_stream) {
        return (countStringBoundaryExpressions(token_stream) % 2 == 0 ? false : true);
        //int amount_exprs = countStringBoundaryExpressions(token_stream);
    }

    public int countStringBoundaryExpressions (ArrayList<Token> token_stream) {
        int count = 0;
        for (int i = 0; i <= token_stream.size() - 1; i ++) { 
            if ((token_stream.get(i).getName()) == "SYMBOL_STRINGEXPRBOUNDARY") {
                count = count + 1;
            }
        }
        return count;
    }

    public void output( String string ) {
        if (this.is_verbose) System.out.println(string);
    }

    public void debugoutput( String string ) {
        if (this.is_verbose) System.out.println(string);
    }

    public String bytearrNumbersAsString ( byte[] bytearr) {
        String val_string = "";
        for (int i = 0; i<= bytearr.length - 1; i++ ) {
            val_string = val_string + ((int) bytearr[i]) + ", "; 
        }
        return val_string;
    }


    public void endProgram(String reason) {
            this.output("Ending execution, " + reason);
            System.exit(0);
    }


    // From previous attempt at project
    public int CountOccurrencesOfByteDecimalValue (byte[] arr, int byte_decimal_value) {
        int occurrences = 0;
        for (int i = 0; i<= arr.length -1; i++) {
            int val = arr[i];
            boolean isDecVal = (val == byte_decimal_value);
            if (isDecVal) {
                occurrences = occurrences + 1;
            }
        }

        //System.out.println("Amount of Lines: " + line_amt);

        return occurrences; 
    }

    // From previous attempt at project
    public int CountLineFeeds (byte[] arr) {
        int amountOfLineFeeds = CountOccurrencesOfByteDecimalValue(arr, 10) + 1; // plus one because first line counts as 1
        return amountOfLineFeeds;
    }

    public String[] convertByteArrayToStringArray (byte[] byte_arr) {

        String[] string_array = (new String(byte_arr)).split(""); // sub_arr to string
        return string_array;
        //for (byte b : byte_arr) {

        //}

    }

    // From previous attempt at project
    public int[] GetIndicesOfLineFeeds (byte[] arr) {
        int line_feed_amt = CountLineFeeds(arr);
        int[] indices = new int[line_feed_amt];
        int amount_added = 0;
        
        for (int i = 0; i<= arr.length -1; i++) {
            int val = arr[i];
            boolean is_line_feed = (val == 10);
            if (is_line_feed) {
                indices[amount_added] = i;
                amount_added = amount_added + 1;  
            }
        }

        return indices;

    }

    public int getCurrentLine(int index) {
        // Toolkit.println("LEN" + indices.length);
        int line_number = 1;
        int i, index_at_newline;

        for (i = 0; i < indices.length; i++) {
            // Toolkit.println("Index: " + index + " Value at Indice: " + indices[i]);

            index_at_newline = indices[i];

            if (index < indices[i]) {
                return i;
            } // else if ( ) {
            else {
                if (i == indices.length - 1) {
                    return i + 1;
                }
            }
        }
        return i;
    }


    // From previous attempt at project, gross but it should work
    public byte[] removeSpecialCharacters(byte[] sub_arr) {
        //System.out.println("Sub Array Length: "+ sub_arr.length);
        int specialchar_count = 0;
        for (int i = 0; i <= sub_arr.length - 1; i++) {
            if ((sub_arr[i] == 13) || (sub_arr[i] == 10) || (sub_arr[i] == 11) || (sub_arr[i] == 32))
                specialchar_count++;
        }

        // Toolkit.println("A");
        int current_index = 0;
        byte[] modified_sub_arr = new byte[sub_arr.length - specialchar_count]; // create byte arr of new size
        for (int j = 0; j <= sub_arr.length - 1; j++) {
            // Toolkit.println("B");
            if ((sub_arr[j] == 13) || (sub_arr[j] == 10) || (sub_arr[j] == 11) || (sub_arr[j] == 32)) {
                // Toolkit.println("B1");
            } // do nothing if special char
            else {
                modified_sub_arr[current_index] = sub_arr[j]; // add char to new modified array
                current_index++;
                // Toolkit.println("B2");
            }
        }

        //System.out.println("Modified Sub Array Length: "+ modified_sub_arr.length); 
        return modified_sub_arr;
    }

    // lazy edit of the first gross one
    public byte[] removeSpecialCharactersExceptSpaces(byte[] sub_arr) {
        int specialchar_count = 0;
        for (int i = 0; i <= sub_arr.length - 1; i++) {
            if ((sub_arr[i] == 13) || (sub_arr[i] == 11) || (sub_arr[i] == 10))
                specialchar_count++;
        }

        // Toolkit.println("A");
        int current_index = 0;
        byte[] modified_sub_arr = new byte[sub_arr.length - specialchar_count]; // create byte arr of new size
        for (int j = 0; j <= sub_arr.length - 1; j++) {
            // Toolkit.println("B");
            if ((sub_arr[j] == 13) || (sub_arr[j] == 11) || (sub_arr[j] == 10)) {
                // Toolkit.println("B1");
            } // do nothing if special char
            else {
                modified_sub_arr[current_index] = sub_arr[j]; // add char to new modified array
                current_index++;
                // Toolkit.println("B2");
            }
        }

        return modified_sub_arr;
    }

}
