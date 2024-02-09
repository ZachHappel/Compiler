public class Token {
    
    public String name;
    public String attribute;
    public int position; // Index within src file as a whole
    public int line_number; 
    public int line_index; // Where on the line, with first char on line being 0

}
