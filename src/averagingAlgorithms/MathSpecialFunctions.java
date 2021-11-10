
package averagingAlgorithms;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.special.Gamma;
import ensdf_datapoint.dataPt;

/**
 * This class implements special mathematical functions for use in
 * averaging methods. These functions include: factorial, erf, erfc,
 * normal Cumulative distribution function (CDF), inverse erf, gamma function,
 * incomplete gamma function, and inverse incomplete gamma function.
 * Most of these  functions are simply aliases for the implementation in
 * the Apache Commons Math library. <br><br>
 * 
 * Date Modified: 13/08/2015
 * 
 * @author Michael Birch
 */
public class MathSpecialFunctions {
    
    /**
     * Returns n!.
     * @param n number to take the factorial of.
     * @return n!
     */
    public static final double factorial(int n){
        return CombinatoricsUtils.factorialDouble(n);
    }
    
    /**
     * Returns erf(x), where erf is the error function. 
     * @see <a href="https://en.wikipedia.org/wiki/Error_function">https://en.wikipedia.org/wiki/Error_function</a>.
     * @param x number at which to compute the error function
     * @return erf(x)
     */
    public static final double erf(double x){
        return Erf.erf(x);
    }
    
    /**
     * Returns erfc(x), where erfc is the complementary error function, defined
     * by erfc(x) = 1 - erf(x). 
     * @see <a href="https://en.wikipedia.org/wiki/Error_function">https://en.wikipedia.org/wiki/Error_function</a>.
     * @param x number at which to compute the complementary error function
     * @return erfc(x)
     */
    public static final double erfc(double x){
        return Erf.erfc(x);
    }
    
    /**
     * Returns the cumulative distribution function (CDF) for the asymmetric
     * Gaussian defined by the {@link ensdf_datapoint.dataPt} at the value x.
     * I.e. returns the integral from -inf. to x of the probability density
     * function d.{@link ensdf_datapoint.dataPt#gaussian(double) gaussian(x')}.
     * @param d {@link ensdf_datapoint.dataPt} defining the asymmetric Gaussian
     * distribution
     * @param x value at which to compute the CDF
     * @return the CDF evaluated at x
     * @see <a href="https://en.wikipedia.org/wiki/Cumulative_distribution_function">https://en.wikipedia.org/wiki/Cumulative_distribution_function</a>.
     */
    public static final double normalIntegral(dataPt d, double x){
        double v, l, u;
        v = d.getValue();
        l = d.getLower();
        u = d.getUpper();
        if(x <= d.getValue()){
            return l / (l + u) * (1d + erf((x - v) / (Math.sqrt(2d) * l)));
        }else{
            return u / (u + l) * erf((x - v) / (Math.sqrt(2d) * u)) + l/(u + l);
        }
    }
    
    //return area under gaussian defined by d from a to b
    /**
     * Returns the area under the asymmetric Gaussian defined by 
     * the {@link ensdf_datapoint.dataPt} between a and b. I.e. returns
     * the integral from a to b of the probability density 
     * d.{@link ensdf_datapoint.dataPt#gaussian(double) gaussian(t)}.
     * @param d {@link ensdf_datapoint.dataPt} defining the asymmetric Gaussian
     * distribution
     * @param a lower end point of the interval
     * @param b upper end point of the interval
     * @return the area under the asymmetric Gaussian defined by 
     * the {@link ensdf_datapoint.dataPt} between a and b
     */
    public static double GaussianArea(dataPt d, double a, double b){
        return normalIntegral(d, b) - normalIntegral(d, a);
    }
    
    /**
     * Returns the inverse error function evaluated at y0. I.e. returns
     * x such that {@link #erf(double) erf(x)} = y0.
     * @param y0 the value at which to evaluate the inverse error function
     * @return the inverse error function evaluated at y0
     */
    public static final double inverseErf(double y0){
        return Erf.erfInv(y0);
    }
    
    /**
     * Returns the Gamma function evaluated at x.
     * @param x the value at which to evaluate the Gamma function
     * @return the Gamma function evaluated at x
     * @see <a href="https://en.wikipedia.org/wiki/Gamma_function">https://en.wikipedia.org/wiki/Gamma_function</a>.
     */
    public static final double GammaFunction(double x){
        return Gamma.gamma(x);
    }
    
    /**
     * Returns the natural logarithm of the Gamma function evaluated at x.
     * @param x the value at which to evaluate the natural logarithm of the Gamma function
     * @return the natural logarithm of the Gamma function evaluated at x
     * @see <a href="https://en.wikipedia.org/wiki/Gamma_function">https://en.wikipedia.org/wiki/Gamma_function</a>.
     */
    public static final double lngamma(double x){
        return Gamma.logGamma(x);
    }
    
    /**
     * Returns the regularized lower incomplete Gamma function evaluated at the
     * point (a, x). I.e. returns P(a, x)
     * @param a
     * @param x
     * @return the regularized lower incomplete Gamma function evaluated at the
     * point (a, x)
     * @see <a href="https://en.wikipedia.org/wiki/Incomplete_gamma_function#Regularized_Gamma_functions_and_Poisson_random_variables">https://en.wikipedia.org/wiki/Incomplete_gamma_function</a>.
     */
    public static final double regularizedLowerIncompleteGamma(double a, double x){
        return Gamma.regularizedGammaP(a, x);
    }
    
    /**
     * Returns the regularized upper incomplete Gamma function evaluated at the
     * point (a, x). I.e. returns Q(a, x).
     * @param a
     * @param x
     * @return the regularized upper incomplete Gamma function evaluated at the
     * point (a, x)
     * @see <a href="https://en.wikipedia.org/wiki/Incomplete_gamma_function#Regularized_Gamma_functions_and_Poisson_random_variables">https://en.wikipedia.org/wiki/Incomplete_gamma_function</a>.
     */
    public static final double regularizedUpperIncompleteGamma(double a, double x){
        return Gamma.regularizedGammaQ(a, x);
    }
    
    /**
     * Returns the lower incomplete Gamma function evaluated at the
     * point (a, x). I.e. returns P(a, x)
     * @param a
     * @param x
     * @return the lower incomplete Gamma function evaluated at the
     * point (a, x)
     * @see <a href="https://en.wikipedia.org/wiki/Incomplete_gamma_function">https://en.wikipedia.org/wiki/Incomplete_gamma_function</a>.
     */
    public static final double lowerIncompleteGamma(double a, double x){
        return GammaFunction(a) * regularizedLowerIncompleteGamma(a, x);
    }
    
    /**
     * Returns the upper incomplete Gamma function evaluated at the
     * point (a, x). I.e. returns P(a, x)
     * @param a
     * @param x
     * @return the upper incomplete Gamma function evaluated at the
     * point (a, x)
     * @see <a href="https://en.wikipedia.org/wiki/Incomplete_gamma_function">https://en.wikipedia.org/wiki/Incomplete_gamma_function</a>.
     */
    public static final double upperIncompleteGamma(double a, double x){
        return GammaFunction(a) * regularizedUpperIncompleteGamma(a, x);
    }
    
    // inverse of the lower incomplete gamma function
    // computed using bisection root finding of
    // g(x) = y - lowerIncompleteGamma(s, x)
    /**
     * Returns the inverse lower incomplete Gamma function evaluated at the
     * point (s, y), ie returns x such that <code>
     * {@link #lowerIncompleteGamma(double, double) lowerIncompleteGamma(s, x)} = y</code>.
     * This computation is performed using the bisection root finding algorithm
     * on the function <code>g(x) = y - lowerIncompleteGamma(s, x)</code>.
     * @param s
     * @param y
     * @return the inverse lower incomplete Gamma function evaluated at the
     * point (s, y)
     * @see <a href="https://en.wikipedia.org/wiki/Bisection_method">https://en.wikipedia.org/wiki/Bisection_method</a>.
     */
    public static final double invLowerIncompleteGamma(double s, double y){
        double precision = 0.000001d; //how close to zero to get the function
                                      // g(x) = y - lowerIncompleteGamma(s, x)
        int maxIterations = 2000; // max number of iterations for the bisection
        double lower, upper, middle; // lower, upper and middle 
                                     // points of bisection interval
        double gLower, gUpper, gMiddle; // g(x) evaluated at lower, upper
                                        // and middle
        boolean bisectionFail;
        int i;
        
        double diffSign; //sign of g(x)
        double dMiddle; //step size should bisection fail
        
        lower = 0d;
        upper = 10d;
        gLower = y;
        gUpper = y - lowerIncompleteGamma(s, upper);
        middle = (upper + lower)/2d;
        gMiddle = y - lowerIncompleteGamma(s, middle);
        
        //search for interval which contains zero of g(x)
        while(gUpper >= 0d){
            lower = upper;
            gLower = gUpper;
            upper *= 1.1d; //add 10% until g(x) less than zero
            gUpper = y - lowerIncompleteGamma(s, upper);
        }
        
        bisectionFail = false;
        for(i = 1; i <= maxIterations; i++){
            middle = (upper + lower)/2d;
            gMiddle = y - lowerIncompleteGamma(s, middle);
            
            if (Math.abs(gMiddle) < precision){ //found result
                break;
            }else if(gMiddle < 0d){
                upper = middle;
            }else{
                lower = middle;
            }
            
            if (i == maxIterations){
                bisectionFail = true;
            }
        }
        
        // bisection failed, try stepping towards correct value
        if (bisectionFail){
            diffSign = Math.signum(gMiddle);
            dMiddle = 0.1d;
            while(Math.abs(gMiddle) > precision && dMiddle > precision){
                middle += diffSign * dMiddle;
                gMiddle = y - lowerIncompleteGamma(s, middle);
                if (Math.signum(gMiddle) != diffSign){ //moved too far
                    middle -= diffSign * dMiddle; //step back
                    dMiddle *= 0.1; //reduce step size
                }
            }
        }
        return middle;
    }
}
