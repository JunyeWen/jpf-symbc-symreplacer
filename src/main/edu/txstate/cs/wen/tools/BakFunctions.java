package edu.txstate.cs.wen.tools;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.BinaryRealExpression;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.numeric.LinearIntegerConstraint;
import gov.nasa.jpf.symbc.numeric.Operator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.RealConstant;
import gov.nasa.jpf.symbc.numeric.RealConstraint;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
import gov.nasa.jpf.symbc.numeric.SymbolicReal;

public class BakFunctions {
	
    // BACKUP: this method deal with Integer types
    public void testPCReplaceBAK(PathCondition pc) {
        System.out.println("###########################");
        System.out.println("TEST!");
        // Sample: Replace a symbolic variable with the concrete value in the input
        System.out.println("PC (Before): " + pc);
    	System.out.println("Can be solved: " + pc.solve());
    	// WARNING: ORIGINAL PC NEED TO BE BACK UP AND RECOVER, OR IT MAY PERMENATELY CHANGE THE CONSTRAINTS
        Constraint header = pc.header;
        Map<Integer, SymbolicInteger> leftBak = new HashMap<Integer, SymbolicInteger>();
        Map<Integer, SymbolicInteger> rightBak = new HashMap<Integer, SymbolicInteger>();
        
        int index = 1;
        do {
            if (header.getLeft() instanceof SymbolicInteger) {
            	leftBak.put(index, (SymbolicInteger) header.getLeft());
                String leftString = header.getLeft().toString();
                leftString = leftString.substring(0, leftString.indexOf("["));
                //System.out.println(leftString);
                //System.out.println(varValue.get(leftString));
                int i = Integer.valueOf(GeneralTools.VARVALUE.get(leftString).toString());
                header.setLeft(new IntegerConstant(i));
            }
            if (header.getRight() instanceof SymbolicInteger) {
            	rightBak.put(index, (SymbolicInteger) header.getRight());
                String rightString = header.getRight().toString();
                rightString = rightString.substring(0, rightString.indexOf("["));
                //System.out.println(rightString);
               	//System.out.println(varValue.get(rightString));
               	int i = Integer.valueOf(GeneralTools.VARVALUE.get(rightString).toString());
               	header.setRight(new IntegerConstant(i));
            }
           	header = header.and;
           	index++;
        } while (header != null);
       	System.out.println("PC (After): " + pc);
       	System.out.println("Can be solved: " + pc.solve());
       	
       	header = pc.header;
       	index = 1;
        do {
            if (leftBak.get(index)!=null) {
                header.setLeft(leftBak.get(index));
            }
            if (rightBak.get(index)!=null) {
                header.setLeft(rightBak.get(index));
            }
           	header = header.and;
           	index++;
        } while (header != null);
       	System.out.println("PC (Recover): " + pc);
       	System.out.println("Can be solved: " + pc.solve());
       	
        // Sample: Create and solve our own PC
        PathCondition pc2 = new PathCondition();
		SymbolicInteger l = new SymbolicInteger("x_1_SYMINT");
		SymbolicInteger r = new SymbolicInteger("y_2_SYMINT");
		//IntegerConstant r = new IntegerConstant(5);
		Comparator c = Comparator.valueOf("LE");
		pc2.header = new LinearIntegerConstraint(l,c,r);
		pc2.solve();
		System.out.println(pc2);                    		
		System.out.println(l.solution);
		
		System.out.println("###########################");
    }
    
    // For DEBUG: print expressing information
    public void printExpressionTemp(Expression e) {
    	System.out.println("Type: " + e.getClass());
    	System.out.println("Expr: " + e);
    } 
    
    // For DEBUG: check the type of expressions
    public void check(Constraint header) { 	 
    	//System.out.println(header.getClass());
    	check2(header.getLeft());
    	check2(header.getRight());
    	
    	header = header.and;  
    	if (header != null) {
    		check(header);
    	}    
    }
    
    // For DEBUG: check the type of expressions
    public void check2(Expression e) {
    	if (e instanceof BinaryRealExpression) {
    		check2(((BinaryRealExpression) e).getLeft());
    		check2(((BinaryRealExpression) e).getRight());
    	} else {
    		if (e instanceof SymbolicReal)
    		System.out.println(e.getClass() + ": " + e);
    	}
    	
    }
    
	
}
