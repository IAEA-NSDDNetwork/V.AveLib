package visualaveraginglibrary;

import ensdf_datapoint.dataPt;
import java.util.List;
import java.util.ArrayList;
import text_io.textTable;

/**
 * This class contains methods for generating GNU Plot scripts to visualize
 * the data and averages.
 * 
 * Date Modified: 17/11/2015
 * 
 * @author Michael Birch
 */

public class plotting {
    /**
     * Creates the data file used by GNU Plot for plotting
     * @param data the dataset used to obtain the average
     * @return List of Strings which can be written to create the data file
     */
    public static List<String> generateDataFile(dataPt[] data){
        textTable result = new textTable();
        dataPt d;
        String quote = "\"";
        
        for(int i=0; i<data.length; i++){
            d = data[i];
            result.setCell(i, 0, String.valueOf(i+1));
            result.setCell(i, 1, quote + d.getName() + quote);
            result.setCell(i, 2, String.valueOf(d.getValue()));
            result.setCell(i, 3, String.valueOf(d.getUpper()));
            result.setCell(i, 4, String.valueOf(d.getLower()));
        }
        
        return result.toStringList();
    }
    
    /**
     * Creates the GNU Plot script which can plot the data contained in
     * the specified data file and average.
     * @param average the average of the data (obtained by any method)
     * @param datasetTitle the title of the dataset being plotted (data
     * file name is then '[datasetTitle]_data.dat').
     * @param rotateXLabels if <code>True</code> then the tick mark labels
     * on the x-axis will be rotated 45 degrees to the right.
     * @return List of Strings which can be written to create the script
     */
    public static List<String> generateScript(dataPt average, String datasetTitle,
            Boolean rotateXLabels){
        List<String> result;
        String dataFileName = datasetTitle + "_data.dat";
        
        result = new ArrayList<>();
        
        result.add("set terminal pngcairo");
        result.add("set output '" + datasetTitle + ".png'");
        result.add("set autoscale");
        result.add("stats '" + dataFileName + "' using 1 nooutput");
        result.add("xmax=STATS_max+1");
        result.add("stats '" + dataFileName + "' using ($3+$4):($3-$5) nooutput");
        result.add("yrange=STATS_max_x - STATS_min_y");
        result.add("ymin=STATS_min_y - 0.05*abs(yrange)");
        result.add("ymax=STATS_max_x + 0.05*abs(yrange)");
        result.add("set xrange [0:xmax]");
        result.add("set yrange [ymin:ymax]");
        result.add("set style fill pattern 2");
        result.add("");
        result.add("f(x) = " + String.valueOf(average.getValue()));
        result.add("g(x) = " + String.valueOf(average.getValue() - average.getLower()));
        result.add("h(x) = " + String.valueOf(average.getValue() + average.getUpper()));
        result.add("");
        if(rotateXLabels){
            result.add("set xtics rotate by 45 right");
        }
        result.add("plot '+' using 1:(g($1)):(h($1)) title '" + average.getName() + 
                " Uncert.' with filledcurves \\");
        result.add("  linetype rgb 'light-red', \\");
        result.add("   f(x) title '" + average.getName() + 
                "' with lines linetype rgb 'red' linewidth 2, \\");
        result.add("  '" + dataFileName + 
                "' using 1:3:($3+$4):($3-$5):xtic(2) title 'Data' with yerrorbars \\");
        result.add("  pointtype 7 linetype -1");
        
        return result;
    }
}
