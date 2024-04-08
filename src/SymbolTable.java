import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    
        //Scope       ID     {type, isInitialized, isUsed, etc.}
    //new Map<String,Map<String,SymbolTableEntry>>() {};
    public Map<String, SymbolTableScope> table = new HashMap<>(); 

    public String currentScopeName;
    public SymbolTableScope currentScope; 
    ArrayList<String> alpha = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"));
    ArrayList<Integer> numbs = new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7,8,9)); 
    



    public SymbolTable () {
        //SymbolTableScope root_scope = new SymbolTableScope();
    }


    public SymbolTableScope getScope(String scope_id) {
        return table.get(scope_id);
    }

    public void setCurrentScope(SymbolTableScope scope) {
        this.currentScope = scope; 
    }

    public void createNewScope() {

    }
    
    



}
