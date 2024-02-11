public class LexemeInfo {


    private int[] indices;
    private int required_length;
    private int matched_length;
    private boolean specific_criteria_required;
    private boolean criteria_met;

    public LexemeInfo (int[] indices, int required_length, int matched_length) {
        this.indices = indices;
        this.required_length = required_length;
        this.matched_length = matched_length;
    }

}
