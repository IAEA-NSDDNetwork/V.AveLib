
package ensdf_datapoint;

import java.util.Comparator;

/**
 * This class holds the result of a measurement with a central value, upper
 * uncertainty and lower uncertainty (at the usual 68% CL). It contains
 * methods for interfacing in the ENSDF format for uncertainties.
 * <br><br>
 * Date Modified: 21/08/2015
 * 
 * @author Michael Birch
 */
public class dataPt {
    //central value, upper and lower uncertainies
    private double value, upper, lower;
    private String name;
    private String minDisplayPlace;
    //constant used in computing variance of asym. gaussian
    private static final double varianceFactor = 1d - (2d/Math.PI);
    /**
     * The default name given to dataPt objects which are not explicitly named
     */
    public static final String defaultName = "<default>";
    
    //Constructors
    /**
     * Create a dataPt object with central value <code>val</code>, upper
     * uncertainty <code>u</code> and lower uncertainty <code>l</code>.
     * @param val central value
     * @param u upper uncertainty
     * @param l lower uncertainty
     */
    public dataPt(double val, double u, double l){
        this.value = val;
        this.upper = u;
        this.lower = l;
        this.name = defaultName;
        this.minDisplayPlace = "none";
    }
    /**
     * Create a dataPt object with central value <code>val</code>, upper
     * uncertainty <code>u</code>, lower uncertainty <code>l</code> and
     * name <code>n</code>.
     * @param val central value
     * @param u upper uncertainty
     * @param l lower uncertainty
     * @param n name of the measurement
     */
    public dataPt(double val, double u, double l, String n){
        this.value = val;
        this.upper = u;
        this.lower = l;
        this.name = n;
    }
    /**
     * Create a dataPt object with central value <code>x[0]</code>, upper
     * uncertainty <code>x[1]</code> and lower uncertainty <code>x[2]</code>.
     * @param x array containing the central value, upper uncertainty 
     * and lower uncertainty (in that order)
     */
    public dataPt(double[] x){ //construct from array
        this(x[0], x[1], x[2]);
    }
    /**
     * Create a dataPt object with central value <code>x[0]</code>, upper
     * uncertainty <code>x[1]</code> and lower uncertainty <code>x[2]</code>.
     * @param x array containing the central value, upper uncertainty 
     * and lower uncertainty (in that order)
     * @param n name of the measurement
     */
    public dataPt(double[] x, String n){
        this(x[0], x[1], x[2], n);
    }
    /**
     * Default constructor, creates a dataPt object with central value zero
     * and uncertainty equal to one.
     */
    public dataPt(){ //default constructor
        this(0d,1d,1d);
    }
    /**
     * Create a copy of the given dataPt.
     * @param d dataPt to copy
     */
    public dataPt(dataPt d){ //copy constructor
        this.value = d.value;
        this.upper = d.upper;
        this.lower = d.lower;
        this.name = d.name;
    }
    
    //string parsing
    
    //returns an array with two elements which split the string s at the
    //splitter substring
    /**
     * Returns an array with two elements which split the string s at the
     * splitter substring. If splitter appears more than once in s then
     * the split happens at the first occurrence.
     * @param s String to split
     * @param splitter substring matched at the split
     * @return an array with two elements which split the string s at the
     * splitter substring. If splitter appears more than once in s then
     * the split happens at the first occurrence.
     */
    public static final String[] singleSplit(String s, String splitter){
        String[] result;
        int ind;
        
        result = new String[2];
        
        if(s.contains(splitter)){
            ind = s.indexOf(splitter);
            result[0] = s.substring(0, ind);
            result[1] = s.substring(ind+splitter.length());
        }else{
            result[0] = s;
            result[1] = "";
        }
        
        return result;
    }
    
    /**
     * Returns the character used for a decimal point in the present system.
     * Note: this method is important since some language settings use '.'
     * while others use ','
     * @return the character used for a decimal point in the present system.
     */
    public static final String getDecimalChar(){
        return String.valueOf(0.1).split("0")[1].split("1")[0];
    }
    
    /**
     * Returns the smallest place value of any digit in the String
     * @param value a numeric String
     * @return the smallest place value of any digit in the String
     */
    public static final int minPlace(String value){
        String decimalChar;
        int offset;
        
        decimalChar = dataPt.getDecimalChar();
        
        offset = 0;
        //check for sci. not. offset in min place
        if(value.contains("e")){
            offset = Integer.parseInt(value.split("e")[1]);
            value = value.split("e")[0];
        }else if(value.contains("E")){
            offset = Integer.parseInt(value.split("E")[1]);
            value = value.split("E")[0];
        }
        
        if(value.contains(decimalChar)){
            return value.indexOf(decimalChar) - (value.length() - 1) + offset;
        }else{
            return 0 + offset;
        }
    }
    
    /**
     * Returns <code>true</code> if the String s can be parsed into a dataPt
     * object. If d is not null and the result of the function is 
     * <code>true</code> then d will be assigned to be the dataPt created
     * by parsing s.
     * @param s the String to parse
     * @param d the dataPt object to assign if s can be parsed
     * @return <code>true</code> if the String s can be parsed into a dataPt
     * object
     */
    public static final boolean isParsable(String s, dataPt d){
        String name;
        String valueStr, uncertStr, upperStr, lowerStr;
        int minPlaceValue;
        double value, upper, lower;
        
        if(s.contains(":")){
            name = s.split(":")[0].trim();
            valueStr = singleSplit(s, ":")[1].trim();
        }else{
            name = "<default>";
            valueStr = s.trim();
        }
        
        
        if(valueStr.contains(" ")){
            uncertStr = singleSplit(valueStr, " ")[1].trim();
            valueStr = singleSplit(valueStr, " ")[0];
        }else if(valueStr.contains("(")){
            if(!valueStr.contains(")")){
                return false;
            }
            uncertStr = singleSplit(valueStr, "(")[1].trim();
            valueStr = singleSplit(valueStr, "(")[0].trim();
        }else{
            return false;
        }
        
        try{
            value = Double.parseDouble(valueStr);
            minPlaceValue = minPlace(valueStr);
        }catch(NumberFormatException e){
            return false;
        }
        
        uncertStr = uncertStr.replace("(", "");
        uncertStr = uncertStr.replace(")", "");
        uncertStr = uncertStr.replace(" ", "");
        
        if(uncertStr.substring(0, 1).equals("+")){
            if(!uncertStr.contains("-")){
                return false;
            }
            upperStr = uncertStr.split("-")[0].replace("+", "");
            lowerStr = uncertStr.split("-")[1];
        }else if(uncertStr.substring(0, 1).equals("-")){
            if(!uncertStr.contains("+")){
                return false;
            }
            lowerStr = uncertStr.split("\\+")[0].replace("-", "");
            upperStr = uncertStr.split("\\+")[1];
        }else{
            upperStr = uncertStr;
            lowerStr = uncertStr;
        }
        
        try{
            upper = Double.parseDouble(upperStr)*Math.pow(10, minPlaceValue);
            lower = Double.parseDouble(lowerStr)*Math.pow(10, minPlaceValue);
        }catch(NumberFormatException e){
            return false;
        }
        
        try{
            d.setLower(lower);
            d.setUpper(upper);
            d.setValue(value);
            d.setName(name);
        }catch(NullPointerException e){
            // do nothing if data point is null
        }
        
        return true;
    }
    /**
     * Returns <code>true</code> if s can be parsed into a dataPt object.
     * @param s the String to attempt to parse
     * @return <code>true</code> if s can be parsed into a dataPt object
     */
    public static final boolean isParsable(String s){
        return isParsable(s, null);
    }
    
    /**
     * Creates a dataPt object by parsing the String s. The String should
     * represent a dataPt object using the ENSDF format, e.g. "10.5(12)".
     * If s cannot be parsed then a null pointer is returned.
     * @param s String to parse
     * @return the dataPt object represented by s.
     */
    public static final dataPt constructFromString(String s){
        dataPt result;
        boolean parsable;
        
        result = new dataPt();
        parsable = dataPt.isParsable(s, result);
        if(parsable){
            return result;
        }else{
            return null;
        }
    }
    
    /**
     * Prints a quantity in the ENSDF format. The uncertainty part of the
     * resulting String is limited to a max. of 25 (as per ENSDF standard
     * practice). IF <code>ENSDF_format</code> is <code>true</code> then
     * the String <code>uncert</code> is already provided in the ENSDF format
     * at it is only limited to a maximum of 25. <br>
     * E.g. ENSDFprint("10.00", "35", true) produces "10.0 4" and
     * ENSDFprint("10.0000001", "0.07413268", false) produces "10.00 7".
     * @param value a String representing the central value of the quantity
     * @param uncert a String representing the uncertainty on the quantity
     * @param ENSDF_format provide the value <code>true</code> if the 
     * <code>uncert</code> argument is already given in the ENSDF format
     * @return a String representing the quantity in the ENSDF format
     */
    public static final String ENSDFprint(String value, String uncert, boolean ENSDF_format){
        double val, u;
        double[] d;
        
        if(value == null || uncert == null){
            return "";
        }
        
        try{
            val = Double.parseDouble(value);
            try{
                u = Double.parseDouble(uncert);
                if(ENSDF_format){ //uncertainty is given in the ENSDF format
                    //return without name and with max 25 on uncertainty
                    return constructFromString(value + " " + uncert).toString(false, true).replace("(", " ").replace(")", "");
                }else{ //raw uncertainty is given
                    d = new double[3];
                    d[0] = val;
                    d[1] = u;
                    d[2] = u;
                    //return with max 25 on uncertainty
                    return fmtHandler.double_to_ENSDF(d, true).replace("(", " ").replace(")", "");
                }
            }catch(NumberFormatException e){
                return fmtHandler.double_to_nSigFig(val, 3) + " " + uncert;
            }
        }catch(NumberFormatException e){
            return value + " " + uncert;
        }
    }
    
    
    //getters and setters
    /**
     * Returns the upper uncertainty.
     * @return the upper uncertainty
     */
    public double getUpper(){
        return this.upper;
    }
    /**
     * Returns the lower uncertainty.
     * @return the lower uncertainty
     */
    public double getLower(){
        return this.lower;
    }
    /**
     * Returns the central value.
     * @return the central value
     */
    public double getValue(){
        return this.value;
    }
    /**
     * Returns the name of the dataPt
     * @return the name of the dataPt
     */
    public String getName(){
        return this.name;
    }
    /**
     * Sets the central value to be x.
     * @param x the new central value
     */
    public void setValue(double x){
        this.value = x;
    }
    /**
     * Sets the upper uncertainty to be x.
     * @param x the new upper uncertainty
     */
    public void setUpper(double x){
        this.upper = x;
    }
    /**
     * Sets the lower uncertainty to be x.
     * @param x the new lower uncertainty
     */
    public void setLower(double x){
        this.lower = x;
    }
    /**
     * Sets the name to be n.
     * @param n the new name
     */
    public void setName(String n){
        this.name = n;
    }
    /**
     * Sets the smallest place value for which digits will be printed.
     * @param s a String which can be parsed into an <code>int</code>
     * representing the minimum place value to print
     */
    public void setMinDisplayPlace(String s){
        this.minDisplayPlace = s;
    }
    
    //comparison methods
    /**
     * Returns <code>true</code> if each numeric property of d is the same
     * as this dataPt.
     * @param d dataPt to compare with
     * @return <code>true</code> if each numeric property of d is the same
     * as this dataPt.
     */
    public boolean equals(dataPt d){
        return (this.value == d.value) && (this.upper == d.upper)
                && (this.lower == d.lower);
    }
    /**
     * Returns <code>true</code> if each numeric property of d is the same
     * as this dataPt. If <code>compareNames</code> is also <code>true</code>
     * then the two names must also be the same.
     * @param d dataPt to compare with
     * @param compareNames if this parameter is <code>true</code> then the
     * name of d must also match this dataPt.
     * @return <code>true</code> if each numeric property of d is the same
     * as this dataPt. If <code>compareNames</code> is also <code>true</code>
     * then the two names must also be the same.
     */
    public boolean equals(dataPt d, boolean compareNames){
        boolean result;
        result = this.equals(d);
        if(compareNames){
            result = result && (this.name.equals(d.name));
        }
        return result;
    }
    /**
     * Returns <code>true</code> if any of the numeric members of this dataPt
     * are <code>NaN</code>.
     * @return <code>true</code> if any of the numeric members of this dataPt
     * are <code>NaN</code>.
     */
    public boolean isNaN(){
        return Double.isNaN(this.value) || Double.isNaN(this.upper)
                || Double.isNaN(this.lower);
    }
    
    //conversion methods
    /**
     * Returns a double array with elements given by the central value,
     * upper uncertainty, and lower uncertainty (in that order).
     * @return a double array with elements given by the central value,
     * upper uncertainty, and lower uncertainty (in that order).
     */
    public double[] toDouble(){
        double[] result;
        result = new double[3];
        result[0] = this.value;
        result[1] = this.upper;
        result[2] = this.lower;
        return result;
    }
    /**
     * Prints the dataPt with the uncertainty given in the ENSDF format. The
     * uncertainty is not limited to 25, however only two digits will be kept
     * in the uncertainty. If the dataPt has a name different than the default
     * name then it will also be printed.<br>
     * E.g. dataPt d = new dataPt(10.0d, 2.1d, 3.7d, "example"); d.toString();
     * produces "example: 10.0(+21-37)".
     * @return a String representing the dataPt with the 
     * uncertainty given in the ENSDF format
     */
    @Override public String toString(){
        if(this.name.equals(defaultName)){
            if(this.gaussVariance() < 1e-40){ //zero uncertainty case
                return String.valueOf((int)this.value) + "(0)";
            }else{
                return this.toString(false, false);
            }
        }else{
            if(this.gaussVariance() < 1e-40){ //zero uncertainty case
                return String.valueOf((int)this.value) + "(0)";
            }else{
                return this.toString(true, false);
            }
        }
    }
    /**
     * Prints the dataPt with the uncertainty given in the ENSDF format. The
     * uncertainty is not limited to 25, however only two digits will be kept
     * in the uncertainty. If <code>named</code> is <code>true</code> then 
     * the name will also be printed.
     * @param named if <code>true</code> then the name of the dataPt will be
     * included in the returned String
     * @return a String representing the dataPt with the 
     * uncertainty given in the ENSDF format
     */
    public String toString(boolean named){
        return this.toString(named, false);
    }
    /**
     * Prints the dataPt with the uncertainty given in the ENSDF format. If
     * <code>max25</code> is <code>true</code> then the uncertainty part of the
     * String will be at most 25, otherwise 2 digits will be kept in the
     * uncertainty. If <code>named</code> is <code>true</code> then 
     * the name will also be printed.
     * @param named if <code>true</code> then the name of the dataPt will be
     * included in the returned String
     * @param max25 if <code>true</code> then the uncertainty part of the
     * returned String will be at most 25
     * @return a String representing the dataPt with the 
     * uncertainty given in the ENSDF format
     */
    public String toString(boolean named, boolean max25){
        int lowestPlace;
        String result;
        
        //handle zero uncertainty case
        if(this.gaussVariance() < 1e-40){
            this.minDisplayPlace = "0";
        }
        
        try{
            lowestPlace = Integer.parseInt(this.minDisplayPlace);
            result = fmtHandler.double_to_ENSDF(this, lowestPlace);
        }catch(NumberFormatException e){
            // no minium place specified, use default
            result = fmtHandler.double_to_ENSDF(this, max25);
        }
        
        if(named){
            return this.name.concat(": ").concat(result);
        }else{
            return result;
        }
    }
    
    //arithmatic
    /**
     * Returns the variance of the Asymmetric Gaussian distribution defined by this dataPt.
     * If the upper and lower uncertainties are equal then the result of this
     * function is that value squared.
     * @return the variance of the Asymmetric Gaussian defined by this dataPt
     */
    public final double gaussVariance(){
        return varianceFactor*Math.pow(this.getUpper() - this.getLower(), 2d) + 
                this.getUpper()*this.getLower();
    }
    /**
     * Returns the value of the Asymmetric Gaussian probability density
     * function defined by this dataPt evaluated at x.
     * @param x the point at which to evaluate the probability density
     * @return the value of the Asymmetric Gaussian probability density
     * function defined by this dataPt evaluated at x
     */
    public final double gaussian(double x){
        if(x <= this.value){
            return Math.sqrt(2d / Math.PI) / (this.lower + this.upper) * 
                    Math.exp(-(x - this.value)*(x - this.value) / 
                            (2d * this.lower*this.lower));
        }else{
            return Math.sqrt(2d / Math.PI) / (this.lower + this.upper) * 
                    Math.exp(-(x - this.value)*(x - this.value) / 
                            (2d * this.upper*this.upper));
        }
    }
    
    /**
     * Returns <code>true</code> if the ranges defined by the error bars of
     * this dataPt and the given dataPt overlap
     * @param other dataPt to compare with
     * @return <code>true</code> if the ranges defined by the error bars of
     * this dataPt and the given dataPt overlap
     */
    public boolean overlaps(dataPt other){
        dataPt upperVal, lowerVal;
        
        if(this.value > other.value){
            upperVal = this;
            lowerVal = other;
        }else{
            upperVal = other;
            lowerVal = this;
        }
        
        return (upperVal.value - upperVal.lower) < (lowerVal.value + lowerVal.upper);
    }
    
    /**
     * Returns the absolute difference between the central value of this
     * dataPt and the given mean, normalized to the standard deviation of this
     * dataPt. I.e. returns <code> abs(this.getValue() - mean)/Math.sqrt( this.gaussVariance() )</code>.
     * @param mean the given mean
     * @return <code> abs(this.getValue() - mean)/Math.sqrt( this.gaussVariance() )</code>
     */
    public double normalizedDeviation(double mean){
        return Math.abs(this.value - mean) / Math.sqrt(this.gaussVariance());
    }
    
    /**
     * Adds v to the central value
     * @param v amount to add to the central value
     */
    public void addToValue(double v){
        this.value += v;
    }
    /**
     * Adds v to the upper uncertainty
     * @param v amount to add to the upper uncertainty
     */
    public void addToUpper(double v){
        this.upper += v;
    }
    /**
     * Adds v to the lower uncertainty
     * @param v amount to add to the lower uncertainty
     */
    public void addToLower(double v){
        this.lower += v;
    }
    
    /**
     * Returns a {@link java.util.Comparator} object which compares two dataPt
     * objects using their {@link #normalizedDeviation(double) normalized
     * deviations} with respect to the given mean. This Comparator will sort the
     * points in decreasing order of their normalized deviations (i.e. highest
     * normalized deviation first).
     * @param mean given mean
     * @return a {@link java.util.Comparator} object which compares two dataPt
     * objects using their {@link #normalizedDeviation(double) normalized
     * deviations} with respect to the given mean.
     */
    public static Comparator<dataPt> normalizedDeviationComparatorConstructor(double mean){
        return (dataPt d1, dataPt d2) -> {
            Double normDev1 = d1.normalizedDeviation(mean);
            return -normDev1.compareTo(d2.normalizedDeviation(mean));
        };
    }
}
