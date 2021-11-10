
package averagingAlgorithms;

/**
 * This class gives the max/min values of a double-type array as well as the
 * (first) indices of their location.
 * 
 * Date Modified: 14/08/2015
 * 
 * @author Michael Birch
 */
public class arrayMaxMin {
    /**
     * The maximum value in the array
     */
    public final double max;
    /**
     * The minimum value in the array
     */
    public final double min;
    /**
     * The index of the (first occurance of) the maximum value
     */
    public final int maxInd;
    /**
     * The index of the (first occurance of) the minimum value
     */
    public final int minInd;
    
    /**
     * Performs a linear search of the given array, identifying the 
     * maximum, minimum and their (first) locations in the array.
     * @param a array to search
     */
    public arrayMaxMin(double[] a){
        double maximum, minimum;
        int i, maximumIndex, minimumIndex;
        
        maximum = a[0];
        minimum = a[0];
        maximumIndex = 0;
        minimumIndex = 0;
        if(a.length > 1){
            for(i=1; i<a.length; i++){
                if(a[i] > maximum){
                    maximum = a[i];
                    maximumIndex = i;
                }
                if(a[i] < minimum){
                    minimum = a[i];
                    minimumIndex = i;
                }
            }
        }
        this.max = maximum;
        this.min = minimum;
        this.maxInd = maximumIndex;
        this.minInd = minimumIndex;
    }
}
