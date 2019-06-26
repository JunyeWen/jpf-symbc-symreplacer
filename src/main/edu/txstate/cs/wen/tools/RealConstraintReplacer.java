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

public class RealConstraintReplacer {
  
    // WORKING: this method deal with Real types, for DNN
    public static void replaceAndSolvePC(PathCondition pc) {
        System.out.println("###########################");
        System.out.println("WARNING: WORKING IN PROGRESS!");
        long startTime;
        long endTime;
        Boolean solvable;
        
        
        System.out.println("Before replacing:");
        //System.out.println("Expression types (Before): ");
        //check(pc.header);
        startTime = System.currentTimeMillis();
        solvable = pc.solve();
        endTime = System.currentTimeMillis();
    	System.out.println("Can be solved (Before): " + solvable);
    	System.out.println("Time (Before): " + (endTime - startTime));
    	/*
    	if (solvable) {
    		System.out.println("PC (Before): " + pc);
    	}
        */
        
        Constraint header = pc.header;
        PathCondition pc2 = pc.make_copy();
        PathCondition.flagSolved = false;
        if (header instanceof RealConstraint) {
        	pc2.header = copyRealHeader((RealConstraint) header);
        	replaceRealHeader((RealConstraint) pc2.header);
        	absorbRealHeader((RealConstraint) pc2.header);
        }
        System.out.println("After replacing:");
        //System.out.println("Expression types (After): ");
        //check(pc.header);
        startTime = System.currentTimeMillis();
        solvable = pc2.solve();
        endTime   = System.currentTimeMillis();
    	System.out.println("Can be solved (After): " + solvable);
    	System.out.println("Time (After): " + (endTime - startTime));
    	/*
    	if (solvable) {
    		System.out.println("PC (After): " + pc2);
    	}
    	*/
    	
    }
   
	// Replace symbolic variables to input concrete values
    public static void replaceRealHeader(Constraint header) { 	 
    	if (header.getLeft() instanceof BinaryRealExpression) {
      		replaceBinaryRealExpression((BinaryRealExpression) header.getLeft());                            		
      	}
      	if (header.getRight() instanceof BinaryRealExpression) {

      		replaceBinaryRealExpression((BinaryRealExpression) header.getRight());                            		
      	}
      	header = header.and;  
      	if (header != null) {
      		replaceRealHeader(header);
      	}    
	} 
    
    public static BinaryRealExpression replaceBinaryRealExpression(BinaryRealExpression e) {
    	if (e.getLeft() instanceof BinaryRealExpression) {
    		e.setLeft(replaceBinaryRealExpression((BinaryRealExpression) e.getLeft()));
    	}
    	if (e.getRight() instanceof BinaryRealExpression) {
    		e.setRight(replaceBinaryRealExpression((BinaryRealExpression) e.getRight()));
    	}

    	if (e.getLeft() instanceof SymbolicReal) {
    		if (e.getLeft().toString().contains("_0_0_0")) {
    			e.setLeft(replaceSymbolicReal((SymbolicReal) e.getLeft()));
    		}
    		if (e.getLeft().toString().contains("_2_2_0")) {
    			e.setLeft(replaceSymbolicReal((SymbolicReal) e.getLeft()));
    		}
    		if (e.getLeft().toString().contains("_4_4_0")) {
    			e.setLeft(replaceSymbolicReal((SymbolicReal) e.getLeft()));
    		}
    	}
    	if (e.getRight() instanceof SymbolicReal) {
    		if (e.getRight().toString().contains("_0_0_0")) {
    			e.setRight(replaceSymbolicReal((SymbolicReal) e.getRight()));
    		}
    		if (e.getRight().toString().contains("_2_2_0")) {
    			e.setRight(replaceSymbolicReal((SymbolicReal) e.getRight()));
    		}
    		if (e.getRight().toString().contains("_4_4_0")) {
    			e.setRight(replaceSymbolicReal((SymbolicReal) e.getRight()));
    		}
    	}
    	return e;
    }
   
	public static RealConstant replaceSymbolicReal(SymbolicReal e) {
		String key = e.toString();
		if (key.lastIndexOf("[")!= -1) {
			key = key.substring(0, key.lastIndexOf("["));
		}
		double value = (double) GeneralTools.VARVALUE.get(key);
		RealConstant replaceConstant = new RealConstant(value);
		return replaceConstant;
	}

	// Copy the PC
	public static RealConstraint copyRealHeader(RealConstraint header) {
		if (header == null) {
			return null;
		}

		Comparator comp = header.getComparator();	
		
		RealExpression left = (RealExpression) copyRealConstraint(header.getLeft());
		RealExpression right = (RealExpression) copyRealConstraint(header.getRight());
		RealConstraint newHeader = new RealConstraint(left, comp, right);
		
		newHeader.and = copyRealHeader((RealConstraint) header.and);
		return newHeader;
	}

	public static Expression copyRealConstraint(Expression e) {
		if (e == null) {
			return null;
		}
		
		if (e instanceof SymbolicReal) {
			SymbolicReal temp = (SymbolicReal) e;
			return new SymbolicReal(temp.getName());
		}
		
		if (e instanceof RealConstant) {
			RealConstant temp = (RealConstant) e;
			return new RealConstant(temp.value);
		}
		
		if (e instanceof BinaryRealExpression) {
			BinaryRealExpression temp = (BinaryRealExpression) e;
			Operator op = temp.getOp();
			RealExpression left = (RealExpression) copyRealConstraint(temp.getLeft());
			RealExpression right = (RealExpression) copyRealConstraint(temp.getRight());
			return new BinaryRealExpression(left, op, right);
		}
		
			
		throw new RuntimeException("## Error! e: " + e.getClass());
	}
	
	// Shrink the PC by apply constraint calculations
	public static void absorbRealHeader(RealConstraint header) {
		if (header == null) {
			return;
		}
		
		header.setLeft((RealExpression) absorbRealExpression(header.getLeft()));
		header.setRight((RealExpression) absorbRealExpression(header.getRight()));
		
		absorbRealHeader((RealConstraint) header.and);
	}
		
	public static Expression absorbRealExpression(RealExpression e) {
		if (e instanceof SymbolicReal || e instanceof RealConstant) {
			return e;
		}
		
		if (e instanceof BinaryRealExpression) {
			BinaryRealExpression be = (BinaryRealExpression) e;
			
			if (be.getLeft() instanceof BinaryRealExpression) {
				BinaryRealExpression beL = (BinaryRealExpression) be.getLeft();
				if (!(beL.getLeft() instanceof SymbolicReal) && !(beL.getRight() instanceof SymbolicReal)) {
					be.setLeft(absorbRealExpression(be.getLeft()));
				}	
			}
			
			if (be.getRight() instanceof BinaryRealExpression) {
				BinaryRealExpression beR = (BinaryRealExpression) be.getRight();
				if (!(beR.getLeft() instanceof SymbolicReal) && !(beR.getRight() instanceof SymbolicReal)) {
					be.setRight(absorbRealExpression(be.getRight()));
				}	
			}
			
			
			if (be.getLeft() instanceof SymbolicReal && be.getRight() instanceof SymbolicReal) {
				return e;
			}
			
			if (be.getLeft() instanceof SymbolicReal && be.getRight() instanceof RealConstant) {
				return e;
			}
			
			if (be.getLeft() instanceof RealConstant && be.getRight() instanceof SymbolicReal) {
				return e;
			}
			
			if (be.getLeft() instanceof RealConstant && be.getRight() instanceof RealConstant) {
				Operator opRef = be.getOp();
				double left = Double.valueOf(be.getLeft().toString().substring(6));
				double right = Double.valueOf(be.getRight().toString().substring(6));
				switch(opRef){
			      case PLUS:
			    	  return new RealConstant(left + right);
			      case MINUS:
			    	  return new RealConstant(left - right);
			      case MUL:
			    	  return new RealConstant(left * right);
			      case DIV:
			    	  return new RealConstant(left / right);
			      case REM:
			    	  return new RealConstant(left % right);
			      default:
			          throw new RuntimeException("## Error! left: " + left + ", right: " + right + ", op: " + opRef);
				}
			}
		} else {
			throw new RuntimeException("## Error! e: " + e.getClass());
		}
		return e;
		//throw new RuntimeException("## How did you get here?! e: " + e.getClass());
		
	}
}
