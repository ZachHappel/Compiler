
import java.util.ArrayList;


public class Parse {

    //ArrayList<Token> token_stream;
    public static ArrayList<Production> derivation = new ArrayList<>();
    public static ArrayList<Token> token_stream;
    public static Toolkit toolkit;

    public Parse () { }

    public void match (String seq) {

    }

    public void parseProgram () {
        Production program = new Production("Program");
        parseBlock();
        match("EOP");
    }

    public void parseBlock() {
        Production block = new Production("Block");
        match("KEYWORD_OPENBLOCK");
        parseStatementList();
        match("KEYWORD_CLOSEBLOCK");
    }

    public void parseStatementList() {

    }


    
    
    public static ArrayList<Production> ParseTokens ( ArrayList<Token> ts, Toolkit tk ) {
        
        token_stream = ts; 
        toolkit = tk; 

        return derivation;

    }


}
