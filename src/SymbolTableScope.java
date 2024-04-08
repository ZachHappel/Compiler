import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTableScope {
    
    String name;
    SymbolTableScope parent;
    ArrayList<SymbolTableScope> children = new ArrayList<SymbolTableScope>();
    Map<String, SymbolTableEntry> entries = new HashMap<>();

    public SymbolTableScope (String name) {
        setName(name);
    }

    public void setName (String name) {
        this.name = name;
    }

    public void setScopeParent (SymbolTableScope x) {this.parent = x;}
    public void addScopeChild (SymbolTableScope x) {this.children.add(x);}
    public ArrayList<SymbolTableScope> getScopeChildren () {return this.children;}


    public void createAndInsertEntry (String id, String type, boolean is_initialized, boolean is_used) {
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


}
