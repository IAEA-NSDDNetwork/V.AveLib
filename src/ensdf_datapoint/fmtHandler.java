
package ensdf_datapoint;

/**
 * This class does the string handling for data points with ENSDF formatted
 * uncertainties. <br>
 * Recall this format is [value](+[upper]-[lower]), where [upper] and [lower]
 * show the uncertainty in terms of significant figures, e.g. 10.00 +0.10-0.05
 * would be formatted as 10.00(+10-5). Also note that if the upper and lower
 * uncertainties are the same then the format reduced 
 * 
 * Date Modified: 13/08/2015
 * 
 * @author Michael Birch
 */
public class fmtHandler {
    
    /**
     * Reads a string which is of the form [value](+[upper]-[lower])
     * or [value]([uncertainty]), returns a double array where the
     * elements are the value, upper and lower uncertainties,
     * respectively.
     * @param s String giving a quantity in the ENSDF format
     * @return a double array where the
     * elements are the value, upper and lower uncertainties,
     * respectively.
     */
    public static double[] ENSDF_to_double(String s){
        double[] result;
        String[] s_split; //string array for value, upper and lower
                          //uncertainties
        placeValueRep[] places; //hold the above strings represented as
                                //digits and place values
        int i, count;
        StringBuffer temp;
        char c;
        boolean valueComplete;
        
        temp = new StringBuffer();
        s_split = new String[3];
        count = 0;
        valueComplete = false;
        // read the string, splitting as necessary
        for(i=0; i<s.length(); i++){
            c = s.charAt(i);
            if((c == ' ' || c == '(') && count == 0){
                s_split[count] = temp.toString();
                temp = new StringBuffer(); //empty buffer
                valueComplete = true;
            }else if(c == '+' && valueComplete){
                if(count != 0){ //read lower first
                    s_split[count] = temp.toString();
                    temp = new StringBuffer(); //empty buffer
                }
                count = 1;
            }else if(c == '-' && valueComplete){
                if(count != 0){ //read upper first
                    s_split[count] = temp.toString();
                    temp = new StringBuffer(); //empty buffer
                }
                count = 2;
            }else if(c == ')' || c == ' '){
                // do nothing
            }else{
                temp = temp.append(c);
            }
        }
        //fill the uncertainty strings if they were not filled above
        for(i=1; i<3; i++){
            if(s_split[i] == null){
                s_split[i] = temp.toString();
            }
        }
        
        result = new double[3];
        places = new placeValueRep[3];
        try{
            for(i=0; i<3; i++){
                places[i] = new placeValueRep(s_split[i]);
            }
        }catch(NumberFormatException e){ //string not a number
            for(i=1; i<3; i++){
                result[i] = Double.NaN;
            }
            return result;
        }
        s_split[1] += "E" + String.valueOf(places[0].leastPlace());
        s_split[2] += "E" + String.valueOf(places[0].leastPlace());
        for(i=0; i<3; i++){
            result[i] = Double.parseDouble(s_split[i]);
        }
        return result;
    }
    /**
     * Reads a string which is of the form [value](+[upper]-[lower])
     * or [value]([uncertainty]), returns a {@link dataPt} object with
     * the appropriate central value, upper uncertainty and lower uncertainty.
     * @param s a String representing a quantity in the ENSDF format
     * @return a {@link dataPt} object with appropriate central value, 
     * upper uncertainty and lower uncertainty defined by the input String
     */
    public static dataPt ENSDF_to_dataPt(String s){
        return new dataPt(ENSDF_to_double(s));
    }
    
    /**
     * Prints a quantity with central value, upper uncertainty, and lower 
     * uncertainty given by the double array x (with elements in that order)
     * in the ENSDF format. <code>minDisplayPlace</code> gives the smallest place
     * value of the digits in the central value to print. This function essentially
     * performs the opposite operation as {@link #ENSDF_to_double(java.lang.String) 
     * ENSDF_to_double}.
     * @param x double array with elements given by the  central value, 
     * upper uncertainty, and lower uncertainty, respectively
     * @param minDisplayPlace the smallest place
     * value of the digits in the central value to print
     * @return a String representing the quantity in the ENSDF format
     */
    public static String double_to_ENSDF(double[] x, int minDisplayPlace){
        //place value representations of the value,
        //upper and lower uncertainties respectively.
        placeValueRep v_pv, u_pv, l_pv;
        String result, temp1, temp2;
        int lowestPlace,i;
        
        v_pv = new placeValueRep(x[0]);
        u_pv = new placeValueRep(x[1]);
        l_pv = new placeValueRep(x[2]);
        lowestPlace = minDisplayPlace;
        result = v_pv.toString(lowestPlace);
        temp1 = u_pv.toString(lowestPlace, false);
        temp2 = l_pv.toString(lowestPlace, false);
        if(temp1.equals(temp2)){
            result = result.concat("(" + temp1 + ")");
        }else{
            result = result.concat("(+" + temp1 + "-" + temp2 + ")");
        }
        return result;
    }
    
    /**
     * Prints a quantity with central value, upper uncertainty, and lower 
     * uncertainty given by the double array x (with elements in that order)
     * in the ENSDF format. If <code>max25</code> is <code>true</code> then
     * the uncertainty part of the result will be at most 25 (ENSDF 
     * standard practice), otherwise two digits will be kept in the 
     * uncertainty.
     * @param x double array with elements given by the  central value, 
     * upper uncertainty, and lower uncertainty, respectively
     * @param max25 if <code>true</code> then
     * the uncertainty part of the result will be at most 25 (ENSDF 
     * standard practice), otherwise two digits will be kept in the 
     * uncertainty.
     * @return a String representing the quantity in the ENSDF format
     */
    public static String double_to_ENSDF(double[] x, boolean max25){
        placeValueRep u_pv, l_pv;
        String temp1, temp2;
        int lowestPlace;
        
        u_pv = new placeValueRep(x[1]);
        l_pv = new placeValueRep(x[2]);
        lowestPlace = Math.min(u_pv.largestNonZeroPlace() - 1, 
                l_pv.largestNonZeroPlace() - 1);
        temp1 = u_pv.toString(lowestPlace, false);
        temp2 = l_pv.toString(lowestPlace, false);
        
        max25 = false; //disable the max25 restriction. Comment out this line to re-enable this feature
        while(Integer.parseInt(temp1) > 25 && Integer.parseInt(temp2) > 25 
                && max25){ //limit uncertainty part to 25
            lowestPlace += 1;
            temp1 = u_pv.toString(lowestPlace, false);
            temp2 = l_pv.toString(lowestPlace, false);
        }
        if(Integer.parseInt(temp1) == 0 || Integer.parseInt(temp2) == 0){
            lowestPlace -= 1;
        }

        return double_to_ENSDF(x, lowestPlace);
    }
    
    /**
     * Prints a quantity with central value, upper uncertainty, and lower 
     * uncertainty given by the double array x (with elements in that order)
     * in the ENSDF format. Two digits of the uncertainty are printed.
     * @param x double array with elements given by the  central value, 
     * upper uncertainty, and lower uncertainty, respectively
     * @return a String representing the quantity in the ENSDF format
     */
    public static String double_to_ENSDF(double[] x){
        return double_to_ENSDF(x, false);
    }
    /**
     * Calls {@link #double_to_ENSDF(double[], int) double_to_ENSDF} with the
     * double array created by {@link dataPt#toDouble() x.toDouble()}.
     * @param x {@link dataPt} defining the quantity
     * @param minDisplayPlace the smallest place
     * value of the digits in the central value to print
     * @return a String representing the quantity in the ENSDF format
     */
    public static String double_to_ENSDF(dataPt x, int minDisplayPlace){
        return double_to_ENSDF(x.toDouble(), minDisplayPlace);
    }
    /**
     * Calls {@link #double_to_ENSDF(double[], boolean) double_to_ENSDF} with the
     * double array created by {@link dataPt#toDouble() x.toDouble()}.
     * @param x {@link dataPt} defining the quantity
     * @param max25 if <code>true</code> then
     * the uncertainty part of the result will be at most 25 (ENSDF 
     * standard practice), otherwise two digits will be kept in the 
     * uncertainty.
     * @return a String representing the quantity in the ENSDF format
     */
    public static String double_to_ENSDF(dataPt x, boolean max25){
        return double_to_ENSDF(x.toDouble(), max25);
    }
    /**
     * Calls {@link #double_to_ENSDF(double[]) double_to_ENSDF} with the
     * double array created by {@link dataPt#toDouble() x.toDouble()}.
     * @param x {@link dataPt} defining the quantity
     * @return a String representing the quantity in the ENSDF format
     */
    public static String double_to_ENSDF(dataPt x){
        return double_to_ENSDF(x.toDouble(), false);
    }
    
    /**
     * Returns a String representation of the given double which has n
     * significant figures.
     * @param x given double
     * @param n number of significant figures to print
     * @return a String representation of the given double which has n
     * significant figures.
     */
    public static String double_to_nSigFig(double x, int n){        
        java.math.BigDecimal bd = new java.math.BigDecimal(x);
        bd = bd.round(new java.math.MathContext(n));
        return bd.toString();
    }
}
