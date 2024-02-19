import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Toolkit {
    public Boolean is_verbose;
    public Boolean is_debug_mode;
    public int[] indices;

    public Toolkit() {
        this.is_verbose = true;
        this.is_debug_mode = false; 
    }

    public Toolkit( Boolean is_verbose, Boolean is_debug_mode ) { 
        this.is_verbose = is_verbose; 
        this.is_debug_mode = is_debug_mode;
    }

    

    
    public Map<Integer, String> debug_strings = new HashMap<Integer, String>(){{
        put(0, "DEBUG - Retrieved Indices - Token Start and End ");
        put(1, "DEBUG - Is Within Comment ");
        put(2, "DEBUG - Byte Arr Length >=2  ");
        put(3, "DEBUG - Byte Arr Length >=2 - Last Two are * /");
        put(4, "DEBUG - SET SYMBOL CLOSE COMMENT");
        put(5, "DEBUG - Get Name Is Not Null after IsOfKeyword And Remove Keyword Possibilities ");
        put(6, "DEBUG - Performed isOfSymbol");
        put(7, "DEBUG - Removed Impossibilities");
        put(8, "DEBUG - TOKEN NAME NOT NULL");
        put(9, "DEBUG - Performed isOfKeyword");
        put(10, "DEBUG - Window Byte Arr Length 1");
        put(11, "DEBUG - Window Byte Arr Length 1 - Assigned Byte");
        put(12, "DEBUG - Window Byte Arr Length 1 - Byte Is A-Z OR is Space and isWithinString");
        put(13, "DEBUG - Window Byte Arr Length 1 - Byte Is A-Z Or SPACE AND Byte Is Within String -> Character");
        put(14, "DEBUG - Window Byte Arr Length 1 - Byte Is Not Within String");
        put(15, "DEBUG - Window Byte Arr Length 1 - Byte Is Not Within String - No Keyword Semi Matches -> Identifier");
        put(16, "DEBUG - Window Byte Arr Length 1 - Byte Is Not Within String - Keyword Semi Matches Exist -> Could Be Identifier, Not A Character");
        put(17, "DEBUG - Window Byte Arr Length 1 - Not A-Z ");
        put(18, "DEBUG - Window Byte Arr Length 1 - Not A-Z but IS 0-9");
        put(19, "DEBUG - Window Byte Arr Length 1 - Not A-Z but IS 0-9 && Is Within String - ERROR");
        put(20, "DEBUG - Window Byte Arr Length 1 - Not A-Z but IS 0-9 && NOT Within String -> Digit");
        put(21, "DEBUG - Window Byte Arr Length 1 - Not A-Z - EOP! ");
        put(22, "DEBUG - Window Byte Arr Length 1 - Not A-Z - Outside Of IF/ELSE-IF For IF Digit or EOP");
        put(23, "DEBUG - Window Byte Arr Length 1 - Outside Of A-Z else Digit or EOP");
        put(24, "DEBUG - Byte Arr Window > 1");
        put(25, "DEBUG - First Token Already Made");
        put(26, "DEBUG - Window Expanded");
        put(27, "DEBUG - Just Performed Checks and Checked To See If Token Has Name");
        put(28, "DEBUG - Just Set Line Numbers");
        put(29, "DEBUG - Has Match && Source Has Been Exhausted");
        put(30, "DEBUG -  No Match && Source Still Remains");
        put(31, "DEBUG - No Match && Source Exhausted");
        put(32, "DEBUG - No Match && Source Exhausted && Last Possibilities Are Impossible");
        put(33, "DEBUG - No Match && Source Exhausted && Possibilities ARE Possible");
        put(34, "DEBUG - No Match && Source Exhausted && Last Possibilities Are Impossible");
        put(35, "DEBUG - No Match && Source Exhausted && Possibilities ARE Possible BUT full match indices are less than length of source file");
        put(36, "DEBUG - No Match && Source Exhausted && Possibilities ARE Possible && Longest Full Match Indices are NOT longer than source file --- (?)");
        put(37, "DEBUG - No Match && Source Exhausted && Outside of IF/ELSE");
        put(38, "DEBUG - Outside of IF/ELSE Has Match");
        put(39, "DEBUG - TOKEN NAME NOT NULL");
        put(40, "DEBUG - Has Match && Source Still Remains");
    }};
    
    public void debug(int key) { 
        if (this.is_debug_mode) {
            System.out.println(debug_strings.get(key));
        }
    }

    public boolean getIsVerbose() { return this.is_verbose; }
    public boolean getIsDebugMode() { return this.is_debug_mode; }
    
    public void setIsVerbose( Boolean is_verbose ) { this.is_verbose = is_verbose; }
    public void setIsDebugMode( Boolean is_debug_mode ) { this.is_debug_mode = is_debug_mode; }

    public String createStringOfSpaces(int how_many_spaces) {
        String string_of_spaces = "";
        if (how_many_spaces == 0) return "";
        for (int i = 0; i <= how_many_spaces -1; i++) {
            string_of_spaces = string_of_spaces + "░";
        }
        return string_of_spaces;
    }

    public String createStringOfActualSpaces(int how_many_spaces) {
        String string_of_spaces = "";
        if (how_many_spaces == 0) return "";
        for (int i = 0; i <= how_many_spaces -1; i++) {
            string_of_spaces = string_of_spaces + " ";
        }
        return string_of_spaces;
    }

    public void printTokenStream (ArrayList<Token> token_stream) {

        for (Token t : token_stream) {           
            if (this.is_verbose) System.out.println(t.getName() + "| Value/Attribute: \"" + t.getAttribute() + "\", Indices: " + Arrays.toString(t.getIndices()) + ", Start Pos: " + t.getStartPos() + ", End Pos: " + t.getEndPos());
        }
        //System.out.println(s.length());
    }

    public void printTokenStreamDetailed (ArrayList<Token> token_stream) {
        System.out.println("\n┌--------------------------------------------------------------------------------------------------------------------┐");
        System.out.println("|--------------------------------------------------Token Stream------------------------------------------------------|");
        System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
        for (Token t : token_stream) {
            String token_name = t.getName();
            int token_name_length = token_name.length();
            int amount_of_extra_spaces = 25 - token_name_length ;
            // 
            String additional_spaces = createStringOfSpaces(amount_of_extra_spaces);
            String indices_portion = "Indices:░" + Arrays.toString(t.getIndices()) + ",░Start Pos:░" + t.getStartPos() + "░░End Pos:░" + t.getEndPos(); 
            System.out.println("|░"+t.getName() + createStringOfSpaces(25 - token_name.length()) + "░|░░░Value/Attribute:░░░\"" + t.getAttribute()  + "\"" + createStringOfSpaces(30 - 21 - (t.getAttribute().length())) +  "|░" + indices_portion + createStringOfSpaces(52 - indices_portion.length()) + "░|");
        }
        System.out.println("|--------------------------------------------------------------------------------------------------------------------|");
        System.out.println("└--------------------------------------------------------------------------------------------------------------------┘");
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
                count = count + 1;
            }
        } //System.out.println("Count of Comment Symbols: " + count);
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
        // In use, line end number is representative of the current position of the lexeme whereas start position is not -- due to how bytearr matching is conducted
         
        int i, index_at_newline;

        for (i = 0; i < indices.length; i++) {
            // Toolkit.println("Index: " + index + " Value at Indice: " + indices[i]);

            index_at_newline = indices[i];

            if (index < indices[i]) {
                return i + 1;
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
            if ((sub_arr[i] == 13) || (sub_arr[i] == 10) || (sub_arr[i] == 11) || (sub_arr[i] == 32) || (sub_arr[i] == 9))
                specialchar_count++;
        }

        // Toolkit.println("A");
        int current_index = 0;
        byte[] modified_sub_arr = new byte[sub_arr.length - specialchar_count]; // create byte arr of new size
        for (int j = 0; j <= sub_arr.length - 1; j++) {
            // Toolkit.println("B");
            if ((sub_arr[j] == 13) || (sub_arr[j] == 10) || (sub_arr[j] == 11) || (sub_arr[j] == 32) || (sub_arr[j] == 9)) {
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
            if ((sub_arr[i] == 13) || (sub_arr[i] == 11) || (sub_arr[i] == 10) || (sub_arr[i] == 9))
                specialchar_count++;
        }

        // Toolkit.println("A");
        int current_index = 0;
        byte[] modified_sub_arr = new byte[sub_arr.length - specialchar_count]; // create byte arr of new size
        for (int j = 0; j <= sub_arr.length - 1; j++) {
            // Toolkit.println("B");
            if ((sub_arr[j] == 13) || (sub_arr[j] == 11) || (sub_arr[j] == 10) || (sub_arr[j] == 9)) {
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

    public String cleanIdentifierAttributeString (byte[] src, int start, int end) {
        String cleaned_string = "";
        for (int i = start; i <= end - 1; i++) {
            byte b = src[i];
            if (b >= 97 || b < 123) cleaned_string = Character.toString((char) b); 
        } return cleaned_string; 
    }


    public void printRemainingBytes(byte[] src, int index) {
        for (int i = 0; i <= src.length - 1; i++) {
            byte b = src[i];
            if (i >= index) {
                if (this.is_verbose) System.out.print(b + ", ");
            }
           
        } 
    }

    public ArrayList<Token> removeCommentTokens (ArrayList<Token> ts) {
        ArrayList<Token> ts_local = new ArrayList<Token>(ts);
        for (Token t: ts_local) {
            if ( (t.getName()).contains("COMMENT") ) {
                ts.remove(t);
            }
        }
        return ts;
    }

    public void printByteArrayLimitLineLength(byte[] src, int line_length) {

        String current_line = "[";
        for (int i = 0; i <= src.length - 1; i ++) {
            byte b = src[i];
            int current_line_length = current_line.length(); 
            String byte_string = b + "";
            //String b_string = new String(Arrays.toString(new byte[]{b}));
            
            String formatted_byte_string = (i == src.length - 1) ? byte_string + " ]" : byte_string + ", ";
            //System.out.println("b_string");
            //System.out.println(b_string);
           
            
            if ( (current_line_length + formatted_byte_string.length() < line_length)) {
                current_line = current_line + formatted_byte_string; 
                
                if (i == src.length - 1) {
                    System.out.println(current_line.substring(0, current_line.length() - 1) + createStringOfActualSpaces(line_length - current_line.length() - 1 ) + "]");
                }
            } else {
                System.out.println(current_line);
                current_line = " "; 
                current_line = current_line + formatted_byte_string;
            }
        }

    }


    public void generateProgramOverview (byte[] src, int program_index) {
        //System.out.println("\n----------------------------\n|-----Compiling Program----|\n----------------------------\n| Program " + program_index + "\n| Length: " + src.length );
        System.out.println("" + //
        "\n----------------------------------------------------------------------------------------------------------------------\n" +//
        "----------------------------------------------------------------------------------------------------------------------\n" + //
        "------------------------------------------------------ PROGRAM " + (program_index + 1) + " -----------------------------------------------------\n" + //
        "----------------------------------------------------------------------------------------------------------------------\n\nOVERVIEW");
        System.out.println("\nByte Representation:" );
        printByteArrayLimitLineLength(src, 80);
        //System.out.println("\nByte Represenation: \n" + Arrays.toString(src));
        System.out.println("\nCharacter Representation:\n" + new String(src)+ "\n\n------------------");
    }

    public ArrayList<byte[]> subdivideSourceIntoPrograms (byte[] src) {
        int program_amount = 0;
        ArrayList<String> program_info_strings = new ArrayList<String>();
        ArrayList<byte[]> programs = new ArrayList<>();
        int curr_program_start = 0;
        int curr_program_end = 0;
        for (int i = 0; i <= src.length - 1; i++ ) {
            byte b = src[i];
            if (b == 36) {
                byte[] new_program = Arrays.copyOfRange(src, curr_program_start, i+1);
                program_info_strings.add(" Indices: [" + curr_program_start + ", " + i+1 + "]");
                program_amount++;
                curr_program_end = i+1;
                curr_program_start = curr_program_end;
                programs.add(new_program);
            }
        }
        //System.out.println("End of program found: " + b + " Index of: " + i + " | Indices: [" + curr_program_start + ", " + i+1 + "]" );

        int count = 1;
        for (byte[] p : programs) {
            //System.out.println("\n----------------------------\nProgram " + count + ") Length: " + p.length);
            String all_bytes = "";
            String all_bytes_chars = "";
            for (byte b : p) {
                all_bytes = all_bytes + b;
                all_bytes_chars = all_bytes_chars + (char) b;
            }
            //System.out.println("All Bytes: " + all_bytes);
            //System.out.println("All Bytes Chars: " + all_bytes_chars);
            
            count++;
            //System.out.println("---------------------------");
            //System.out.println
        }

        System.out.println("Programs:");
        
        for (int j = 0; j <= program_info_strings.size() - 1; j++) System.out.println( "[" + (j+1) + "] " + program_info_strings.get(j));
        //System.out.println("\n(Total: " + program_amount + ")\n");
        return programs;
    }


}
