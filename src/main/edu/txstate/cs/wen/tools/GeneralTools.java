package edu.txstate.cs.wen.tools;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.BinaryRealExpression;
import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.SymbolicReal;

public class GeneralTools {
	// This map is used to store the input value for each var for later use. Key is the var name in final PC.
	static Map<String, Object> VARVALUE = new HashMap<String, Object>();

    // Store symbolic values based on JPF file
    public static void storeJPFSymbMethodVarInfo(String symVarNameStr, Object[] argValues, int index) {
        // Show the varname/value pair to make sure we got it right
    	/*
        System.out.println("###########################");
        System.out.println("symVarNameStr="+symVarNameStr);
        System.out.println("value="+argValues[index]);
        System.out.println("###########################");
        */
        // Store the corresponding concrete value for each symbolic variable, so we can replace them later
        //VARVALUE.put(symVarNameStr, argValues[index]);
    }
    
    // Store symbolic values that are NOT in JPF file
    public static void storeHeader(Constraint header) {
    	 // Show some information of the PC
    	/*System.out.println("###########################");
        System.out.println("Header:"+header);
        System.out.println("Left:"+header.getLeft());
        System.out.println("Left type:"+header.getLeft().getClass());
        System.out.println("Input value:"+varValue.get(header.getLeft().toString()));
        System.out.println("Comparator:"+header.getComparator());
        System.out.println("Right:"+header.getRight());
        System.out.println("Right type:"+header.getRight().getClass());
        System.out.println("Input value:"+varValue.get(header.getRight().toString()));*/
        
       	if (header.getLeft() instanceof BinaryRealExpression) {
       		storeRealExpression(header.getLeft());                            		
       	} else {
       		//printExpressionTemp(header.getLeft());
       	}
       	if (header.getRight() instanceof BinaryRealExpression) {
       		storeRealExpression(header.getRight());                            		
       	} else {
       		//printExpressionTemp(header.getRight());
       	}
        header = header.and;  
        if (header != null) {
        	storeHeader(header);
        }
        //System.out.println("###########################");
        
    }
    
    // Recursively breakdown expression to the most refined level. Only support Real types for now
    public static void storeRealExpression(Expression e) {
    	//System.out.println("Type: " + e.getClass());
    	//System.out.println("Expr: " + e);
    	if (e instanceof BinaryRealExpression) {
    		BinaryRealExpression bre = (BinaryRealExpression) e;
    		storeRealExpression(bre.getLeft());
    		storeRealExpression(bre.getRight());
    	}
    	/*
    	if (e instanceof SymbolicReal) {
    		//System.out.println(e.toString().substring(e.toString().lastIndexOf("_")+1));
    		double value = Double.valueOf(e.toString().substring(e.toString().lastIndexOf("_")+1));
    		VARVALUE.put(e.toString(), value);
    	}
    	*/
    }
}
