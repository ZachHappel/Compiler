public class Terminal extends Production {
 
    public String name;
    public Token token;
    public String token_name;
    public String token_attribute;
    public String prodKind = "Terminal";

    public Terminal (String name) {
        super(name, "Terminal");
    }

    public void setToken (Token t) {this.token = t;};
    public void setTokenName (String t_n) {this.token_name = t_n;};
    public void setTokenAttribute(String t_a) {this.token_attribute= t_a;};

    public Token  getToken () { return this.token;}
    public String getTokenName () { return this.token_name;}
    public String getTokenAttribute () { return this.token_attribute;}

}
