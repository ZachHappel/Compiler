import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTableScope {
    
    String name;
    SymbolTableScope parent;
    ArrayList<SymbolTableScope> accessible_scopes = new ArrayList<SymbolTableScope>();
    ArrayList<SymbolTableScope> children = new ArrayList<SymbolTableScope>();

        //id    data (type, init, used)
    Map<String, SymbolTableEntry> entries = new HashMap<>();

    public SymbolTableScope (String name) {
        setName(name);
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getName () {
        return this.name;
    }

    public void setScopeParent (SymbolTableScope x) {this.parent = x;}
    public void addScopeChild (SymbolTableScope x) {this.children.add(x);}
    public void addAccessibleScope (SymbolTableScope x) {this.accessible_scopes.add(x);}
    public ArrayList<SymbolTableScope> getAccessibleScopes () { return this.accessible_scopes; }

    public ArrayList<SymbolTableScope> getScopeChildren () {return this.children;}
    public boolean hasChildren () {
        //System.out.print(", hasChildren = " + this.children.size() + ", ");
        for (int i = 0; i <= children.size() - 1; i++) {
            System.out.println(children.get(i).getName()); 
        }
        return this.children.size() > 0 ? true : false;
     }
    public SymbolTableScope getLastChild () { return this.children.get(children.size() - 1);}

    public SymbolTableScope getScopeParent () {
        if (parent == null) System.out.println("ERROR: Parent unset"); // Unchecked
        return this.parent;

    }

    public boolean hasParent () {
        if (!(parent == null)) return true;
        else return false;
    } 
    
    
    public void createAndInsertEntry (String type, String id, boolean is_initialized, boolean is_used) {
        SymbolTableEntry new_entry = new SymbolTableEntry(type, is_initialized, is_used);
        this.entries.put(id, new_entry);
    }

    public void insertEntry (String id, SymbolTableEntry entry) {
        this.entries.put(id, entry);
    }

    public SymbolTableEntry retrieveEntry (String id) {
        return this.entries.get(id);
    }


    // && entryExists, entryExistsAndIsSameType to check if exists and is same type, both true 
    public boolean entryExists(String id) {
        return this.entries.containsKey(id);
    }

    public boolean entryExistsAndIsSameType (String id, String type) {
        boolean exists = entryExists(id);
        
        if (exists) {
            SymbolTableEntry entry = entries.get(id);
            String entry_type = entry.getType(); 
            if (type.equals(entry_type)) return true;
            else return false; // Different type //!!!!!! Throw error
        } else return false; // Does not exist in the table

    }

    public void updateEntryInitializationState (String id, boolean init_state) {
        entries.get(id).setIsInitialized(init_state);
    }

    public void updateEntryUsedState (String id, boolean used_state) {
        entries.get(id).setisUsed(used_state);
    }

    public String getAllChildrenNames () {
        String children_names = "";
        for (int i = 0; i <= children.size() - 1; i++) {
            children_names = children_names + children.get(i).getName();
        } return children_names;
    }

    public String getAllAccessibleScopesNames () {
        String accesible_scopes_names = "";
        for (int i = 0; i <= accessible_scopes.size() - 1; i++) {
            accesible_scopes_names += accessible_scopes.get(i).getName() + ", ";
        } return accesible_scopes_names;
    }

    public String getEntriesAndTheirDetails() {
        String entries_details = "";
        for (Map.Entry<String, SymbolTableEntry> entry_n : entries.entrySet()) {
           String e_name = entry_n.getKey(); // Name
           String e_details = entry_n.getValue().getDetailsString(); // Details of entry
           String name_and_details = e_name + " - " + e_details + "\n";
           entries_details+=name_and_details; 
        } return entries_details; 
    }

}
