public class ParsingException extends Exception {
    
    public ParsingException (String location, String reason, String expected, String received, Token current_token) {
        //String message = ; 
        super(
            "\n[ PARSE ERROR - " + location + "\n Reason:" + reason + ", \n Expected: " + expected + " \n Received: " + received + 
        ", \n Token Information: \n - Name: " + current_token.getName() + "\n - Attribute: " + current_token.getAttribute() + "\n - Line Number: " + current_token.getEndLineNumber() + " ]" ); 
    }

    public ParsingException(String location, String reason, String expected, String received, Token current_token, Throwable cause)  {
        super(
            "\n[ PARSE ERROR - " + location + "\n Reason:" + reason + ", \n Expected: " + expected + " \n Received: " + received + 
        ", \n Token Information: \n - Name: " + current_token.getName() + "\n - Attribute: " + current_token.getAttribute() + "\n - Line Number: " + current_token.getEndLineNumber() + " ]" , cause);
    }
}
