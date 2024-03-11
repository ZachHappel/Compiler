import java.util.ArrayList;

public class Production {
    
    public String name;
    public Boolean success = false;
    public ArrayList<Production> children = new ArrayList<Production>();
    public Production parent;

    
    public Production (String name) {
        this.name = name; 
    }

    public String getName () {return this.name;}
    public Boolean success () {return this.success;}
    public ArrayList<Production> getChildren () {return this.children;}
    public Production getParent () {return this.getParent();}
    public Production getChild(int index) {return children.get(index);}


    public void setName(String name) { this.name = name;}
    public void setSuccess (Boolean success) {this.success = success;}

    public void addChild (Production child) { this.children.add(child); }
    public void addParent (Production parent) { this.parent = parent;}

}
