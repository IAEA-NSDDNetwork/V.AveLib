
package ensdf_datapoint;

import java.util.*;

/**
 * This class represents a number by the place value of its digits. <br>
 * Note: place values are given in log10 form, i.e. a pair (d,p) will
 * be interpreted as d*10^p.
 * 
 * Date Modified: 13/08/2015
 * 
 * @author Michael Birch
 */
public class placeValueRep {
    private ArrayList digits, placeValues;
    private boolean minus;
    private double number;
    /**
     * The character used for a decimal point in the present system. <br>
     * Note: this member is important since some language settings use '.'
     * while others use ','
     */
    public static final String decSep = String.valueOf(String.valueOf(1.2d).charAt(1));
    
    /**
     * Default constructor, creates a placeValueRep object representing the
     * number 10.
     */
    public placeValueRep(){
        this.digits = new ArrayList(1);
        this.placeValues = new ArrayList(1);
        this.digits.set(1, 0);
        this.placeValues.set(1, 0);
        this.minus = false;
        this.number = 10d;
    }
    /**
     * Creates a placeValueRep object from the given String, which represents
     * a number. All the digits present in the String, along with their place
     * values will be stored. <br>
     * E.g. "10.00" will be converted into a the digits array {1, 0, 0, 0}
     * and place value array {1, 0, -1, -2}.
     * @param s the String representing the number.
     * @throws NumberFormatException if s cannot be parsed into a double.
     */
    public placeValueRep(String s) throws NumberFormatException{
        String s2;
        double d;
        int i, j, count, offset;
        char c;
        boolean setPlace;
        
        this.digits = new ArrayList();
        this.placeValues = new ArrayList();
        this.minus = false;
        setPlace = false;
        try{
            d = Double.parseDouble(s); //check this is a valid number
        }catch(NumberFormatException e){
            throw e;
        }
        this.number = d;
        s = s.toLowerCase();
        offset = 0;
        if (s.indexOf("e") > -1){
            s2 = s.split("e")[1];
            s = s.split("e")[0];
            offset = Integer.parseInt(s2);
        }
        count = 0;
        for(i=0; i<s.length(); i++){
            c = s.charAt(i);
            if(Character.isDigit(c)){
                j = Integer.parseInt(String.valueOf(c));
                this.digits.add(j);
                if(setPlace){
                    this.placeValues.add(count);
                    count -= 1;
                }else{
                    this.placeValues.add(0);
                }
            }else if(c == '-'){
                this.minus = true;
            }else if(c == '+'){
                // do nothing
            }else{ //found the decimal point!
                setPlace = true;
                count = this.placeValues.size() - 1 + offset;
                // set the place values of all previous digits
                for(j=0; j<this.placeValues.size(); j++){
                    this.placeValues.set(j, count);
                    count -= 1;
                }
            }
        }
        if(!setPlace){
            count = this.placeValues.size() - 1 + offset;
            for(j=0; j<this.placeValues.size(); j++){
                this.placeValues.set(j, count);
                count -= 1;
            }
        }
    }
    /**
     * Calls {@link #placeValueRep(java.lang.String) placeValueRep} with 
     * argument <code>String.valueOf(d)</code>.
     * @param d double used to construct a placeValueRep object
     */
    public placeValueRep(double d){
        this(String.valueOf(d));
    }
    
    /**
     * Returns the number this placeValueRep object represents.
     * @return the number this placeValueRep object represents.
     */
    public double toDouble(){
        return this.number;
    }
    
    /**
     * Rounds the given double to the given number of decimal places. Note:
     * the number of decimal places can be negative, in which case the number
     * is rounded to the nearest <code> 10^{-n_dec} </code>. <br>
     * E.g. round(127.356, 1) gives 127.4 and round(127.356, -1) gives
     * 130.
     * @param x number to round
     * @param n_dec number of decimal places to round to
     * @return rounded number
     */
    public static double round(double x, int n_dec){
        double y;
        y = Math.pow(10d, (double)n_dec);
        return (double)Math.round(x * y) / y;
    }
    /**
     * Returns a new placeValueRep object with all the digits that have place
     * value smaller than the given place removed.
     * @param p given placeValueRep object
     * @param place place value to round to
     * @return rounded placeValueRep object
     */
    public static placeValueRep round(placeValueRep p, int place){
        return new placeValueRep(round(p.toDouble(), -place));
    }
    
    /**
     * Returns <code>String.valueOf(this.{@link #toDouble() toDouble()})</code>.
     * @return <code>String.valueOf(this.{@link #toDouble() toDouble()})</code>
     */
    @Override public String toString(){
        return String.valueOf(this.toDouble());
    }
    /**
     * Represents the number given by this placeValueRep object as a String.
     * Only digits with place value greater than or equal to <code>minPlace</code>
     * are printed in the String. If <code>printDecimal</code> is <code>true</code>
     * then the decimal point is included after the digit with place value 
     * equal to zero, otherwise it is not printed. In the case that the number
     * will be printed in scientific notation and <code>printDecimal</code> is 
     * <code>false</code>, then the exponent part of the String will also not
     * be printed.
     * @param minPlace smallest place value of the digits to print
     * @param printDecimal if <code>true</code>
     * then the decimal point is included after the digit with place value
     * equal to zero.
     * @return String representing the number given by this placeValueRep
     */
    public String toString(int minPlace, boolean printDecimal){
        int i, j, offset;
        String result;
        placeValueRep rounded;
        rounded = round(this, minPlace);
        
        if(rounded.minus){
            result = "-";
        }else{
            result = new String();
        }
        if(rounded.placeValues.indexOf(0) < 0){ //offset for sci. not.
            offset = (int)rounded.placeValues.get(1);
        }else{
            offset = 0;
        }
        
        try{
            j = rounded.largestNonZeroPlace() - offset;
            //check if the largest non-zero place is after the decimal point
            //and "0.00..." as necessary
            if(j < 0 && printDecimal){
                result += "0.";
                for(i=-1; i>j; i--){
                    result = result.concat("0");
                }
            }
            for(i = rounded.largestNonZeroPlace(); true; i--){
                if(i < minPlace){
                    if(i < 0){
                        break;
                    }else{
                        result = result.concat("0");
                    }
                }else{
                    result = result.concat(String.valueOf(
                            rounded.getDigitByPlaceValue(i)));
                }
                if(i - offset == 0 && printDecimal){
                    result = result.concat(decSep);
                }
            }
            if(offset != 0 && printDecimal){
                result = result.concat("E" + String.valueOf(offset));
            }
            //get rid of tailing decimal place if it exists.
            if(result.lastIndexOf(decSep) == result.length() - 1){
                result = result.substring(0, result.length() - 1);
            }
        }catch(NumberFormatException e){
            // catch case that number is 0, so there is no largest non-zero place
            result = "0";
            if(minPlace < 0){
                result += ".";
                for(i=0; i<-minPlace; i++){
                    result += "0";
                }
            }
        }
        
        return result;
    }
    /**
     * Calls <code>this.{@link #toString(int, boolean) toString(minPlace, true)}</code>.
     * @param minPlace smallest place value of the digits to print
     * @return String representing the number given by this placeValueRep
     */
    public String toString(int minPlace){
        return this.toString(minPlace, true);
    }
    
    /**
     * Returns the smallest place value of any digit in this placeValueRep object.
     * @return the smallest place value of any digit in this placeValueRep object
     */
    public int leastPlace(){
        return (int)this.placeValues.get(this.placeValues.size() - 1);
    }
    /**
     * Returns the largest place value which has a non-zero digit.
     * @return the largest place value which has a non-zero digit
     * @throws NumberFormatException if all digits are zero
     */
    public int largestNonZeroPlace() throws NumberFormatException{
        int i;
        for(i=0; i<this.digits.size(); i++){
            if((int)this.digits.get(i) != 0){
                return (int)this.placeValues.get(i);
            }
        }
        throw new NumberFormatException();
    }
    /**
     * Returns the digit with place value pv.
     * @param pv the place value of the digit to return
     * @return the digit with place value pv
     */
    public int getDigitByPlaceValue(int pv){
        int i;
        for(i=0; i<this.placeValues.size(); i++){
            if((int)this.placeValues.get(i) == pv){
                return (int)this.digits.get(i);
            }
        }
        return 0;
    }
}
