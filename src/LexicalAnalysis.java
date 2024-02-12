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
                toolkit.output("Symbol Byte (" + byte_from_keyword + ") " + equality + " Window Byte (" + byte_from_window + ")");
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
        "\"".getBytes(), 
        "/*".getBytes(),
        "*/".getBytes(),
    };

    public static Map<String, String> symbols_name_map = new HashMap<String, String >() {{
        put("=", "SYMBOL_ASSIGNMENT");
        put("!=", "SYMBOL_INEQUIVALENCE");
        put("==", "SYMBOL_EQUIVALENCE");
        put("{", "SYMBOL_OPENBLOCK");
        put("}", "SYMBOL_CLOSEBLOCK");
        put("\"", "SYMBOL_STRINGEXPRBOUNDARY");
        put("/*", "SYMBOL_OPENCOMMENT");
        put("*/", "SYMBOL_CLOSECOMMENT");
        
    }};

    
    public static Map<String, int[]> isOfSymbol (byte[] window_bytearr, Token token) {
        toolkit.output("\n(!) IS OF SYMBOL?");
        boolean partial_match = false;
        Map<String, int[]> current_symbol_matches = new HashMap<String, int[]>() {{}};
        //ArrayList<String> current_keyword_matches = new ArrayList<String>();
        
        int[] indices = new int[]{token.getStartPos(), token.getEndPos()};
        int window_bytearr_size = window_bytearr.length; 
        int amount_of_symbols = symbols.length; 

        toolkit.output("(...) Symbol Bytes vs Window Bytes: (if bytes are not equal, the loop breaks, attempts next symbol if available)");
        
        for (int j = 0; j <= amount_of_symbols - 1; j++ ) {
            byte[] symbol = symbols[j];
            
            for (int s = 0; s <= symbol.length - 1 && s <= window_bytearr.length - 1; s++) {
                byte byte_from_symbol = symbol[s];
                byte byte_from_window =  window_bytearr[s];
                String equality = (byte_from_symbol == byte_from_window) ? "==" : "!=";
                toolkit.output("Symbol Byte (" + byte_from_symbol + ") " + equality + " Window Byte (" + byte_from_window + ")");
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

                    if (symbol_pattern.equals(new String (window_bytearr))) {

                        if (symbol_pattern == "=" && (token.getPossibilities().size() != 1)) {
                            toolkit.output("(WAITING: Need exactly ONE possibility to definitively, fully-match SYMBOL_ASSIGNMENT)");
                            current_symbol_matches.put(symbol_token_name, indices);
                            token.updatePossibility(symbol_token_name, indices); 
                            // Cannot return now, as "=" is the starting point of another symbol
                            break; // breaks to innermost loop
                        }
                        
                        toolkit.output("Symbol Full-match: " + symbol_token_name + ", indices: " + Arrays.toString(indices));
                        token.setName(symbol_token_name);
                        token.setAttribute(new String (window_bytearr)); // Set value/attribute to String representation of byte window
                        //NEED - Line Number
                        //Token in stream successfully updated and ready for new token to be added. 
                        //This will happen when performChecks returns token_stream to generateTokens which checks if name on most
                        //recent token has been set

                        // Can return now because in no case will a full keyword match interfere with another keyword
                        return current_symbol_matches;
                    }

                    
                }
                    
            }
        }

        if (partial_match) token.removePossibilitiesOfSpecifiedType("SYMBOL", true);
    
        return current_symbol_matches; 
        // PerformChecks need to remove, from Possibilities, all keywords that are not retured from here as being semi-matches.
        // If it fails here on X keyword, it is not X keyword
    }

    

    public static void removeAllSymbolsFromPossibilities(Token token ) {
        Map<String, int[]> token_lexeme_possibilities_local = new HashMap<>(token.getPossibilities());
        for (Map.Entry<String, int[]> entry : (token_lexeme_possibilities_local).entrySet()) {
            String possibility_name = entry.getKey();
            if (possibility_name.contains("SYMBOL")) token.removePossiblity(possibility_name);
        }
    }

    

    
    public static void removeSymbolImpossibilities( Map<String, int[]> symbol_matches, Token token ) {
        System.out.println("\n(!) REMOVE SYMBOL IMPOSSIBILITIES");
        int symbol_matches_amount = symbol_matches.size(); 
        System.out.println("Symbol Semi Matches: " + symbol_matches.size());
        // REASON: This should resolve ConcurrentModificationException that was occurring when access and modifying the token's Lexeme Possibilities HashMap
        // Use this as a reference and make removals on the actual token's hashmap
        Map<String, int[]> token_lexeme_possibilities_local = new HashMap<>(token.getPossibilities());

        ArrayList<String> symbol_match_names = new ArrayList<String>(); 
        
        // Add all matching names to ArrayList
        for (Map.Entry<String, int[]> entry : symbol_matches.entrySet()) { symbol_match_names.add(entry.getKey());}
        
        for (Map.Entry<String, int[]> entry : (token_lexeme_possibilities_local).entrySet()) {
            String possibility_name = entry.getKey();
            
            // If possibility is of kind "SYMBOL"
            // but is not among those which have been identified as being semi-matches
            if (possibility_name.contains("SYMBOL_") && !symbol_match_names.contains(possibility_name)) {
                System.out.println("Removing SYMBOL Possibility: " + possibility_name); 
                token.removePossiblity(possibility_name);
                symbol_matches_amount = symbol_matches_amount - 1;
            }
            

        }
        System.out.println("Remaining Symbol Semi Matches: " + symbol_matches_amount);

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
        Map<String, int[]> lexemes_possibilities_before_checking = new HashMap<>(token.getPossibilities());
        
        
        System.out.println("(?) isWithinString: " + isWithinString);
        System.out.println("(?) isWithinComment: " + isWithinComment);
        
        // If not in string, we want all special characters removed
        // If it IS within a string, we want all special characters BUT spaces removed
        byte[] window_bytearr = isWithinString ? toolkit.removeSpecialCharactersExceptSpaces(Arrays.copyOfRange(src, token.getStartPos(), token.getEndPos())) : toolkit.removeSpecialCharacters(Arrays.copyOfRange(src, token.getStartPos(), token.getEndPos()));
        System.out.println("(?) window_bytearr: " + new String(window_bytearr)); 
        int[] indices = new int[]{token.getStartPos(), token.getEndPos()};

        // Modifications made to token LexemePossibilities via updatePossibility when there is a partial match or a full match
        Map<String, int[]> keyword_matches = isOfKeyword(window_bytearr, token);
        System.out.println("\n(...) Keyword Matches: "); for (Map.Entry<String, int[]> entry : keyword_matches.entrySet()) { String name = entry.getKey(); int[] i = entry.getValue(); System.out.println("Keyword: " + name + " Indices: " + Arrays.toString(i) + " Length of match: " + (i[1] - i[0]) + ", matching: " + new String(window_bytearr));   }
        removeKeywordImpossibilities(keyword_matches, token);
        if (token.getName() != null) {
            return token_stream;
        }

        Map<String, int[]> symbol_matches = isOfSymbol(window_bytearr, token);
        System.out.println("\n(...) Symbol Matches: "); for (Map.Entry<String, int[]> entry : symbol_matches.entrySet()) { String name = entry.getKey(); int[] i = entry.getValue(); System.out.println("Symbol: " + name + " Indices: " + Arrays.toString(i) + " Length of match: " + (i[1] - i[0]) + ", matching: " + new String(window_bytearr));   }
        removeSymbolImpossibilities(symbol_matches, token);
        if (token.getName() != null) {
            return token_stream;
        }

        int window_bytearr_length_before_moving_special_characters = token.getEndPos() - token.getStartPos();


        // Needs to be...
        // If length before moving special chars is 1
        // But if length after moving special chars is 0... return? I think

        // if window != [...,...,..., *, /] -> ignore  =>  return token_stream
        // NEED CONSIDER - This may need to be moved before any symbol or keyword matching
        if (isWithinComment) {
            //Wait... 
            // Encountered when /* Token already created... So we do not need to make it (even though I am going to delete later.. Ik this is just for testing)
            System.out.println("Within Comment");

            if (window_bytearr.length >= 2) {
                if ( (window_bytearr[window_bytearr.length - 2 ] == 42) && (window_bytearr[window_bytearr.length - 1 ] == 47) ) {
                    System.out.println("Last 2 bytes in window are close comment symbol");
                   
                    
                    /**
                    Token open_comment_token = new Token(token.getStartPos(), token.getStartPos() + 1); // Create that initial OPENCOMMENT 
                    open_comment_token.setName("SYMBOL_OPENCOMMENT");
                    open_comment_token.setAttribute("/*");
                    **/
                    //token_stream.
                    token.setStartPos(token.getEndPos() - 1);
                    token.setName("SYMBOL_CLOSECOMMENT");
                    token.setAttribute("*/"); // Instead of having to concoct some ridiculous subarray converted to string, or something of the sort, just putting in /* is much easier and should not cause any trouble
                    
                    //Token close_comment_token = new Token(token.getEndPos() - 1, token.getEndPos());
                    //close_comment_token.setName("SYMBOL_CLOSECOMMENT");
                    //close_comment_token.setAttribute("*/");
    
                    //token_stream.remove(token_stream.size() - 1);
                    //token_stream.add(open_comment_token); //Even though we will remove these... Add open comment first
                    //token_stream.add(close_comment_token); // Then add close comment
    
                    System.out.println("Returning token stream with open and close comment tokens inserted");
                    return token_stream;
                }
            }
            
            
            System.out.println("SYMBOL_ENDCOMMENT not matched, returning token_stream");
            return token_stream;
        }

        if (window_bytearr_length_before_moving_special_characters == 1) {
            System.out.println("(Window Length of 1) ");
            
            if (window_bytearr.length == 0) {
                System.out.println("(?) Window ByteArr is of length 0 because first byte is special character. Returniing token stream, as incrementaion of end pos is required, if possible at current location within source file");
                return token_stream;
            }
            
            byte b = window_bytearr[0]; // only byte in window
            
            // [a-z]
            if ( b >= 97 && b < 123) { 

                if (isWithinString) {
                    token.updatePossibility("CHARACTER", indices); // MATCH?
                } else {
                    token.updatePossibility("IDENTIFIER", indices);
                    token.removePossiblity("EOP"); // Not sure if this and the below 2 should be here
                    token.removePossiblity("DIGIT");
                    token.removePossibilitiesOfSpecifiedType("SYMBOL", false);
                    //removeAllSymbolsFromPossibilities(token);
                }
                
                
            
            // { 
            } else if ( b == 123) {
                token.updatePossibility("SYMBOL_OPENBLOCK", indices);
            } 
            
        }
        
        token.printRemainingPossibilities(true);

       

       
        
      

        return token_stream; 


    }

    // IGNORE
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

    public static ArrayList<Token> generateTokens (byte[] src, ArrayList<Token> token_stream) {
        
        System.out.println("\n~~~~~~~~~~~~~~~~~~~GENERATE TOKENS~~~~~~~~~~~~~~~~~~~");
        // If no token in stream and there exists source input
        // create token, add it to the token stream,
        // call generateTokens recursively
        if (token_stream.size() == 0 && src.length > 0) {
            System.out.println("Adding First Token");
            Token starting_token = new Token(0, 0); // end_pos will get incremented
            token_stream.add(starting_token);
            System.out.println("Current Stream Size: " + token_stream.size());

            return generateTokens(src, token_stream);
        }

        // Get most recent token
        Token current_token = token_stream.get(token_stream.size() - 1);   
        boolean has_match = current_token.name == null ? false : true;

        System.out.println("(?) Current Token Name: " + current_token.getName() + " / Amount of Lexeme Possibilities: " + (current_token.getPossibilities()).size() + " / Current Window Size: " + (current_token.getEndPos() - current_token.getStartPos()));
        
       
        if (has_match) {
            // If match has been made, and no new token appeneded, then success -- time to return
            System.out.println("Has Match, SUCCESS");
            return token_stream;
        }

        // Increase the end position, and check  //!!
        // REMOVING INCREMENT HERE, against all judgement... But I think it is actually wrong according to my notes
        // Sike, I am leaving it
       
        System.out.println("> Expanding window (end_pos + 1)");
        current_token.setEndPos(current_token.getEndPos() + 1);
        

        performChecks(src, token_stream); 
        // Need to implement check for in string and comments in a more comprehensive way
        // Pretty positive within-string expr and definitely within-comment need be addressed here

        //Token new_current_token = token_stream.get(token_stream.size() - 1); // DEF not needed but scared
        boolean has_match_after_checks = current_token.name == null ? false : true;
        
        if (has_match_after_checks) {
            System.out.println("\n> Definitive match\n");
            //NEED - Source length appears to be off when compared to test file... Make sure to check
            System.out.println("End Position of Matched Lexeme: " + current_token.getEndPos());
            System.out.println("Source length - 1: " + (src.length - 1));
            
            // Not sure if this should be: 
            // src.length == curr.getEndPos   or
            // src.length - 1 >= curr.getEndPos
            if (src.length == current_token.getEndPos()) {
                System.out.println("generateLexemes reached end of src, returning token_stream");
                return token_stream; // Success
            } else {
                System.out.println("Creating a new token and adding it to the token stream");
                Token new_token = new Token(current_token.getEndPos(), current_token.getEndPos()); // removed increment
                token_stream.add(new_token);
                System.out.println("Updated Token Stream: ");
                toolkit.printTokenStream(token_stream);
                return generateTokens(src, token_stream);
            }
        } else {

            if (src.length - 1 != current_token.getEndPos() - 1) {
                //System.out.println("!!!!!!!!!!!!!!!!!!! INCREMENT !!!!!!!!!!!!!!!!!!!!!!!");
                //current_token.setEndPos(current_token.getEndPos() + 1);
                token_stream = generateTokens(src, token_stream);
            } else {
                // Need check here for remaining unmatched token when end of src is reached
                // When testing single keyword testfile, there are 3 possibilities remaining when window = "int", 
                // End of src, and nothing happens it exits
                // What should happen is that the longest match should be taken, assigned to the token name, value set, and new token
                // should be created
                System.out.println("src.length - 1 != current_token.getEndPos() - 1 ");
            }

        }

        //System.out.println("After Checks: " + token_stream.size());
        return token_stream;
    }



    public static ArrayList<Token> Lex(Toolkit tk, String filename) {
        toolkit = tk; 
        byte[] file_source_bytearr = getSourceFileData(filename);
        indices = toolkit.GetIndicesOfLineFeeds(file_source_bytearr);
        System.out.println("Byte Arr:");
        
        
        /**  Test for isOfKeyword 
        //byte[] test_arr = new byte[]{105};
        byte[] test_arr = new byte[]{105, 110};
        for (byte b: test_arr) System.out.println("Byte: " + b + ", Char: " + (char) b);
        isOfKeyword(test_arr, 0, 2);
        //System.out.println(file_source_bytearr);
        //Toolkit.GetIndicesOfLineFeeds(file_byte_arr);
       **/


       
        ArrayList<Token> token_stream = generateTokens(file_source_bytearr, new ArrayList<Token>());
        
        System.out.println("\n\n(#) LEXICAL ANALYSIS COMPLETE. \nToken Stream: ");
        for (Token t : token_stream) {
            System.out.println("Token: " + t.getName() + " Value/Attribute: " + t.getAttribute());
        }
        
        return token_stream;

        //return new ArrayList<Token>();
    }

    //public ArrayList Lex () {

    //}
    
}
