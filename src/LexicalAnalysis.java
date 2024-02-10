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
    public static Toolkit toolkit;
   
    public static int getCurrentLine(int index) {
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





    public static ArrayList<Token> Lex(Toolkit tk, String filename) {
        toolkit = tk; 
        ArrayList<Token> token_stream = new ArrayList<>();
        byte[] file_source_bytearr = getSourceFileData(filename);
        indices = Toolkit.GetIndicesOfLineFeeds(file_source_bytearr);
        //Toolkit.GetIndicesOfLineFeeds(file_byte_arr);

        return token_stream;
    }

    //public ArrayList Lex () {

    //}
    
}
