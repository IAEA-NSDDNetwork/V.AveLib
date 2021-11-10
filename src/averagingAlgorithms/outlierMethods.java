
package averagingAlgorithms;

import ensdf_datapoint.dataPt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class contains methods for identifying outliers in a data set
 * by various criteria, including: Chauvenet's, Birch's, Peirce's, 
 * and Modified Peirce's (as modified by Birch).
 * 
 * Date Modified: 21/08/2015
 * 
 * @author Michael Birch
 */
public class outlierMethods {
    /**
     * Returns the outliers in the dataset, as identified by Chauvenet's
     * criterion.
     * @param dataset dataset in which to look for outliers
     * @return the outliers in the dataset, as identified by Chauvenet's
     * criterion
     */
    public static final dataPt[] ChauvenetCriterion(dataPt[] dataset){
        List<dataPt> points;
        List<dataPt> outliers;
        dataPt[] dataPt_arr;
        int i = 0; //for counting loops
        int n = 0; //number of points
	int rejectNum;
	double mean;
	double stdDev;
	double maxDev; //maximum deviation from mean
	dataPt unwtAv;
	boolean leaveLoop;
        
        n = dataset.length;
        leaveLoop = false;
        outliers = new ArrayList<>();
        points = new ArrayList<>();
        dataPt_arr = new dataPt[0];
        
        for(i=0; i<n; i++){
            points.add(dataset[i]);
        }
        
        while(!leaveLoop){
            n = points.size();
            leaveLoop = true;
            unwtAv = averagingMethods.unweightedAverage(points.toArray(dataPt_arr));
            mean = unwtAv.getValue();
            //multiply by square root of n to recover sample standard
            // deviation from unweighted average estimate
            stdDev = unwtAv.getLower() * Math.sqrt((double)n);
            maxDev = Math.sqrt(2d) * MathSpecialFunctions.inverseErf(
                    ((double)(2*n) - 1d) / ((double)(2*n))) * stdDev;
            
            rejectNum = 0;
            for(i=0; i<n; i++){
                if(Math.abs(points.get(i - rejectNum).getValue() - mean) > 
                        maxDev){
                    outliers.add(points.get(i - rejectNum));
                    points.remove(i - rejectNum);
                    leaveLoop = false;
                    rejectNum += 1;
                }
            }
        }
        return(outliers.toArray(dataPt_arr));
    }
    
    // returns outliers in the data set as identified by Birch's
    // criterion. The argument k gives the maximum tolarable probability
    // that a data point is inconsistent with the given mean in the
    // sense of the difference being zero.
    /**
     * Returns the outliers in the dataset as identified by Birch's
     * criterion. The argument k gives the maximum tolerable probability
     * that a data point is inconsistent with the given mean in the
     * sense of the difference being greater than zero. I.e. a data point, <code>x</code>
     * is identified as an outlier if <code>Pr( abs(x.value - givenMean.value)
     * &gt; 0 ) &gt; k</code>, assuming normal distributions for both <code>x
     * </code> and <code>givenMean</code> with means given by their respective
     * central values and standard deviations given by their respective 
     * uncertainties.
     * @param dataset dataset in which to search for outliers
     * @param givenMean given mean to check for consistency with
     * @param k maximum tolerable probability with which a data point is 
     * inconstant with the given mean
     * @return the outliers in the dataset as identified by Birch's
     * criterion
     */
    public static final dataPt[] BirchCriterion(dataPt[] dataset, dataPt givenMean, double k){
        List<dataPt> outliers;
        List<dataPt> sortedSet;
        dataPt[] dataPt_arr;
        int i, j;
        int n;
        int rejectNum;
        double mean, meanVariance;
        double deviation, totalVariance;
        
        n = dataset.length;
        mean = givenMean.getValue();
        meanVariance = givenMean.gaussVariance();
        
        sortedSet = new ArrayList<>();
        //copy data points into list to be sorted
        for(i=0; i<n; i++){
            sortedSet.add(dataset[i]);
        }
        
        //sort points according to normalized deviation (in decreasing order)
        Collections.sort(sortedSet, 
                dataPt.normalizedDeviationComparatorConstructor(mean));
        
        outliers = new ArrayList<>();
        dataPt_arr = new dataPt[0];
        
        rejectNum = 0;
        for(i=0; i<n; i++){
            deviation = Math.abs(sortedSet.get(i).getValue() - mean);
            totalVariance = sortedSet.get(i).gaussVariance() 
                    + meanVariance;
            if(0.5d + 0.5d*MathSpecialFunctions.erf(deviation / 
                    Math.sqrt((2d * totalVariance))) > k){
                outliers.add(sortedSet.get(i));
                rejectNum += 1;
            }
            if(n - rejectNum < 3){ //exit if only a pair of points remain
                break;
            }
        }
        return(outliers.toArray(dataPt_arr));
    }
    /**
     * Uses {@link #BirchCriterion(ensdf_datapoint.dataPt[], ensdf_datapoint.dataPt, double) 
     * Birch's Criterion} to identify outliers with the weighted average of the
     * dataset as the given mean.
     * @param dataset dataset in which to search for outliers
     * @param k maximum tolerable probability with which a data point is 
     * inconstant with the given mean
     * @return the outliers in the dataset as identified by Birch's
     * criterion
     */
    public static final dataPt[] BirchCriterion(dataPt[] dataset, double k){
        return BirchCriterion(dataset, averagingMethods.weightedAverage(dataset), k);
    }
    /**
     * Uses {@link #BirchCriterion(ensdf_datapoint.dataPt[], ensdf_datapoint.dataPt, double) 
     * Birch's Criterion} to identify outliers with 99% given as the probability.
     * @param dataset dataset in which to search for outliers
     * @param givenMean given mean to check for consistency with
     * @return the outliers in the dataset as identified by Birch's
     * criterion
     */
    public static final dataPt[] BirchCriterion(dataPt[] dataset, dataPt givenMean){
        return BirchCriterion(dataset, givenMean, 0.99d);
    }
    /**
     * Uses {@link #BirchCriterion(ensdf_datapoint.dataPt[], ensdf_datapoint.dataPt, double) 
     * Birch's Criterion} to identify outliers with the weighted average of the
     * dataset as the given mean and 99% probability.
     * @param dataset dataset in which to search for outliers
     * @return the outliers in the dataset as identified by Birch's
     * criterion
     */
    public static final dataPt[] BirchCriterion(dataPt[] dataset){
        return BirchCriterion(dataset, averagingMethods.weightedAverage(dataset), 0.99d);
    }
    
    // calculates the maximum deviation from the mean normalized to 
    // the standard deviation using Peirces's criterion
    /**
     * Calculates the maximum deviation from the mean normalized to 
     * the standard deviation using Peirces's criterion.
     * @param numPts the number of measurements in the dataset
     * @param numOutliers the number of outliers believed to be in the
     * dataset
     * @return the maximum deviation from the mean normalized to 
     * the standard deviation using Peirces's criterion
     */
    public static final double calcPeircesMaxNormDev(int numPts, int numOutliers){
        final double precision = 1e-12;
        final double sqrt2 = Math.sqrt(2d);
        double result;
        double NlnQ, lambda, x, R, newR;
        
        NlnQ = 0d;
        lambda = 0d;
        x = 0d;
        R = 1d;
        newR = 0d;
        result = 0d;
        
        NlnQ = (double)numOutliers * Math.log((double)numOutliers) + 
                (double)(numPts - numOutliers) * Math.log((double)(numPts - numOutliers)) -
                (double)numPts * Math.log((double)numPts);
        
        // iterate to find R
        while(true){
            // use Q and R to find lambda
            lambda = Math.exp((NlnQ - (double)numOutliers * Math.log(R)) / 
                    ((double)(numPts - numOutliers)));
            // use lambda to find x
            x = Math.sqrt(1d + (double)(numPts - numOutliers - 1) * 
                    (1d - lambda*lambda) / ((double)numOutliers));
            // use x to find R
            newR = Math.exp(0.5d * (x*x - 1d)) * 
                    MathSpecialFunctions.erfc(x / sqrt2);
            if(Math.abs(R - newR) < precision){
                break;
            }else{
                R = newR;
            }
        }
        
        result = x; //this is what we really wanted
        return result;
    }
    
    /**
     * Returns outliers in the dataset as identified by Peirce's criterion.
     * @param dataset dataset in which to search for outliers
     * @return outliers in the dataset as identified by Peirce's criterion.
     */
    public static final dataPt[] PeirceCriterion(dataPt[] dataset){
        List<dataPt> outliers;
        dataPt[] dataPt_arr;
        int i, n;
        dataPt unwtAv;
        double mean, stdDev;
        boolean[] isOutlier;
        int globalNumOutliers, interationNumOutliers;
        boolean leaveLoop;
        double maxNormDev;
        
        n = dataset.length;
        
        unwtAv = averagingMethods.unweightedAverage(dataset);
        mean = unwtAv.getValue();
        stdDev = unwtAv.getLower() * Math.sqrt((double)n);
        
        isOutlier = new boolean[n];
        for(i=0; i<n; i++){
            isOutlier[i] = false;
        }
        
        leaveLoop = false;
        globalNumOutliers = 1; // assume one outlier
        while(!leaveLoop){
            leaveLoop = true;
            maxNormDev = calcPeircesMaxNormDev(n, globalNumOutliers);
            
            globalNumOutliers -= 1;
            interationNumOutliers = 0;
            for(i=0; i<n; i++){
                if(!(isOutlier[i]) && (Math.abs(dataset[i].getValue() - mean) 
                        / stdDev > maxNormDev)){
                    isOutlier[i] = true;
                    interationNumOutliers += 1;
                    globalNumOutliers += 1;
                }
            }
            if(interationNumOutliers > 0){
                globalNumOutliers += 1; // assume one extra outlier
                leaveLoop = false;
            }
        }
        
        outliers = new ArrayList<>();
        dataPt_arr = new dataPt[0];
        
        for(i=0; i<n; i++){
            if(isOutlier[i]){
                outliers.add(dataset[i]);
            }
        }
        return(outliers.toArray(dataPt_arr));
    }
    
    /**
     * Returns outliers in the dataset as identified by the 
     * Modified Peirce's criterion.
     * @param dataset dataset in which to search for outliers
     * @return outliers in the dataset as identified by the 
     * Modified Peirce's criterion.
     */
    public static final dataPt[] ModifiedPeirceCriterion(dataPt[] dataset){
        final double sqrt2 = Math.sqrt(2d);
        List<dataPt> outliers;
        List<dataPt> sortedSet;
        dataPt[] dataPt_arr;
        int i, n, m;
        dataPt wtAv;
        double mean;
        boolean leaveLoop;
        
        double[] normDev; //normalized deviation
        double nmRatio, k, rmax;
        
        n = dataset.length;
        outliers = new ArrayList<>();
        dataPt_arr = new dataPt[0];
        if(n == 2){ //Don't try anything on Two data points
            return(outliers.toArray(dataPt_arr));
        }
        
        wtAv = averagingMethods.weightedAverage(dataset);
        mean = wtAv.getValue();
        
        normDev = new double[n];
        sortedSet = new ArrayList<>();
        
        //copy data points to list
        for(i=0; i<n; i++){
            sortedSet.add(dataset[i]);
        }
        
        //sort points according to normalized deviation (in decreasing order)
        Collections.sort(sortedSet, 
                dataPt.normalizedDeviationComparatorConstructor(mean));
        
        //calculate normalized deviations
        for(i=0; i<n; i++){
            normDev[i] = sortedSet.get(i).normalizedDeviation(mean);
        }
        
        leaveLoop = false;
        m=1;
        while(!leaveLoop){
            leaveLoop = true;
            
            nmRatio = (double)n / (double)m;
            k = (double)m * Math.exp(nmRatio - 1d * Math.log((double)(n - m)) 
                    - nmRatio * Math.log((double)n));
            rmax = sqrt2 * MathSpecialFunctions.inverseErf(1d - k);
            
            if(rmax < normDev[m - 1]){ //point exceeds maximum deviation
                //remove all points with too large deviation
                while(rmax < normDev[m-1] && n-m > 1){
                    outliers.add(sortedSet.get(m-1));
                    m += 1;
                }
                leaveLoop = false;
            }
            if(n - m <= 1){ //only two points left!
                leaveLoop = true;
            }
        }
        m -= 1;
        return(outliers.toArray(dataPt_arr));
    }
    
    /**
     * Returns the variance which must be associated with <code>mean</code> in
     * order for <code>(mean +/- sqrt(variance))</code> to be consistent with the
     * dataset with probability <code>p</code>.
     * @param mean
     * @param dataset
     * @param p
     * @return the variance which must be associated with <code>mean</code> in
     * order for <code>(mean +/- sqrt(variance))</code> to be consistent with the
     * dataset with probability <code>p</code>
     */
    public static final double consistantVariance(double mean, dataPt[] dataset, double p){
        double k;
        int i,n;
        double[] resultArr;
        double d;
        
        n = dataset.length;
        k = MathSpecialFunctions.inverseErf(2.0d * (p / 100.0d) - 1.0d);
        k = k*k;
        
        resultArr = new double[n];
        for(i=0; i<n; i++){
            d = mean - dataset[i].getValue();
            resultArr[i] = d*d/(2d * k) - dataset[i].gaussVariance();
        }
        return Arrays.stream(resultArr).max().getAsDouble();
    }
}
