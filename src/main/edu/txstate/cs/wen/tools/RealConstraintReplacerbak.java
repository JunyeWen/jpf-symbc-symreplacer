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

public class RealConstraintReplacerbak {
	public static int RANK_MIN;
	public static int RANK_INIT;
	public static int RANK_STEP;
	
	public static void readConfig(Config conf) {
		if (conf.getProperty("symbolic.replace.init_rank").isEmpty()) {
			RANK_INIT = 0;
		} else {
			RANK_INIT = Integer.valueOf(conf.getProperty("symbolic.replace.init_rank"));
			System.out.println("RANK_INIT="+RANK_INIT);
		}
		
		if (conf.getProperty("symbolic.replace.init_rank").isEmpty()) {
			RANK_MIN = 0;
		} else {
			RANK_MIN = Integer.valueOf(conf.getProperty("symbolic.replace.min_rank"));
			System.out.println("RANK_MIN="+RANK_MIN);
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
			
			System.out.println("RANK_STEP="+RANK_STEP);
		}
		
	}
	
    
    // Rank symbolic variables
	public Map<String, Double> RANKMAP;
	public List<String> REPLACELIST;
	public int STEP;
	
    // WORKING: this method deal with Real types, for DNN
	public long startTime;
	public long endTime;
	public Boolean solvable;
    public void replaceAndSolvePC(PathCondition pc) {
        System.out.println("###########################");
        System.out.println("WARNING: WORKING IN PROGRESS!");      
        /*
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
        System.out.println("STEP="+STEP);
        if (header instanceof RealConstraint) {  	
        	/////////TEST!!!!!/////////
        	int i;
        	boolean breakFlag = false;
        	for (i = RANK_INIT; i >= RANK_MIN; i=i-STEP) {
        		//PathCondition pc2 = pc.make_copy();
        		PathCondition pc2 = new PathCondition();
                PathCondition.flagSolved = false;
                

        		System.out.print("Begin ranking...");
                startTime = System.currentTimeMillis();
            	rankRealHeader(pc.header);
        		sortRanking(i);
        		endTime   = System.currentTimeMillis();
        		System.out.println("Finished. Time: " + (endTime - startTime));
                
                
                System.out.print("Begin copying...");
                startTime = System.currentTimeMillis();
        		pc2.header = this.copyRealHeader((RealConstraint) header);
        		endTime   = System.currentTimeMillis();
        		System.out.println("Finished. Time: " + (endTime - startTime));
        		
        		RANKMAP  = new HashMap<String, Double>();

        		
        		System.out.print("Begin replacing...");
                startTime = System.currentTimeMillis();
            	replaceRealHeader((RealConstraint) pc2.header);
            	endTime   = System.currentTimeMillis();
        		System.out.println("Finished. Time: " + (endTime - startTime));
        		
        		System.out.print("Begin absorbing...");
                startTime = System.currentTimeMillis();
            	absorbRealHeader((RealConstraint) pc2.header);
            	endTime   = System.currentTimeMillis();
        		System.out.println("Finished. Time: " + (endTime - startTime));
        		
            	Thread t1 = new Thread("PC Solving Thread") {
            		public void run() {
            			startTime = System.currentTimeMillis();
                        solvable = pc2.solve();
                        endTime   = System.currentTimeMillis();
            		}
            	};
        		
            	solvable = false;
            	
            	t1.start();
                try {
                    t1.join(30000);
                } catch (InterruptedException e) {
                    System.out.println("t1 interrupted when waiting join");
                    e.printStackTrace();
                }
                t1.interrupt();
            	
            	System.out.println("Can be solved (After): " + solvable);
            	if (solvable) {
            		PCLIST  = new ArrayList<String>();
            		printRealHeader((RealConstraint) pc2.header);
            		for (String s : PCLIST) {
            			System.out.println(s);
            		}
        		}
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
    
	// Replace symbolic variables to input concrete values
    public void replaceRealHeader(Constraint header) { 	 
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
    
    public BinaryRealExpression replaceBinaryRealExpression(BinaryRealExpression e) {
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
   
	public RealConstant replaceSymbolicReal(SymbolicReal e) {
		String key = e.toString();
		/*
		if (key.lastIndexOf("[")!= -1) {
			key = key.substring(0, key.lastIndexOf("["));
		}
		
		double value = (double) GeneralTools.VARVALUE.get(key);
		*/
		double value = (double) Observations.values.get(key);
		
		//System.out.println("key="+key+",value="+value);
		
		
		RealConstant replaceConstant = new RealConstant(value);
		return replaceConstant;
	}

	// Copy the PC
	public RealConstraint copyRealHeader(RealConstraint header) {
		if (header == null) {
			return null;
		}

		Comparator comp = header.getComparator();	
		
		RealExpression left = (RealExpression) this.copyRealConstraint(header.getLeft());
		RealExpression right = (RealExpression) copyRealConstraint(header.getRight());
		RealConstraint newHeader = new RealConstraint(left, comp, right);
		
		newHeader.and = copyRealHeader((RealConstraint) header.and);
		return newHeader;
	}

	public Expression copyRealConstraint(Expression e) {
		if (e == null) {
			return null;
		}
		
		if (e instanceof SymbolicReal) {
			return new SymbolicReal(((SymbolicReal) e).getName());
		}
		
		if (e instanceof RealConstant) {
			return new RealConstant(((RealConstant) e).value);
		}
		
		if (e instanceof BinaryRealExpression) {
			
			/*
			Operator op = ((BinaryRealExpression) e).getOp();
			RealExpression left = (RealExpression) copyRealConstraint(((BinaryRealExpression) e).getLeft());
			//System.out.println(left);
			RealExpression right = (RealExpression) copyRealConstraint(((BinaryRealExpression) e).getRight());
			//System.out.println(right);
			return new BinaryRealExpression(left, op, right);
			*/
			return new BinaryRealExpression((RealExpression) copyRealConstraint(((BinaryRealExpression) e).getLeft()),
					((BinaryRealExpression) e).getOp(),
					(RealExpression) copyRealConstraint(((BinaryRealExpression) e).getRight())
					);
		}
		
			
		throw new RuntimeException("## Error! e: " + e.getClass());
	}
	
	
	// Print the PC
	public void printRealHeader(RealConstraint header) {
		if (header == null) {
			return;
		}
		
		printRealConstraint(header.getLeft());
		printRealConstraint(header.getRight());
			
		printRealHeader((RealConstraint) header.and);
	}

	public void printRealConstraint(Expression e) {
		if (e == null) {
			return;
		}
			
		if (e instanceof SymbolicReal) {
			//System.out.println(e);
			if (!PCLIST.contains(e.toString())) {
				PCLIST.add(e.toString());
			}
			return;
		}
			
			
		if (e instanceof BinaryRealExpression) {
			printRealConstraint(((BinaryRealExpression) e).getLeft());
			printRealConstraint(((BinaryRealExpression) e).getRight());
			return;
		}
			
				
		//throw new RuntimeException("## Error! e: " + e.getClass());
	}
	
	public List<String> PCLIST;
	
	// Shrink the PC by apply constraint calculations
	public void absorbRealHeader(RealConstraint header) {
		if (header == null) {
			return;
		}
		
		header.setLeft((RealExpression) absorbRealExpression(header.getLeft()));
		header.setRight((RealExpression) absorbRealExpression(header.getRight()));
		
		absorbRealHeader((RealConstraint) header.and);
	}
		
	public Expression absorbRealExpression(RealExpression e) {
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
