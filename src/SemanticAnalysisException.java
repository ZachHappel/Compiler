
public class SemanticAnalysisException extends Exception {
    
    public SemanticAnalysisException (String location, String reason) {

        super(
            "\n[ SEMANTIC ANALYSIS ERROR - " + location + "\n Reason:" + reason + " ]" ); 
    }

    public SemanticAnalysisException(String location, String reason, Throwable cause)  {
         super(
            "\n[ SEMANTIC ANALYSIS ERROR - " + location + "\n Reason:" + reason + " ]" ); 
    }
}
