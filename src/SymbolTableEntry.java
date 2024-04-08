
/*
 
SymbolTable

    //Scope       ID     {type, isInitialized, isUsed, etc.}
Map<String, Map<String, SymbolTableEntry>>

getCurrentScope
getCurrentScopeName
setScope
getScope
createNewScope () {
    ArrayList<String> alpha = new ArrayList<>(){"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    ArrayList<Integer> numbs = new ArrayList<>(){0,1,2,3,4,5,6,7,8,9};
    boolen
    String scope_name = getCurrentScopeName ()w
    boolean isLastCharAlpha = ( is last char of scope_name an alphabet character a-z ? true : false) 
    if last is alpha: 
}


existsWithinScope(String id) 



Symbol Table

    Scope 0:

        Scope 0a: 

        Scope 0b: 

        Scope 0c: 
            
            Scope 0c0:

                Scope 0c0a:

            Scope 0c1:
        
        Scope 0d:


    $ 




 */

public class SymbolTableEntry {

    String type;
    boolean is_initialized;
    boolean is_used;
   
    public SymbolTableEntry (String type, boolean is_init, boolean is_used) {
        this.type = type; // int, boolean, string
        this.is_initialized = is_init;
        this.is_used = is_used; 
    }

    public String getType () {return this.type;};
    public boolean getIsUsed () { return this.is_used; } ;
    public boolean getIsInitialized () { return this.is_initialized; };

    public void setType (String x) {this.type = x;}; 
    public void setIsInitialized (boolean x) {this.is_initialized = x;};
    public void setisUsed (boolean x) {this.is_used = x;};

}
