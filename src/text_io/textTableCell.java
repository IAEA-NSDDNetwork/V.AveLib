
package text_io;

/**
 * This class defines the class used as cells in the 
 * {@link textTable} class.
 * <br><br>
 * Date Modified: 12/08/2015
 * 
 * @author Michael Birch
 */
public class textTableCell {
    public int row;
    public int col;
    public String content;
    
    /**
     * Default constructor, creates a blank cell at position (0,0).
     */
    public textTableCell(){
        this.row = 0;
        this.col = 0;
        this.content = "";
    }
    
    /**
     * Creates a cell at (r, c) with contents d.
     * @param r row index
     * @param c column index
     * @param d contents of the cell
     */
    public textTableCell(int r, int c, String d){
        this.row = r;
        this.col = c;
        this.content = new String(d);
    }
    
    /**
     * Creates a blank cell at (r, c).
     * @param r row index
     * @param c column index
     */
    public textTableCell(int r, int c){
        this(r, c, "");
    }
    
    /**
     * Returns <code>true</code> if the row and column indices are equal
     * to r and c, repectively.
     * @param r row index
     * @param c column index
     * @return <code>true</code> if the row and column indices are equal
     * to r and c, repectively.
     */
    public boolean isAt(int r, int c){
        return (this.row == r) && (this.col == c);
    }
}
