package gov.nasa.jpf.tool;

import java.lang.reflect.InvocationTargetException;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.symbc.Observations;

public class RunSPFDnn extends RunJPF {
	
	  public static void main (String[] args) {
		    try {
		      int options = getOptions(args);

		      if (args.length == 0 || isOptionEnabled(HELP, options)) {
		        showUsage();
		        return;
		      }

		      if (isOptionEnabled(ADD_PROJECT, options)){
		        addProject(args);
		        return;
		      }
		      
		      if (isOptionEnabled(DELAY_START, options)) {
		        delay("press any key to start");
		      }
		      
		      if (isOptionEnabled(LOG, options)) {
		        Config.enableLogging(true);
		      }

		      Config conf = new Config(args);

		      if (isOptionEnabled(SHOW, options)) {
		        conf.printEntries();
		      }

		      ClassLoader cl = conf.initClassLoader(RunJPF.class.getClassLoader());

		      if (isOptionEnabled(VERSION, options)) {
		        showVersion(cl);
		      }

		      if (isOptionEnabled(BUILD_INFO, options)) {
		        showBuild(cl);
		      }
		      
		      /* YN: load internal data for DNN. */
		      String dataKey = "symbolic.dnn.data";
		      if (conf.containsKey(dataKey)) {
		    	  String dataPath = conf.getProperty(dataKey);
		    	  System.out.println("Read internal data for DNN: " + dataPath);
		    	  Observations.loadInternalData(dataPath);
				  System.out.println("Done. Okay let's start!");
		      } else {
		    	  System.out.println("Proceed without loading data.");
		      }
		      /**/
		      

		      // using JPFShell is Ok since it is just a simple non-derived interface
		      // note this uses a <init>(Config) ctor in the shell class if there is one, i.e. there is no need for a separate
		      // start(Config,..) or re-loading the config itself
		      JPFShell shell = conf.getInstance("shell", JPFShell.class);
		      if (shell != null) {
		        shell.start( removeConfigArgs(args)); // responsible for exception handling itself

		      } else {
		        // we have to load JPF explicitly through the URLClassLoader, and
		        // call its start() via reflection - interfaces would only work if
		        // we instantiate a JPF object here, which would force us to duplicate all
		        // the logging and event handling that preceedes JPF instantiation
		        Class<?> jpfCls = cl.loadClass(JPF_CLASSNAME);
		        if (!call( jpfCls, "start", new Object[] {conf,args})){
		          error("cannot find 'public static start(Config,String[])' in " + JPF_CLASSNAME);
		        }
		      }
		      
		      if (isOptionEnabled(DELAY_EXIT, options)) {
		        delay("press any key to exit");
		      }

		      
		    } catch (NoClassDefFoundError ncfx){
		      ncfx.printStackTrace();
		    } catch (ClassNotFoundException cnfx){
		      error("cannot find " + JPF_CLASSNAME);
		    } catch (InvocationTargetException ix){
		      // should already be handled by JPF
		      ix.getCause().printStackTrace();
		    }
		    
		  }

}
