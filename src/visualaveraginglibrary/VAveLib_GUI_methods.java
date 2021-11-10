
package visualaveraginglibrary;

import ensdf_datapoint.dataPt;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;


/*
 * Author: Michael Birch
 * Date Modified: 06/01/2015
 * Description:
 * This class defines methods for the V. Ave Lib GUI.
 */
public class VAveLib_GUI_methods {
    
    // creates an array of dataPt class objects from the input
    public static dataPt[] createDataset(String Data){
        int i, count;
        String[] lines;
        String line;
        List<dataPt> dataPoints;
        dataPt tmp;
        
        if(Data.equals("")){
            JOptionPane.showMessageDialog(null, "No data entered!");
            return null;
        }
        
        dataPoints = new ArrayList<>();
        lines = Data.split("\n");
        count = 1;
        for(i=0; i<lines.length; i++){
            if(lines[i].contains("#")){
                line = lines[i].split("#")[0];
            }else{
                line = lines[i];
            }
            if(line.trim().equals("")){
                continue; //skip blank lines
            }
            if(dataPt.isParsable(line.trim())){
                tmp = dataPt.constructFromString(line.trim());
                if(tmp.getName().equals("<default>")){
                    tmp.setName(String.valueOf(count));
                }
                dataPoints.add(new dataPt(tmp));
                count += 1;
            }else{
                JOptionPane.showMessageDialog(null, "Format error on line " + 
                        String.valueOf(i+1) + ". Input cannot be parsed.");
                return null;
            }
        }
        return dataPoints.toArray(new dataPt[0]);
    }
    
    private static String getValueString(String line){
        String result;
        
        if(line.contains(":")){
            result = line.split(":")[1].trim();
        }else{
            result = line.trim();
        }
        
        if(result.contains("(")){
            result = result.replace("(", " ");
        }
        
        result = result.split(" ")[0];
        
        if(result.contains("E")){ //ignore sci. not. exponent
            result = result.split("E")[0];
        }else if(result.contains("e")){
            result = result.split("e")[0];
        }
        
        return result;
    }
    
    private static String getChar(String s, int ind){
        return s.substring(ind, ind+1);
    }
    
    private static String getLastChar(String s){
        return getChar(s, s.length() - 1);
    }
    
    //return the place value of the least significant figure of a single number
    private static int line_leastSigFig(String line){
        boolean hasDecimal;
        int i;
        String decimalChar;
        
        decimalChar = dataPt.getDecimalChar();
        
        hasDecimal = line.contains(decimalChar);
        
        if(hasDecimal){
            // ends in decimal point
            if(getLastChar(line).equals(decimalChar)){
                return 0;
            }else{
                return line.indexOf(decimalChar) - (line.length() - 1);
            }
        }else{
            for(i=line.length() - 1; i >= 0; i--){
                if(!getChar(line, i).equals("0")){
                    return (line.length() - 1) - i;
                }
            }
        }
        return 0;
    }
    
    //returns the place value of the least significant figure of a set of numbers,
    //one on each line of Data
    public static int leastSigFig(String Data){
        int i, result;
        String[] lines;
        String tmp;
        
        lines = Data.split("\n");
        tmp = getValueString(lines[0]);
        result = line_leastSigFig(tmp);
        
        for(i=1; i<lines.length; i++){
            tmp = getValueString(lines[i]);
            result = Math.min(result, line_leastSigFig(tmp));
        }
        return result;
    }
    
}
