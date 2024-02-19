import java.io.FileInputStream;
import java.io.InputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;




// linefeed = 10, carriage return = 13, tab = 11
// space = 10
public class LexicalAnalysis {

    public static int[] indices; 
    public static Toolkit toolkit;

    public static ArrayList<String> ungrouped_tks_of_length_one = new ArrayList<>(){{
        add("IDENTIFIER");
        add("CHARACTER");
        add("DIGIT");
        add("EOP");
    }};
   
    public int getCurrentLine(int index) {
        int i;
        for (i = 0; i < indices.length; i++) {
            if (index < indices[i]) return i;
            else if (i == indices.length - 1) return i + 1; 
        }
        return i;
    }

    public static byte[] getSourceFileData(String filepath) {
        
        try {
            Path path = Paths.get(filepath);
            long file_size_in_bytes = Files.size(path);
            InputStream input = new FileInputStream(filepath);
            byte[] source_data_bytearr = new byte[Math.toIntExact(file_size_in_bytes)]; 
            input.read(source_data_bytearr); // Read byte from input stream
            
            String source_data_string = new String(source_data_bytearr);
            //char[] source_data_chararr = source_data_string.toCharArray();
            
            String file_info = "|| File Size: " + file_size_in_bytes + " bytes || Remaining Space: " + input.available()
            + " bytes";
            toolkit.output(file_info);

            input.close();

            return source_data_bytearr; 

        } catch (IOException io_err) {
            System.out.println("IOException encountered when reading source file into data\n Outputting stacktrace and halting further execution.");
            io_err.printStackTrace();
            return new byte[0];
        }
        
    }

    // Need check for source of length 0
    public static ArrayList<Token> generateTokensOld (byte[] source, ArrayList<Token> token_stream) {
        


        System.out.println("Generating Tokens");
        Token new_token; 

        if (token_stream.size() == 0) {
            System.out.println("Empty Token Stream");
            new_token = new Token(0, 1);
        } else {
            int previous_end_pos = (token_stream.get(token_stream.size() - 1)).getEndPos();
            new_token = new Token(previous_end_pos , previous_end_pos + 1);
        }

        byte[] window_bytearr = Arrays.copyOfRange(source, new_token.getStartPos(), new_token.getEndPos());
        String[] window_stringarr = toolkit.convertByteArrayToStringArray(window_bytearr);
        String window = new String(window_bytearr);
        System.out.println("Current Window: " + window);
        //String[] current_string_subarray = toolkit.convertByteArrayToStringArray() 
        

        return new ArrayList<Token>(); 
    }


    public static ArrayList<Token> performChecksTest (byte[] src, ArrayList<Token> token_stream) {
        Token test = new Token(0, 0);
        test.setName("Hello");
        test.setEndPos(1);
        test.setAttribute(new String(Arrays.copyOfRange(src, test.getStartPos(), test.getEndPos())));
        token_stream.add(test);

        return token_stream; 
    }


    public static byte[][] keywords = new byte[][] {
        "int".getBytes(), 
        "string".getBytes(), 
        "boolean".getBytes(),
        "print".getBytes(),
        "while".getBytes(),
        "true".getBytes(), 
        "false".getBytes(),
        "if".getBytes(),
    };

    public static Map<String, int[]> isOfKeyword (byte[] window_bytearr, Token token) {
        toolkit.output("\n(!) IS OF KEYWORD?");
        Map<String, int[]> current_keyword_matches = new HashMap<String, int[]>() {{}};
        //ArrayList<String> current_keyword_matches = new ArrayList<String>();
        
        int[] indices = new int[]{token.getStartPos(), token.getEndPos()};
        int window_bytearr_size = window_bytearr.length; 
        int amount_of_keywords = keywords.length; 
        toolkit.output("(...) Keyword Bytes vs Window Bytes: ");
        
        for (int j = 0; j <= amount_of_keywords - 1; j++ ) {
            byte[] keyword = keywords[j];
            
            for (int k = 0; k <= keyword.length - 1 && k <= window_bytearr.length - 1; k++) {
                byte byte_from_keyword = keyword[k];
                byte byte_from_window =  window_bytearr[k];
                String equality = (byte_from_keyword == byte_from_window) ? "==" : "!=";
                //toolkit.output("Symbol Byte (" + byte_from_keyword + ") " + equality + " Window Byte (" + byte_from_window + ")");
                
                if (byte_from_keyword != byte_from_window) {
                    break;
                }

                
                
                if (k == window_bytearr_size - 1 ) {
                    // Valid thus far, and as such we add it to the HashMap 
                    String type_lowercase = new String (keywords[j]); // Get String version of keyword that is split in bytearray (the arrays within the array)
                    String keyword_token_name = "KEYWORD_" + type_lowercase.toUpperCase(); // Combine with "KEYWORD_" to create proper token name
                    
                    toolkit.output("> Keyword Semi-match: " + keyword_token_name + ", indices: " + Arrays.toString(indices) + ", matching: " + new String(window_bytearr));
                    current_keyword_matches.put(keyword_token_name, indices);
                    token.updatePossibility(keyword_token_name, indices); // JUST ADDED BEFORE BED

                    if (type_lowercase.equals(new String (window_bytearr))) {
                        toolkit.output("Keyword Full-match: " + keyword_token_name + ", indices: " + Arrays.toString(indices));
                        token.setName(keyword_token_name);
                        token.setAttribute(new String (window_bytearr)); // Set value/attribute to String representation of byte window
                        //NEED - Line Number
                        //Token in stream successfully updated and ready for new token to be added. 
                        //This will happen when performChecks returns token_stream to generateTokens which checks if name on most
                        //recent token has been set

                        // Can return now because in no case will a full keyword match interfere with another keyword
                        return current_keyword_matches;
                    }

                    
                }
                    
            }
        }
    
        return current_keyword_matches; 
        // PerformChecks need to remove, from Possibilities, all keywords that are not retured from here as being semi-matches.
        // If it fails here on X keyword, it is not X keyword
    }


    

    


    // Take list of keyword (semi)matches and remove all but those keywords from the token's Lexeme Possibilities 
    public static void removeKeywordImpossibilities( Map<String, int[]> keyword_matches, Token token ) {
        boolean any_removed = false;
        System.out.println("\n(!) REMOVE KEYWORD IMPOSSIBILITIES");
        int keyword_matches_amount = keyword_matches.size(); 
        System.out.println("(?) Keyword Semi Matches: " + keyword_matches.size());
        token.setKeywordSemiMatchesAmount(keyword_matches_amount);
        // REASON: This should resolve ConcurrentModificationException that was occurring when access and modifying the token's Lexeme Possibilities HashMap
        // Use this as a reference and make removals on the actual token's hashmap
        Map<String, int[]> token_lexeme_possibilities_local = new HashMap<>(token.getPossibilities());

        ArrayList<String> keyword_match_names = new ArrayList<String>(); 
        
        // Add all matching names to ArrayList
        for (Map.Entry<String, int[]> entry : keyword_matches.entrySet()) { keyword_match_names.add(entry.getKey());}
        
        System.out.print("(---) Removed KEYWORD Possibilities: " );
        for (Map.Entry<String, int[]> entry : (token_lexeme_possibilities_local).entrySet()) {
            String possibility_name = entry.getKey();
            
            // If possibility is of kind "KEYWORD"
            // but is not among those which have been identified as being semi-matches
            if (possibility_name.contains("KEYWORD_") && !keyword_match_names.contains(possibility_name)) {
                any_removed = true;
                System.out.print(possibility_name + ", ");  // Print what is being removed
                token.removePossiblity(possibility_name);
                keyword_matches_amount = keyword_matches_amount - 1;
            } 
            

        }

        if (!any_removed) System.out.println("[...none...]");
        //System.out.println("\nRemaining Keyword Semi Matches: " + keyword_matches_amount);

    }


    public static byte[][] symbols = new byte[][] {
        "=".getBytes(), 
        "!=".getBytes(), 
        "==".getBytes(),
        "{".getBytes(),
        "}".getBytes(),
        "(".getBytes(),
        ")".getBytes(),
        "\"".getBytes(), 
        "/*".getBytes(),
        "*/".getBytes(),
        "+".getBytes(),
    };

    public static Map<String, String> symbols_name_map = new HashMap<String, String >() {{
        put("=", "SYMBOL_ASSIGNMENT");
        put("!=", "SYMBOL_INEQUIVALENCE");
        put("==", "SYMBOL_EQUIVALENCE");
        put("{", "SYMBOL_OPENBLOCK");
        put("}", "SYMBOL_CLOSEBLOCK");
        put("(", "SYMBOL_OPENPAREN");
        put(")", "SYMBOL_CLOSEPAREN");
        put("\"", "SYMBOL_STRINGEXPRBOUNDARY");
        put("/*", "SYMBOL_OPENCOMMENT");
        put("*/", "SYMBOL_CLOSECOMMENT");
        put("+", "SYMBOL_INTOP");
        
    }};

    
    public static Map<String, int[]> isOfSymbol (byte[] window_bytearr, Token token) {
        toolkit.output("\n(!) IS OF SYMBOL?");
        boolean partial_match = false;
        Map<String, int[]> current_symbol_matches = new HashMap<String, int[]>() {{}};
        //ArrayList<String> current_keyword_matches = new ArrayList<String>();
        
        int[] indices = new int[]{token.getStartPos(), token.getEndPos()};
        int window_bytearr_size = window_bytearr.length; 
        int amount_of_symbols = symbols.length; 

        // Need to add debug option for long debug
        //toolkit.output("(...) Symbol Bytes vs Window Bytes: (if bytes are not equal, the loop breaks, attempts next symbol if available)");
        
        for (int j = 0; j <= amount_of_symbols - 1; j++ ) {
            byte[] symbol = symbols[j];
            
            for (int s = 0; s <= symbol.length - 1 && s <= window_bytearr.length - 1; s++) {
                byte byte_from_symbol = symbol[s];
                byte byte_from_window =  window_bytearr[s];
                String equality = (byte_from_symbol == byte_from_window) ? "==" : "!=";
                //toolkit.output("Symbol Byte (" + byte_from_symbol + ") " + equality + " Window Byte (" + byte_from_window + ")");
                if (byte_from_symbol != byte_from_window) {
                    break;
                }
                
                // End of current window, either full match now or furher matching required
                if (s == window_bytearr_size - 1 ) {
                    // Valid thus far, and as such we add it to the HashMap 
                    String symbol_pattern = new String (symbols[j]); // Get String version of symbol that is split in bytearray (the arrays within the array)
                    String symbol_token_name = symbols_name_map.get(symbol_pattern);
                    
                    //symbols_name_map. // Combine with "SYMBOL_" to create proper token name
                    
                    toolkit.output("Symbol Semi-match: " + symbol_token_name + ", indices: " + Arrays.toString(indices) + ", matching: " + new String(window_bytearr));
                    current_symbol_matches.put(symbol_token_name, indices);
                    token.updatePossibility(symbol_token_name, indices); 
                    partial_match = true; 

                    // If symbol pattern fully matches, for each byte, the window_bytearr
                    if (symbol_pattern.equals(new String (window_bytearr))) {

                        // If previous pass with this token determined that assignment lexeme is possible
                        if (token.getHasMatchedAssignment()) {
                            System.out.println("Token Has Matched Assignment");
                        }

                        // If the pattern is "=" and no previous pass has determined that the assignment lexeme is possible
                        // This time, update has_matched_assignment with true, update the possibility so indices of SYMBOL_ASSIGNMENT possibility are accurate, and put the possibility in current_symbol_matches
                        if (symbol_pattern.equals("=") && !token.getHasMatchedAssignment()) {
                            System.out.println("Valid SYMBOL_ASSIGNMENT");
                            token.setHasMatchedAssignment(true);
                            token.setMatchedAssignmentValue(new String (window_bytearr));
                            token.updatePossibility(symbol_token_name, indices); 
                            current_symbol_matches.put(symbol_token_name, indices);
                         //   break; // Not return, because we need the remaining symbol pattern checks to happen still
                        } else {
                            toolkit.output("Symbol Full-match: " + symbol_token_name + ", indices: " + Arrays.toString(indices));
                            token.setName(symbol_token_name);
                            token.setAttribute(new String (window_bytearr)); // Set value/attribute to String representation of byte window
                            token.setStartPos(indices[0]);
                            token.setEndPos(indices[1]);
                        }

                        
                        
                        //NEED - Line Number
                        //Token in stream successfully updated and ready for new token to be added. 
                        //This will happen when performChecks returns token_stream to generateTokens which checks if name on most
                        //recent token has been set
                        
                        // Can return now because in no case will a full keyword match interfere with another keyword
                        return current_symbol_matches;
                    }

                    
                }
                    
            }


            if (token.getHasMatchedAssignment() && current_symbol_matches.size() == 0) {
                toolkit.output("Previously, potential match for SYMBOL_ASSIGNMENT was noted. Now, there are no more remaining symbol matches so we can therefore conclude that the current token is SYMBOL_ASSIGNMENT");
                int[] assignment_indices = token.getPossibilities().get("SYMBOL_ASSIGNMENT");
                //System.out.println("Assignment Indices: " + Arrays.toString(assignment_indices));

                token.setName("SYMBOL_ASSIGNMENT");
                token.setStartPos(assignment_indices[0]);
                token.setEndPos(assignment_indices[1]);
                token.setAttribute(token.getMatchedAssignmentValue());
            }
        }

        if (partial_match) token.removePossibilitiesOfSpecifiedType("SYMBOL", true);
    
        return current_symbol_matches; 
    }


    public static void removeAllSymbolsFromPossibilities(Token token ) {
        Map<String, int[]> token_lexeme_possibilities_local = new HashMap<>(token.getPossibilities());
        for (Map.Entry<String, int[]> entry : (token_lexeme_possibilities_local).entrySet()) {
            String possibility_name = entry.getKey();
            if (possibility_name.contains("SYMBOL")) token.removePossiblity(possibility_name);
        }
    }

    
 
    public static void removeSymbolImpossibilities( Map<String, int[]> symbol_matches, Token token ) {
        toolkit.output("\n(!) REMOVE SYMBOL IMPOSSIBILITIES");
        int symbol_matches_amount = symbol_matches.size(); 
        toolkit.output("Symbol Semi Matches: " + symbol_matches.size());
        token.setSymbolSemiMatchesAmount(symbol_matches.size());
        
        // REASON: This should resolve ConcurrentModificationException that was occurring when access and modifying the token's Lexeme Possibilities HashMap
        // Use this as a reference and make removals on the actual token's hashmap
        Map<String, int[]> token_lexeme_possibilities_local = new HashMap<>(token.getPossibilities());

        ArrayList<String> symbol_match_names = new ArrayList<String>(); 
        
        for (Map.Entry<String, int[]> entry : symbol_matches.entrySet()) { symbol_match_names.add(entry.getKey());}  // Add all matching names to ArrayList
        
        for (Map.Entry<String, int[]> entry : (token_lexeme_possibilities_local).entrySet()) {
            String possibility_name = entry.getKey();
            
            // If possibility is of kind "SYMBOL" but is not among those which have been identified as being semi-matches
            if (possibility_name.contains("SYMBOL_") && !symbol_match_names.contains(possibility_name)) {
                toolkit.output("Removing SYMBOL Possibility: " + possibility_name); 
                token.removePossiblity(possibility_name);
                symbol_matches_amount = symbol_matches_amount - 1;
            }
        }

        toolkit.output("Remaining Symbol Semi Matches: " + symbol_matches_amount);

    }
    

    // IS OF KEYWORDS FUNCT
    // USES MATRIX WITH KEYWORDS CONVERTED TO BYTES
    // RETURN KEYWORDS IT IS OF "IF, PRINT, WHILE" etc
    // FOR KEYWORDS IT IS OF, UPDATE INDICES... FOR K: KEYWORDSAPARTOF -> Update tokenpossibilities(k name, tk indices)
    public static ArrayList<Token> performChecks (byte[] src, ArrayList<Token> token_stream) {
        System.out.println("\n(!) PERFORM CHECKS");

        Token token = token_stream.get(token_stream.size() - 1);
        boolean isWithinString = toolkit.isWithinStringExpression(token_stream);
        boolean isWithinComment = toolkit.isWithinComment(token_stream);
        
        System.out.println("(?) isWithinString: " + isWithinString);
        System.out.println("(?) isWithinComment: " + isWithinComment);
        
        // If not in string, we want all special characters removed
        // If it IS within a string, we want all special characters EXCEPT spaces removed
        // window_bytearr is a copy of the byte[] src from the current token's start_pos to end_pos 

        /*

        This means that no matter what lay between pattern-matching byte chars, only the pattern-critical chars will be considered when testing for matches.
        Additionally, when the pattern is matched and a token match is delcared, via assigning the originally "null" token with a name and attribute (the pattern which was matched,)
        the indices of the token still refer directly to the breadth of search within the byte[] src, thus allowing lex to proceed immediately beyond the last considered index.

        */

        /*
        
        window_bytearr:
        A byte array used in the implementation of the expanding window concept. 
        Made via copy of the src array, using the most-recent token's indices as its own
        Using predetermined factors relevant to understanding the position of the window within the source code, isWithinString and isWithinComment, particular bytes can be stripped from the byte array before the value is assigned
        
        removeSpecialCharactersExceptSpaces: removes special characters of byte value 13, 11, and 10 
        This is used for within strings

        removeSpecialCharacters: removes special characters of byte value 13, 11, 10, and 32 (space)
        This is used for within comments


        */

       
        System.out.println("\n");



        byte[] window_bytearr = isWithinString ? toolkit.removeSpecialCharactersExceptSpaces(Arrays.copyOfRange(src, token.getStartPos(), token.getEndPos())) : toolkit.removeSpecialCharacters(Arrays.copyOfRange(src, token.getStartPos(), token.getEndPos()));
        System.out.println("(?) window_bytearr: " + new String(window_bytearr)); 
        System.out.println("(?) window_bytearr_vals: " + toolkit.bytearrNumbersAsString(window_bytearr)); 

        int[] indices = new int[]{token.getStartPos(), token.getEndPos()}; toolkit.debug(0);
        toolkit.debug(0);
        // Modifications made to token LexemePossibilities via updatePossibility when there is a partial match or a full match
        
        byte first_byte = src[token.getStartPos()];
        if ( (isWithinString) && !( (first_byte >= 97 && first_byte < 123 ) || (first_byte == 32 ) || (first_byte == 34) ) ) {
            // Purpose of this is to prevent multi-line string expressions. 
            // It wasn't until the lexer was completed that I realized that new-line, tabs, etc. should be error-invoking, whereas I just ignored those bytes
            System.out.println("Encountered invalid character within String\n Error occurred with byte, " + first_byte);
            System.exit(0); // Error handling needed
        }


        // if window != [...,...,..., *, /] -> ignore  =>  return token_stream
        if (isWithinComment) {
            toolkit.debug(1);
            if (window_bytearr.length >= 2) {
                toolkit.debug(2);
                if ( (window_bytearr[window_bytearr.length - 2 ] == 42) && (window_bytearr[window_bytearr.length - 1 ] == 47) ) { // Close commment symbol
                    toolkit.debug(3);
                    token.setStartPos(token.getEndPos() - 1);
                    
                    token.setName("SYMBOL_CLOSECOMMENT");
                    token.setAttribute("*/"); // Instead of having to concoct some ridiculous subarray converted to string, or something of the sort, just putting in /* is much easier and should not cause any trouble
                    toolkit.debug(4);
                    return token_stream;
                }
            }
            
            
            toolkit.output("SYMBOL_ENDCOMMENT not matched, returning token_stream");
            return token_stream;
        }
        
        
        Map<String, int[]> keyword_matches = isOfKeyword(window_bytearr, token); toolkit.debug(9);
        
        toolkit.output("\n(...) Keyword Matches: "); 
        for (Map.Entry<String, int[]> entry : keyword_matches.entrySet()) { String name = entry.getKey(); int[] i = entry.getValue(); System.out.println("Keyword: " + name + " Indices: " + Arrays.toString(i) + " Length of match: " + (i[1] - i[0]) + ", matching: " + new String(window_bytearr));   }
        removeKeywordImpossibilities(keyword_matches, token);
        if (token.getName() != null) {
            toolkit.debug(5);
            return token_stream;
        }

        Map<String, int[]> symbol_matches = isOfSymbol(window_bytearr, token);
        toolkit.debug(6);

        toolkit.output("\n(...) Symbol Matches: "); 
        for (Map.Entry<String, int[]> entry : symbol_matches.entrySet()) { String name = entry.getKey(); int[] i = entry.getValue(); System.out.println("Symbol: " + name + " Indices: " + Arrays.toString(i) + " Length of match: " + (i[1] - i[0]) + ", matching: " + new String(window_bytearr));   }
        removeSymbolImpossibilities(symbol_matches, token);
        toolkit.debug(7);

        if (token.getName() != null) {
            toolkit.debug(39);
            return token_stream;
        }

        int window_bytearr_length_before_moving_special_characters = token.getEndPos() - token.getStartPos();


        

        //Window ByteArr is of length 0 because first byte is special character. Returniing token stream, as incrementaion of end pos is required, if possible at current location within source file

        //System.out.println("window_bytearr_length_before_moving_special_characters: " + window_bytearr_length_before_moving_special_characters);
        
        // NEED - Adding "|| window_bytearr.length == 1" needs to be watched
        if (window_bytearr_length_before_moving_special_characters == 1 || window_bytearr.length == 1) {
            
            toolkit.debug(10);
           
            if (window_bytearr.length == 0) return token_stream; // 0 => only when first byte is a special char 
           
            byte b = window_bytearr[0]; // only byte in window

            toolkit.debug(11);
            
            if ( b >= 97 && b < 123 || (b == 32 && isWithinString)) { // [a-z]
                toolkit.debug(12);
                token.removePossiblity("EOP"); // Not sure if this and the below 2 should be here
                token.removePossiblity("DIGIT");
                token.removePossibilitiesOfSpecifiedType("SYMBOL", false);
                
                if (isWithinString) {
                    toolkit.debug(13);
                    
                    //token.setName("CHARACTER");
                    token.setName(b == 32 ? "SPACE" : "CHARACTER");
                    token.setAttribute(new String (window_bytearr));
                    return token_stream; 
                  
                } 

                else {
                    toolkit.debug(14);
                    if ( token.getKeywordSemiMatchesAmount() == 0 ) {
                        toolkit.debug(15);
                        token.setName("IDENTIFIER");
                        token.setAttribute(new String (window_bytearr));
                        return token_stream;
                        
                    } else {
                        toolkit.debug(16);
                        token.updatePossibility("IDENTIFIER", indices);
                        token.removePossiblity("CHARACTER");

                    }
                    
                }
                
            } 
            
            else {
                
                toolkit.debug(17);
                token.removePossiblity("IDENTIFIER");
                token.removePossiblity("CHARACTER");

                
                if (b > 47 && b <= 57)  { 
                    
                    if (isWithinString) {
                        toolkit.debug(19);
                        System.out.println("Throw error here! Digit in string");
                        System.exit(0);
                    }

                    toolkit.debug(20);
                    System.out.println("Digit!!!!!!!!");
                    token.setName("DIGIT");
                    token.setAttribute(new String(window_bytearr));
                    return token_stream;
                }

                else if ( b == 36 ) {
                    toolkit.debug(21);
                    token.setName("EOP");
                    token.setAttribute(new String(window_bytearr));
                    return token_stream;

                } toolkit.debug(22);

            } toolkit.debug(23);

        } toolkit.debug(24);


        token.printRemainingPossibilities(true);
        return token_stream; 
    }

   


    public static ArrayList<Token> generateTokens (byte[] src, ArrayList<Token> token_stream) {
        
        System.out.println("\n~~~~~~~~~~~~~~~~~~~GENERATE TOKENS~~~~~~~~~~~~~~~~~~~");
        
        if (token_stream.size() == 0 && src.length > 0) {
            toolkit.output("Adding First Token");
            token_stream.add(new Token(0, 0)); // end_pos gets incremented each recursive loop
            return generateTokens(src, token_stream);
        }

        toolkit.debug(25);

        Token current_token = token_stream.get(token_stream.size() - 1); // Get most recent token
        current_token.printShortTokenSummary(toolkit.getIsVerbose(), token_stream);

        System.out.println("");
        

        boolean has_match = current_token.name == null ? false : true;
        if (has_match) return token_stream; // Success
               

        toolkit.output("> Expanding window (end_pos + 1)");
        current_token.setEndPos(current_token.getEndPos() + 1);
        toolkit.debug(26);

        performChecks(src, token_stream); 
        boolean has_match_after_checks = current_token.name == null ? false : true;
        toolkit.debug(27);
        
        if (has_match_after_checks) {
            toolkit.output("\n> Definitive match\n"); //NEED - Source length appears to be off when compared to test file... Make sure to check
            toolkit.output("End Position of Matched Lexeme: " + current_token.getEndPos());
            toolkit.output("Source length - 1: " + (src.length - 1));
            

            current_token.setStartLineNumber(toolkit.getCurrentLine(current_token.getStartPos()));
            current_token.setEndLineNumber(toolkit.getCurrentLine(current_token.getEndPos()));
            toolkit.debug(28);

            toolkit.output("Generate Tokens - Remaining Byte Array: ");
            toolkit.printRemainingBytes(src, current_token.getEndPos());

            
            System.out.println("");
            // >=   maybe?
            if (src.length == current_token.getEndPos()) {
                toolkit.debug(29);
                toolkit.output("generateLexemes reached end of src, returning token_stream");
                return token_stream; // Success
            } else {
                toolkit.debug(40);
                toolkit.output("Creating a new token and adding it to the token stream");
                Token new_token = new Token(current_token.getEndPos(), current_token.getEndPos()); // removed increment
                token_stream.add(new_token);
                System.out.println("Updated Token Stream: ");
                toolkit.printTokenStream(token_stream);
                return generateTokens(src, token_stream);
            }

        } else {

            if (current_token.getPossibilities().size() == 1 && (ungrouped_tks_of_length_one.contains(current_token.getFinalRemainingPossibilityName()))) {
                toolkit.output("There is one lexeme possibility remaining and it is among those that are ungrouped and of one character length. Therefore, we are able to create a token using the current indices and the name of the last remaining lexeme possibility.");
                String final_possibility_string = current_token.getFinalRemainingPossibilityName();
                int[] final_possibility_indices = current_token.getFinalRemainingPossibilityIndices();
                current_token.setName(final_possibility_string);
                current_token.setStartPos(final_possibility_indices[0]);
                current_token.setEndPos(final_possibility_indices[1]);
                
                current_token.setAttribute(new String (Arrays.copyOfRange(src, final_possibility_indices[0], final_possibility_indices[1])));
                Token new_token = new Token(current_token.getEndPos(), current_token.getEndPos()); // removed increment
                token_stream.add(new_token);
                toolkit.output("Updated Token Stream: ");
                toolkit.printTokenStream(token_stream);
                return generateTokens(src, token_stream);
                
            }

            if (src.length - 1 != current_token.getEndPos() - 1) {
                toolkit.debug(30);
                //System.out.println("!!!!!!!!!!!!!!!!!!! INCREMENT !!!!!!!!!!!!!!!!!!!!!!!");
                //current_token.setEndPos(current_token.getEndPos() + 1);
                token_stream = generateTokens(src, token_stream);
            
            
             // Need check here for remaining unmatched token when end of src is reached
            // When testing single keyword testfile, there are 3 possibilities remaining when window = "int", 
            // End of src, and nothing happens it exits
            // What should happen is that the longest match should be taken, assigned to the token name, value set, and new token
            // should be created
            
            } else {
                toolkit.debug(31);
                current_token.processLongestMatch();
                toolkit.output("SHARED longest match: " + current_token.getThereExistsSharedLongestMatch() + ", UNIQUE longest match: " + current_token.getThereExistsUniqueLongestMatch());
                String longest_full_match_name = current_token.getLongestMatchName();
                int[] longest_full_match_indices = current_token.getPossibilities().get(longest_full_match_name);
                toolkit.output("Longest Match name: " + longest_full_match_name + ", Longest Match indices: " + Arrays.toString(longest_full_match_indices));
               
                toolkit.output("src.length - 1, " + (src.length - 1));
                toolkit.output("current_token.getEndPos() - 1 " + (current_token.getEndPos() - 1));


                // Check to see if of the remaining possibilites, are all completely without any match whatsoever?
                if (current_token.getRemainingPossibilitiesAreImpossible()) {
                    toolkit.debug(32);
                    token_stream.removeLast(); 
                    return token_stream;
                    // If so, remove unmatchable token and return the token stream
                }
                
                toolkit.debug(33);
                String tk_value = new String(Arrays.copyOfRange(src, longest_full_match_indices[0], longest_full_match_indices[1])); 
                System.out.println("Value at range: " + tk_value);
                current_token.setName(longest_full_match_name);
                current_token.setStartPos(longest_full_match_indices[0]);
                current_token.setEndPos(longest_full_match_indices[1]);

                //String tk_value_cleaned = Arrays.toString(toolkit.removeSpecialCharactersExceptSpaces(tk_value.getBytes()));

                if (longest_full_match_name == "IDENTIFIER") tk_value = toolkit.cleanIdentifierAttributeString(src, longest_full_match_indices[0], longest_full_match_indices[1]);
                current_token.setAttribute(tk_value);

                if (longest_full_match_indices[1] <= src.length - 1) {
                    toolkit.debug(35);
                    Token new_token = new Token(current_token.getEndPos(), current_token.getEndPos()); // removed increment
                    token_stream.add(new_token);
                    token_stream = generateTokens(src, token_stream);
                }
                //(longest_full_match_name);
                toolkit.debug(36);
            }
            toolkit.debug(37);
        }
        toolkit.debug(38);
        //System.out.println("After Checks: " + token_stream.size());
        return token_stream;
    }



    public static ArrayList<Token> Lex(Toolkit tk, String filename) {
        toolkit = tk; 
        byte[] file_source_bytearr = getSourceFileData(filename); // Read input from src file
        toolkit.setIndices(toolkit.GetIndicesOfLineFeeds(file_source_bytearr)); // Pass byte arr to GetIndicesOfLineFeed to get indices, update toolkit object its value
        
        //for (byte b: file_source_bytearr) System.out.println(b);
        //System.exit(0);
        ArrayList<Token> token_stream = generateTokens(file_source_bytearr, new ArrayList<Token>()); // Create token stream
        
        System.out.println("\n\n(#) LEXICAL ANALYSIS COMPLETE. \nToken Stream: ");
        for (Token t : token_stream) {
            System.out.println("Token: [" + t.getName() + ", " + t.getAttribute() + "] (Ln: " + t.getEndLineNumber() + ") " );
        }

        toolkit.printTokenStreamDetailed(token_stream);
        
        return token_stream;
        
    }
    
    
}


 // IGNORE

 //System.out.println(Arrays.toString(file_source_bytearr));
    //public ArrayList Lex () {

    //}
    
    //indices = 
            //System.out.println("Byte Arr:");
            
             
            /**  Test for isOfKeyword 
            //byte[] test_arr = new byte[]{105};
            byte[] test_arr = new byte[]{105, 110};
            for (byte b: test_arr) System.out.println("Byte: " + b + ", Char: " + (char) b);
            isOfKeyword(test_arr, 0, 2);
            //System.out.println(file_source_bytearr);
            //Toolkit.GetIndicesOfLineFeeds(file_byte_arr);
    **/


    /**
     * else if ( b == 123) {
                token.updatePossibility("SYMBOL_OPENBLOCK", indices);
            } 
     */
    //toolkit.removeSpecialCharacters(Arrays.copyOfRange(src, token.getStartPos(), token.getEndPos()));
    //toolkit.removeSpecialCharactersExceptSpaces(Arrays.copyOfRange(src, token.getStartPos(), token.getEndPos()));    
    // Map<String, int[]> testHash = new HashMap<>(token.getPossibilities());
    //testHash.put("Hello", new int[]{1,1});
    //boolean changeMadeOnPurposeMadeEqualityFunctionReturnFalse = (toolkit.hashmapEqualityExists(lexemes_possibilities_before_checking, testHash));
    //System.out.println("is modified equalityHashMapTest equal to unmodified: " + changeMadeOnPurposeMadeEqualityFunctionReturnFalse);
    /**
     * if (toolkit.hashmapEqualityExists(lexemes_possibilities_before_checking, token.getLexemePossibilities())) {
            System.out.println("Checking Funct Works -- Lexeme Possibilities Same Before Check");
        } else {
            System.out.println("Checking Funct Works -- Lexeme Possibilities Are Not Same");
            //System.out.println("Length Before: " + lexemes_possibilities_before_checking.size() + ", Length After: " + (token.getLexemePossibilities()).size());
        }
     */
      /**
        Token test = new Token(0, 0);
        test.setName("Hello");
        test.setEndPos(1);
        test.setAttribute(new String(Arrays.copyOfRange(src, test.getStartPos(), test.getEndPos())));
        token_stream.add(test);
        **/

        /**
         *  // Lexeme Possibilities - Has the amount changed since performing these checks?
         *  if (toolkit.hashmapEqualityExists(lexemes_possibilities_before_checking, token.getLexemePossibilities())) {
            System.out.println("Lexeme Possibilities Unchanged During Last Check");
        } else {
            System.out.println("Lexeme Possibilities have changed");
            //System.out.println("Length Before: " + lexemes_possibilities_before_checking.size() + ", Length After: " + (token.getLexemePossibilities()).size());
        }


        if ((token.getLexemePossibilities()).size() == (lexemes_possibilities_before_checking.size())) {
            // This case proves that there has been 


        } else {    
        }

        
         */

         
        //Token close_comment_token = new Token(token.getEndPos() - 1, token.getEndPos());
        //close_comment_token.setName("SYMBOL_CLOSECOMMENT");
        //close_comment_token.setAttribute("*/");

        //token_stream.remove(token_stream.size() - 1);
        //token_stream.add(open_comment_token); //Even though we will remove these... Add open comment first
        //token_stream.add(close_comment_token); // Then add close comment
         /**
        Token open_comment_token = new Token(token.getStartPos(), token.getStartPos() + 1); // Create that initial OPENCOMMENT 
        open_comment_token.setName("SYMBOL_OPENCOMMENT");
        open_comment_token.setAttribute("/*");
        **/
        //token_stream.

               /**
        // One possibility remaining -> emit it
        if (token.getPossibilities().size() == 1) {
            //token.updateTokenWithRemainingNameAndIndices();

            System.out.println("Token Indices Length: " + (token.getEndPos() - token.getStartPos())); 
            System.out.println("window_bytearr length: " + window_bytearr.length);
            
            // If tokens that are not of either keyword or symbol and they are matched with one char
            // May be able to combine this if statement with the one above using &&, if this really is the only case where this occurs, which this approach suggest because I believe it to be the case
            if (ungrouped_tks_of_length_one.contains(token.getFinalRemainingPossibilityName()) && (token.getFinalRemainingPossibilityName() == "IDENTIFIER")) { 
                token.setName(token.getFinalRemainingPossibilityName());
                
                // This is highly important because the next token is generated with the previous token's end index as both its start and end pos -- generateToken recursively increments the endPos, expanding the window, algorithmically 
                //token.setEndPos(token.getStartPos() + 1); // Being one character, the first indice at start_pos is already valid but the end position needs to be adjusted to start_pos + 1
                // OOPS ^ Huge headache caused by this
                
                token.setEndPos(token.getEndPos() - 1);
                // start = endPos - 2
                // endPos = endPos - 1

                token.setAttribute( new String ( Arrays.copyOfRange(window_bytearr, 0, 1)));

                return token_stream; 

                

            }

            // Creates a copy of the byte window using the length of the token match, starting at 0, so receives the correct value and not whatever is currently in the byte window
            // Fixed the issue over matching that I talked about in class
            //String tk_value = new String(Arrays.copyOfRange(window_bytearr, 0, (token.getEndPos() - token.getStartPos())));  
            //token.setAttribute(tk_value);
        }






        //Map<String, int[]> lexemes_possibilities_before_checking = new HashMap<>(token.getPossibilities());
        **/