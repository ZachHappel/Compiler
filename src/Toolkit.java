public class Toolkit {
    public Boolean is_verbose;

    public Toolkit() {
        this.is_verbose = true; 
    }

    public Toolkit( Boolean is_verbose ) {
        this.is_verbose = is_verbose; 
    }

    public void setIsVerbose( Boolean is_verbose ) {
        this.is_verbose = is_verbose;
    }

    public boolean getIsVerbose() {
        return this.is_verbose;
    }

    public void output( String string ) {
        if (this.is_verbose) System.out.println(string);
    }

    public void endProgram(String reason) {
            this.output("Ending execution, " + reason);
            System.exit(0);
    }
    public static int CountOccurrencesOfByteDecimalValue (byte[] arr, int byte_decimal_value) {
        int occurrences = 0;
        for (int i = 0; i<= arr.length -1; i++) {
            int val = arr[i];
            boolean isDecVal = (val == byte_decimal_value);
            if (isDecVal) {
                occurrences = occurrences + 1;
            }
        }

        //System.out.println("Amount of Lines: " + line_amt);

        return occurrences; 
    }

    public static int CountLineFeeds (byte[] arr) {
        int amountOfLineFeeds = CountOccurrencesOfByteDecimalValue(arr, 10) + 1; // plus one because first line counts as 1
        return amountOfLineFeeds;
    }

    public static int[] GetIndicesOfLineFeeds (byte[] arr) {
        int line_feed_amt = CountLineFeeds(arr);
        int[] indices = new int[line_feed_amt];
        int amount_added = 0;
        
        for (int i = 0; i<= arr.length -1; i++) {
            int val = arr[i];
            boolean is_line_feed = (val == 10);
            if (is_line_feed) {
                indices[amount_added] = i;
                amount_added = amount_added + 1;  
            }
        }

        return indices;

    }

}
