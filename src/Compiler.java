import java.io.FileInputStream;
import java.io.InputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.ArrayList;



// Default should verbose mode, provide option for simplified output
public class Compiler {

    public static FormatToolkit Toolkit;

    

    // Args: filename, optional flag "-s"
    public static void main(String[] args) {
        if (args.length > 2 || args.length == 0) Toolkit.endProgram("Invalid amount of arguments.\n (REQUIRED) Argument One: filename \n (OPTIONAL) Flags: '-t' for terse mode.");
        String fileName = args[0];

    }


}