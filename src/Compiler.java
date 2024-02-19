import java.io.FileInputStream;
import java.io.InputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;

import javax.tools.Tool;

import java.util.ArrayList;



// Default should verbose mode, provide option for simplified output
public class Compiler {
    
    public static Toolkit Toolkit = new Toolkit();
    public static LexicalAnalysis LexicalAnalysis = new LexicalAnalysis(); 
    

    public static byte[] getSourceFileData(String filepath) {
        
        try {
            Path path = Paths.get(filepath);
            long file_size_in_bytes = Files.size(path);
            InputStream input = new FileInputStream(filepath);
            byte[] source_data_bytearr = new byte[Math.toIntExact(file_size_in_bytes)]; 
            input.read(source_data_bytearr); // Read byte from input stream
            
            String source_data_string = new String(source_data_bytearr);
            //char[] source_data_chararr = source_data_string.toCharArray();
            
            //String file_info = "|| File Size: " + file_size_in_bytes + " bytes || Remaining Space: " + input.available() + " bytes";
            String file_info = "  (File Size: " + file_size_in_bytes + " bytes)\n\n";
            
            System.out.print(file_info);

            input.close();

            return source_data_bytearr; 

        } catch (IOException io_err) {
            System.out.println("IOException encountered when reading source file into data\n Outputting stacktrace and halting further execution.");
            io_err.printStackTrace();
            return new byte[0];
        }
        
    }

    // Args: filename, optional flag "-s"
    public static void main(String[] args) {
        
       
        if (args.length > 2 || args.length == 0) Toolkit.endProgram("Invalid amount of arguments.\n (REQUIRED) Argument One: filename \n (OPTIONAL) Flags: '-t' for terse mode.");
        if (args.length == 2 && args[1].equals("-t")) {
            Toolkit.setIsVerbose(false);
        } else if (args.length == 2 && args[1].equals("-d")) {
            Toolkit.setIsDebugMode(true);
        }


        String file_path = args[0];
        String f_name = file_path.contains("/") ? (file_path.split("/"))[file_path.split("/").length - 1] : file_path; 

        System.out.println("\n\nCompiler\nWritten by Zacharie Happel\n");
        System.out.print("\nFile: \"" + f_name + "\" | Verbose: " + Toolkit.getIsVerbose() + " | Debug Mode: " + Toolkit.getIsDebugMode());
       

        byte[] file_source_bytearr = getSourceFileData(file_path); // Read input from src file
        ArrayList<byte[]> programs = Toolkit.subdivideSourceIntoPrograms(file_source_bytearr);

        Toolkit.generateProgramOverview(programs.get(0), 0);
        
        //LexicalAnalysis.Lex(Toolkit, fileName);
        //LexicalAnalysis.Lex(Toolkit, fileName);
    }


}