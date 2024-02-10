import java.io.FileInputStream;
import java.io.InputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.ArrayList;




// linefeed = 10, carriage return = 13, tab = 11
// space = 10
public class LexicalAnalysis {

    public static int[] indices; 

    public static int[] GetIndicesOfLineFeeds (byte[] arr) {
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

    public static int getCurrentLine(int index) {
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





    public static ArrayList<Token> Lex() {

        ArrayList<Token> token_stream = new ArrayList<>();


        return token_stream;
    }

    //public ArrayList Lex () {

    //}
    
}
