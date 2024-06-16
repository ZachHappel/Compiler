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
        try { insertScope(root_scope_name, root_scope); } catch (SemanticAnalysisException e) {System.out.println("Err");}
    }


    public SymbolTableScope getScope(String scope_id) {
        return table.get(scope_id);
    }


    public void exitScope () {
        setCurrentScope(current_scope.getScopeParent()); 
    }

    public String getCurrentScopeName () {
        return current_scope.getName();
    }


    public boolean endsInLetter (String s) {
        String last_char = "" + s.charAt(s.length() - 1);
        if (alpha.contains(last_char)) {
            //System.out.print(", Alpha contains: " + last_char + ", returning true");
            return true;
        } else {
            //System.out.println(", Alpha DOES NOT contains: " + last_char + ", returning false");
            return false;
        }
    }


    public void setCurrentScope(SymbolTableScope scope) {
        this.current_scope = scope; 
    }

    public void createNewScope() {
        String new_scope_name = "";
        String current_scope_name = this.current_scope.getName();
        //System.out.println("Current Scopes: " + getScopeNames()); 
        //System.out.println("Current Scope Name: " + current_scope_name);


        // Need to remediate root scope not having parent 
        // Need to prevent integer overflow if more than 9 siblings or 25 siblings, depending on whether number or letter (Check: is actually limited to 9? )
        if ( current_scope.hasParent() )  {
           // System.out.print("Current scope has parent: " + current_scope.getScopeParent().getName() + ", ");
            SymbolTableScope currents_parent = current_scope.getScopeParent();
            if (currents_parent.hasChildren()) {
                //System.out.print("the current scope's parent has children, ''" + currents_parent.getAllChildrenNames() + "'', ");
                //System.out.print(", current scope's children, ''" + current_scope.getAllChildrenNames() + "'', ");
                //Siblings exist
                
                if (current_scope.hasChildren()) {
                    SymbolTableScope last_child = current_scope.getLastChild();
                    String last_child_name = last_child.getName(); 
                    //System.out.print("last child name: " + last_child_name);
                    if ( endsInLetter(last_child_name) ) {
                        //System.out.println(", ends in letter, ");
                        String last_letter = String.valueOf(last_child_name.charAt(last_child_name.length() - 1));
                        int next_alpha_index = alpha.indexOf(last_letter) + 1;
                        new_scope_name = last_child_name.substring(0, last_child_name.length() - 1) + alpha.get(next_alpha_index); // Next letter to append
                        //System.out.println("i) New Scope Name: " + new_scope_name);
                    
                    } else {
                        //System.out.println("~ends in number, ");
                        String last_number = String.valueOf(last_child_name.charAt(last_child_name.length() - 1));
                        int last_number_as_int = Integer.parseInt(last_number);  
                        int next_alpha_index = numbs.indexOf(last_number_as_int) + 1;
                        new_scope_name = last_child_name.substring(0, last_child_name.length() - 1) + numbs.get(next_alpha_index); // Next number to append
                        //System.out.println("ii) New Scope Name: " + new_scope_name);
                    }

                } else {
                    //System.out.println("Xxx"); 
                    SymbolTableScope last_child = currents_parent.getLastChild();
                    String last_child_name = last_child.getName(); 
                    //System.out.print("Xxx last child name: " + last_child_name);
                    
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
            //System.out.println("Current scope has no parent");
            new_scope_name = current_scope_name + alpha.get(0); // ROOT
        }


        SymbolTableScope new_scope = new SymbolTableScope(new_scope_name);
        
        //System.out.println("iii) New Scope Name: " + new_scope_name);
        new_scope.setScopeParent(current_scope); // Dive in
        current_scope.addScopeChild(new_scope);
        setCurrentScope(new_scope); // Set new_scope as the current_scope
        calculateAccessibleScopes(current_scope); // Figure out which scopes the current (prev. "new_scope") has access to
        table.put(new_scope_name, new_scope); // Add scope to table

    }

    // Regarding Current Scope; Which scopes does it have acces to? 
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
        //System.out.println("Accessible Scope Names: " + scope.getAllAccessibleScopesNames()); 
    }
    
    public void insertScope (String name, SymbolTableScope scope) throws SemanticAnalysisException {
        if (!this.table.containsKey(name)) {
            this.table.put(name, scope);
            if (! (current_scope != null)) setCurrentScope(table.get(name));
            else throw new SemanticAnalysisException("SymbolTable, insertScope()", "Unable to insert scope into Symbol Table.");
        } else throw new SemanticAnalysisException("SymbolTable, insertScope()", "Unable to insert scope into Symbol Table.");
        
    }

    public String getScopeNames () {
        String scope_names = "";
        for (Map.Entry<String, SymbolTableScope> scope_n : table.entrySet()) {
            scope_names+= scope_n.getKey() + ", ";
        }   return scope_names; 
    }

    public String getScopesAndEntries () {
        String scopes_and_entries = "";
        for (Map.Entry<String, SymbolTableScope> scope_n : table.entrySet()) {
            scopes_and_entries = scopes_and_entries + "\nScope: " + scope_n.getKey() + "\n" + scope_n.getValue().getEntriesAndTheirDetails();
            
        }   return scopes_and_entries; 
    }

    public int getScopeCount () {return table.size();}

    // Check to see if variable already exists with the same ID 
    // Actually not good-- You can have two declarations of a variable with same ID as long as scopes do not interfere
    // Meaning, as long as it is not in accessible scopes
    public boolean existsWithinSymbolTable (Terminal identifier_terminal) {
        String identifier_value = identifier_terminal.getTokenAttribute();
        boolean exists_within_a_scope = false;
        for (Map.Entry<String, SymbolTableScope> scope_n : table.entrySet()) {
            if (scope_n.getValue().entryExists(identifier_value)) {
                exists_within_a_scope = true;
            }
        } return exists_within_a_scope; 
    } 

    // Checks to see if variable has already been declared in the current scope's accessible scopes, if true that means that the current vardecl is invalid
    public boolean existsWithinAccessibleScopes (Terminal identifier_terminal)  {
       
        String identifier_value = identifier_terminal.getTokenAttribute();
        ArrayList<SymbolTableScope> current_scope_accessibles = current_scope.getAccessibleScopes();
        boolean exists_within_a_scope = false;

        if (current_scope.entryExists(identifier_value)) {
            return true; // If in current scope, return
        } 
        
        for (int s = 0; s <= current_scope_accessibles.size() - 1; s++) {
            SymbolTableScope scope = current_scope_accessibles.get(s);
            if (scope.entryExists(identifier_value)) {
                exists_within_a_scope = true;
            }
        } 
        
        return exists_within_a_scope; 
    } 

   

    public boolean existsWithinCurrentScope (Terminal identifier_terminal) {
       
        String identifier_value = identifier_terminal.getTokenAttribute();
        ArrayList<SymbolTableScope> current_scope_accessibles = current_scope.getAccessibleScopes();
        boolean exists_within_a_scope = false;
        
        if (current_scope.entryExists(identifier_value)) {
            //System.out.println("ID Value: " + identifier_value + " exists in current scope"); 
            return true; // If in current scope, return
        } else return false; 
        /**
        for (int s = 0; s <= current_scope_accessibles.size() - 1; s++) {
            SymbolTableScope scope = current_scope_accessibles.get(s);
            if (scope.entryExists(identifier_value)) {
                exists_within_a_scope = true;
            }
        } return exists_within_a_scope; 
        **/
    } 


    public SymbolTableEntry retrieveEntryFromAccessibleScopes (Terminal identifier_terminal) {
        String identifier_value = identifier_terminal.getTokenAttribute(); // Id
        ArrayList<SymbolTableScope> current_scope_accessibles = current_scope.getAccessibleScopes(); // Obtain which it has reach/access to
        
        if (current_scope.entryExists(identifier_value)) return current_scope.retrieveEntry(identifier_value); // If in current scope, return
        
        // Iterate over list of scopes in which the current scope has access to
        for (int s = 0; s <= current_scope_accessibles.size() - 1; s++) {
            SymbolTableScope scope = current_scope_accessibles.get(s);
            if (scope.entryExists(identifier_value)) {
                SymbolTableEntry entry = scope.retrieveEntry(identifier_value); // If exists, retrieve it
                return entry; 
            }
        } return new SymbolTableEntry("", false, false); // otherwise return blank SymbolTableEntry with "" as type
    }


    public SymbolTableScope getScopeLocation (Terminal identifier_terminal) throws SemanticAnalysisException {
        String identifer_value = identifier_terminal.getTokenAttribute();
        ArrayList<SymbolTableScope> current_scope_accessibles = current_scope.getAccessibleScopes(); // Obtain which it has reach/access to

        if (current_scope.entryExists(identifer_value)) return current_scope;

        for (int s = 0; s <= current_scope_accessibles.size() - 1; s++ ) {
            SymbolTableScope scope = current_scope_accessibles.get(s); 
            if (scope.entryExists(identifer_value)) {
                return scope;
            }
        }

        // If Identifier_Terminal's ID cannot be located within accessible scopes,
        // something went wrong and an exception needs to be thrown
        throw new SemanticAnalysisException(
            "SymbolTable, getScopeLocation()",
            "Unable to location scope for identifier value: " + identifer_value
            );
    }


    // Finds entry of identifier value, returns the type associated with
    public String getTypeFromAccessibleScopes (Terminal identifier_terminal) {
        SymbolTableEntry entry = retrieveEntryFromAccessibleScopes(identifier_terminal);
        String type = entry.getType();
        return type;
    }

    // Returns first occurence, from current scope backtracking upwards through the scope
    // If already declared in current scope, returns name of current scope
    public String getConflictingScopeName (Terminal identifier_terminal) throws SemanticAnalysisException{
        ArrayList<SymbolTableScope> current_scope_accessibles = current_scope.getAccessibleScopes(); // Obtain which it has reach/access to
        String identifier_value = identifier_terminal.getTokenAttribute(); // Id 
        if (current_scope.entryExists(identifier_value)) {
            return current_scope.getName(); // Scope where entry with same identifer exists
        } else {
            for (int s = 0; s <= current_scope_accessibles.size() - 1; s++) {
                SymbolTableScope scope = current_scope_accessibles.get(s);
                if (scope.entryExists(identifier_value)) {
                    return scope.getName(); // Scope where entry with same identifer exists
                    
                }
            }
        } throw new SemanticAnalysisException("SymbolTable, getConflictingScopeName()", "Unable to locate conflicting scope name.");
    }



    
    // Returns true if type = any of the variations 
    // Used for comparing types and assignments, ensuring they are correctly associated
    public boolean isValidAssignment (String type, String assignment_type) {
        // type: int, string, boolean
        
        if ( type.equals("boolean") && ( 
            (assignment_type.equals("KEYWORD_TRUE") || 
            (assignment_type.equals("KEYWORD_FALSE")) ||
            (assignment_type.equals("boolean"))  ))) {
            return true; 
        }

        if ( type.equals("int") && ( 
            (assignment_type.equals("DIGIT") || 
            (assignment_type.equals("int"))))) {
            return true; 
        }

        if ( type.equals("string") && (
            (assignment_type.equals("CHARACTER")) ||
            (assignment_type.equals("string"))) ) {
                return true; 
            }
        
        return false;
    }

    public boolean existsWithinAccessibleScopesAndValidAssignment(Terminal identifier_terminal, String assignment_type) {

        SymbolTableEntry e = retrieveEntryFromAccessibleScopes(identifier_terminal); 
        String entry_type = e.getType(); 
    if (entry_type.equals("")) return false; else { /*System.out.println("Located type: "+ entry_type)*/;} /*System.out.println("Btw.. Assignment_Type: " + assignment_type);*/// retrive..() returns false if not found
        boolean is_valid = isValidAssignment(entry_type, assignment_type);
        return is_valid;

    }

    public void setAsUsed(Terminal identifier_terminal) {
        SymbolTableEntry retrieved_entry = retrieveEntryFromAccessibleScopes(identifier_terminal);
        retrieved_entry.setisUsed(true);
    }

    public void setAsInitialized(Terminal identifier_terminal) {
        SymbolTableEntry retrieved_entry = retrieveEntryFromAccessibleScopes(identifier_terminal);
        retrieved_entry.setIsInitialized(true);
    }


    // Creates entry in current scope. First checks if there exists conflicts with scopes accessible to current scope, or the current scope itself
    public void performEntry(Terminal type_terminal, Terminal identifier_terminal) throws SemanticAnalysisException {
        //System.out.println("Preparing to add into Symbol Table: " + type_terminal.getTokenAttribute() + " " + identifier_terminal.getTokenAttribute());
        String type_value = type_terminal.getTokenAttribute(); // Type
        String identifier_value = identifier_terminal.getTokenAttribute(); // Id 
        if (!existsWithinCurrentScope(identifier_terminal)) {
            current_scope.createAndInsertEntry(type_value, identifier_value, false, false); //Change
        } else {
            throw new SemanticAnalysisException
            ("SymbolTable, performEntry()", "Variable already exists with ID, " + identifier_value + ". Conflicting declaration exists within Scope: " + getConflictingScopeName(identifier_terminal) );
        }
    }

    public SymbolTableEntry retrieveEntryFromAccessibleScopes (SymbolTableScope scope, Terminal identifier_terminal) {
        String identifier_value = identifier_terminal.getTokenAttribute(); // Id
        ArrayList<SymbolTableScope> current_scope_accessibles = scope.getAccessibleScopes(); // Obtain which it has reach/access to
        
        if (scope.entryExists(identifier_value)) return scope.retrieveEntry(identifier_value); // If in current scope, return
        
        // Iterate over list of scopes in which the current scope has access to
        for (int s = 0; s <= current_scope_accessibles.size() - 1; s++) {
            SymbolTableScope scope_n = current_scope_accessibles.get(s);
            if (scope_n.entryExists(identifier_value)) {
                SymbolTableEntry entry = scope_n.retrieveEntry(identifier_value); // If exists, retrieve it
                return entry; 
            }
        } return new SymbolTableEntry("", false, false); // otherwise return blank SymbolTableEntry with "" as type
    }

    

    public SymbolTableScope retrieveScopeWithNearestDeclaration (NonTerminal parent, Terminal identifier) {
        SymbolTableScope parent_scope = getScope(parent.getScopeName());
        String identifier_id = identifier.getTokenAttribute();
        
        if (parent_scope.entryExists(identifier_id)) return parent_scope; 
        
        else { 
            ArrayList<SymbolTableScope> accessible_scopes = parent_scope.getAccessibleScopes();
            for (int s = 0; s <= accessible_scopes.size() - 1; s++) {
                SymbolTableScope scope_n = accessible_scopes.get(s);
                if (scope_n.entryExists(identifier_id)) {
                    return scope_n; 
                }
            }
        } return new SymbolTableScope("error");
    }


    public String givenScopeFindScopeLocationOfID (String scope_name, String identifier_value) {
        SymbolTableScope located_scope; 
        
        for (Map.Entry<String, SymbolTableScope> scope_n : table.entrySet()) {
            if (scope_n.getKey().equals(scope_name)) {
                located_scope = scope_n.getValue(); 
                ArrayList<SymbolTableScope> located_scope_accessibles = located_scope.getAccessibleScopes();
                if (located_scope.entryExists(identifier_value)) return located_scope.getName(); // If in current scope, return
                else { 
                    for (int s = 0; s <= located_scope_accessibles.size() - 1; s++) {
                        SymbolTableScope scope = located_scope_accessibles.get(s);
                        if (scope.entryExists(identifier_value)) {
                            return scope.getName(); 
                        }
                    } 
                }
                

            }  
        }  return "";
    }


}

