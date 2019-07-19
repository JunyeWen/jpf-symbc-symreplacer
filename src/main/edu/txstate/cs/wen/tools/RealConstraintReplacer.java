package edu.txstate.cs.wen.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.numeric.BinaryRealExpression;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.Operator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.RealConstant;
import gov.nasa.jpf.symbc.numeric.RealConstraint;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicReal;

public class RealConstraintReplacer {
	public static int RANK_MIN;
	public static int RANK_INIT;
	public static int RANK_STEP;
	
	public static void readConfig(Config conf) {
		if (conf.getProperty("symbolic.replace.init_rank").isEmpty()) {
			RANK_INIT = 0;
		} else {
			RANK_INIT = Integer.valueOf(conf.getProperty("symbolic.replace.init_rank"));
		}
		
		if (conf.getProperty("symbolic.replace.init_rank").isEmpty()) {
			RANK_MIN = 0;
		} else {
			RANK_MIN = Integer.valueOf(conf.getProperty("symbolic.replace.min_rank"));
		}
		
		if (conf.getProperty("symbolic.replace.init_rank").isEmpty()) {
			RANK_STEP = 10;
		} else {
			RANK_STEP = Integer.valueOf(conf.getProperty("symbolic.replace.min_rank"));
			if (RANK_STEP < 1) {
				RANK_STEP = 1;
			} if (RANK_STEP > 100) {
				RANK_STEP = 100;
			}
		}
		
	}
	
    
    // Rank symbolic variables
    static Map<String, Double> RANKMAP = new HashMap<String, Double>();
    static List<String> REPLACELIST = new ArrayList<String>();
    static int STEP;
	
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
        
        STEP = (RANK_INIT - RANK_MIN) * RANK_STEP / 100;
        
        if (STEP < 1) {
        	STEP = 1;
        }
        
        if (header instanceof RealConstraint) {  	
        	/////////TEST!!!!!/////////
        	int i;
        	boolean breakFlag = false;
        	for (i = RANK_INIT; i >= RANK_MIN; i=i-STEP) {
        		PathCondition pc2 = pc.make_copy();
                PathCondition.flagSolved = false;
        		pc2.header = copyRealHeader((RealConstraint) header);
            	rankRealHeader((RealConstraint) pc2.header);
            	
        		sortRanking(i);
            	replaceRealHeader((RealConstraint) pc2.header);
            	absorbRealHeader((RealConstraint) pc2.header);

        		
        		startTime = System.currentTimeMillis();
                solvable = pc2.solve();
                endTime   = System.currentTimeMillis();
            	System.out.println("Can be solved (After): " + solvable);
            	System.out.println("Time (After): " + (endTime - startTime));
            	
            	if (solvable || breakFlag) {
            		break;
            	}
            	
            	if (i - STEP < RANK_MIN) {
            		// Make sure last iteration replace RANK_MIN symbolic vars
            		i = RANK_MIN + STEP;
            		breakFlag = true;
            	}
            	
        	}
        	if (i < RANK_MIN) {
        		System.out.println("PC NOT SOLVABLE AFTER REPLACEMENT!");
        	}
        	
        }
        System.out.println("After replacing:");
        //System.out.println("Expression types (After): ");
        //check(pc.header);
        
        /*
        startTime = System.currentTimeMillis();
        solvable = pc2.solve();
        endTime   = System.currentTimeMillis();
    	System.out.println("Can be solved (After): " + solvable);
    	System.out.println("Time (After): " + (endTime - startTime));
    	*/
    	/*
    	if (solvable) {
    		System.out.println("PC (After): " + pc2);
    	}
    	*/
    	
    }
    
    public static void rankRealHeader(Constraint header) { 	 
    	if (header.getLeft() instanceof BinaryRealExpression) {
    		rankBinaryRealExpression((BinaryRealExpression) header.getLeft());                            		
      	}
      	if (header.getRight() instanceof BinaryRealExpression) {
      		rankBinaryRealExpression((BinaryRealExpression) header.getRight());                            		
      	}
      	header = header.and;  
      	if (header != null) {
      		rankRealHeader(header);
      	}    
	} 
    
    public static void rankBinaryRealExpression(BinaryRealExpression e) {
    	if (e.getLeft() instanceof BinaryRealExpression) {
    		BinaryRealExpression bre = (BinaryRealExpression) e.getLeft();
    		if (bre.getLeft() instanceof RealConstant && bre.getRight() instanceof SymbolicReal) {
    			double value = Double.valueOf(bre.getLeft().toString().substring(6));
    			// Impact should be considered as absolute value?
    			if (value < 0) {
    				value = 0 - value;
    			}
    			String var = bre.getRight().toString();
    			//System.out.println("Find: " + value + ", " + var);
    			if (RANKMAP.containsKey(var)) {
    				RANKMAP.put(var, RANKMAP.get(var)+value);
    			} else {
    				RANKMAP.put(var, value);
    			}
    		}
    		if (bre.getLeft() instanceof SymbolicReal && bre.getRight() instanceof RealConstant) {
    			double value = Double.valueOf(bre.getRight().toString().substring(6));
    			// Impact should be considered as absolute value?
    			if (value < 0) {
    				value = 0 - value;
    			}
    			String var = bre.getLeft().toString();
    			//System.out.println("Find: " + value + ", " + var);
    			if (RANKMAP.containsKey(var)) {
    				RANKMAP.put(var, RANKMAP.get(var)+value);
    			} else {
    				RANKMAP.put(var, value);
    			}
    		}	
    		rankBinaryRealExpression(bre);
    	}
    	if (e.getRight() instanceof BinaryRealExpression) {
    		BinaryRealExpression bre = (BinaryRealExpression) e.getRight();   	
    		if (bre.getLeft() instanceof RealConstant && bre.getRight() instanceof SymbolicReal) {
    			double value = Double.valueOf(bre.getLeft().toString().substring(6));
    			// Impact should be considered as absolute value
    			if (value < 0) {
    				value = 0 - value;
    			}
    			String var = bre.getRight().toString();
    			//System.out.println("Find: " + value + ", " + var);
    			assert(value >=0);
    			if (RANKMAP.containsKey(var)) {
    				RANKMAP.put(var, RANKMAP.get(var)+value);
    			} else {
    				RANKMAP.put(var, value);
    			}
    		}
    		if (bre.getLeft() instanceof SymbolicReal && bre.getRight() instanceof RealConstant) {
    			double value = Double.valueOf(bre.getRight().toString().substring(6));
    			// Impact should be considered as absolute value
    			if (value < 0) {
    				value = 0 - value;
    			}
    			String var = bre.getLeft().toString();
    			//System.out.println("Find: " + value + ", " + var);
    			if (RANKMAP.containsKey(var)) {
    				assert(RANKMAP.get(var)>0);
    				RANKMAP.put(var, RANKMAP.get(var)+value);
    			} else {
    				RANKMAP.put(var, value);
    			}
    		}
    		rankBinaryRealExpression(bre);
    	}
    }
    
    public static void sortRanking(int rank) {
    	if (rank < RANK_MIN) {
    		rank = RANK_MIN;
    	}
    	// Sort
    	List<Entry<String, Double>> sortList = new ArrayList<Entry<String, Double>>(RANKMAP.entrySet());
		Collections.sort(sortList, new java.util.Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				if (o2.getValue() < o1.getValue()) return 1;
		        if (o1.getValue() < o2.getValue()) return -1;
		        return 0;
			}
		});
		
		REPLACELIST.clear();
		
		int i = 0;
		for(Entry<String, Double> t : sortList){
			//System.out.println(t.getKey()+":"+t.getValue());
			if (i < rank) {
				REPLACELIST.add(t.getKey());
				i++;
			}
		}
		
		System.out.println("Replace List: " + REPLACELIST.size());
		for (String s : REPLACELIST) {
			System.out.println(s);
		}
		
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
    		BinaryRealExpression bre = (BinaryRealExpression) e.getLeft();
    		e.setLeft(replaceBinaryRealExpression(bre));
    	}
    	if (e.getRight() instanceof BinaryRealExpression) {
    		BinaryRealExpression bre = (BinaryRealExpression) e.getRight();   		
    		e.setRight(replaceBinaryRealExpression(bre));
    	}

    	if (e.getLeft() instanceof SymbolicReal) {
    		String sym = e.getLeft().toString();
    		SymbolicReal sr = (SymbolicReal) e.getLeft();
    		if (REPLACELIST.contains(sym)) {
    			//System.out.println("Replace: " + sym);
    			e.setLeft(replaceSymbolicReal(sr));
    		}
    		/*
    		if (sym.contains("_0_0_0")) {
    			e.setLeft(replaceSymbolicReal(sr));
    		}
    		*/
    	}
    	if (e.getRight() instanceof SymbolicReal) {
    		String sym = e.getRight().toString();
    		SymbolicReal sr = (SymbolicReal) e.getRight();
    		if (REPLACELIST.contains(sym)) {
    			//System.out.println("Replace: " + sym);
    			e.setLeft(replaceSymbolicReal(sr));
    		}
    		/*
    		if (sym.contains("_0_0_0")) {
    			e.setRight(replaceSymbolicReal(sr));
    		}
    		*/
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
	}
}
