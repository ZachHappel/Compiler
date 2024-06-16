import java.util.ArrayList;

public class Production implements Cloneable {
    
    public String name;
    public Boolean success = false;
    public ArrayList<Production> children = new ArrayList<Production>();
    public Production parent;
    public String prodKind;
    public String scope_name; 

    public ArrayList<Production> ast_children = new ArrayList<Production>();
    public Production ast_parent;

    
    public Production (String name) {
        this.name = name; 
    }

    public Production (String name, String prodKind) {
        this.name = name; 
        this.prodKind = prodKind;
    }

    

    /*
    public static Production copy( Production original ) {
        Production new_prod = new Production(original.getName());
        new_prod.prodKind = original.getProdKind();
        new_prod.set = original.id;
        //... etc. 
        return newUser;
   } */

    public String getName () {return this.name;}
    public Boolean success () {return this.success;}
    public ArrayList<Production> getChildren () {return this.children;}
    public Production getParent () {return this.parent;}
    public Production getChild(int index) {return children.get(index);}
    public String getScopeName () {return this.scope_name; }
    public String getProdKind() { return this.prodKind; }
    public void setName(String name) { this.name = name;}
    public void setSuccess (Boolean success) {this.success = success;}
    public void addChild (Production child) { this.children.add(child); }
    public void addParent (Production parent) { this.parent = parent;}
    public void removeChild (int index) { this.children.remove(index);}
    public void removeChildren () {this.children = new ArrayList<>();}
    
    public Production getASTParent () {return this.ast_parent;}
    public Production getASTChild(int index) {return ast_children.get(index);}
    public ArrayList<Production> getASTChildren () {return this.ast_children;}
    
    public void addASTChild (Production child, String scope_name) {
         this.ast_children.add(child); 
         this.scope_name = scope_name; 
        
    }

    public void addASTParent (Production parent) { this.ast_parent = parent;}
    public void removeASTChild (int index) { this.ast_children.remove(index);}
    public void removeASTChildren () {this.ast_children = new ArrayList<>();}


}
