
package averagingAlgorithms;

import ensdf_datapoint.dataPt;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import text_io.textTable;

/**
 * This class generates the reports which give detailed output from the
 * averaging methods.
 * 
 * Date Modified: 21/08/2015
 * 
 * @author Michael Birch
 */
public class averagingReport {
    public dataPt[] outliers;
    public dataPt[] adjustedDataSet;
    public dataPt[] originalDataSet;
    public dataPt[] means;
    public double[] ptChiSq;
    public double[] normalizedResiduals;
    public double[] differenceFromMeanSq;
    public double[] relativeWeights;
    public double reducedChiSq;
    public double criticalChiSq;
    public double rejectionConfidence;
    public boolean useUnweightedMean;
    public double hypTest;
    public double[] hypTestRpt;
    public Integer[] changedPoints;
    public double bootstrap_NUM_MEDIANS;
    public String dataSetName;
    public String methodName;
    
    /**
     * Default constructor: all numeric members are set to -1, all other 
     * members are set to null.
     */
    public averagingReport(){
        this.adjustedDataSet = null;
        this.criticalChiSq = -1.0d;
        this.differenceFromMeanSq = null;
        this.means = null;
        this.normalizedResiduals = null;
        this.originalDataSet = null;
        this.outliers = null;
        this.ptChiSq = null;
        this.reducedChiSq = -1.0d;
        this.rejectionConfidence = -1.0d;
        this.relativeWeights = null;
        this.useUnweightedMean = false;
        this.changedPoints = null;
        this.bootstrap_NUM_MEDIANS = -1;
        this.hypTest = -1.0d;
        this.hypTestRpt = null;
        this.dataSetName = null;
        this.methodName = null;
    }
    
    /**
     * Creates the String used as the header for the averaging
     * method in the full report.
     * @return full report method header
     */
    public String methodHeader(){
        return "-------" + this.methodName + "-------" + "\n";
    }
    
    /**
     * Creates the String used as the footer for the averaging method in the
     * full report. It is the same number of characters as the header, but
     * they are all dashes.
     * @return full report method footer
     */
    public String methodFooter(){
        String result = "";
        int i;
        for(i=0; i<14+this.methodName.length(); i++){
            result += "-";
        }
        return result + "\n";
    }
    
    /**
     * Creates the String used as the header for the dataset in the full report.
     * @return full report dataset header
     */
    public String dataSetHeader(){
        return "*******" + this.dataSetName + "*******" + "\n";
    }
    
    /**
     * Creates the String used as the footer for the dataset in the
     * full report. It is the same number of characters as the header, but
     * they are all dashes.
     * @return full report dataset footer
     */
    public String dataSetFooter(){
        String result = "";
        int i;
        for(i=0; i<14+this.dataSetName.length(); i++){
            result += "*";
        }
        return result + "\n";
    }
    
    /**
     * Returns the smallest uncertainty in the original dataset.
     * @return the smallest uncertainty in the original dataset.
     */
    public double minUncert(){
        int i;
        double uncerts[];
        
        if(originalDataSet.length == 0){
            return 0d;
        }
        
        uncerts = new double[originalDataSet.length];
        
        for(i=0; i<originalDataSet.length; i++){
             uncerts[i] = Math.sqrt(this.originalDataSet[i].gaussVariance());
        }
        return MathBasicFunction.min(uncerts);
    }
    
    /**
     * Returns <code>true</code> if the given average has smaller uncertainty
     * than the smallest uncertainty in the original dataset.
     * @param average proposed average for the dataset
     * @return <code>true</code> if the given average has smaller uncertainty
     * than the smallest uncertainty in the original dataset.
     */
    public boolean isSmallerUncert(dataPt average){
        boolean smallerUncert;
        int i;
        
        smallerUncert = true;
        for(i=0; i<this.originalDataSet.length; i++){
            smallerUncert = smallerUncert && 
                    this.originalDataSet[i].gaussVariance() > average.gaussVariance();
        }
        
        return smallerUncert;
    }
    
    /**
     * Returns a short summary of the information contained in the report.
     * The result of this function is the message given to the user after
     * running one of the averaging methods via the V.AveLib GUI.
     * @param average the proposed average of the dataset
     * @return a short summary of the information contained in the report.
     */
    public String briefReport(dataPt average){
        String result;
        boolean smallerUncert; //true if result has uncertainty smaller than 
                               //smallest input uncert
        int i;
        
        if(this.dataSetName != null){
            result = this.dataSetHeader();
        }else{
            result = "";
        }
        if(this.methodName != null){
            result += this.methodHeader();
        }
        result += average.toString() + "\n";
        if(this.reducedChiSq > -1.0d){
            result += "Chi^2/(N-1) = " + String.format("%1.2f", this.reducedChiSq) + "\n";
            if(this.criticalChiSq > -1){
                result += "Critical Chi^2/(N-1) = " + String.format("%1.2f", this.criticalChiSq)
                    + " for rejection at " + String.valueOf(this.rejectionConfidence)
                    + "% confidence level.\n";
            }
        }
        if(this.hypTest > -1.0d){
            result += "Confidence Level = " + String.format("%1.1f", 
                    this.hypTest*100d) + "%.\n";
        }
        
        smallerUncert = isSmallerUncert(average);
        if(smallerUncert){
            result += "Note: result has lower uncertainty than the smallest measured uncertainty.\n";
        }
        
        if(this.methodName != null){
            result += this.methodFooter();
        }
        if(this.dataSetName != null){
            result += this.dataSetFooter();
        }
        
        return result;
    }
    
    /**
     * Calls <code>{@link #briefReport(ensdf_datapoint.dataPt)
     * briefReport(this.means[0])}</code>. I.e. summarizes the result
     * of the first element in the means array.
     * @return a short summary of the information contained in the report.
     */
    public String briefReport(){
        return briefReport(this.means[0]);
    }
    
    /**
     * Returns a String representation of the double x given to 2 decimal
     * places and in scientific notation if there is not at lest one significant
     * figure within the first two decimal places.
     * @param x number to print as a String
     * @return String representation of the double x given to 2 decimal
     * places
     */
    private String doublePrint(double x){
        if(Math.abs(x) < 0.01){
            return String.format("%1.2e", x);
        }else{
            return String.format("%1.2f", x);
        }
    }
    
    /**
     * Returns a complete listing of the data contained in the report. The
     * result of this function is the output saved when the user chooses to
     * save a report file from the V.AveLib GUI.
     * @return a complete listing of the data contained in the report
     */
    public List<String> fullReport(){
        textTable reportData = new textTable();
        int i;
        List<String> result;
        int n = this.originalDataSet.length;
        
        //unweighted average report
        if(this.differenceFromMeanSq != null){
            reportData.setCell(0, 0, "Data Point");
            reportData.setCell(0, 1, "(Difference from mean)**2");
            for(i=0; i<this.differenceFromMeanSq.length; i++){
                reportData.setCell(i+1, 0, this.originalDataSet[i].toString(true));
                reportData.setCell(i+1, 1, doublePrint(this.differenceFromMeanSq[i]));
            }
        }else if(this.normalizedResiduals != null){ //NRM report
            reportData.setCell(0, 0, "Data Point");
            reportData.setCell(0, 1, "Relative Weight (%)");
            reportData.setCell(0, 2, "Point Chi**2");
            reportData.setCell(0, 3, "Normalized Residual");
            for(i=0; i<this.adjustedDataSet.length; i++){
                if(Arrays.asList(this.changedPoints).contains(i)){
                    reportData.setCell(i+1, 0, this.adjustedDataSet[i].toString(true) + "**");
                }else{
                    reportData.setCell(i+1, 0, this.adjustedDataSet[i].toString(true));
                }
                reportData.setCell(i+1, 1, doublePrint(this.relativeWeights[i]*100d));
                reportData.setCell(i+1, 2, doublePrint(this.ptChiSq[i]));
                reportData.setCell(i+1, 3, doublePrint(this.normalizedResiduals[i]));
            }
        }else if (this.hypTestRpt != null){ // EVM report
            reportData.setCell(0, 0, "Data Point");
            reportData.setCell(0, 1, "Relative Weight (%)");
            for(i=0; i<this.originalDataSet.length; i++){
                reportData.setCell(i+1, 0, this.originalDataSet[i].toString(true));
                reportData.setCell(i+1, 1, doublePrint(this.relativeWeights[i]*100d));
            }
        } else if (this.ptChiSq == null && this.useUnweightedMean == false && this.relativeWeights != null){ //MP Report
            reportData.setCell(0, 0, "Data Point");
            reportData.setCell(0, 1, "Relative Weight (%)");
            for(i=0; i<this.originalDataSet.length; i++){
                reportData.setCell(i+1, 0, this.originalDataSet[i].toString(true));
                reportData.setCell(i+1, 1, doublePrint(this.relativeWeights[i]*100d));
            }
        }else if (this.adjustedDataSet != null){ //RT and LWM
            reportData.setCell(0, 0, "Data Point");
            reportData.setCell(0, 1, "Relative Weight (%)");
            reportData.setCell(0, 2, "Point Chi**2");
            for(i=0; i<this.adjustedDataSet.length; i++){
                if(this.changedPoints == null){
                    reportData.setCell(i+1, 0, this.adjustedDataSet[i].toString(true));
                }else{
                    if(Arrays.asList(this.changedPoints).contains(i)){
                        reportData.setCell(i+1, 0, this.adjustedDataSet[i].toString(true) + "**");
                    }else{
                        reportData.setCell(i+1, 0, this.adjustedDataSet[i].toString(true));
                    }
                }
                reportData.setCell(i+1, 1, doublePrint(this.relativeWeights[i]*100d));
                reportData.setCell(i+1, 2, doublePrint(this.ptChiSq[i]));
            }
        } else if (this.relativeWeights != null) { //weighted average report
            reportData.setCell(0, 0, "Data Point");
            reportData.setCell(0, 1, "Relative Weight (%)");
            reportData.setCell(0, 2, "Point Chi**2");
            for(i=0; i<this.originalDataSet.length; i++){
                reportData.setCell(i+1, 0, this.originalDataSet[i].toString(true));
                reportData.setCell(i+1, 1, doublePrint(this.relativeWeights[i]*100d));
                reportData.setCell(i+1, 2, doublePrint(this.ptChiSq[i]));
            }
        }else if(this.bootstrap_NUM_MEDIANS != -1) {
            reportData.setCell(0, 0, "Data Point");
            //reportData.setCell(0, 1, "Relative Weight (%)");
            //reportData.setCell(0, 2, "Point Chi**2");
            for(i=0; i<this.originalDataSet.length; i++){
                reportData.setCell(i+1, 0, this.originalDataSet[i].toString(true));
                //reportData.setCell(i+1, 1, doublePrint(this.relativeWeights[i]*100d));
                //reportData.setCell(i+1, 2, doublePrint(this.ptChiSq[i]));
            }
        }
        
        result = new ArrayList<>();
        
        if(this.dataSetName != null){
            result.add(this.dataSetHeader());
        }
        if(this.methodName != null){
            result.add(this.methodHeader());
        }
        
        if(!reportData.isEmpty()){
            for(String line : reportData.toStringList()){
                result.add(line);
            }
        }
        
        if(this.bootstrap_NUM_MEDIANS != -1){ //bootstrap report
        	result.add("");
            result.add("Number of sub-sample medians taken: " + 
                    String.valueOf((int) this.bootstrap_NUM_MEDIANS));
            result.add("Chi**2/(N-1): " + doublePrint(this.reducedChiSq));
        }
        if(this.changedPoints != null){
            if(this.changedPoints.length > 0){
                result.add("** Uncertainty adjusted");
            }
        }
        
        if(this.useUnweightedMean){
            result.add("LWM Adopted the unweighted average since the weighted and unwighted averages did not agree within uncertainty.");
        }
        
        if (this.hypTestRpt != null){
            result.add("");
            result.add("~~Confidence Test Summary~~");
            result.add("Expected number of points below mean: " + 
                    String.valueOf(Math.round(hypTestRpt[0] * (double)n)));
            result.add("Observed number below mean: " + String.valueOf((int) hypTestRpt[2]));
            result.add("Expected number of points above mean: " + 
                    String.valueOf(Math.round(hypTestRpt[1] * (double)n)));
            result.add("Observed number above mean: " + String.valueOf((int) hypTestRpt[3]));
            result.add("Resulting statistic: " + String.format("%6.3f", this.hypTestRpt[4]));
        }
        
        if(this.outliers != null){
            result.add("");
            result.add("Points excluded as outliers:");
            for(i=0; i<outliers.length; i++){
                result.add(outliers[i].toString());
            }
        }
        
        result.add("");
        result.add("Number of input values: " + String.valueOf(this.originalDataSet.length));
        
        if (this.ptChiSq == null && this.useUnweightedMean == false && this.reducedChiSq > -1){
            result.add("Chi**2/(N-1): " + doublePrint(this.reducedChiSq));
        }
        
        if(this.criticalChiSq > -1){
            result.add("Chi**2/(N-1): " + String.format("%1.2f", this.reducedChiSq));
            result.add("Critical Chi**2/(N-1): " + doublePrint(this.criticalChiSq));
        }
        if(this.hypTest != -1){
            result.add("Confidence Level: "  + String.format("%1.1f", 
                    this.hypTest*100d) + "%");
        }
        
        result.add("");
        for(i=0; i<this.means.length; i++){
            result.add(means[i].toString());
        }
        
        if(this.methodName != null){
            result.add(this.methodFooter());
        }
        
        if(this.dataSetName != null){
            result.add(this.dataSetFooter());
        }
        
        return result;
    }
}
