/*
 * Author: Michael Birch
 * Date Modified: 18/11/2015
 * Description:
 * This class contains methods to generate random numbers according to
 * various distributions
 */


package averagingAlgorithms;

/**
 * This class contains methods to generate random numbers according to
 * various distributions.
 * 
 * Date Modified: 21/08/2015
 * 
 * @author Michael Birch
 */
public class statSampling {
    //returns n random numbers uniformly distributed on [0,1)
    /**
     * Returns random numbers uniformly distributed on [0,1).
     * @param n number of random numbers to return
     * @return <code>n</code> random numbers uniformly distributed on [0,1).
     */
    public static final double[] runif(int n){
        double[] result;
        int i;

        result = new double[n];
        for(i=0; i<n; i++){
            result[i] = Math.random();
        }
        return result;
    }
    
    //return n random numbers normally distributed with mean = mean and
    // standard deviaiton = sd
    /**
     * Returns random numbers which are normally distributed.
     * @param n number of random numbers to return
     * @param mean mean of the normal distribution
     * @param sd standard deviation of the normal distribution
     * @return random numbers which are normally distributed.
     */
    public static final double[] rnorm(int n, double mean, double sd){
        double result[];
        int i;
        java.util.Random generator;
       
        generator = new java.util.Random();
        result = new double[n];
        for(i=0; i<n;i++){
            result[i] = generator.nextGaussian() * sd + mean;
        }
        return result;
    }
    /**
     * Return a single random number sampled from a normal distribution.
     * @param mean mean of the normal distribution
     * @param sd standard deviation of the normal distribution
     * @return a single random number sampled from a normal distribution.
     */
    public static final double rnorm(double mean, double sd){
        return rnorm(1, mean, sd)[0];
    }
    
    /**
     * Returns random numbers which are distributed according to an
     * asymmetric normal distribution. 
     * @param n number of random numbers to return
     * @param peak the location of the maximum of the probability density
     * (different from the mean due to the asymmetry)
     * @param lowSD standard deviation to the left of the peak
     * @param upSD standard deviation to the right of the peak
     * @return random numbers which are distributed according to an
     * asymmetric normal distribution.
     */
    public static final double[] rAnorm(int n, double peak, double lowSD, double upSD){
        double[] u, result;
        int i;
        u = runif(n);
        result = new double[n];
        for(i=0; i<n; i++){
            if(u[i] < lowSD/(upSD + lowSD)){
                result[i] = peak - Math.abs(rnorm(0d, lowSD));
            }else{
                result[i] = peak + Math.abs(rnorm(0d, upSD));
            }
        }
        return result;
    }
    /**
     * Returns a single random number sampled from an asymmetric normal distribution.
     * @param peak the location of the maximum of the probability density
     * (different from the mean due to the asymmetry)
     * @param lowSD standard deviation to the left of the peak
     * @param upSD standard deviation to the right of the peak
     * @return a single random number sampled from an asymmetric normal distribution.
     */
    public static final double rAnorm(double peak, double lowSD, double upSD){
        return rAnorm(1, peak, lowSD, upSD)[0];
    }
    
    /**
     * Returns random integers in the interval [low,high).
     * @param n number of random numbers to return
     * @param low lower bound of the random numbers (included, this number could
     * be one of the results)
     * @param high upper bound of the random numbers (excluded, this number
     * will never be one of the results)
     * @return random integers in the interval [low,high).
     */
    public static final int[] rInt(int n, int low, int high){
        int result[];
        int i;
        java.util.Random generator;
       
        generator = new java.util.Random();
        result = new int[n];
        for(i=0; i<n;i++){
            result[i] = generator.nextInt(high - low) + low;
        }
        return result;
    }
    /**
     * Returns a single random integer in the interval [low,high)
     * @param low lower bound of the random numbers (included, this number could
     * be one of the results)
     * @param high upper bound of the random numbers (excluded, this number
     * will never be one of the results)
     * @return a single random integer in the interval [low,high)
     */
    public static final int rInt(int low, int high){
        return rInt(1, low, high)[0];
    }
    
    /**
     * Returns an array of random integers on the interval [low, high) of
     * the specified size with no repeats. Clearly, size must be less than
     * (high - low).
     * @param low lower bound of the random numbers (included, this number could
     * be one of the results)
     * @param high upper bound of the random numbers (excluded, this number
     * will never be one of the results)
     * @param size the size of the returned array
     * @return an array of random integers on the interval [low, high) of
     * the specified size with no repeats
     */
    public static final int[] sample(int low, int high, int size){
        int[] numbers, result;
        int i, n, max, count;
        
        n = high - low;
        numbers = new int[n];
        for(i=0; i<n; i++){
            numbers[i] = low + i;
        }
        max = n;
        result = new int[size];
        for(count=0; count<size; count++){
            i = rInt(0, max);
            result[count] = numbers[i];
            numbers[i] = numbers[max-1];
            max--;
        }
        
        return result;
    }
}
