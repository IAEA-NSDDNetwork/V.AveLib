
package averagingAlgorithms;

import java.util.stream.DoubleStream;
import java.util.function.DoubleFunction;

/**
 * This class contains methods for performing basic mathematical operations,
 * eg sums over arrays. <br><br>
 * 
 * Date Modified: 01/12/2015/
 * 
 * @author Michael Birch
 */
public class MathBasicFunction {
    
    /**
     * Returns an array which is the element-wise product of x and y
     * @param x array to multiply
     * @param y array to multiply
     * @return an array which is the element-wise product of x and y
     */
    public static final double[] product(double[] x, double[] y){
        double[] z;
        z = new double[x.length];
        for(int i=0; i<x.length; i++){
            z[i] = x[i]*y[i];
        }
        return z;
    }
    
    /**
     * Returns the sum of the array x.
     * @param x array to sum
     * @return the sum of the array
     */
    public static final double sum(double[] x){
        /*
        double total;
        int i;
        total = (double)0;
        for(i=0; i<x.length; i++){
            total += x[i];
        }
        return(total);
        */
        return DoubleStream.of(x).sum(); //use built-in language feature
    }
    
    //returns the sum of the array x, weighted by the array weights
    /**
     * Returns the sum of the array x, weighted by the array weights. I.e.
     * returns sum( weights[i]*x[i] )/sum( weights[i] ).
     * @param x array to sum
     * @param weights array of weights
     * @return the sum of the array x, weighted by the array weights.
     */
    public static final double weightedSum(double[] x, double[] weights){
        double result, weightSum;
        int i,n;
        
        weightSum = sum(weights);
        result = 0d;
        n = x.length;
        for(i=0; i<n; i++){
            result += x[i] * weights[i] / weightSum;
        }
        
        return result;
    }
    
    /**
     * Returns the largest element of the array x
     * @param x array to find the max. value in
     * @return the largest element of the array x
     */
    public static final double max(double[] x){
        return (new arrayMaxMin(x)).max;
    }
    
    /**
     * Returns the smallest element of the array x
     * @param x array to find the min. value in
     * @return the smallest element of the array x
     */
    public static final double min(double[] x){
        return (new arrayMaxMin(x)).min;
    }
    
    /**
     * Returns the index of largest element in the array x
     * @param x array to find the max. value in
     * @return the index of largest element in the array x
     */
    public static final int maxInd(double[] x){
        return (new arrayMaxMin(x)).maxInd;
    }
    
    /**
     * Returns the index of smallest element in the array x
     * @param x array to find the min. value in
     * @return the index of smallest element in the array x
     */
    public static final int minInd(double[] x){
        return (new arrayMaxMin(x)).minInd;
    }
    
    /**
     * Alias for {@link Math#abs(double) Math.abs}.
     * @param x number to take the absolute value of
     * @return <code>{@link Math#abs(double) Math.abs(x)}</code>
     */
    public static final double abs(double x){
        return Math.abs(x);
    }
    
    /**
     * Returns a new array with elements given by the absolute value of
     * those in x.
     * @param x array to take the absolute value of
     * @return a new array with elements given by the absolute value of
     * those in x.
     */
    public static final double[] abs(double[] x){
        double result[];
        int i;
        result = new double[x.length];
        for(i=0; i<x.length; i++){
            result[i] = Math.abs(x[i]);
        }
        return result;
    }
    
    /**
     * Finds the value of x such that f(x) is a maximum on the interval
     * [lowerBound, upperBound].
     * @param f the function to maximize
     * @param lowerBound the lower bound of the interval
     * @param upperBound the upper bound of the interval
     * @return the value of x such that f(x) is a maximum on the interval
     * [lowerBound, upperBound]
     */
    public static final double findMax(DoubleFunction<Double> f,
            double lowerBound, double upperBound){
        final int N = 100;
        final double eps = 1e-20;
        double[] x;
        double[] fx;
        double a, b, dx;
        int i;
        
        x = new double[N];
        fx = new double[N];
        b = upperBound;
        a = lowerBound;
        while(b - a > eps){
            dx = (b - a)/((double)(N-1));
            for(i=0; i<N; i++){ //compute f at sample of points between a and b
                x[i] = a + dx*(double)i;
                fx[i] = f.apply(x[i]);
            }
            i = maxInd(fx);
            try{
                a = x[i-1];
                b = x[i+1];
            }catch(ArrayIndexOutOfBoundsException e){
                if(i == 0){
                    b = x[1];
                }else{
                    a = x[i-1];
                }
            }
        }
        dx = (b - a)/((double)(N-1));
        for(i=0; i<N; i++){ //compute f at sample of points between a and b
            x[i] = a + dx*(double)i;
            fx[i] = f.apply(x[i]);
        }
        i = maxInd(fx);
        return x[i];
    }
    
    /**
     * Uses Brent's method to find the root of the function f on the interval
     * <code>[lowerBound, upperBound]</code>. Returns a double value, x, such
     * that f(x) = 0.
     * @param f the function (which takes one double argument and returns
     * a double value) to find the root.
     * @param lowerBound the lower bound of the interval to search for the root
     * @param upperBound the upper bound of the interval to search for the root
     * @throws IllegalArgumentException
     * @return a double value, x, such that f(x) = 0.
     */
    public static final double uniroot(DoubleFunction<Double> f, double lowerBound, 
            double upperBound) throws IllegalArgumentException{
        final double eps = 1e-20;
        final int maxit = 5000;
        double a, b, c, d, s, fa, fb, fc, fs, tmp;
        boolean mflag, useBisection;
        int count;
        
        fa = f.apply(lowerBound);
        fb = f.apply(upperBound);
        
        //root is not contained in the interval
        if(fa*fb > 0){
            throw new IllegalArgumentException("uniroot: The given function does not have a root within the specified interval");
        }
        
        if(Math.abs(fa) < Math.abs(fb)){
            a = upperBound;
            b = lowerBound;
        }else{
            a = lowerBound;
            b = upperBound;
        }
        
        c = a;
        d = 0.0d;
        mflag = true;
        count = 0;
        while(Math.abs(a - b) > eps && Math.abs(fb) > eps && count < maxit){
            fa = f.apply(a);
            fb = f.apply(b);
            fc = f.apply(c);
            
            if(fa != fc && fb != fc){
                //inverse quadratic interpolation
                s = (a*fb*fc)/((fa - fb)*(fa - fc)) + (b*fa*fc)/((fb - fa)*(fb - fc)) +
                        (c*fa*fb)/((fc - fa)*(fc - fb));
            }else{
                //secant method
                s = b - fb*(b - a)/(fb - fa);
            }
            
            useBisection = false;
            useBisection = useBisection || !(0.75d*a + 0.25d*b >= s && s <= b);
            useBisection = useBisection || (mflag && Math.abs(s-b) >= 0.5d*Math.abs(b-c));
            useBisection = useBisection || (!mflag && Math.abs(s-b) >= 0.5d*Math.abs(c-d));
            useBisection = useBisection || (mflag && Math.abs(b-c) < eps);
            useBisection = useBisection || (!mflag && Math.abs(b-c) < eps);
            
            if(useBisection){
                s = 0.5d*(a+b);
                mflag = true;
            }else{
                mflag = false;
            }
            
            d = c;
            c = b;
            fs = f.apply(s);
            
            if(fa*fs < 0){
                b = s;
                fb = fs;
            }else{
                a = s;
                fa = fs;
            }
            
            if(Math.abs(fa) < Math.abs(fb)){
                //swap a and b
                tmp = a;
                a = b;
                b = tmp;
                
                //swap fa and fb (to match the a,b swap)
                tmp = fa;
                fa = fb;
                fb = tmp;
            }
            count++;
        }
        
        return b;
    }
}
