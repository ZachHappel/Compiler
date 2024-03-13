import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TreeDraw {
    


    public Map<Integer, ArrayList<String>> levels = new HashMap<>();
    public int max_width;

    public TreeDraw () { }


    
    public void createTreeLevelsHashMap (Production node, int index) {
        
        
        
        for ( int i = 0; i <= node.getChildren().size() - 1; i++ ) {
            Production child = node.getChild(i);
            
            System.out.print(index + " " + child.getName() + ", "); 
            
            if (!levels.containsKey(index)) {
                
                if (child.getClass().getSimpleName().equals("NonTerminal")) {
                    NonTerminal nt_child = (NonTerminal) child; 
                    levels.put(index, new ArrayList<String>() {{add( "[" + nt_child.getName() + "]" );}});
                } else {
                    Terminal t_child = (Terminal) child;
                    levels.put(index, new ArrayList<String>() {{add( "<" + t_child.getTokenAttribute() + ">" );}});
                }
                
            } else {
                
                ArrayList<String> current_entries = levels.get(index);

                if (child.getClass().getSimpleName().equals("NonTerminal")) {
                    NonTerminal nt_child = (NonTerminal) child; 
                    current_entries.add("[" + nt_child.getName() + "]" );
                } else {
                    Terminal t_child = (Terminal) child;
                    current_entries.add("<" + t_child.getTokenAttribute() + ">");
                }
                
                levels.put(index, current_entries);
            }
            
            createTreeLevelsHashMap(child, index + 1);
        }
    }


    public int getMaxWidth () {
        int max = 0;
        for (Map.Entry<Integer, ArrayList<String>> entry : levels.entrySet()) {
            if ( (entry.getValue()).size() > max ) max = (entry.getValue()).size();
            //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }
        return max;
    }


    public void draw (ArrayList<Production> tree) {
        System.out.println("\n(Incomplete) Pretty Tree Draw");
        //ArrayList<String> root_arraylist = new ArrayList<>() {{ add( (tree.get(0)).getName()); add((tree.get(1)).getName()); }};
        //levels.put(0, root_arraylist);
        Production root = tree.get(0);
        System.out.println("\nCreating Tree Levels HashMap"); 
        createTreeLevelsHashMap(root, 0); // 1 because we already added root arraylist
        max_width = getMaxWidth(); 
        System.out.println("\n\nMax Width: " + max_width);
        
        System.out.println("\nUnformatted Levels");
        for (Map.Entry<Integer, ArrayList<String>> entry : levels.entrySet()) System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
    }

}
