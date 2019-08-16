# GSoC-2019 Project
Author:
  Junye Wen (junye.wen@txstate.edu)

Mentor:
  Guowei Yang (gyang@txstate.edu)
  Corina Pasareanu (corina.pasareanu@west.cmu.edu)
  Yannic Noller (yannic.noller@informatik.hu-berlin.de)

Project description:
  This project implements a new listener in jpf-symbc.
  This new listener allows user to replace symbolic variables in a generated path condition to its input concrete value based on how important the PC is, so that only the most important symbolic variables will be kept and PC solving speeds up.

How to use:
1. This repository is a fork of jpf-symbc, so that you must edit your local Java pathfinder configuration file (site.properties) and set the path of jpf-symbc to the repository.
2. All symbolic variabes must be added via Debug.addSymbolicDouble() in the subject. Symbolic variables added via .jpf files are not fully supported.
3. Edit .jpf configuration as follows:
  #Add the listener
  listener = .symbc.SymbolicReplacerListener
  #Initial rank means how many symbolic variables you want to replace in the first iteration.
  #Must be an integer greater or equal than 0.
  #If set to 0, no symbolic variable will be replaced.
  symbolic.replace.init_rank = 50
  #Minimum rank is the number of symbolic variabels you want to replace in the last iteration.
  #Must be an integer greater or equal than 0.
  symbolic.replace.min_rank = 0
  #Step is a percentage, must be an integer between 1 and 100.
  symbolic.replace.step = 20
Explanation on the example above:
  1. First iteration will replace 50 least important symbolic variables to its concrete values.
  2. If the PC after replacement is not solvable or still taking too long to solve (over 30s), 20% less symbolic variables will be replaced in next iteration, which means 50-(50-0)x20% = 40 symbolic variables will be replaced in second iteration.
  3. At least 0 symbolic variables will be replaced in the last iteration.

Code instruction:
1. Package edu.txstate.cs.wen.tools:
  Includes the replacing methors and other tool methods.
  RealConstraintReplacer.java is the class that we are using in this version, and some other alternative solution and implementation can also be found in other classes in this package.
2. Class SymbolicReplacerListener.java:
  The listener to be called in .jpf file.
3. Class SymbolicListenerLogging.java:
  This is the SymbolicListener.java with a logging to time PC solving.
4. Package gov.nasa.jpf.symbc.numeric:
  Multiple classes in this package is edited for implementation purpose.
5. src/examples.mnist2
  A small example subject

Usage consideration:
  Only support real symbolic variables, real constants and binary real expressions. Some sample code to support interger types can be found in edu.txstate.cs.wen.tools package.

# jpf-symbc
Symbolic PathFinder:
This is the new location for NASA's Symbolic PathFinder project which was originally at:
https://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-symbc.
The two copies will be kept in sync.

To compile and run the tool you will also need jpf-core which can be downloaded from here:
https://github.com/javapathfinder/jpf-core

See related projects that use Symbolic PathFinder:
https://github.com/isstac
