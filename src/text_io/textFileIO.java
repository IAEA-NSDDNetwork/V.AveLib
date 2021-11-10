
package text_io;

import java.io.IOException;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;

/**
 * Defines methods for reading and writing text files.
 * <br><br>
 * Date Modified: 12/08/2015
 * 
 * @author Michael Birch
 */
public class textFileIO {
    final static Charset ENCODING = StandardCharsets.UTF_8;
    
    /**
     * Returns <code>true</code> when the file specified by the given
     * String exists.
     * @param file path to the file
     * @return <code>true</code> when the file specified by the given
     * String exists.
     */
    public static final boolean exist(String file){
        File f = new File(file);
        return f.exists() && !f.isDirectory();
    }
    
    /**
     * Reads the text file specified by the given String. Returns each line
     * as a separate String in the returned List.
     * @param file path to the text file to read
     * @return a list with each String being one line of the file
     * @throws IOException 
     */
    public static final List<String> read(String file) throws IOException {
        Path path = Paths.get(file);
        return Files.readAllLines(path, ENCODING);
    }
    
    /**
     * Writes each String in the given List as a single line in the file
     * specified by the given String. This method will overwrite the file 
     * if it already exists.
     * @param lines the contents of the file to write
     * @param file the path of the file to write to
     * @throws IOException 
     */
    public static final void write(List<String> lines, String file) throws IOException {
        Path path = Paths.get(file);
        Files.write(path, lines, ENCODING);
    }
    
    /**
     * Writes each String in the given List as a single line in the file
     * specified by the given String. This method will overwrite the file
     * if it already exists, unless the <code>append</code> argument 
     * is <code>true</code>.
     * @param lines the contents of the file to write
     * @param file the path of the file to write to
     * @param append set to <code>true</code> if you do not wish to overwrite
     * the file if it already exists
     * @throws IOException 
     */
    public static final void write(List<String> lines, String file, boolean append) throws IOException {
        Path path = Paths.get(file);
        if(append){
            Files.write(path, lines, ENCODING, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        }else{
            Files.write(path, lines, ENCODING, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }
    
    /**
     * Writes each String in the given array as a single line in the file
     * specified by the given String. This method will overwrite the file 
     * if it already exists.
     * @param lines the contents of the file to write
     * @param file the path of the file to write to
     * @throws IOException 
     */
    public static final void write(String[] lines, String file) throws IOException{
        int i;
        List<String> linesList;
        
        linesList = new ArrayList<>();
        for(i=0; i < lines.length; i++){
            linesList.add(lines[i]);
        }
        
        write(linesList, file);
    }
    
    /**
     * Writes each String in the given array as a single line in the file
     * specified by the given String. This method will overwrite the file
     * if it already exists, unless the <code>append</code> argument 
     * is <code>true</code>.
     * @param lines the contents of the file to write
     * @param file the path of the file to write to
     * @param append set to <code>true</code> if you do not wish to overwrite
     * the file if it already exists
     * @throws IOException 
     */
    public static final void write(String[] lines, String file, boolean append) throws IOException{
        int i;
        List<String> linesList;
        
        linesList = new ArrayList<>();
        for(i=0; i < lines.length; i++){
            linesList.add(lines[i]);
        }
        
        write(linesList, file, append);
    }
}
