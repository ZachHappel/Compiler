import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    
        //Scope       ID     {type, isInitialized, isUsed, etc.}
    //new Map<String,Map<String,SymbolTableEntry>>() {};
    public Map<String, SymbolTableScope> table = new HashMap<>(); 

    //public String currentScopeName;
    public SymbolTableScope current_scope; 
    ArrayList<String> alpha = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"));
    ArrayList<Integer> numbs = new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7,8,9)); 
    



    public SymbolTable () {
        String root_scope_name = "0"; // Remember
        SymbolTableScope root_scope = new SymbolTableScope(root_scope_name);
        insertScope(root_scope_name, root_scope);
    }


    public SymbolTableScope getScope(String scope_id) {
        return table.get(scope_id);
    }


    public void exitScope () {
        setCurrentScope(current_scope.getScopeParent()); 
    }


    public boolean endsInLetter (String s) {
        
        return (
            alpha.contains(s.charAt(s.length() - 1)) ?
            true :
            false
        );
    }


    public void setCurrentScope(SymbolTableScope scope) {
        this.current_scope = scope; 
    }

    public void createNewScope() {
        String new_scope_name = "";
        String current_scope_name = this.current_scope.getName();



        // Need to remediate root scope not having parent 
        // Need to prevent integer overflow if more than 9 siblings and 26, depending on whether number or char
        if ( current_scope.hasParent() )  {
            SymbolTableScope currents_parent = current_scope.getScopeParent();
            if (currents_parent.hasChildren()) {
                //Siblings exist
                SymbolTableScope last_child = currents_parent.getLastChild();
                String last_child_name = last_child.getName(); 
                if ( endsInLetter(last_child_name) ) {
                    int next_alpha_index = alpha.indexOf(last_child_name.charAt(last_child_name.length() - 1)) + 1;
                    new_scope_name = last_child_name.substring(0, last_child_name.length() - 1) + alpha.get(next_alpha_index); // Next number to append
                    System.out.println("i) New Scope Name: " + new_scope_name);
                } else {
                    int next_numbs_index = numbs.indexOf(last_child_name.charAt(last_child_name.length() - 1)) + 1; 
                    new_scope_name = last_child_name.substring(0, last_child_name.length() - 1) + numbs.get(next_numbs_index); // Next number to append
                    System.out.println("ii) New Scope Name: " + new_scope_name);
                }
            } else {
                // Only will work if length of name > 1 
                if (endsInLetter(current_scope_name)) {
                    new_scope_name = current_scope_name.substring(0, current_scope_name.length() - 1) + numbs.get(0);
                    
                } else {
                    new_scope_name = current_scope_name.substring(0, current_scope_name.length() - 1) + alpha.get(0);
                }
            }
        } else {
            new_scope_name = current_scope_name + alpha.get(0); // ROOT
        }


        //NOTE:  Are changes being recorded within the HashMap or do they need to be manually done? ?????
        SymbolTableScope new_scope = new SymbolTableScope(new_scope_name);
        
        System.out.println("Current Scope Name: " + current_scope_name);
        System.out.println("iii) New Scope Name: " + new_scope_name);
        new_scope.setScopeParent(current_scope); // Dive in
        current_scope.addScopeChild(new_scope);
        setCurrentScope(new_scope);
        table.put(new_scope_name, new_scope);

    }
    
    public void insertScope (String name, SymbolTableScope scope) {
        if (!this.table.containsKey(name)) {
            this.table.put(name, scope);
            if (! (current_scope != null)) setCurrentScope(table.get(name));
        } else {
            /////////////////Error
        }
        
    }



}
