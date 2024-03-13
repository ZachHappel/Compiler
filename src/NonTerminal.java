public class NonTerminal extends Production {
    

    public String name;
    public Token token_name;
    public String prodKind = "NonTerminal";


    public NonTerminal (String name) {
        super(name, "NonTerminal");
    }


   

}