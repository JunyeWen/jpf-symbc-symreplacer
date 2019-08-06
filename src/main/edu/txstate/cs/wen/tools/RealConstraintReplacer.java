package edu.txstate.cs.wen.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.Observations;
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
	// Settings from configuration file
	public static int RANK_MIN;
	public static int RANK_INIT;
	public static int RANK_STEP;
	
    // Rank symbolic variables
	public Map<String, Double> RANKMAP;
	public List<String> REPLACELIST;
	public int STEP;
	
	// Logging and misc
	public long startTime;
	public long endTime;
	public Boolean solvable;
	
	public static void readConfig(Config conf) {
		if (conf.getProperty("symbolic.replace.init_rank").isEmpty()) {
			RANK_INIT = 0;
		} else {
			RANK_INIT = Integer.valueOf(conf.getProperty("symbolic.replace.init_rank"));
			System.out.println("symbolic.replace.init_rank="+RANK_INIT);
		}
		
		if (conf.getProperty("symbolic.replace.init_rank").isEmpty()) {
			RANK_MIN = 0;
		} else {
			RANK_MIN = Integer.valueOf(conf.getProperty("symbolic.replace.min_rank"));
			System.out.println("symbolic.replace.min_rank="+RANK_MIN);
		}
		
		if (conf.getProperty("symbolic.replace.step").isEmpty()) {
			RANK_STEP = 10;
		} else {
			RANK_STEP = Integer.valueOf(conf.getProperty("symbolic.replace.step"));
			if (RANK_STEP < 1) {
				RANK_STEP = 1;
			} if (RANK_STEP > 100) {
				RANK_STEP = 100;
			}
			
			System.out.println("symbolic.replace.step="+RANK_STEP);
		}
		
	}

    // WORKING: this method deal with Real types, for DNN
	public void replaceAndSolvePC(PathCondition pc) {
        System.out.println("###########################");
        System.out.println("WARNING: WORKING IN PROGRESS!");      
        
        Constraint header = pc.header;
        
        STEP = (RANK_INIT - RANK_MIN) * RANK_STEP / 100;
        
        if (STEP < 1) {
        	STEP = 1;
        }
        System.out.println("STEP="+STEP);
        if (header instanceof RealConstraint) {  	
        	/////////TEST!!!!!/////////
        	int i;
        	boolean breakFlag = false;
        	
        	System.out.print("Begin ranking...");
        	RANKMAP  = new HashMap<String, Double>();
            startTime = System.currentTimeMillis();
        	rankRealHeader(pc.header);
    		endTime   = System.currentTimeMillis();
    		System.out.println("Finished. Time: " + (endTime - startTime)/1000L + " seconds.");
    		System.out.println("Size: " + RANKMAP.size());
    		
        	for (i = RANK_INIT; i >= RANK_MIN; i=i-STEP) {
        		//PathCondition pc2 = pc.make_copy();
        		PathCondition pc2 = new PathCondition();
                //PathCondition.flagSolved = false;
                
        		System.out.println("Choosing " + i + " least important symbolic variables");
        		System.out.print("Begin sorting...");
                startTime = System.currentTimeMillis();
        		sortRanking(i);
        		endTime   = System.currentTimeMillis();
        		System.out.println("Finished. Time: " + (endTime - startTime)/1000L + " seconds.");
                System.out.println("Size: "+REPLACELIST.size());
        		
                System.out.print("Begin copying...");
                startTime = System.currentTimeMillis();
        		pc2.header = this.craRealHeader((RealConstraint) header);
        		endTime   = System.currentTimeMillis();
        		System.out.println("Finished. Time: " + (endTime - startTime)/1000L + " seconds.");
        		
            	Thread t1 = new Thread("PC Solving Thread") {
            		public void run() {
            			System.out.print("Begin solving...");
            			startTime = System.currentTimeMillis();
                        solvable = pc2.solve();
                        endTime = System.currentTimeMillis();
                        System.out.println("Finished. Time: " + (endTime - startTime)/1000L + " seconds.");
            		}
            	};
        		
            	solvable = false;
            	
            	int timeMS = 30;
            	t1.start();
                try {
                    t1.join(timeMS*1000);
                } catch (InterruptedException e) {
                    System.out.println("PC Solving Thread interrupted when waiting join.");
                    e.printStackTrace();
                }
                
                if (t1.isAlive()) {
                	System.out.println("PC Solving took longer than " + timeMS + " seconds.");
                	System.out.println("Thread will be terminated.");
                	t1.interrupt();
                	solvable = false;
                } else {
                	System.out.println("PC Solving finished within " + timeMS + " seconds");
                }
            	
            	System.out.println("PC can be solved: " + solvable);
            	/*
            	System.out.print("Begin printing...");
            	startTime = System.currentTimeMillis();
            	if (solvable) {
            		System.out.println("Result:");
            		PCLIST  = new ArrayList<String>();
            		printRealHeader((RealConstraint) pc2.header);
            		for (String s : PCLIST) {
            			System.out.println(s);
            		}
        		}
        		endTime = System.currentTimeMillis();
                System.out.println("Finished. Time: " + (endTime - startTime)/1000L + " seconds.");
        		*/
            	
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
        //System.out.println("After replacing:");
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
    
	// Rank and sort symbolic variables
    public void rankRealHeader(Constraint header) { 	 
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
    
    public void rankBinaryRealExpression(BinaryRealExpression e) {
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
    
    public void sortRanking(int rank) {
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
		
		REPLACELIST = new ArrayList<String>();
		
		int i = 0;
		for(Entry<String, Double> t : sortList){
			//System.out.println(t.getKey()+":"+t.getValue());
			if (i < rank) {
				REPLACELIST.add(t.getKey());
				i++;
			}
		}
		/*
		System.out.println("Replace List: " + REPLACELIST.size());
		for (String s : REPLACELIST) {
			System.out.println(s);
		}
		*/
    }
    
	// Copy, replace and absorb PC
	public RealConstraint craRealHeader (RealConstraint header) {
		if (header == null) {
			return null;
		}
		Comparator comp = header.getComparator();	
		RealExpression left = (RealExpression) craRealConstraint(header.getLeft());
		RealExpression right = (RealExpression) craRealConstraint(header.getRight());
		RealConstraint newHeader = new RealConstraint(left, comp, right);
		
		newHeader.and = craRealHeader((RealConstraint) header.and);
		return newHeader;
	}
	
	public Expression craRealConstraint(Expression e) {
		if (e == null) {
			return null;
		}
		
		if (e instanceof RealConstant) {
			// RealConstant: Keep
			return new RealConstant(((RealConstant) e).value);
		}
		
		if (e instanceof SymbolicReal) {
			// SymbolicReal: Check if needs to be replaced by RealConstant
			String sym = ((SymbolicReal) e).getName();
			
			if (REPLACELIST.contains(sym)) {
				//DEBUG
				//System.out.println("replace");
    			// Replace, return a RealConstant
				double value = (double) Observations.values.get(sym);
				RealConstant replaceConstant = new RealConstant(value);
				return replaceConstant;
    		} else {
    			// Don't replace, return SymbolicReal
    			return new SymbolicReal(sym);
    		}
		}
		
		if (e instanceof BinaryRealExpression) {
			// BinaryRealExpression: recursively copy and replace left and right, and absorb if necessary
			Operator op = ((BinaryRealExpression) e).getOp();
			RealExpression left = (RealExpression) craRealConstraint(((BinaryRealExpression) e).getLeft());
			RealExpression right = (RealExpression) craRealConstraint(((BinaryRealExpression) e).getRight());
			//DEBUG
			//System.out.println(left+" "+op+" "+right);
			if (left instanceof RealConstant && right instanceof RealConstant) {
				//DEBUG
				//System.out.println("absorb");
				// Left and right are both RealConstant, absorb needed
				double leftValue = ((RealConstant) left).value;
				double rightValue = ((RealConstant) right).value;
				switch(op){
			      case PLUS:
			    	  return new RealConstant(leftValue + rightValue);
			      case MINUS:
			    	  return new RealConstant(leftValue - rightValue);
			      case MUL:
			    	  return new RealConstant(leftValue * rightValue);
			      case DIV:
			    	  return new RealConstant(leftValue / rightValue);
			      case REM:
			    	  return new RealConstant(leftValue % rightValue);
			      default:
			          throw new RuntimeException("## Error: op not supported. left: " + leftValue + ", right: " + rightValue + ", op: " + op);
				}
			} else {
				// Absorb not needed
				return new BinaryRealExpression(left, op, right); 
			}
		}
		
		// Other types: not supported, throw exception
		throw new RuntimeException("## Error: not supported. e: " + e.getClass());
	}
	
	// Print PC
	public static void printRealHeader(RealConstraint header) {
		if (header == null) {
			return;
		}
		
		printRealConstraint(header.getLeft());
		printRealConstraint(header.getRight());
			
		printRealHeader((RealConstraint) header.and);
	}

	public static void printRealConstraint(Expression e) {
		if (e == null) {
			return;
		}
			
		if (e instanceof SymbolicReal) {
			System.out.println(e);
			if (PCLIST.isEmpty()||!PCLIST.contains(e.toString())) {
				PCLIST.add(e.toString());
			}
			return;
		}
			
			
		if (e instanceof BinaryRealExpression) {
			printRealConstraint(((BinaryRealExpression) e).getLeft());
			printRealConstraint(((BinaryRealExpression) e).getRight());
			return;
		}
	}
	
	public static List<String> PCLIST = new ArrayList<String>();
	
}
