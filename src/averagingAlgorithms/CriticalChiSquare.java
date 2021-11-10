
package averagingAlgorithms;

/**
 * This class contains functions for computing the critical chi squared
 * for a given confidence level and number of degrees of freedom. <br><br>
 * 
 * Date Modified: 13/08/2015
 * 
 * @author Michael Birch
 */
public class CriticalChiSquare {
    private int dof;
    private double conf;
    private double value;
    
    /**
     * Calculates the critical chi^2 with d degrees of freedom and c as
     * the confidence level. I.e. if X ~ chiSq(d), then Pr(X &lt; x) = c,
     * where x is the critical chi^2.
     * @param d number of degrees of freedom
     * @param c confidence level
     */
    public CriticalChiSquare(int d, double c){
        this.setDOF(d);
        this.conf = c;
        this.setValue();
    }
    
    /**
     * Sets the number of degrees of freedom
     * @param d new number of degrees of freedom
     */
    public final void setDOF(int d){
        this.dof = Math.min(d, 340);
        // calculation becomes too difficult for dof>340,
        // so the maximum allowed value of dof=340. 
        // This is not detrimental since the result from dof=340 will 
        // only be slightly larger than subsequent degrees of freedom.
    }
    
    /**
     * Computes the critical chi^2
     */
    public final void setValue(){
        double k;
        k = (double)this.dof / 2d;
        this.value = 2d * 
                MathSpecialFunctions.invLowerIncompleteGamma(
                        k, this.conf * MathSpecialFunctions.GammaFunction(k));
    }
    
    /**
     * Returns the critical chi^2. If <code>reduced</code> is <code>true</code>
     * then the result is divided by the number of degrees of freedom.
     * @param reduced if <code>true</code>
     * then the result is divided by the number of degrees of freedom
     * @return the critical chi^2
     */
    public double getValue(boolean reduced){
        double result;
        if (reduced){
            result  = this.value / (double)this.dof;
        }else{
            result = this.value;
        }
        return result;
    }
    /**
     * Returns the the critical chi^2.
     * @return the critical chi^2
     */
    public double getValue(){
        return this.getValue(false);
    }
    
    /**
     * Returns <code>true</code> if the number of degrees of freedom and the
     * confidence level is the same between this object and the given values.
     * @param d number of degrees of freedom to compare with
     * @param c confidence level to compare with
     * @return <code>true</code> if the number of degrees of freedom and the
     * confidence level is the same between this object and the given values.
     */
    public boolean compare(int d, double c){
        return this.dof == d && this.conf == c;
    }
}
