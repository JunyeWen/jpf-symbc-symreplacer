package gov.nasa.jpf.symbc;

import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.util.InternalData;

public class Observations {
    
    /** Used for user-defined cost. */
	public static double lastObservedCost = 0.0;
    
	/** Used for maximization of user-defined cost. */
	public static Expression lastObservedSymbolicExpression = null;
	
	/** Used to set current input size in side-channel analysis in order to generate correct input file. */
    public static int lastObservedInputSize = -1;
    
    /** Used by MetricListener to propagate last measured metric value. */
    public static double lastMeasuredMetricValue = 0.0;
    
    public static void reset() {
        lastObservedCost = 0.0;
        lastObservedSymbolicExpression = null;
        lastObservedInputSize = -1;
        lastMeasuredMetricValue = 0.0;
    }
    
    /* YN: read and store the internal data of the DNN. */
    public static InternalData internal = null;

    public static void loadInternalData(String path) {
        Observations.internal = InternalData.createFromDataFiles(path);
    }

    public static String dataDir = "";

    public static void setDataDir(String dir) {
        dataDir = dir;
    }

    public static String getDataDir() {
        return dataDir;
    }
}
