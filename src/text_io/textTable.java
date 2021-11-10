
package text_io;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import org.apache.commons.lang3.ArrayUtils;

/**
 * This class defines a class for writing data to a text file in a tabular format.
 * <br><br>
 * Date Modified: 12/08/2015
 * 
 * @author Michael Birch
 */
public class textTable {
    private List<textTableCell> data;
    private List<Integer> hruleList; //list of rows after which to put a row of dashes
    private int nrow, ncol;
    
    /**
     * Default constructor. Creates an empty table with no rows or columns
     */
    public textTable(){
        this.nrow = 0;
        this.ncol = 0;
        this.data = new ArrayList<>();
        this.hruleList = new ArrayList<>();
    }
    
    /**
     * Returns a sorted List with duplicates removed
     * @param l List to be sorted/stripped of duplicates
     * @return a sorted List with duplicates removed
     */
    public final List<Integer> unique(List<Integer> l){
        Set<Integer> hs;
        List<Integer> result;
        
        hs = new HashSet<>();
        hs.addAll(l);
        result = new ArrayList<>();
        result.addAll(hs);
        Collections.sort(result);
        
        return result;
    }
    
    /**
     * Determines which positions in the String s would
     * be column separators for the table. The column separator is a
     * double space.
     * @param s String representing one row of the table
     * @return List of positions where column breaks should be
     */
    public final List<Integer> columnBreaks(String s){
        int doubleSpaceIndex;
        String tmp;
        List<Integer> result;
        
        result = new ArrayList<>();
        tmp = s.replaceFirst("\\s+$", ""); //remove trailing spaces only
        doubleSpaceIndex = tmp.indexOf("  ");
        
        while(doubleSpaceIndex != -1){
            tmp = tmp.substring(doubleSpaceIndex).trim();
            result.add(s.indexOf(tmp));
            doubleSpaceIndex = tmp.indexOf("  ");
        }
        
        return result;
    }
    
    /**
     * Returns the substring of s which begins at the character with index
     * <code>i</code> and extends to the character at index <code>j - 1</code>.
     * If either of the specified indices are out of bounds for the String then
     * the largest substring consistent with those bounds is returned. I.e.
     * if <code>i &lt; 0</code> then <code>i</code> is set to 0, if <code>i
     * </code> is greater than or equal to the length of the String then the
     * empty String is returned, if <code>j</code> is greater than the length
     * of the String then <code>j</code> is set to be equal to the length of
     * the String.
     * @param s the String to take the substring from
     * @param i the beginning index, inclusive.
     * @param j the ending index, exclusive.
     * @return the specified substring.
     */
    private String substring(String s, int i, int j){
        if(s.equals("")){
            return "";
        }
        if(i < 0){
            i = 0;
        }else if(i >= s.length() || i > j){
            return "";
        }
        if(j > s.length()){
            j = s.length();
        }
        return s.substring(i, j);
    }
    /**
     * Returns the substring of s which begins at the character with index
     * <code>i</code> and extends to the end of the String. If the index is
     * out of bounds then the largest substring consistent with that index
     * is returned.
     * @param s the String to take the substring from
     * @param i the beginning index, inclusive.
     * @return the specified substring.
     */
    private String substring(String s, int i){
        return substring(s, i, s.length());
    }
    
    /**
     * Constructs a text table object from a given list of Strings. Each
     * element of the list represents a row of the table. If there are more
     * than <code>maxCols</code> columns detected then column breaks are
     * removed from the left. Columns are determined by a double space,
     * unless <code>tabSeparated</code> is <code>true</code>, in which case
     * columns are separated by a single tab.
     * @param lines List of Strings representing the rows of the table
     * @param maxCols the maximum number of columns allowed in the table
     * @param tabSeparated if <code>true</code> then a tab characters is used
     * as the column separator, otherwise a double space is used.
     */
    public textTable(List<String> lines, int maxCols, boolean tabSeparated){
        List<Integer> colSep;
        Iterator<Integer> it;
        Integer sep;
        int i, j, rowOffset;
        String s, cell;
        
        if(tabSeparated){
            this.parseTabSeparatedLines(lines, maxCols);
            return;
        }
        
        colSep = new ArrayList<>();
        for(String str : lines){
            if(!str.contains(" ")){
                continue;
            }
            colSep.addAll(columnBreaks(str));
        }
        colSep = unique(colSep);
        
        //remove any colum separators which split a line incorrectly
        for(String str : lines){
            it = colSep.iterator();
            while(it.hasNext()){
                sep = it.next();
                if(sep >= str.length()){
                    continue;
                }
                if(sep == 0 || str.charAt(sep-1) != ' '){
                    it.remove();
                }
            }
        }
            
        //remove column separators if there are too many
        while(colSep.size() > maxCols-1){
            colSep.remove(0);
        }
        
        this.ncol = colSep.size() + 1;
        this.nrow = lines.size();
        this.data = new ArrayList<>();
        this.hruleList = new ArrayList<>();
        rowOffset = 0;
        for(i=0; i < lines.size(); i++){
            s = lines.get(i);
            if(s.contains("-") && s.replace("-", "").equals("")){
                //hrule lines do not contribute to row count
                this.nrow -= 1;
                rowOffset += 1;
                this.hruleList.add(i - rowOffset);
            }
            if(colSep.isEmpty()){
                this.data.add(new textTableCell(i - rowOffset, 0, s.trim()));
            }else{
                for(j=0; j<colSep.size(); j++){
                    if(j==0){
                        cell = substring(s, 0, colSep.get(j)).trim();
                    }else{
                        cell = substring(s, colSep.get(j-1), colSep.get(j)).trim();
                    }
                    this.data.add(new textTableCell(i - rowOffset, j, cell));
                }
                cell = substring(s, colSep.get(colSep.size()-1)).trim();
                this.data.add(new textTableCell(i - rowOffset, colSep.size(), cell));
            }
        }
    }
    /**
     * Constructs a text table object from a given list of Strings. Each
     * element of the list represents a row of the table. If there are more
     * than <code>maxCols</code> columns detected then column breaks are
     * removed from the left. The column separator is a double space
     * @param lines List of Strings representing the rows of the table
     * @param maxCols the maximum number of columns allowed in the table
     */
    public textTable(List<String> lines, int maxCols){
        this(lines, maxCols, false);
    }
    /**
     * Constructs a text table object from a given list of Strings. Each
     * element of the list represents a row of the table. The column separator
     * used is a double space. The maximum number of columns is 1,000,000.
     * @param lines List of Strings representing the rows of the table
     */
    public textTable(List<String> lines){
        this(lines, 1000000, false);
    }
    
    /**
     * Constructs a text table object from a given list of Strings. Each
     * element of the list represents a row of the table. The column separator
     * used is a tab character.
     * @param lines List of Strings representing the rows of the table
     * @param maxCols the maximum number of columns allowed in the table
     */
    public final void parseTabSeparatedLines(List<String> lines, int maxCols){
        int rowOffset, i, j;
        String s;
        String[] tabSplit;
        
        this.ncol = 1;
        this.nrow = lines.size();
        this.data = new ArrayList<>();
        this.hruleList = new ArrayList<>();
        rowOffset = 0;
        
        for(i=0; i < lines.size(); i++){
            s = lines.get(i).trim();
            if(s.equals("")){ //skip blank lines
                continue;
            }
            if(s.contains("-") && s.replace("-", "").equals("")){
                //hrule lines do not contribute to row count
                this.nrow -= 1;
                rowOffset += 1;
                this.hruleList.add(i - rowOffset);
            }
            tabSplit = s.split("\t");
            if(tabSplit.length > maxCols){
                //remove column separators from the left if there are too
                //many columns
                while(tabSplit.length > maxCols){
                    tabSplit[1] = tabSplit[0] + "\t" + tabSplit[1];
                    tabSplit = ArrayUtils.remove(tabSplit, 0);
                }
            }
            if(tabSplit.length > this.ncol){
                this.ncol = tabSplit.length;
            }
            for(j=0; j<tabSplit.length; j++){
                this.data.add(new textTableCell(i - rowOffset, j, tabSplit[j].trim()));
            }
        }
    }

    /**
     * Returns <code>true</code> if there is no data in the table.
     * @return <code>true</code> if there is no data in the table
     */
    public boolean isEmpty(){
        return this.data.isEmpty();
    }
    
    /**
     * Returns <code>true</code> if there is a {@link textTableCell} object
     * with indices (r,c).
     * @param r row index
     * @param c column index
     * @return <code>true</code> if there is a {@link textTableCell} object
     * with indices (r,c).
     */
    public boolean cellExists(int r, int c){
        if(this.isEmpty()){
            return false;
        }
        
        for(textTableCell cell : this.data){
            if(cell.isAt(r, c)){
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Sets the contents of the cell at row index r and column index c. If
     * no cell exists at that index then a new {@link textTableCell} object
     * is created.
     * @param r row index
     * @param c column index
     * @param content String containing the contents of the cell
     */
    public void setCell(int r, int c, String content){
        boolean contentsSet;
        this.nrow = Math.max(this.nrow, r+1);
        this.ncol = Math.max(this.ncol, c+1);
        
        contentsSet = false;
        for(textTableCell cell : this.data){
            if(cell.isAt(r, c)){
                cell.content = new String(content);
                contentsSet = true;
            }
        }
        
        if(!contentsSet){
            data.add(new textTableCell(r, c, content));
        }
    }
    
    /**
     * Concatenates the String <code>content</code> to the cell with indices
     * (r,c). If no cell exists at (r,c) then a new {@link textTableCell} object
     * is created and its contents are set to <code>content</code>.
     * @param r row index
     * @param c column index
     * @param content String containing the content to append to the cell
     */
    public void appendCell(int r, int c, String content){
        boolean contentsSet;
        this.nrow = Math.max(this.nrow, r+1);
        this.ncol = Math.max(this.ncol, c+1);
        
        contentsSet = false;
        for(textTableCell cell : this.data){
            if(cell.isAt(r, c)){
                cell.content = cell.content.concat(content);
                contentsSet = true;
            }
        }
        
        if(!contentsSet){
            data.add(new textTableCell(r, c, content));
        }
    }
    
    /**
     * Causes a horizontal rule (ie row of dashes) to be written AFTER the
     * row with index r when the table is printed.
     * @param r row index
     */
    public void addHrule(int r){
        this.hruleList.add(r);
    }
    
    public String getCell(int r, int c){
        for(textTableCell cell : this.data){
            if(cell.isAt(r, c)){
                return cell.content;
            }
        }
        
        return "";
    }
    
    /**
     * Returns the number of columns in the table.
     * @return the number of columns in the table
     */
    public int getncol(){
        return this.ncol;
    }
    /**
     * Returns the number of rows in the table.
     * @return the number of rows in the table
     */
    public int getnrow(){
        return this.nrow;
    }
    
    /**
     * Returns a 2D String array where each element as the contents 
     * of a cell in the table.
     * @return a 2D String array where each element as the contents 
     * of a cell in the table
     */
    public String[][] toArray(){
        String[][] result;
        int i, j;
        
        result = new String[this.nrow][this.ncol];
        
        for(i=0; i<this.nrow; i++){
            for(j=0; j<this.ncol; j++){
                result[i][j] = this.getCell(i, j);
            }
        }
        
        return result;
    }
    
    /**
     * Prints the table, where each row is represented as a String in the
     * returned List. The columns of the table are separated by spaces.
     * @return a List of Strings, where each element of the list represents a
     * row of the table.
     */
    public List<String> toStringList(){
        String[][] tableArr;
        int[] minColWidth;
        int i, j, currentCol;
        List<String> result;
        String row;
        
        tableArr = this.toArray();
        minColWidth = new int[this.ncol];
        result = new ArrayList<>();
        
        //Determin minimum column width so that all text fits into each column
        for(j=0; j<this.ncol; j++){
            minColWidth[j] = 2;
            for(i=0; i<this.nrow; i++){
                try{
                    minColWidth[j] = Math.max(tableArr[i][j].length()+2, 
                            minColWidth[j]);
                }catch(NullPointerException e){
                    minColWidth[j] = Math.max(2, minColWidth[j]);
                }
            }
        }
        
        for(i=0; i<this.nrow; i++){
            row = "";
            currentCol = 0; //equal to the sum of minColWidth for all columns added to the row string so far 
            for(j=0; j<this.ncol; j++){
                currentCol += minColWidth[j];
                row += tableArr[i][j];
                while(row.length() < currentCol){
                    row += " ";
                }
            }
            result.add(row);
            if(hruleList.contains(i)){
                row = "";
                while(row.length() < currentCol){
                    row += "-";
                }
                result.add(row);
            }
        }
        
        return result;
    }
    
    /**
     * Prints the table, where each row is represented as a String in the
     * returned List.
     * @param tabSeparated if <code>true</code> then the columns will
     * be separated by tabs. The columns will be separated by spaces otherwise
     * @return a List of Strings, where each element of the list represents a
     * row of the table
     */
    public List<String> toStringList(boolean tabSeparated){
        int i, j, rowLength;
        String row;
        List<String> result;
        String[][] tableArr;
        
        //if not tab separated then use the usual space separated printing routine
        if(!tabSeparated){
            return toStringList();
        }
        
        tableArr = this.toArray();
        result = new ArrayList<>();
        
        for(i=0; i<this.nrow; i++){ //loop over rows of the table
            row = tableArr[i][0];
            for(j=1; j<this.ncol; j++){
                //add each column of the current row, separated by tabs
                row += "\t" + tableArr[i][j];
            }
            result.add(row);
            if(hruleList.contains(i)){
                rowLength = row.length();
                row = "";
                for(j=0; j<rowLength; j++){
                    row += "-";
                }
                result.add(row);
            }
        }
        
        return result;
    }
}
