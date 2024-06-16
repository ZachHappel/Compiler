public class CodeGenerationException extends Exception {
    public CodeGenerationException (String location, String reason) {

        super(
            "\n[ CODE GENERATION ERROR - " + location + "\n Reason:" + reason + " ]" ); 
    }

    public CodeGenerationException(String location, String reason, Throwable cause)  {
         super(
            "\n[ CODE GENERATION ERROR - " + location + "\n Reason:" + reason + " ]" ); 
    }
}
