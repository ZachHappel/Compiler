
import java.util.ArrayList;


public class Parse {

    // TODO: Add error handling? How to decide when it failed?

    //ArrayList<Token> token_stream;
    public static ArrayList<Production> derivation = new ArrayList<>();
    public static ArrayList<Token> token_stream;
    public static Toolkit toolkit;
    public int token_pointer = 0;

    public Parse () { }

    public Terminal match (String seq, int token_pointer_local) {
        Terminal t = new Terminal(seq);
        if (token_stream.get(token_pointer_local).getName() == seq) {
            t.setSuccess(true);
        }
        return new Terminal("");
    }

    public void parseProgram () {
        Production program = new Production("Program");
        parseBlock(program);
        Terminal eop = match("EOP", token_pointer);
        
        if (eop.getSuccess()) {
            derivation.add(program);
            derivation.add(eop);
        }
    }

    public void parseBlock(Production program) {
        
        Production block = new Production("Block");
        Terminal open_block = match("KEYWORD_OPENBLOCK", token_pointer);
        Production statement_list = parseStatementList();
        match("KEYWORD_CLOSEBLOCK", token_pointer);

    }

    public Production parseStatementList() {


        return new Production("STATEMENT_LIST")
    }


    
    
    public static ArrayList<Production> ParseTokens ( ArrayList<Token> ts, Toolkit tk ) {
        
        token_stream = ts; 
        toolkit = tk; 

        return derivation;

    }


}
