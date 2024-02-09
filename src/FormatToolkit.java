// Rework from my last project

public class FormatToolkit {

    public Boolean is_verbose;

    public FormatToolkit() {
        this.is_verbose = true; 
    }

    public FormatToolkit( Boolean is_verbose ) {
        this.is_verbose = is_verbose; 
    }

    public void setIsVerbose( Boolean is_verbose ) {
        this.is_verbose = is_verbose;
    }

    public boolean getIsVerbose() {
        return this.is_verbose;
    }

    public void output( String string ) {
        if (this.is_verbose) System.out.println(string);
    }

    public void endProgram(String reason) {
            this.output("Ending execution, " + reason);
            System.exit(0);
    }
}
