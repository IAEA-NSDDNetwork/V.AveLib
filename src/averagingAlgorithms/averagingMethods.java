
package averagingAlgorithms;

import ensdf_datapoint.dataPt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import java.util.function.DoubleFunction;
import java.util.function.Function;

/**
 * This class contains the methods which perform the various averaging
 * algorithms. The current algorithms are:
 *   -Unweighted Average
 *   -Weighted Average
 *   -Limit of Statistical Weights (LWM)
 *   -Normalized Residuals Method (NRM)
 *   -Rajeval Technique (RT)
 *   -Expected Value Method (EVM)
 *   -Bootstrap
 *   -Mandel Paule (MP)
 * 
 * Date Modified: 14/08/2015
 * 
 * @author Michael Birch
 */
public final class averagingMethods {
    /**
     * This List keeps track of previous critical chi^2 calculations to 
     * avoid doing extra work.
     */
    private static List<CriticalChiSquare> previousCritChiSq = null;
    private static int lastCritChiSqIndex = 0;
    /**
     * The confidence level at which to compute critical chi^2. See
     * {@link CriticalChiSquare#CriticalChiSquare(int, double) CriticalChiSquare}
     * for details.
     */
    public static double critChiSqConf = 0.95d;
    
    /**
     * Computes the unweighted average (arithmetic mean) of the {@link ensdf_datapoint.dataPt}
     * objects. Details of the calculation including a list of input values,
     * with their squared differences from the mean will be saved in <code>
     * rpt</code>, if its value is different from <code>null</code>.
     * @param dataset measurements to average
     * @param rpt variable to save calculation details in
     * @return the unweighted average (arithmetic mean)
     */
    public static final dataPt unweightedAverage(dataPt[] dataset, averagingReport rpt){
        dataPt result;
        int i; // for counting loops
        int n; //number of data points
        double[] deviationArray;
        double internaluncert, externaluncert;
        
        n = dataset.length;
        result = new dataPt(0d, 0d, 0d, "Unweighted Average");
        for(i=0; i<n; i++){
            result.addToValue(dataset[i].getValue());
        }
        result.setValue(result.getValue()/(double)n);
        
        deviationArray = new double[n];
        externaluncert = 0.0d;
        internaluncert = 0.0d;
        for(i=0; i<n; i++){
            deviationArray[i] = Math.pow(result.getValue() - dataset[i].getValue(), 2d);
            externaluncert += deviationArray[i];
            internaluncert += 1.0d/dataset[i].gaussVariance();
        }
        externaluncert = Math.sqrt(externaluncert/((double)n*(n-1)));
        internaluncert = 1.0d/Math.sqrt(internaluncert);
        result.setUpper(Math.max(internaluncert, externaluncert));
        result.setLower(result.getUpper());
        
        try{
            rpt.differenceFromMeanSq = deviationArray.clone();
            rpt.originalDataSet = dataset;
            rpt.means = new dataPt[1];
            rpt.means[0] = new dataPt(result);
            rpt.methodName = "Unweighted Average";
        }catch(NullPointerException e){
            //do nothing if rpt is null
        }
        
        return result;
    }
    /**
     * Compute the unweighted average (arithmetic mean) without returning
     * any details. Calls <code>{@link #unweightedAverage(ensdf_datapoint.dataPt[], averagingAlgorithms.averagingReport) 
     * unweightedAverage(dataset, null)}</code>.
     * @param dataset measurements to average
     * @return the unweighted average (arithmetic mean)
     */
    public static final dataPt unweightedAverage(dataPt[] dataset){
        return unweightedAverage(dataset, null);
    }
    
    /**
     * Computes the chi^2 (typically associated with the weighted average)
     * for the dataset with respect to the given mean. I.e. computes
     * <code>SUM( (x[i]-mean)^2/sigma[i]^2 )</code>, where <code>x[i]</code>
     * is the i-th measurement and <code>sigma[i]^2</code> is the variance
     * for the i-th measurement.
     * @param dataset
     * @param mean
     * @return the chi^2 of the dataset with respect to the given mean
     */
    public static final double WeightedAveChiSq(dataPt[] dataset, double mean){
        double result, w;
        
        result = 0d;
        for (dataPt datapt : dataset) {
            if (datapt.getValue() > mean) {
                w = 1.0d / (datapt.getLower() * datapt.getLower());
            } else {
                w = 1.0d / (datapt.getUpper() * datapt.getUpper());
            }
            result += w * Math.pow(datapt.getValue() - mean, 2d);
        }
        
        return result;
    }
    
    /**
     * Computes the weighted average of the dataset (using the original algorithm
     * from the Visual Basic code). If <code>forceInternalUncert</code>
     * is <code>true</code> then the result of the function will not multiply
     * the uncertainty by <code>sqrt( chi^2 )</code>, as is normally done to
     * compute the "external uncertainty". Details of the calculation including
     * the relative weight of each measurement, the contribution of each
     * measurement to the chi^2, the total reduced chi^2 and the critical
     * reduced chi^2 are saved in <code>rpt</code>, if its value is not
     * <code>null</code>.
     * @param dataset measurements to average
     * @param forceInternalUncert if <code>true</code> then return the "internal uncertainty", even 
     * if the "external uncertainty" is larger.
     * @param rpt save details of the calculation to this variable
     * @return the weighted average of the dataset
     */
    public static final dataPt weightedAverage_legacy(dataPt[] dataset, boolean forceInternalUncert, averagingReport rpt){
        dataPt result;
        int i; //used for counting loops
        int n; //number of datapoints
        double[] normWeight; //normalized weighting for each datapoint
        double weightSum; //sum of all the weights; used for normaization
        double upperTot; //sum of squares of upper uncertainties in data set; used to calculate internal uncertainty
        double lowerTot; //sum of squares of lower uncertainties in data set; used to calculate internal uncertainty
        double symIntUnc; //internal symmetric uncertainty
        double chiSq; //chi-squared
        double extUnc; //external uncertainty
        
        n = dataset.length;
        normWeight = new double[n];
        weightSum = 0d;
        lowerTot = 0d;
        upperTot = 0d;
        
        for(i=0; i<n; i++){
            normWeight[i] = 1d/dataset[i].gaussVariance();
            lowerTot += dataset[i].getLower()*dataset[i].getLower();
            upperTot += dataset[i].getUpper()*dataset[i].getUpper();
            weightSum += normWeight[i];
        }
        
        result = new dataPt();
        result.setName("Weighted Average");
        result.setValue(0d);
        for (i=0; i<n; i++){
            normWeight[i] /= weightSum;
            result.addToValue(normWeight[i]*dataset[i].getValue());
        }
        // lower internal uncertainty
        result.setLower(Math.sqrt(2d / (1d + upperTot / lowerTot) / weightSum));
        // upper internal uncertainty
        result.setUpper(Math.sqrt((2d / (1d + lowerTot / upperTot) / weightSum)));
        // symmetric internal uncertainty
        symIntUnc = Math.sqrt((1d / weightSum));
        
        // if upper and lower are the same make both the symmetric error
        if(Math.abs(result.getLower()/result.getUpper() - 1d) < 0.01d){
            result.setLower(symIntUnc);
            result.setUpper(symIntUnc);
        }
        
        chiSq = WeightedAveChiSq(dataset, result.getValue()); //calculate chi-squared
        extUnc = Math.sqrt(chiSq / (weightSum * (double)(n-1))); //external uncertainty
        
        try{
            rpt.originalDataSet = dataset;
            rpt.means = new dataPt[2];
            rpt.means[0] = new dataPt(result);
            rpt.means[0].setName("Weighted Average (Internal Uncertainty)");
            rpt.means[1] = new dataPt(result.getValue(), extUnc, extUnc, 
                    "Weighted Average (External Uncertainty)");
            rpt.reducedChiSq = chiSq / (double)(n-1);
            rpt.criticalChiSq = criticalChiSq(n-1, critChiSqConf, true);
            rpt.rejectionConfidence = 100d*critChiSqConf;
            rpt.relativeWeights = normWeight.clone();
            rpt.ptChiSq = new double[dataset.length];
            for(i=0; i < dataset.length; i++){
                rpt.ptChiSq[i] = Math.pow((double)2 * (result.getValue() - 
                        dataset[i].getValue()) / (dataset[i].getLower() + 
                        dataset[i].getUpper()), 2);
            }
            rpt.methodName = "Weighted Average";
        }catch(NullPointerException e){
            // do nothing if rpt is null
        }
        
        // return external uncertaity if greater than internal
        if(result.gaussVariance() < Math.pow(extUnc, 2d) && !(forceInternalUncert)){
            result.setLower(extUnc);
            result.setUpper(extUnc);
        }
        
        return result;
    }
    
    /**
     * Computes the weighted average of the dataset. If <code>forceInternalUncert</code>
     * is <code>true</code> then the result of the function will not multiply
     * the uncertainty by <code>sqrt( chi^2 )</code>, as is normally done to
     * compute the "external uncertainty". Details of the calculation including
     * the relative weight of each measurement, the contribution of each
     * measurement to the chi^2, the total reduced chi^2 and the critical
     * reduced chi^2 are saved in <code>rpt</code>, if its value is not
     * <code>null</code>.
     * @param dataset measurements to average
     * @param forceInternalUncert if <code>true</code> then return the "internal uncertainty", even 
     * if the "external uncertainty" is larger.
     * @param rpt save details of the calculation to this variable
     * @return the weighted average of the dataset
     */
    public static final dataPt weightedAverage(dataPt[] dataset, boolean forceInternalUncert, averagingReport rpt){
        DoubleFunction<Double> f, g, lnL, DlnL;
        Function<Double, double[]> weightCalc;
        int n;
        double mu_max, lowerBound, upperBound, lowerUncert, upperUncert, tmp;
        double[] centers, lowers, uppers, weights, normWeight;
        dataPt result;
        double chiSq, totWeight;
        dataPt wave_ext;
        
        n = dataset.length;
        
        //calculation of the weights which are used in f and lnL
        weightCalc = (mu) -> {
            double[] w;
            w = new double[n];
            for(int i=0; i<n; i++){
              if(dataset[i].getValue() > mu){
                  w[i] = 1.0d / (dataset[i].getLower()*dataset[i].getLower());
              }else{
                  w[i] = 1.0d / (dataset[i].getUpper()*dataset[i].getUpper());
              }
          }
            return w;
        };
        
        //the weighted mean is given by the fixed point of the function f.
        f = (mu) -> {
          double[] w;
          double totalWeight, sum;
          
          w = weightCalc.apply(mu);
          totalWeight = 0.0d;
          sum = 0.0d;
          for(int i=0; i<n; i++){
              sum += w[i]*dataset[i].getValue();
              totalWeight += w[i];
          }
          
          return sum/totalWeight;
        };
        
        //the root of this function is the fixed point of f
        g = (mu) -> {
            return f.apply(mu) - mu;
        };
        
        //the log(liklihood) function
        lnL = (mu) -> {
            double[] w;
            double sum;
            
            w = weightCalc.apply(mu);
            sum = 0.0d;
            for(int i=0; i<n; i++){
                sum += (dataset[i].getValue() - mu)*(dataset[i].getValue() - mu)*w[i];
            }
            
            return -0.5d*sum;
        };
        
        centers = new double[n];
        lowers = new double[n];
        uppers = new double[n];
        
        for(int i=0; i<n; i++){
            centers[i] = dataset[i].getValue();
            lowers[i] = centers[i] - 3.0d*dataset[i].getLower();
            uppers[i] = centers[i] + 3.0d*dataset[i].getUpper();
        }
        
        lowerBound = MathBasicFunction.min(centers);
        upperBound = MathBasicFunction.max(centers);
        try{
            tmp = MathBasicFunction.uniroot(g, lowerBound, upperBound);
        }catch(IllegalArgumentException e){
            //if the root finding method fails then try to find the
            //maximum likelihood value by direct search
            tmp = MathBasicFunction.findMax(lnL, lowerBound, upperBound);
        }
        mu_max = tmp;
        
        //the roots of this function give the 67% confidence interval, i.e.
        //the uncertainties
        DlnL = (mu) -> {
            return lnL.apply(mu) - (lnL.apply(mu_max) - 0.5d);
        };
        
        lowerBound = MathBasicFunction.min(lowers);
        upperBound = MathBasicFunction.max(uppers);
        
        lowerUncert = mu_max - MathBasicFunction.uniroot(DlnL, lowerBound, mu_max);
        upperUncert = MathBasicFunction.uniroot(DlnL, mu_max, upperBound) - mu_max;
        
        result = new dataPt(mu_max, upperUncert, lowerUncert, "Weighted Average");
        
        chiSq = WeightedAveChiSq(dataset, result.getValue()) / (double)(n-1); //calculate reduced chi-squared
        weights = weightCalc.apply(result.getValue());
        totWeight = MathBasicFunction.sum(weights);
        
        normWeight = new double[n];
        for(int i=0; i<n; i++){
            normWeight[i] = weights[i] / totWeight;
        }
        
        wave_ext = new dataPt(result);
        wave_ext.setLower(Math.sqrt(chiSq)*result.getLower());
        wave_ext.setUpper(Math.sqrt(chiSq)*result.getUpper());
        
        try{
            rpt.originalDataSet = dataset;
            rpt.means = new dataPt[2];
            rpt.means[0] = new dataPt(result);
            rpt.means[0].setName("Weighted Average (Internal Uncertainty)");
            rpt.means[1] = new dataPt(wave_ext);
            rpt.means[1].setName("Weighted Average (External Uncertainty)");
            rpt.reducedChiSq = chiSq;
            rpt.criticalChiSq = criticalChiSq(n-1, critChiSqConf, true);
            rpt.rejectionConfidence = 100d*critChiSqConf;
            rpt.relativeWeights = normWeight.clone();
            rpt.ptChiSq = new double[dataset.length];
            for(int i=0; i < dataset.length; i++){
                rpt.ptChiSq[i] = Math.pow(result.getValue() - 
                        dataset[i].getValue(), 2)*weights[i];
            }
            rpt.methodName = "Weighted Average";
        }catch(NullPointerException e){
            // do nothing if rpt is null
        }
        
        // return external uncertaity if greater than internal
        if(result.gaussVariance() <  wave_ext.gaussVariance()  && !(forceInternalUncert)){
            result.setLower(wave_ext.getLower());
            result.setUpper(wave_ext.getUpper());
        }
        
        return result;
    }
    /**
     * Calls <code>{@link #weightedAverage(ensdf_datapoint.dataPt[], boolean, averagingAlgorithms.averagingReport) 
     * weightedAverage(dataset, false, rpt)}</code>.
     * @param dataset measurements to average
     * @param rpt save details of the calculation to this variable
     * @return the weighted average of the dataset
     */
    public static final dataPt weightedAverage(dataPt[] dataset, averagingReport rpt){
        return weightedAverage(dataset, false, rpt);
    }
    /**
     * Calls <code>{@link #weightedAverage(ensdf_datapoint.dataPt[], boolean, averagingAlgorithms.averagingReport) 
     * weightedAverage(dataset, false, null)}</code>.
     * @param dataset measurements to average
     * @return the weighted average of the dataset
     */
    public static final dataPt weightedAverage(dataPt[] dataset){
        return weightedAverage(dataset, false, null);
    }
    /**
     * Calls <code>{@link #weightedAverage(ensdf_datapoint.dataPt[], boolean, averagingAlgorithms.averagingReport) 
     * weightedAverage(dataset, forceInteralUncert, null)}</code>.
     * @param dataset measurements to average
     * @param forceInternalUncert if <code>true</code> then return the "internal uncertainty", even 
     * if the "external uncertainty" is larger.
     * @return the weighted average of the dataset
     */
    public static final dataPt weightedAverage(dataPt[] dataset, boolean forceInternalUncert){
        return weightedAverage(dataset, forceInternalUncert, null);
    }
    
    /**
     * Returns the critical chi^2 with <code>d</code> degrees of freedom and
     * at confidence level <code>conf</code>. If <code>reduced</code> is
     * <code>true</code> then the critical chi^2 is divided by the number of
     * degrees of freedom. This function first searches the List {@link #previousCritChiSq
     * previousCritChiSq} to see if the calculation has already been done and
     * returns that result if it has. A new calculation is done if a matching
     * one is not found and that result is added to the {@link #previousCritChiSq
     * previousCritChiSq} List. See {@link CriticalChiSquare#CriticalChiSquare(int, double) 
     * CriticalChiSquare} for more details regarding critical chi^2 calculations.
     * @param d number of degrees of freedom
     * @param conf confidence level
     * @param reduced if <code>true</code> then the critical chi^2 is divided by the number of
     * degrees of freedom
     * @return the critical chi^2 with <code>d</code> degrees of freedom and
     * at confidence level <code>conf</code>
     */
    public static final double criticalChiSq(int d, double conf, boolean reduced){
        int i;
        int dof;
        CriticalChiSquare last;
        
        dof = Math.min(d, 340);
        if (averagingMethods.previousCritChiSq == null){
            averagingMethods.previousCritChiSq = new ArrayList<>();
            averagingMethods.previousCritChiSq.add(new CriticalChiSquare(dof, conf));
            averagingMethods.lastCritChiSqIndex = 0;
            return averagingMethods.previousCritChiSq.get(0).getValue(reduced);
        }else{ //check previous calculations to avoid extra work
            last = averagingMethods.previousCritChiSq.get(
                    averagingMethods.lastCritChiSqIndex);
            
            if (last.compare(dof, conf)){
                return last.getValue(reduced);
            }
            
            for(i=0; i<averagingMethods.previousCritChiSq.size(); i++){
                last = averagingMethods.previousCritChiSq.get(i);
                if(last.compare(dof, conf)){
                    averagingMethods.lastCritChiSqIndex = i;
                    return last.getValue(reduced);
                }
            }
            
            //requested calculation has not yet been performed
            averagingMethods.previousCritChiSq.add(
                    new CriticalChiSquare(dof, conf));
            averagingMethods.lastCritChiSqIndex = averagingMethods.
                    previousCritChiSq.size() - 1;
            return averagingMethods.previousCritChiSq.get(
                    averagingMethods.lastCritChiSqIndex).getValue(reduced);
        }
    }
    /**
     * Calls {@link #criticalChiSq(int, double, boolean) criticalChiSq(d, conf, false)}.
     * @param d number of degrees of freedom
     * @param conf confidence level
     * @return the critical chi^2 with <code>d</code> degrees of freedom and
     * at confidence level <code>conf</code>
     */
    public static final double criticalChiSq(int d, double conf){
        return criticalChiSq(d, conf, false);
    }
    
    /**
     * Computes the arithmetic mean of the asymmetric Gaussian distributions
     * defined by the given array, evaluated at x.
     * @param s array of {@link ensdf_datapoint.dataPt dataPt} objects
     * @param x point at which to evaluate the mean probability density function
     * @return the arithmetic mean of the asymmetric Gaussian distributions
     * defined by the given array, evaluated at x.
     */
    public static final double totalG(dataPt[] s, double x){
        double sum;
        int i;
        sum = 0d;
        for(i=0; i<s.length; i++){
            sum += s[i].gaussian(x);
        }
        sum /= s.length; //divide by number of data points to maintain total area=1
        return sum;
    }
    
    /**
     * Performs a modified chi^2 test to determine the validity of the assumption
     * used in the Expected Value Method (EVM). See <a href="http://dx.doi.org/10.1016/j.nds.2014.07.019">
     * M. Birch, B. Singh, Nucl. Data Sheets 120, 106 (2014)</a> for mathematical
     * details of the test.
     * @param dataset dataset used to get the EVM result
     * @param EVM the EVM result
     * @param returnArray the elements of this array are filled with details from
     * the test calculation. In particular, the first element is assigned to
     * be the probability a measurement will be less than the EVM result
     * (under the assumption of used to get the EVM result), the second element
     * is the probability of a measurement being above the EVM result, the
     * third and fourth elements are the actual number of measurements below/above
     * the EVM, repectively, and the fifth element is the resulting Q-statistic.
     * @return the "Q-statistic" resulting from the test
     */
    public static final double EVMHypTest(dataPt[] dataset, dataPt EVM, double[] returnArray){
        double result;
        int lowerCount, upperCount; //number of points below/above MBR.value
        double pLow, pHigh; //probability of being below/above
        int i;
        int n;
        
        n = dataset.length;
        
        pLow = 0d;
        lowerCount = 0;
        upperCount = 0;
        for(i=0; i<n; i++){
            // area from -inf to EVM.vlaue
            pLow += MathSpecialFunctions.normalIntegral(dataset[i], EVM.getValue());
            
            if (dataset[i].getValue() < EVM.getValue()){
                lowerCount += 1;
            }else{
                upperCount += 1;
            }
        }
        pLow /= (double)n; //renormalize
        pHigh = 1d - pLow;
        
        // Q-statistic, approximate ch-square distribution with 1 dof
        result = Math.pow(lowerCount - (double)n * pLow, 2d) / ((double)n * pLow) + 
                Math.pow(upperCount - (double)n * pHigh, 2d) / ((double)n * pHigh);
        try{
            returnArray[0] = pLow;
            returnArray[1] = pHigh;
            returnArray[2] = lowerCount;
            returnArray[3] = upperCount;
            returnArray[4] = result;
        }catch(NullPointerException e){
            // do nothing if rpt is null
        }
        return result;
    }
    
    /**
     * Computes the Expected Value Method result for the given dataset. Details
     * of the calculation are stored in <code>rpt</code> if it is not
     * <code>null</code>, including the relative weight of each measurement 
     * and a summary of the confidence level test which is done to check the
     * validity of the EVM assumption.
     * @param dataset measurements to average
     * @param rpt variable to store the details of the calculation in
     * @return Expected Value Method result for the given dataset
     * @see <a href="http://dx.doi.org/10.1016/j.nds.2014.07.019"> M. Birch, B. Singh, Nucl. Data Sheets 120, 106 (2014)</a>
     */
    public static final dataPt evm(dataPt[] dataset, averagingReport rpt){
        dataPt result;
        int i;
        int n;
        double[] normWeight;
        double weightSum;
        double extUnc;
        
        n = dataset.length;
        normWeight = new double[n];
        
        weightSum = 0d;
        for(i=0; i<n; i++){ //sum up weightings
            normWeight[i] = totalG(dataset, dataset[i].getValue());
            weightSum += normWeight[i];
        }
        
        result = new dataPt(0d, 0d, 0d, "Expected Value Method");
        for(i=0; i<n; i++){
            normWeight[i] /= weightSum; //normalize
            // add each value to result according to weighting
            result.addToValue(normWeight[i] * dataset[i].getValue());
            // sum up variances according to weightings
            result.addToLower(Math.pow(normWeight[i] * dataset[i].getLower(), 2d));
            result.addToUpper(Math.pow(normWeight[i] * dataset[i].getUpper(), 2d));
        }
        result.setLower(Math.sqrt(result.getLower()));
        result.setUpper(Math.sqrt(result.getUpper()));
        
        extUnc = 0d;
        for(i=0; i<n; i++){ //compute variance of mean p.d.f.
            extUnc += normWeight[i] * (result.getValue() - dataset[i].getValue()) *
                    (result.getValue() - dataset[i].getValue());
        }
        extUnc = Math.sqrt(extUnc);
        
        try{
            rpt.originalDataSet = dataset;
            rpt.means = new dataPt[2];
            rpt.means[0] = new dataPt(result);
            rpt.means[0].setName("EVM (Internal Uncertainty)");
            rpt.means[1] = new dataPt(result.getValue(), extUnc, extUnc, 
                    "EVM (External Uncertainty)");
            rpt.hypTestRpt = new double[5];
            rpt.hypTest = EVMHypTest(dataset, result, rpt.hypTestRpt);
            // confidence level for the test of the EVM hypothesis
            rpt.hypTest = 1d - MathSpecialFunctions.erf(Math.sqrt(0.5d * rpt.hypTest));
            rpt.relativeWeights = normWeight.clone();
            rpt.methodName = "Expected Value Method";
        }catch(NullPointerException e){
            // do nothing if rpt is null
        }
        
        // return external uncertaity if greater than internal
        if(result.gaussVariance() < Math.pow(extUnc, 2d)){
            result.setLower(extUnc);
            result.setUpper(extUnc);
        }
        
        return result;
    }
    /**
     * Calls <code>{@link #evm(ensdf_datapoint.dataPt[], averagingAlgorithms.averagingReport) evm(dataset, null)}
     * </code>.
     * @param dataset measurements to be averaged
     * @return the EVM result for the dataset
     */
    public static final dataPt evm(dataPt[] dataset){
        return evm(dataset, null);
    }
    
    /**
     * Returns an array of weights (to be used in a weighted average) which are
     * proportional to 1/sigma^2, where sigma^2 is the variance of the
     * measurement. If <code>normalize</code> is <code>true</code> then the
     * weights will be normalized such that their sum is equal to 1.
     * @param dataset measurements to calculate the weights for
     * @param normalize if <code>true</code> then the
     * weights will be normalized such that their sum is equal to 1
     * @return weights proportional to 1/sigma^2
     */
    public static final double[] calcSigmaSqWeights(dataPt[] dataset, boolean normalize){
        int i; //used for counting loops
        int n; //number of datapoints
        double[] result; //normalized weighting for each datapoint
        double weightSum; //sum of all the weights; used for normaization
        
        n = dataset.length;
        result = new double[n];
        weightSum = 0d;
        
        for(i=0; i<n; i++){ // sum up all the weightings
            result[i] = 1d / 1d/dataset[i].gaussVariance();
            weightSum += result[i];
        }
        
        if(normalize){
            for(i=0; i<n; i++){ // normalize weightings
                result[i] /= weightSum;
            }
        }
        
        return result;
    }
    
    // finds the most precise value farthest from the mean
    /**
     * Returns the measurement in the dataset which has the lowest uncertainty
     * and (in the case of ties) has central value farthest from <code>
     * meanVal</code>. This function is used in the {@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * Limitation of Statistical Weights (LWM) method}.
     * @param dataset set of measurements
     * @param meanVal proposed mean value
     * @return the measurement in the dataset which has the lowest uncertainty
     * and (in the case of ties) has central value farthest from <code>
     * meanVal</code>
     */
    public static final dataPt findPresValue(dataPt[] dataset, double meanVal){
        dataPt result;
        int i;
        
        result = new dataPt(dataset[0]);
        for(i=1; i<dataset.length; i++){
            if(result.gaussVariance() > dataset[i].gaussVariance()){
                // found more precise value
                result = new dataPt(dataset[i]);
            }else if(result.gaussVariance() == dataset[i].gaussVariance()){
                if(Math.abs(result.getValue() - meanVal) < 
                        Math.abs(dataset[i].getValue() - meanVal)){
                    // found further value
                    result = new dataPt(dataset[i]);
                }
            }
        }
        return result;
    }
    
    /**
     * Prompts the user with a question to remove the given measurement from
     * the analysis because it is an outlier. 
     * @param method the outlier detection method which found the measurement to
     * be an outlier
     * @param d the proposed outlier measurement
     * @return <code>true</code> if the user responds 'Yes', <code>false</code>
     * otherwise.
     */
    public static final boolean askRemove(String method, dataPt d){
        String title, message;
        int answer;
        
        title = method + " - Outlier";
        message = d.toString();
        message += " has been marked as an outlier by " + method +
                "'s criterion. \n Exclude from the analysis?";
        answer = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return answer == JOptionPane.YES_OPTION;
    }
    
    /**
     * Prompts the user with a question to adopt the unweighted average. This
     * method may be called as part of the {@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * Limitation of Statistical Weights (LWM) method}.
     * @return <code>true</code> if the user responds 'Yes', <code>false</code>
     * otherwise.
     */
    private static boolean askAdoptUnWt(){
        String title, message;
        int answer;
        
        title = "Adopt unweighted mean?";
        message = "Warning! The LWM weighted mean does not overlap the unweighted mean. The method perscribes adoption of the unweighted mean. Would you like to adopt the unweighted mean?";
        answer = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return answer == JOptionPane.YES_OPTION;
    }
    
    /**
     * Performs a linear search for the <code>{@link ensdf_datapoint.dataPt dataPt}
     * needle</code> in the array <code>haystack</code>. If <code>compareNames</code>
     * is true then the names of the <code>{@link ensdf_datapoint.dataPt dataPt}
     * </code> objects are also considered in determining equality.
     * @param needle <code>{@link ensdf_datapoint.dataPt dataPt}</code> to search for
     * @param haystack array to search through
     * @param compareNames if <code>true</code> then the names of the 
     * <code>{@link ensdf_datapoint.dataPt dataPt}</code> objects 
     * are also considered in determining equality
     * @return <code>true</code> if the needle is found in the haystack.
     */
    public static final boolean dataPtInArray(dataPt needle, dataPt[] haystack,
            boolean compareNames){
        int i;
        for(i=0; i<haystack.length; i++){
            if(needle.equals(haystack[i], compareNames)){
                return true;
            }
        }
        return false;
    }
    /**
     * Calls {@link #dataPtInArray(ensdf_datapoint.dataPt, ensdf_datapoint.dataPt[], boolean) 
     * dataPtInArray(needle, haystack, false)}.
     * @param needle <code>{@link ensdf_datapoint.dataPt dataPt}</code> to search for
     * @param haystack array to search through
     * @return <code>true</code> if the needle is found in the haystack.
     */
    public static final boolean dataPtInArray(dataPt needle, dataPt[] haystack){
        return dataPtInArray(needle, haystack, false);
    }
    
    // Limit of statistical weights
    // outlierMethod values:
    // 0: chauvenet
    // 1: peirce
    // 2: modified peirce
    // 3: birch
    /**
     * Uses the Limitation of Statistical Weights Method (LWM) to determine the
     * average of the measurements in the dataset. Details
     * of the calculation are stored in <code>rpt</code> if it is not
     * <code>null</code>, including the relative weight of each measurement,
     * the contribution of each measurement to the chi^2, the total reduced
     * chi^2, the critical reduced chi^2, and which indication of which
     * measurements were marked as outlier or had uncertainties adjusted.
     * @param dataset measurements to average
     * @param weightLimit the maximum allowed relative weight. I.e. if the 
     * weight of a measurement (with the sum of weights normalized to 1) is
     * greater than <code>weightLimit</code> then the uncertainty of that
     * measurement is increased to lower its weight to be equal to 
     * <code>weightLimit</code>.
     * @param outlierMethod the method used to find outliers. Values correspond
     * to methods as follows:<br>
     * 0: {@link outlierMethods#ChauvenetCriterion(ensdf_datapoint.dataPt[]) Chauvenet's criterion}<br>
     * 1: {@link outlierMethods#PeirceCriterion(ensdf_datapoint.dataPt[]) Peirce's criterion}<br>
     * 2: {@link outlierMethods#ModifiedPeirceCriterion(ensdf_datapoint.dataPt[]) Modified Peirce's criterion}<br>
     * 3: {@link outlierMethods#BirchCriterion(ensdf_datapoint.dataPt[], ensdf_datapoint.dataPt, double) Birch's criterion}
     * @param confidenceLevel the confidence level of the critical chi^2 calculated (in percent).
     * If the weighted average chi^2 exceeds the critical value then the weights
     * will be limited. If the chi^2 is still too large and the weighted average
     * does not overlap the unweighted average then the user is asked to adopted
     * the unweighted average. If that does not happen and the chi^2 is
     * greater than the critical value then the uncertainty of the result
     * is increased such that it overlaps the most precise value. See
     * {@link CriticalChiSquare#CriticalChiSquare(int, double) CriticalChiSquare}
     * for details on calculating the critical value, 
     * {@link ensdf_datapoint.dataPt#overlaps(ensdf_datapoint.dataPt) dataPt.overlaps}
     * for details on determining if two measurements overlap and 
     * {@link #findPresValue(ensdf_datapoint.dataPt[], double) findPresValue}
     * for details about finding the most precise measurement.
     * @param rpt variable where the details of the calculation are saved
     * @return the LWM result
     * @see <a href="https://www-nds.iaea.org/workshops/smr1939/Codes/ENSDF_Codes/mswindows/lweight/lweight.pdf"> D. MacMahon and E. Browne, "LWEIGHT, A Computer Program to Calculate Averages" (2000)</a>
     */
    public static final dataPt lwm(dataPt[] dataset, double weightLimit, 
            int outlierMethod, double confidenceLevel, averagingReport rpt){
        final String[] methods = {"Chauvenet", "Peirce", "Modified Peirce", "Birch"};
        final double epsilon = 0.00001;
        dataPt result;
	dataPt weightedMean;
        averagingReport wtRpt, uwtRpt;
	dataPt unWeightedMean ;
	int i; //used for counting loops
	int n; //number of datapoints
	double[] normWeight; //normalized weighting for each datapoint
	double[] regWeight;
	double weightSum;
	boolean leaveLoop;
        List<dataPt> effectiveDataSetList;
	dataPt[] effectiveDataSet;
        List<dataPt> outliersList;
	dataPt[] outliers ;
	dataPt mostPresVal; //most precise value
        List<Integer> pointsChangedList;
	double adjRatio; //ratio wi`/wi
	double ReducedCritChiSq;
	double redChiSq;
        
        n = dataset.length;
        outliers = new dataPt[0];
        if (n > 2){
            if (outlierMethod == 0){
                outliers = outlierMethods.ChauvenetCriterion(dataset);
            }else if(outlierMethod == 1){
                outliers = outlierMethods.PeirceCriterion(dataset);
            }else if(outlierMethod == 2){
                outliers = outlierMethods.ModifiedPeirceCriterion(dataset);
            }else if(outlierMethod == 3){
                outliers = outlierMethods.BirchCriterion(dataset);
            }
        }
        outliersList = new ArrayList<>();
        for(i=0; i<outliers.length; i++){
            if(askRemove(methods[outlierMethod], outliers[i])){
                outliersList.add(outliers[i]);
            }
        }
        if(outliers.length > 0){
            effectiveDataSetList = new ArrayList<>();
            for(i=0; i<n; i++){
                if(!outliersList.contains(dataset[i])){
                    effectiveDataSetList.add(dataset[i]);
                }
            }
            effectiveDataSet = effectiveDataSetList.toArray(new dataPt[0]);
            n = effectiveDataSet.length;
        }else{
            effectiveDataSet = dataset;
        }
        wtRpt = new averagingReport();
        weightedMean = weightedAverage(effectiveDataSet, wtRpt);
        redChiSq = wtRpt.reducedChiSq;
        ReducedCritChiSq = criticalChiSq(n-1, confidenceLevel/100d, true);
        if(redChiSq < ReducedCritChiSq){ // if chi squared is reasonable then do not limit weightings
            result = new dataPt(weightedMean);
            result.setName("LWM");
            
                // create report
                try{
                    rpt.outliers = outliersList.toArray(new dataPt[0]);
                    rpt.relativeWeights = wtRpt.relativeWeights.clone();
                    rpt.originalDataSet = dataset.clone();
                    rpt.adjustedDataSet = effectiveDataSet.clone();
                    rpt.reducedChiSq = redChiSq;
                    rpt.criticalChiSq = ReducedCritChiSq;
                    rpt.rejectionConfidence = confidenceLevel;
                    rpt.means = wtRpt.means.clone();
                    rpt.means[0].setName("LWM (Internal Uncertainty)");
                    rpt.means[1].setName("LWM (External Uncertainty)");
                    rpt.ptChiSq = wtRpt.ptChiSq.clone();
                    rpt.methodName = "Limitation of Statistical Weights";
                }catch(NullPointerException e){
                    // do nothing if rpt is null
                }
                return(result);
        }
        
        pointsChangedList = new ArrayList<>();
        leaveLoop = false;
        while(!leaveLoop){
            normWeight = calcSigmaSqWeights(effectiveDataSet, true);
            regWeight = calcSigmaSqWeights(effectiveDataSet, false);
            weightSum = MathBasicFunction.sum(regWeight);
            
            leaveLoop = true;
            for(i=0; i<n; i++){
                if(normWeight[i] - weightLimit > epsilon){ //too much weight
                    // increase uncertainty
                    if(!pointsChangedList.contains(i)){
                        pointsChangedList.add(i);
                    }
                    leaveLoop = false;
                    adjRatio = weightLimit * (weightSum - regWeight[i]) / 
                                (regWeight[i] * ((double)1 - weightLimit));
                    effectiveDataSet[i].setLower(effectiveDataSet[i].getLower()/
                            Math.sqrt(adjRatio));
                    effectiveDataSet[i].setUpper(effectiveDataSet[i].getUpper()/
                            Math.sqrt(adjRatio));
                    break;
                }
            }
        }
        
        weightedMean = weightedAverage(effectiveDataSet, wtRpt);
        uwtRpt = new averagingReport();
        unWeightedMean = unweightedAverage(effectiveDataSet, uwtRpt);
        redChiSq = wtRpt.reducedChiSq;
        
        //ask user to adopt unweighted average if the chi sq is still too big
        //and the weighted average disagrees with the unweighted average
        //within one standard deviation
        if(!weightedMean.overlaps(unWeightedMean) && redChiSq > ReducedCritChiSq){
            if(askAdoptUnWt()){
                result = new dataPt(unWeightedMean);
                result.setName("LWM");
                // create report
                try{
                    rpt.outliers = outliersList.toArray(new dataPt[0]);
                    rpt.originalDataSet = dataset.clone();
                    rpt.adjustedDataSet = effectiveDataSet.clone();
                    rpt.differenceFromMeanSq = uwtRpt.differenceFromMeanSq.clone();
                    rpt.useUnweightedMean = true;
                    rpt.changedPoints = pointsChangedList.toArray(new Integer[0]);
                    rpt.means = new dataPt[1];
                    rpt.means[0] = new dataPt(result);
                    rpt.methodName = "Limitation of Statistical Weights";
                }catch(NullPointerException e){
                    // do nothing if rpt is null
                }
                return(result);
            }
        }
        
        result = new dataPt(weightedMean);
        result.setName("LWM");
        mostPresVal = findPresValue(dataset, result.getValue());
        // create report
        try{
            rpt.outliers = outliersList.toArray(new dataPt[0]);
            rpt.relativeWeights = wtRpt.relativeWeights.clone();
            rpt.originalDataSet = dataset.clone();
            rpt.adjustedDataSet = effectiveDataSet.clone();
            rpt.reducedChiSq = redChiSq;
            rpt.criticalChiSq = ReducedCritChiSq;
            rpt.rejectionConfidence = confidenceLevel;
            rpt.means = wtRpt.means.clone();
            rpt.means[0].setName("LWM (Internal Uncertainty)");
            rpt.means[1].setName("LWM (External Uncertainty)");
            rpt.ptChiSq = wtRpt.ptChiSq.clone();
            rpt.changedPoints = pointsChangedList.toArray(new Integer[0]);
            rpt.methodName = "Limitation of Statostical Weights";
        }catch(NullPointerException e){
            // do nothing if rpt is null
        }
        //increase uncertainty to overlap most precise value if chi-squared too large
        if(!result.overlaps(mostPresVal) && redChiSq > ReducedCritChiSq){
            if(result.getValue() < mostPresVal.getValue()){
                result.setUpper(Math.abs(result.getValue() - (mostPresVal.getValue() -
                        mostPresVal.getLower())));
                result.setLower(result.getUpper());
            }else{
                result.setLower(Math.abs(result.getValue() - (mostPresVal.getValue() +
                        mostPresVal.getUpper())));
                result.setUpper(result.getLower());
            }
            //modify report
            try{
                rpt.means = new dataPt[3];
                rpt.means[0] = new dataPt(wtRpt.means[0]);
                rpt.means[1] = new dataPt(wtRpt.means[1]);
                rpt.means[2] = new dataPt(result);
                rpt.means[0].setName("LWM (Internal Uncertainty)");
                rpt.means[1].setName("LWM (External Uncertainty)");
                rpt.means[2].setName("LWM (Uncertainty increased to overlap most precise value)");
                rpt.methodName = "Limitation of Statistical Weights";
            }catch(NullPointerException e){
                // do nothing if rpt is null
            }
        }
        return(result);
    }
    /**
     * Calls <code>{@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * lwm(datset, 0.5d, 0, 99d, null)}</code>
     * @param dataset measurements to be averaged
     * @return the LWM result
     */
    public static final dataPt lwm(dataPt[] dataset){
        return lwm(dataset, 0.5d, 0, 99d, null);
    }
    /**
     * Calls <code>{@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * lwm(datset, 0.5d, 0, 99d, rpt)}</code>
     * @param dataset measurements to be averaged
     * @param rpt variable to save details of the calculation in
     * @return the LWM result
     */
    public static final dataPt lwm(dataPt[] dataset, averagingReport rpt){
        return lwm(dataset, 0.5d, 0, 99d, rpt);
    }
    /**
     * Calls <code>{@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * lwm(datset, 0.5d, outlierMethod, 99d, null)}</code>
     * @param dataset measurements to average
     * @param outlierMethod see {@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * full lwm description}
     * @return the LWM result
     */
    public static final dataPt lwm(dataPt[] dataset, int outlierMethod){
        return lwm(dataset, 0.5d, outlierMethod, 99d, null);
    }
    /**
     * Calls <code>{@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * lwm(datset, 0.5d, 0, confidenceLvel, null)}</code>. See {@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * full lwm description} for argument descriptions.
     * @param dataset
     * @param confidenceLevel
     * @return LWM result
     */
    public static final dataPt lwm(dataPt[] dataset, double confidenceLevel){
        return lwm(dataset, 0.5d, 0, confidenceLevel, null);
    }
    /**
     * Calls <code>{@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * lwm(datset, 0.5d, outlierMethod, confidenceLvel, null)}</code>. See {@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * full lwm description} for argument descriptions.
     * @param dataset
     * @param outlierMethod
     * @param confidenceLevel
     * @return LWM result
     */
    public static final dataPt lwm(dataPt[] dataset, int outlierMethod,
            double confidenceLevel){
        return lwm(dataset, 0.5d, outlierMethod, confidenceLevel, null);
    }
    /**
     * Calls <code>{@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * lwm(datset, 0.5d, outlierMethod, 99d, rpt)}</code>. See {@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * full lwm description} for argument descriptions.
     * @param dataset
     * @param outlierMethod
     * @param rpt
     * @return LWM result
     */
    public static final dataPt lwm(dataPt[] dataset, int outlierMethod,
            averagingReport rpt){
        return lwm(dataset, 0.5d, outlierMethod, 99d, rpt);
    }
    /**
     * Calls <code>{@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * lwm(datset, 0.5d, 0, confidenceLvel, rpt)}</code>. See {@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * full lwm description} for argument descriptions.
     * @param dataset
     * @param confidenceLevel
     * @param rpt
     * @return LWM result
     */
    public static final dataPt lwm(dataPt[] dataset,
            double confidenceLevel, averagingReport rpt){
        return lwm(dataset, 0.5d, 0, confidenceLevel, rpt);
    }
    /**
     * Calls <code>{@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * lwm(datset, 0.5d, outlierMethod, confidenceLvel, rpt)}</code>. See {@link #lwm(ensdf_datapoint.dataPt[], double, int, double, averagingAlgorithms.averagingReport) 
     * full lwm description} for argument descriptions.
     * @param dataset
     * @param outlierMethod
     * @param confidenceLevel
     * @param rpt
     * @return LWM result
     */
    public static final dataPt lwm(dataPt[] dataset, int outlierMethod,
            double confidenceLevel, averagingReport rpt){
        return lwm(dataset, 0.5d, outlierMethod, confidenceLevel, rpt);
    }
    
    /**
     * Calculates the normalized residuals for the dataset using the given
     * weights and mean. The i-th normalized residual is defined as
     * <code>sqrt( w[i]*W/(W-w[i]) )*( x[i] - mean )</code>, where <code>
     * W = SUM( w[i] )</code>, <code>w[i]</code> is the i-th weight and 
     * <code>x[i]</code> is the i-th measurement central value
     * @param dataset measurements
     * @param weights array of weights (~1/sigma^2)
     * @param mean weighted mean calculated from the weights
     * @return the normalized residuals
     * @see <a href="http://dx.doi.org/10.1016/0168-9002(92)90106-E"> M.F. James, R.W. Mills, D.R. Weaver, Nucl. Instr. and Meth. in Phys. Res. A313, 277 (1992)</a>
     */
    public static final double[] CalcNormalizedResiduals(dataPt[] dataset,
            double[] weights, double mean){
        double[] result;
        double weightSum;
        int i;
        
        result = new double[dataset.length];
        weightSum = MathBasicFunction.sum(weights);
        for(i=0; i<result.length; i++){
            result[i] = Math.sqrt(weights[i] * weightSum / 
                    (weightSum - weights[i])) * (dataset[i].getValue() - mean);
        }
        return result;
    }
    
    /**
     * Uses the Normalized Residuals Method (NRM) to compute the average of the
     * dataset. 
     * @param dataset input to take the average of
     * @param confidenceLevel uncertainties of data points with normalized 
     * residuals greater than a critical value determined by this confidence
     * level will be adjusted. This number should be between 0 and 1, the
     * closer to 1, the less higher the critical normalized residual becomes.
     * @param rpt variable where the details of the calculation are saved. This
     * includes information such as which data points
     * had their uncertainties adjusted, as well as all the information given
     * in the weighted average report
     * @return The NRM result
     * @see <a href="http://dx.doi.org/10.1016/0168-9002(92)90106-E"> M.F. James, R.W. Mills, D.R. Weaver, Nucl. Instr. and Meth. in Phys. Res. A313, 277 (1992)</a>
     */
    public static final dataPt nrm(dataPt[] dataset, double confidenceLevel,
            averagingReport rpt){
        dataPt result;
        int i, n;
        double[] weights; //normalized weighting for each datapoint
        double[] normResid; //the normalized residuals
        arrayMaxMin maxNormResid;
        double weightSum;
        dataPt[] effectiveDataSet;
        double criticalR; //used to identify outliers and increase their uncertainty
        boolean leaveLoop;
        double adjRatio; // ratio between new and adjusted weights
        List<Integer> pointsChangedList;
        double outlierProbability; //calculated in %
        int iterationCount;
        averagingReport wtRpt;
        
        n = dataset.length;
        effectiveDataSet = new dataPt[n];
        
        outlierProbability = 100d * (1d - confidenceLevel);
        if(outlierProbability > (double)n){
            outlierProbability = (double)n;
        }
        
        criticalR = Math.sqrt(1.8d * Math.log((double)n / outlierProbability)
            + 2.6);
        
        for(i=0; i<n; i++){ //copy original dataset
            effectiveDataSet[i] = new dataPt(dataset[i]);
        }
        
        weights = calcSigmaSqWeights(effectiveDataSet, false);
        weightSum = MathBasicFunction.sum(weights);
        result = weightedAverage(effectiveDataSet);
        
        normResid = CalcNormalizedResiduals(effectiveDataSet, weights, result.getValue());
        
        leaveLoop = false;
        pointsChangedList = new ArrayList<>();
        iterationCount = 0;
        while(!leaveLoop){
            leaveLoop = true;
            maxNormResid = new arrayMaxMin(MathBasicFunction.abs(normResid));
            //check if the maxium normalized residule is too big,
            //if it is, fix it
            if(maxNormResid.max > criticalR){
                leaveLoop = false;
                i = maxNormResid.maxInd;
                if(!pointsChangedList.contains(i)){
                    pointsChangedList.add(i);
                }
                adjRatio = (1.0d - (weightSum * (Math.pow(normResid[i],2) - 
                        Math.pow(criticalR,2)) / (weightSum * 
                        Math.pow(normResid[i],2) - weights[i]* 
                        Math.pow(criticalR,2))));
                // adjRatio is the adjustment such that the new weight
                // reduces the normalzed residual to the critical value
                weights[i] *= adjRatio; // adjust weight
                effectiveDataSet[i].setLower(effectiveDataSet[i].getLower()/
                        Math.sqrt(adjRatio));
                effectiveDataSet[i].setUpper(effectiveDataSet[i].getUpper()/
                        Math.sqrt(adjRatio));
                
                weightSum = MathBasicFunction.sum(weights);
                result = weightedAverage(effectiveDataSet);
                normResid = CalcNormalizedResiduals(effectiveDataSet, weights, 
                        result.getValue());
            }
            
            iterationCount += 1;
            if(iterationCount > 5000){
                leaveLoop = true;
            }
        }
        
        wtRpt = new averagingReport();
        result = weightedAverage(effectiveDataSet, wtRpt);
        result.setName("NRM");
        
        try{
            rpt.relativeWeights = wtRpt.relativeWeights.clone();
            rpt.originalDataSet = dataset.clone();
            rpt.adjustedDataSet = effectiveDataSet.clone();
            rpt.normalizedResiduals = normResid.clone();
            rpt.reducedChiSq = wtRpt.reducedChiSq;
            rpt.criticalChiSq = criticalChiSq(n-1, critChiSqConf, true);
            rpt.rejectionConfidence = 100d*critChiSqConf;
            rpt.means = wtRpt.means.clone();
            rpt.means[0].setName("NRM (Internal Uncertainty)");
            rpt.means[1].setName("NRM (External Uncertainty)");
            rpt.ptChiSq = wtRpt.ptChiSq.clone();
            rpt.changedPoints = pointsChangedList.toArray(new Integer[0]);
            rpt.methodName = "Normalized Residuals Method";
        }catch(NullPointerException e){
            // do nothing if rpt is null
        }
        return(result);
    }
    /**
     * Calls <code>{@link #nrm(ensdf_datapoint.dataPt[], double, averagingAlgorithms.averagingReport) 
     * nrm(dataset, 0.99d, rpt)}</code>. See {@link #nrm(ensdf_datapoint.dataPt[], double, averagingAlgorithms.averagingReport) 
     * nrm(dataPt[], double, averagingRepor} for full argument descriptions.
     * @param dataset
     * @param rpt
     * @return NRM result
     */
    public static final dataPt nrm(dataPt[] dataset, averagingReport rpt){
        return nrm(dataset, 0.99d, rpt);
    }
    /**
     * Calls <code>{@link #nrm(ensdf_datapoint.dataPt[], double, averagingAlgorithms.averagingReport) 
     * nrm(dataset, confidenceLevel, null)}</code>. See {@link #nrm(ensdf_datapoint.dataPt[], double, averagingAlgorithms.averagingReport) 
     * nrm(dataPt[], double, averagingRepor} for full argument descriptions.
     * @param dataset
     * @param confidenceLevel
     * @return NRM result
     */
    public static final dataPt nrm(dataPt[] dataset, double confidenceLevel){
        return nrm(dataset, confidenceLevel, null);
    }
    /**
     * Calls <code>{@link #nrm(ensdf_datapoint.dataPt[], double, averagingAlgorithms.averagingReport) 
     * nrm(dataset, 0.99d, null)}</code>. See {@link #nrm(ensdf_datapoint.dataPt[], double, averagingAlgorithms.averagingReport) 
     * nrm(dataPt[], double, averagingRepor} for full argument descriptions.
     * @param dataset
     * @return NRM result
     */
    public static final dataPt nrm(dataPt[] dataset){
        return nrm(dataset, 0.99d, null);
    }
    
    // Rajeval Technique
    /**
     * Uses the Rajeval Technique (RT) to compute the average of the dataset.
     * @param dataset input to take the average of
     * @param outlierConfidenceLevel The allowed values of this argument are
     * as follows:<br>
     * 1: user will be asked to reject data point which are outliers within
     * 95% confidence (i.e. if the RT algorithm is at least 95% sure the 
     * data point is an outlier then it will ask the user to exclude it from
     * the average)<br>
     * 2: user will be asked to reject outliers at 99% confidence<br>
     * 3: user will be asked to reject outliers at 99.99% confidence
     * @param rpt variable where the details of the calculation are saved. This
     * includes information such as which data points were excluded, which
     * had their uncertainties adjusted, as well as all the information given
     * in the weighted average report
     * @return the RT result
     * @see <a href="http://dx.doi.org/10.1016/0168-9002(92)90171-Y"> M.U. Rajput and T.D. MacMahon, Nucl. Instr. and Meth. in Phys. Res. A312, 289 (1992)</a>
     */
    public static final dataPt rt(dataPt[] dataset, int outlierConfidenceLevel,
            averagingReport rpt){
        
        //outlierConfidenceLevel of 1 means 95%, 2 means 99%, 3 means 99.99%
        dataPt result, unweightedMean, weightedMean;
        int i, n;
        List<dataPt> effectiveDataSetList;
	dataPt[] effectiveDataSet;
        List<dataPt> outliersList;
        double reducedMean, reducedSD; //mean and standard deviation used in
                                      //finding outliers
        
        //keeps track of which points have uncertainties adjusted
        List<Integer> pointsChangedList;
        boolean leaveLoop;
        double[] outlyingStat;
        double inconsistantStatistic;
        dataPt stdNorm;
        double criticalIncons;
        averagingReport wtRpt;
        
        stdNorm = new dataPt(0, 1, 1, "Standard Normal");
        
        n = dataset.length;
        
        //Stage 1: find outliers
        outlyingStat = new double[n];
        outliersList = new ArrayList<>();
        effectiveDataSetList = new ArrayList<>();
        unweightedMean = unweightedAverage(dataset);
        for(i=0;i<n;i++){
            // ensure number of points not less than 3
            if(n - outliersList.size() < 4){
                break;
            }
            //mean without i-th data point
            reducedMean = unweightedMean.getValue() * (double)n/(double)(n-1) -
                    dataset[i].getValue()/(double)(n-1);
            //standard deviation without the i-th data point
            reducedSD = Math.sqrt(((double)n/(double)(n-2))*unweightedMean.gaussVariance() - 
                    (double)n * (unweightedMean.getValue() - dataset[i].getValue()) *
                           (unweightedMean.getValue() - dataset[i].getValue()) /
                            ((double)((n-1)*(n-1)*(n-2))));
            outlyingStat[i] = (dataset[i].getValue() - reducedMean) / 
                    Math.sqrt(dataset[i].gaussVariance() + reducedSD*reducedSD);
            
            if(Math.abs(outlyingStat[i]) > 1.96d * (double)outlierConfidenceLevel){
                if(askRemove("Rajeval Technique", dataset[i])){
                    outliersList.add(dataset[i]);
                }
            }
        }
        for(i=0;i<n;i++){
            // add a copy of all data points which are not outliers to
            // the effective dataset for (possible) uncertainty modification
            if(!outliersList.contains(dataset[i])){
                effectiveDataSetList.add(new dataPt(dataset[i]));
            }
        }
        effectiveDataSet = effectiveDataSetList.toArray(new dataPt[0]);
        n = effectiveDataSet.length;
        pointsChangedList = new ArrayList<>();
        leaveLoop = false;
        criticalIncons = Math.pow(0.5d, (double)n/(double)(n-1));
        while(!leaveLoop){
            // stage two find inconstistancies
            
            //calculate weighted mean with internal uncertainty
            weightedMean = weightedAverage(effectiveDataSet, true);
            leaveLoop = true;
            for(i=0;i<n;i++){
                inconsistantStatistic = (effectiveDataSet[i].getValue() -
                        weightedMean.getValue()) / Math.sqrt(effectiveDataSet[i].gaussVariance() -
                                weightedMean.gaussVariance());
                if(Math.abs(MathSpecialFunctions.normalIntegral(stdNorm, inconsistantStatistic) -
                        0.5d) > criticalIncons){
                    if(!pointsChangedList.contains(i)){
                        pointsChangedList.add(i);
                    }
                    leaveLoop = false;
                    // stage three adjust uncertainties
                    effectiveDataSet[i].setLower(Math.sqrt(effectiveDataSet[i].getLower()*
                            effectiveDataSet[i].getLower() + weightedMean.getLower()*
                                    weightedMean.getLower()));
                    effectiveDataSet[i].setUpper(Math.sqrt(effectiveDataSet[i].getUpper()*
                            effectiveDataSet[i].getUpper() + weightedMean.getUpper()*
                                    weightedMean.getUpper()));
                }
            }   
        }
        wtRpt = new averagingReport();
        result = weightedAverage(effectiveDataSet, wtRpt);
        result.setName("RT");
        
        // create report
        try{
            rpt.relativeWeights = wtRpt.relativeWeights.clone();
            rpt.originalDataSet = dataset.clone();
            rpt.adjustedDataSet = effectiveDataSet.clone();
            rpt.reducedChiSq = wtRpt.reducedChiSq;
            rpt.criticalChiSq = criticalChiSq(n-1, critChiSqConf, true);
            rpt.rejectionConfidence = 100d*critChiSqConf;
            rpt.means = wtRpt.means.clone();
            rpt.means[0].setName("RT (Internal Uncertainty)");
            rpt.means[1].setName("RT (External Uncertainty)");
            rpt.ptChiSq = wtRpt.ptChiSq.clone();
            rpt.changedPoints = pointsChangedList.toArray(new Integer[0]);
            rpt.outliers = outliersList.toArray(new dataPt[0]);
            rpt.methodName = "Rajeval Technique";
        }catch(NullPointerException e){
            // do nothing if rpt is null
        }
        return(result);
    }
    /**
     * Calls <code>{@link #rt(ensdf_datapoint.dataPt[], int, averagingAlgorithms.averagingReport) 
     * rt(dataset, 2, rpt)}</code>. See {@link #rt(ensdf_datapoint.dataPt[], int, averagingAlgorithms.averagingReport) 
     * rt(dataPt[], int, averagingReport)} for full argument descriptions.
     * @param dataset
     * @param rpt
     * @return RT result
     */
    public static final dataPt rt(dataPt[] dataset, averagingReport rpt){
        return rt(dataset, 2, rpt);
    }
    /**
     * Calls <code>{@link #rt(ensdf_datapoint.dataPt[], int, averagingAlgorithms.averagingReport) 
     * rt(dataset, outlierConfidenceLevel, null)}</code>. See {@link #rt(ensdf_datapoint.dataPt[], int, averagingAlgorithms.averagingReport) 
     * rt(dataPt[], int, averagingReport)} for full argument descriptions.
     * @param dataset
     * @param outlierConfidenceLevel
     * @return RT result
     */
    public static final dataPt rt(dataPt[] dataset, int outlierConfidenceLevel){
        return rt(dataset, outlierConfidenceLevel, null);
    }
    /**
     * Calls <code>{@link #rt(ensdf_datapoint.dataPt[], int, averagingAlgorithms.averagingReport) 
     * rt(dataset, 2, null)}</code>. See {@link #rt(ensdf_datapoint.dataPt[], int, averagingAlgorithms.averagingReport) 
     * rt(dataPt[], int, averagingReport)} for full argument descriptions.
     * @param dataset
     * @return RT result
     */
    public static final dataPt rt(dataPt[] dataset){
        return rt(dataset, 2, null);
    }
    
    /**
     * Calculates the median of the array x.
     * @param x array to calculate the median of. x does not need to be sorted,
     * this method sorts the array then computes the median.
     * @return the median of the array
     * @see <a href="https://en.wikipedia.org/wiki/Median">https://en.wikipedia.org/wiki/Median</a>
     */
    public static final double median(double[] x){
        int n;
        
        n = x.length;
        Arrays.sort(x);
        if(n % 2 == 0){ // even number of elements
            return 0.5d * (x[n/2] + x[n/2 - 1]);
            //return average of middle two elements
        }else{
            n = (int)Math.floor(0.5d * (double)n);
            return x[n]; // return middle element
        }
    }
    
    /**
     * Calculates the {@link #median(double[]) median} of the array created from
     * the central values of the data points in the dataset. This method does 
     * not take into account uncertainties
     * @param dataset measurements to find the median of
     * @return the median central value
     */
    public static final double median(dataPt[] dataset){
        int i,n;
        double[] temp;
        
        n = dataset.length;
        temp = new double[n];
        for(i=0; i<n; i++){
            temp[i] = dataset[i].getValue();
        }
        return median(temp);
    }
    
    /**
     * Calculates the unbiased sample variance of the given array using the 
     * given mean.
     * @param x array to calculate the variance of
     * @param mean mean to use in the variance calculation
     * @return the unbiased sample variance
     * @see <a href="https://en.wikipedia.org/wiki/Variance#Population_variance_and_sample_variance">https://en.wikipedia.org/wiki/Variance</a>
     */
    public static final double estimateVariance(double[] x, double mean){
        double result;
        int i,n;
        
        n = x.length;
        result = 0d;
        for(i=0;i<n;i++){
            result += (x[i] - mean)*(x[i] - mean);
        }
        result /= (double)(n-1);
        return result;
    }
    
    /**
     * Uses the bootstrap method to calculated the average of the dataset. This
     * is a resampling method which creates new datasets of the same size as
     * the input by sampling from the distributions defined by the input
     * data points. The result of the method is mean of the medians of these
     * generated datasets.
     * @param dataset input data points
     * @param NUM_MEDIANS number of medians to calculate (i.e. number of 
     * new datasets to generate by resampling the input)
     * @param rpt variable where the details of the calculation are saved. This
     * includes information such as how many resampled datasets were generated.
     * @return the bootstrap result
     */
    public static final dataPt bootstrap(dataPt[] dataset, int NUM_MEDIANS,
            averagingReport rpt){
        int n, i, j;
        int[] sampleSeq; //sequence of which points to sample from
        double[] medians, sampleData;
        double mean, uncertainty;
        dataPt temp; //temporarily store data point to sample from
        dataPt result;
        
        n = dataset.length;
        
        medians = new double[NUM_MEDIANS];
        sampleData = new double[n];
        for(i=0;i<NUM_MEDIANS;i++){
            sampleSeq = statSampling.rInt(n, 0, n);
            for(j=0; j<n; j++){
                temp = dataset[sampleSeq[j]];
                sampleData[j] = statSampling.rAnorm(temp.getValue(), 
                        temp.getLower(), temp.getUpper());
            }
            medians[i] = median(sampleData);
        }
        mean = MathBasicFunction.sum(medians)/((double)medians.length);
        uncertainty = Math.sqrt(estimateVariance(medians, mean));
        result = new dataPt(mean, uncertainty, uncertainty,"Bootstrap");
        
        try{
            rpt.originalDataSet = dataset.clone();
            rpt.means = new dataPt[1];
            rpt.means[0] = result;
            rpt.reducedChiSq = WeightedAveChiSq(dataset, result.getValue()) / 
                    (double)(n-1);
            rpt.bootstrap_NUM_MEDIANS = NUM_MEDIANS;
            rpt.methodName = "Bootstrap";
        }catch(NullPointerException e){
            // do nothing if rpt is null
        }
        return result;
    }
    /**
     * Calls {@link #bootstrap(ensdf_datapoint.dataPt[], int, averagingAlgorithms.averagingReport) 
     * bootstrap(dataset, 800000, rpt)}. See {@link #bootstrap(ensdf_datapoint.dataPt[], int, averagingAlgorithms.averagingReport) 
     * bootstrap(dataPt[], int, averagingReport)} for full method description.
     * @param dataset
     * @param rpt
     * @return 
     */
    public static final dataPt bootstrap(dataPt[] dataset, averagingReport rpt){
        return bootstrap(dataset, 800000, rpt);
    }
    /**
     * Calls {@link #bootstrap(ensdf_datapoint.dataPt[], int, averagingAlgorithms.averagingReport) 
     * bootstrap(dataset, 800000, null)}. See {@link #bootstrap(ensdf_datapoint.dataPt[], int, averagingAlgorithms.averagingReport) 
     * bootstrap(dataPt[], int, averagingReport)} for full method description.
     * @param dataset
     * @return 
     */
    public static final dataPt bootstrap(dataPt[] dataset){
        return bootstrap(dataset, 800000, null);
    }
    /**
     * Calls {@link #bootstrap(ensdf_datapoint.dataPt[], int, averagingAlgorithms.averagingReport) 
     * bootstrap(dataset, NUM_MEDIANS, null)}. See {@link #bootstrap(ensdf_datapoint.dataPt[], int, averagingAlgorithms.averagingReport) 
     * bootstrap(dataPt[], int, averagingReport)} for full method description.
     * @param dataset
     * @param NUM_MEDIANS
     * @return 
     */
    public static final dataPt bootstrap(dataPt[] dataset, int NUM_MEDIANS){
        return bootstrap(dataset, NUM_MEDIANS, null);
    }
    
    //calculates the weights according to the Mandel-Paule method
    /**
     * Calculates the weights of the data points according to the Mandel-Paule
     * method. <code>w[i] = 1/(y + dataset[i].gaussVariance())</code>
     * @param dataset input data points
     * @param y weights modifier
     * @return the Mandel-Paule weights
     * @see <a href="http://dx.doi.org/10.1080/01621459.1998.10474111"> A.L. Rukhin and M.G. Vangel, J. Am. Stat. Assoc. 93, 303 (1998)</a>
     */
    private static double[] mpWeights(dataPt[] dataset, double y){
        double[] result;
        int i,n;
        
        n = dataset.length;
        result = new double[n];
        for(i=0; i<n; i++){
            result[i] = 1d / (y + dataset[i].gaussVariance());
         }
        
        return result;
    }
    
    /**
     * Calculates the weighted sum of the central values of the given dataset
     * using the given weights. Returns <code>SUM( w[i]*dataset[i].getValue()</code>.
     * @param dataset input data points
     * @param weights given weights
     * @return weighted sum of the central values of the given dataset
     * using the given weights
     * @see <a href="http://dx.doi.org/10.1080/01621459.1998.10474111"> A.L. Rukhin and M.G. Vangel, J. Am. Stat. Assoc. 93, 303 (1998)</a>
     */
    private static double weightedSum(dataPt[] dataset, double[] weights){
        double[] temp;
        int i, n;
       n = dataset.length;
       temp = new double[n];
       for(i=0 ;i<n; i++){
           temp[i] = dataset[i].getValue();
       }
       return MathBasicFunction.weightedSum(temp, weights);
    }
    
    /**
     * Function used by the Mandel-Paule method to estimate the "variance
     * parameter", which should be zero for the correct value of y.
     * @param dataset input data points
     * @param y weights modifier (see {@link #mpWeights(ensdf_datapoint.dataPt[], double) mpWeights})
     * @return Mandel-Paule variance parameter
     * @see <a href="http://dx.doi.org/10.1080/01621459.1998.10474111"> A.L. Rukhin and M.G. Vangel, J. Am. Stat. Assoc. 93, 303 (1998)</a>
     */
    private static double mpFunction(dataPt[] dataset, double y){
        double result;
        double[] weights;
        double mean;
        int n, i;
        
        n = dataset.length;
        weights = mpWeights(dataset, y);
        mean = weightedSum(dataset, weights);
        
        result = 0d;
        for(i=0; i<n; i++){
            result += weights[i] * (dataset[i].getValue() - mean) * 
                    (dataset[i].getValue() - mean);
        }
        result -= (double)(n-1); //subtract expected value of statistic
        return result;
    }
    
    /**
     * Calculated the mean of the dataset using the Mandel-Paule (MP) method. 
     * This method uses the bisection algorithm to find the root of a function.
     * @param dataset Input dataset
     * @param precision precision with which to find the root. I.e. bisection
     * algorithm terminates when <code>abs( f(y) ) &lt; pecision</code>
     * @param maxIt maximum number of iterations to use in the bisection algorithm
     * @param rpt variable where the details of the calculation are saved. This
     * includes information such as the relative weight of each measurement in
     * the final weighted average
     * @return MP result
     * @see <a href="http://dx.doi.org/10.1080/01621459.1998.10474111"> A.L. Rukhin and M.G. Vangel, J. Am. Stat. Assoc. 93, 303 (1998)</a>
     */
    public static final dataPt mp(dataPt[] dataset, double precision, int maxIt,
            averagingReport rpt){
        dataPt result, unweightedMean, weightedMean;
        int n, i;
        double[] weights;
        double weightSum;
        double yLower, yUpper; //defines the interval for the bisection algorithm
        double yMid; //midpoint of the bisection interval
        double fLower, fUpper, fMid; //the MP function values at the lower, 
        //upper and mid y values. The MP function is monotonically decreasing,
        //so fUpper < fLower.
        
        n = dataset.length;
        yLower = 0d;
        fLower = mpFunction(dataset, yLower);
        
        yMid = 0d;
        if(fLower < 0){
            // actual root is a negative value, use y=0 as the solution
            yMid = 0d;
        }else{
            unweightedMean = unweightedAverage(dataset);
            // use uncertainty in the unweighted average as a first guess of
            //the upper bound
            yUpper = (double)n * unweightedMean.gaussVariance();
            fUpper = mpFunction(dataset, yUpper);
            
            if(fUpper > 0){ //haven't crossed zero yet
                yLower = yUpper; //can safely move yLower here
                while(fUpper > 0){
                    yUpper *= 1.1; //add 10% until the function is negative
                    fUpper = mpFunction(dataset, yUpper);
                }
            }
            
            for(i=1; i<=maxIt; i++){ //the bisection
                yMid = 0.5d * (yLower + yUpper); //compute mid-point
                fMid = mpFunction(dataset,yMid);
                
                if(Math.abs(fMid) < precision){
                    break;
                }else if(fMid < 0){
                    yUpper = yMid; //reduce upper bound
                }else{
                    yLower = yMid; //raise upper bound
                }
                if(i == maxIt){
                    JOptionPane.showMessageDialog(null, "Warning! Bisection Algorithm for Mandel Paule method failed, result may not be optimal. Increase the maximum number of iterations to attain a better result.");
                }
            }
        }
        
        weights = mpWeights(dataset, yMid); //calculate weights with bisection solution
        result = new dataPt();
        result.setName("Mandel-Paule");
        result.setValue(weightedSum(dataset, weights));
        
        weightedMean = weightedAverage(dataset);
        //choose larger error between the variance estimate and the weighted
        //average uncertainty
        if(yMid > weightedMean.gaussVariance()){
            result.setLower(Math.sqrt(yMid));
            result.setUpper(Math.sqrt(yMid));
        }else{
            result.setLower(weightedMean.getLower());
            result.setUpper(weightedMean.getUpper());
        }
        
        try{
            rpt.originalDataSet = dataset.clone();
            rpt.means = new dataPt[1];
            rpt.means[0] = result;
            rpt.reducedChiSq = WeightedAveChiSq(dataset, result.getValue()) / 
                    (double)(n-1);
            weightSum = MathBasicFunction.sum(weights);
            rpt.relativeWeights = new double[n];
            for(i=0; i<n; i++){
                rpt.relativeWeights[i] = weights[i] / weightSum;
            }
            rpt.methodName = "Mandel-Paule Method";
        }catch(NullPointerException e){
            // do nothing if rpt is null
        }
        return result;
    }
    /**
     * Calls <code>{@link #mp(ensdf_datapoint.dataPt[], double, int, averagingAlgorithms.averagingReport) 
     * mp(dataset, 1e-12, 1000, rpt)}</code>. See <code>
     * {@link #mp(ensdf_datapoint.dataPt[], double, int, averagingAlgorithms.averagingReport) 
     * mp(dataPt[], double, int, averagingReport)}</code> for full argument
     * descriptions.
     * @param dataset
     * @param rpt
     * @return the MP result
     */
    public static final dataPt mp(dataPt[] dataset,
            averagingReport rpt){
        return mp(dataset, 1e-12, 1000, rpt);
    }
    /**
     * Calls <code>{@link #mp(ensdf_datapoint.dataPt[], double, int, averagingAlgorithms.averagingReport) 
     * mp(dataset, precision, maxIt, null)}</code>. See <code>
     * {@link #mp(ensdf_datapoint.dataPt[], double, int, averagingAlgorithms.averagingReport) 
     * mp(dataPt[], double, int, averagingReport)}</code> for full argument
     * descriptions.
     * @param dataset
     * @param precision
     * @param maxIt
     * @return the MP result
     */
    public static final dataPt mp(dataPt[] dataset,
            double precision, int maxIt){
        return mp(dataset, precision, maxIt, null);
    }
    /**
     * Calls <code>{@link #mp(ensdf_datapoint.dataPt[], double, int, averagingAlgorithms.averagingReport) 
     * mp(dataset, 1e-12, 1000, null)}</code>. See <code>
     * {@link #mp(ensdf_datapoint.dataPt[], double, int, averagingAlgorithms.averagingReport) 
     * mp(dataPt[], double, int, averagingReport)}</code> for full argument
     * descriptions.
     * @param dataset
     * @return the MP result
     */
    public static final dataPt mp(dataPt[] dataset){
        return mp(dataset, 1e-12, 1000, null);
    }
    
    // consistant minimum variance method. Returns the mean such that the
    // associated variance for the mean to be consistent with the dataset
    // with probability p (in %) is minimized.
    /**
     * Calculates the "average" of the dataset using the Consistent Minium
     * Variance method. This method returns the mean such that the associated
     * variance for the mean to be consistent with the dataset (within
     * probability p according to Birch's criterion) is minimized.
     * @param dataset the set of measurements to compute the mean of
     * @param p the probability (in %) that the dataset in consistent with the mean
     * @return the Consistent Minimum Variance method result
     */
    public static final dataPt consistanMinimumVarianceMethod(dataPt[] dataset, 
            double p){
        final int MAXSTEPS = 1000;
        final double precision = Math.sqrt(Math.nextUp(0d)); // sqrt machine epsilon
        double mean, variance;
        double lower, upper, minVar, lowerThird, upperThird;
        int i, n;
        dataPt result;
        
        n = dataset.length;
        
        lower = dataset[0].getValue();
        upper = dataset[0].getValue();
        minVar = dataset[0].gaussVariance();
        
        for(i=1; i<n; i++){
            if(dataset[i].getValue() > upper){
                upper = dataset[i].getValue();
            }else if(dataset[i].getValue() < lower){
                lower = dataset[i].getValue();
            }
            if(dataset[i].gaussVariance() < minVar){
                minVar = dataset[i].gaussVariance();
            }
        }
        mean = 0d;
        
        // search for minimum using a ternary search
        for(i=0; i<MAXSTEPS; i++){
            if (upper - lower < precision){
                break;
            }
            lowerThird = lower + (upper - lower)/3d;
            upperThird = upper - (upper - lower)/3d;
            
            if(outlierMethods.consistantVariance(lowerThird, dataset, p) > 
                    outlierMethods.consistantVariance(upperThird, dataset, p)){
                lower = lowerThird;
            }else{
                upper = upperThird;
            }
            mean = 0.5d * (upper + lower);
        }
        
        
        variance = outlierMethods.consistantVariance(mean, dataset, p);

        if(variance < 0){
            variance = minVar;
        }

        result = new dataPt(mean, Math.sqrt(variance), Math.sqrt(variance));
        
        return(result);
    }
}
