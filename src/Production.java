public class Production {
    
    public String name;
    public Boolean success;
    
    public Production (String name) {
        this.name = name; 
    }

    public String getName () {return this.name;}
    public Boolean getSuccess () {return this.success;}

    public void setSuccess (Boolean success) {this.success = success;}
    
}
