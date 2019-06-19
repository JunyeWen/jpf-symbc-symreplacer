package edu.txstate.cs.wen.tools;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.BinaryRealExpression;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.numeric.LinearIntegerConstraint;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.RealConstant;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
import gov.nasa.jpf.symbc.numeric.SymbolicReal;

public class ConstraintReplacer {

    // This map is used to store the input value for each var for later use. Key is the var name in final PC.
    Map<String, Object> varValue = new HashMap<String, Object>();

    // Store symbolic values based on JPF file
    public void storeJPFSymbMethodVarInfo(String symVarNameStr, Object[] argValues, int index) {
        // Show the varname/value pair to make sure we got it right
    	/*
        System.out.println("###########################");
        System.out.println("symVarNameStr="+symVarNameStr);
        System.out.println("value="+argValues[index]);
        System.out.println("###########################");
        */
        // Store the corresponding concrete value for each symbolic variable, so we can replace them later
        varValue.put(symVarNameStr, argValues[index]);
    }
    
    // Store symbolic values that are NOT in JPF file
    public void storeHeader(Constraint header) {
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
       		storeExpressionRecur(header.getLeft());                            		
       	} else {
       		//printExpressionTemp(header.getLeft());
       	}
       	if (header.getRight() instanceof BinaryRealExpression) {
       		storeExpressionRecur(header.getRight());                            		
       	} else {
       		//printExpressionTemp(header.getRight());
       	}
        header = header.and;  
        if (header != null) {
        	storeHeader(header);
        }
        //System.out.println("###########################");
        
    }
    
    // For DEBUG: print expressing information
    public void printExpressionTemp(Expression e) {
    	System.out.println("Type: " + e.getClass());
    	System.out.println("Expr: " + e);
    }
    
    // Recursively breakdown expression to the most refined level. Only support Real types for now
    public void storeExpressionRecur(Expression e) {
    	//System.out.println("Type: " + e.getClass());
    	//System.out.println("Expr: " + e);
    	if (e instanceof BinaryRealExpression) {
    		BinaryRealExpression bre = (BinaryRealExpression) e;
    		storeExpressionRecur(bre.getLeft());
    		storeExpressionRecur(bre.getRight());
    	}
    	if (e instanceof SymbolicReal) {
    		//System.out.println(e.toString().substring(e.toString().lastIndexOf("_")+1));
    		double value = Double.valueOf(e.toString().substring(e.toString().lastIndexOf("_")+1));
    		varValue.put(e.toString(), value);
    	}
    	
    }
    
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
                int i = Integer.valueOf(varValue.get(leftString).toString());
                header.setLeft(new IntegerConstant(i));
            }
            if (header.getRight() instanceof SymbolicInteger) {
            	rightBak.put(index, (SymbolicInteger) header.getRight());
                String rightString = header.getRight().toString();
                rightString = rightString.substring(0, rightString.indexOf("["));
                //System.out.println(rightString);
               	//System.out.println(varValue.get(rightString));
               	int i = Integer.valueOf(varValue.get(rightString).toString());
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
    
    // WORKING: this method deal with Real types, for DNN
    public void testPCReplace(PathCondition pc) {
        System.out.println("###########################");
        System.out.println("TEST!");
        
        System.out.println("Before replacing:");
        System.out.println("PC (Before): " + pc);
        //System.out.println("Expression types (Before): ");
        //check(pc.header);
        
        long startTime = System.currentTimeMillis();
        Boolean solvable = pc.solve();
        long endTime   = System.currentTimeMillis();
    	System.out.println("Can be solved (Before): " + solvable);
    	System.out.println("Time (Before): " + (endTime - startTime));
    	
        
    	// WARNING: ORIGINAL PC NEED TO BE BACK UP AND RECOVER, OR IT MAY PERMENATELY CHANGE THE CONSTRAINTS
        Constraint header = pc.header;
        tempPath = "";
        replaceHeader(header);
        System.out.println("After replacing:");
        System.out.println("PC (After): " + pc);
        //System.out.println("Expression types (After): ");
        //check(pc.header);
        startTime = System.currentTimeMillis();
        solvable = pc.solve();
        endTime   = System.currentTimeMillis();
    	System.out.println("Can be solved (After): " + solvable);
    	System.out.println("Time (After): " + (endTime - startTime));
        

        header = pc.header;
        tempPath = "";
        restoreHeader(header);
        System.out.println("After restore:");
        System.out.println("PC (AfterRestore): " + pc);
        //System.out.println("Expression types (AfterRestore): ");
        //check(pc.header);
        startTime = System.currentTimeMillis();
        solvable = pc.solve();
        endTime   = System.currentTimeMillis();
    	System.out.println("Can be solved (AfterRestore): " + solvable);
    	System.out.println("Time (AfterRestore): " + (endTime - startTime));
         
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
    
	// Temporary path and replace map that used to store/restore the replaced symbolic variables
	String tempPath = "";
	Map<String, String> replaceMap = new HashMap<String, String>();
	
	// Replace symbolic variables to input concrete values
    public void replaceHeader(Constraint header) { 	 
      	if (header.getLeft() instanceof BinaryRealExpression) {
      		if (!tempPath.equals("")) {
      			tempPath = tempPath.substring(0, tempPath.length()-1);
      		}
      		tempPath = tempPath.concat("L");
      		replaceBinaryRealExpressionRecur((BinaryRealExpression) header.getLeft());                            		
      	}
      	if (header.getRight() instanceof BinaryRealExpression) {
      		if (!tempPath.equals("")) {      			
      			tempPath = tempPath.substring(0, tempPath.length()-1);
      		}
      		tempPath = tempPath.concat("R");
      		replaceBinaryRealExpressionRecur((BinaryRealExpression) header.getRight());                            		
      	}
       header = header.and;  
       if (header != null) {
    	   replaceHeader(header);
       }    
    } 
    
    public BinaryRealExpression replaceBinaryRealExpressionRecur(BinaryRealExpression e) {
    	if (e.getLeft() instanceof BinaryRealExpression) {
    		if (!tempPath.equals("")) {
      			tempPath = tempPath.substring(0, tempPath.length()-1);
      		}
      		tempPath = tempPath.concat("L");
    		e.setLeft(replaceBinaryRealExpressionRecur((BinaryRealExpression) e.getLeft()));
    	}
    	if (e.getRight() instanceof BinaryRealExpression) {
    		if (!tempPath.equals("")) {
      			tempPath = tempPath.substring(0, tempPath.length()-1);
      		}
      		tempPath = tempPath.concat("R");
    		e.setRight(replaceBinaryRealExpressionRecur((BinaryRealExpression) e.getRight()));
    	}

    	if (e.getLeft() instanceof SymbolicReal) {
    		if (!tempPath.equals("")) {
      			tempPath = tempPath.substring(0, tempPath.length()-1);
      		}
      		tempPath = tempPath.concat("L");
    		if (e.getLeft().toString().contains("_0_0_0")) {
    			e.setLeft(replaceSymbolicReal((SymbolicReal) e.getLeft()));
    		}
    	}
    	if (e.getRight() instanceof SymbolicReal) {
    		if (!tempPath.equals("")) {
      			tempPath = tempPath.substring(0, tempPath.length()-1);
      		}
      		tempPath = tempPath.concat("R");
    		if (e.getRight().toString().contains("_0_0_0")) {
    			e.setRight(replaceSymbolicReal((SymbolicReal) e.getRight()));
    		}
    	}
    	return e;
    }
   
	public RealConstant replaceSymbolicReal(SymbolicReal e) {
		String key = e.toString();
		key = key.substring(0, key.lastIndexOf("["));
		replaceMap.put(tempPath, key);
		double value = (double) varValue.get(key);
		RealConstant replaceConstant = new RealConstant(value);
		return replaceConstant;
	}

	// Restore the replaced variables back to symbolic. This must be done or else some expressions are replaced forever
	public void restoreHeader(Constraint header) { 	 
      	if (header.getLeft() instanceof BinaryRealExpression) {
      		if (!tempPath.equals("")) {
      			tempPath = tempPath.substring(0, tempPath.length()-1);
      		}
      		tempPath = tempPath.concat("L");
      		restoreBinaryRealExpressionRecur((BinaryRealExpression) header.getLeft());                            		
      	}
      	if (header.getRight() instanceof BinaryRealExpression) {
      		if (!tempPath.equals("")) {      			
      			tempPath = tempPath.substring(0, tempPath.length()-1);
      		}
      		tempPath = tempPath.concat("R");
      		restoreBinaryRealExpressionRecur((BinaryRealExpression) header.getRight());                            		
      	}
       header = header.and;  
       if (header != null) {
    	   restoreHeader(header);
       }    
    } 
    
    public BinaryRealExpression restoreBinaryRealExpressionRecur(BinaryRealExpression e) {
    	if (e.getLeft() instanceof BinaryRealExpression) {
    		if (!tempPath.equals("")) {
      			tempPath = tempPath.substring(0, tempPath.length()-1);
      		}
      		tempPath = tempPath.concat("L");
    		e.setLeft(restoreBinaryRealExpressionRecur((BinaryRealExpression) e.getLeft()));
    	}
    	if (e.getRight() instanceof BinaryRealExpression) {
    		if (!tempPath.equals("")) {
      			tempPath = tempPath.substring(0, tempPath.length()-1);
      		}
      		tempPath = tempPath.concat("R");
    		e.setRight(restoreBinaryRealExpressionRecur((BinaryRealExpression) e.getRight()));
    	}

    	if (e.getLeft() instanceof RealConstant) {
    		if (!tempPath.equals("")) {
      			tempPath = tempPath.substring(0, tempPath.length()-1);
      		}
      		tempPath = tempPath.concat("L");
      		if (replaceMap.containsKey(tempPath)) {
      			e.setLeft(restoreSymbolicReal());
      		}	
    	}
    	if (e.getRight() instanceof RealConstant) {
    		if (!tempPath.equals("")) {
      			tempPath = tempPath.substring(0, tempPath.length()-1);
      		}
      		tempPath = tempPath.concat("R");
      		if (replaceMap.containsKey(tempPath)) {
      			e.setRight(restoreSymbolicReal());
      		}
    	}
    	return e;
    }
   
	public SymbolicReal restoreSymbolicReal() {
		SymbolicReal sr = new SymbolicReal(replaceMap.get(tempPath));
		return sr;
	}


}
