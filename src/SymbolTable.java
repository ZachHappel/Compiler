import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
    
        //Scope       ID     {type, isInitialized, isUsed, etc.}
    //new Map<String,Map<String,SymbolTableEntry>>() {};
    public Map<String, SymbolTableScope> table = new LinkedHashMap<>(); 

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
        String last_char = "" + s.charAt(s.length() - 1);
        if (alpha.contains(last_char)) {
            System.out.print(", Alpha contains: " + last_char + ", returning true");
            return true;
        } else {
            System.out.println(", Alpha DOES NOT contains: " + last_char + ", returning false");
            return false;
        }
        /**
        System.out.println("Last letter: " + last_char);
        return (
            alpha.contains(s.charAt(s.length() - 1)) ?
            true :
            false
        ); **/
    }


    public void setCurrentScope(SymbolTableScope scope) {
        this.current_scope = scope; 
    }

    public void createNewScope() {
        String new_scope_name = "";
        String current_scope_name = this.current_scope.getName();
        System.out.println("Current Scopes: " + getScopeNames()); 
        System.out.println("Current Scope Name: " + current_scope_name);


        // Need to remediate root scope not having parent 
        // Need to prevent integer overflow if more than 9 siblings and 26, depending on whether number or char
        if ( current_scope.hasParent() )  {
            System.out.print("Current scope has parent: " + current_scope.getScopeParent().getName() + ", ");
            SymbolTableScope currents_parent = current_scope.getScopeParent();
            if (currents_parent.hasChildren()) {
                System.out.print("the current scope's parent has children, ''" + currents_parent.getAllChildrenNames() + "'', ");
                System.out.print(", current scope's children, ''" + current_scope.getAllChildrenNames() + "'', ");
                //Siblings exist
                
                if (current_scope.hasChildren()) {
                    SymbolTableScope last_child = current_scope.getLastChild();
                    String last_child_name = last_child.getName(); 
                    System.out.print("last child name: " + last_child_name);
                    if ( endsInLetter(last_child_name) ) {
                        System.out.println(", ends in letter, ");
                        String last_letter = String.valueOf(last_child_name.charAt(last_child_name.length() - 1));
                        //int last_number_as_int = Integer.parseInt(last_number);  
                        int next_alpha_index = alpha.indexOf(last_letter) + 1;
                        //int next_numbs_index = alpha.indexOf(last_child_name.charAt(last_child_name.length() - 1)) + 1; 
                        new_scope_name = last_child_name.substring(0, last_child_name.length() - 1) + alpha.get(next_alpha_index); // Next number to append
                        //new_scope_name = last_child_name + numbs.get(next_numbs_index); // Next number to append
                        System.out.println("i) New Scope Name: " + new_scope_name);
                    
                    } else {
                        System.out.println("~ends in number, ");
                        String last_number = String.valueOf(last_child_name.charAt(last_child_name.length() - 1));
                        int last_number_as_int = Integer.parseInt(last_number);  
                        int next_alpha_index = numbs.indexOf(last_number_as_int) + 1;
                        new_scope_name = last_child_name.substring(0, last_child_name.length() - 1) + numbs.get(next_alpha_index); // Next number to append
                        //new_scope_name = last_child_name + alpha.get(next_alpha_index); // Next number to append
                        System.out.println("ii) New Scope Name: " + new_scope_name);
                    }
                } else {
                    System.out.println("Xxx"); 
                    SymbolTableScope last_child = currents_parent.getLastChild();
                    String last_child_name = last_child.getName(); 
                    System.out.print("Xxx last child name: " + last_child_name);
                    // Only will work if length of name > 1 
                    if (endsInLetter(last_child_name)) {
                        new_scope_name = last_child_name + numbs.get(0);
                        
                    } else {
                        new_scope_name = last_child_name + alpha.get(0);
                    }
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
            System.out.println("Current scope has no parent");
            new_scope_name = current_scope_name + alpha.get(0); // ROOT
        }


        //NOTE:  Are changes being recorded within the HashMap or do they need to be manually done? ?????
        SymbolTableScope new_scope = new SymbolTableScope(new_scope_name);
        
        
        System.out.println("iii) New Scope Name: " + new_scope_name);
        new_scope.setScopeParent(current_scope); // Dive in
        current_scope.addScopeChild(new_scope);
        setCurrentScope(new_scope); // Set new_scope as the current_scope
        calculateAccessibleScopes(current_scope); // Figure out which scopes the current (prev. "new_scope") has access to
        table.put(new_scope_name, new_scope); // Add scope to table

    }

    // Which scopes does the current scope have access to
    public void calculateAccessibleScopes (SymbolTableScope scope) {
        SymbolTableScope scope_n = scope;
        boolean reached_root = false;

        while (reached_root == false) {
            if (scope_n.hasParent()) {
                SymbolTableScope parent = scope_n.getScopeParent();
                scope.addAccessibleScope(parent); // Add parent to the passed scope's accessible scopes
                scope_n = parent; // Make parent scope_n so scope_n's parent can be added as well
            } else {
                reached_root = true;
            }
        }
        System.out.println("Accessible Scope Names: " + scope.getAllAccessibleScopesNames()); 
    }
    
    public void insertScope (String name, SymbolTableScope scope) {
        if (!this.table.containsKey(name)) {
            this.table.put(name, scope);
            if (! (current_scope != null)) setCurrentScope(table.get(name));
        } else {
            /////////////////Error
        }
        
    }


    public String getScopeNames () {
        String scope_names = "";
        for (Map.Entry<String, SymbolTableScope> scope_n : table.entrySet()) {
            scope_names+= scope_n.getKey() + ", ";
        }   return scope_names; 
    }

    public int getScopeCount () {return table.size();}

    public boolean existsWithinSymbolTable (Terminal identifier_terminal) {
        String identifier_value = identifier_terminal.getTokenAttribute();
        boolean exists_within_a_scope = false;
        for (Map.Entry<String, SymbolTableScope> scope_n : table.entrySet()) {
            if (scope_n.getValue().entryExists(identifier_value)) {
                exists_within_a_scope = true;
            }
        } return exists_within_a_scope; 
    } 

    public void performEntry(Terminal type_terminal, Terminal identifier_terminal) {
        // Current scope depenedent
        // When checking to see if it has been declared, declaration checks need ONLY be done on affected scopes 
        System.out.println("Preparing to add into Symbol Table: " + type_terminal.getTokenAttribute() + " " + identifier_terminal.getTokenAttribute());
        String identifier_value = identifier_terminal.getTokenAttribute(); // Id 
        if (existsWithinSymbolTable(identifier_terminal)) {} // Error
         

    }


}



/** 
public void createNewScope() {
    String new_scope_name = "";
    String current_scope_name = this.current_scope.getName();
    System.out.println("Current Scope Name: " + current_scope_name);


    // Need to remediate root scope not having parent 
    // Need to prevent integer overflow if more than 9 siblings and 26, depending on whether number or char
    if ( current_scope.hasParent() )  {
        System.out.print("Current scope has parent: " + current_scope.getScopeParent().getName() + ", ");
        SymbolTableScope currents_parent = current_scope.getScopeParent();
        if (currents_parent.hasChildren()) {
            System.out.print("current scope has children parent, ");
            //Siblings exist
            SymbolTableScope last_child = currents_parent.getLastChild();
            String last_child_name = last_child.getName(); 
            if ( endsInLetter(last_child_name) ) {
                System.out.println("ends in letter, ");
                int next_alpha_index = alpha.indexOf(last_child_name.charAt(last_child_name.length() - 1)) + 1;
                new_scope_name = last_child_name.substring(0, last_child_name.length() - 1) + alpha.get(next_alpha_index); // Next number to append
                System.out.println("i) New Scope Name: " + new_scope_name);
            } else {
                System.out.println(", ends in number, ");
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
        System.out.println("Current scope has no parent");
        new_scope_name = current_scope_name + alpha.get(0); // ROOT
    }


    //NOTE:  Are changes being recorded within the HashMap or do they need to be manually done? ?????
    SymbolTableScope new_scope = new SymbolTableScope(new_scope_name);
    
    
    System.out.println("iii) New Scope Name: " + new_scope_name);
    new_scope.setScopeParent(current_scope); // Dive in
    current_scope.addScopeChild(new_scope);
    setCurrentScope(new_scope);
    table.put(new_scope_name, new_scope);

}
**/