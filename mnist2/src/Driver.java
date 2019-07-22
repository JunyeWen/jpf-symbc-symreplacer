import java.io.FileInputStream;
import java.io.IOException;
import gov.nasa.jpf.symbc.Debug;

public class Driver {

    static final int IMG_HEIGHT = 28; /* 28 */
    static final int IMG_WIDTH = 28; /* 28 */

    public static void main(String[] args) {

        double[][][] a = new double[IMG_HEIGHT][IMG_WIDTH][1];
        
        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            // Reading input from fuzzed file.
            try (FileInputStream fis = new FileInputStream(fileName)) {
                /* Read pixel values from [0, 255] and normalize them to [0, 1] */
                byte[] bytes = new byte[1];

                for (int i = 0; i < IMG_HEIGHT; i++) {
                    for (int j = 0; j < IMG_WIDTH; j++) {
                        for (int k = 0; k < 1; k++) {

                            if (fis.read(bytes) == -1) {
                                throw new RuntimeException("Not enough data to read input!");
                            }

                            // /* Add symbolic byte. */
                            // byte byteValue = Debug.addSymbolicByte(bytes[0], "sym_" + i + "_" + j + "_" + k);
                            //
                            // /* Normalize value from [-128,127] to be in range [0, 1] */
                            // double value = (byteValue + 128) / 255.0;
                            //
                            // /* Add double Value */
                            // a[i][j][k] = value;

                            double value = (bytes[0] + 128) / 255.0;
                            
                            
                            if (i==0 && j==0 && k==0) {
                            	a[i][j][k] = Debug.addSymbolicDouble(value, "sym_" + i + "_" + j + "_" + k);	
                            } else {
                            	a[i][j][k] = value;
                            }
//                            a[i][j][k] = Debug.addSymbolicDouble(value, "sym_" + i + "_" + j + "_" + k);	

                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                return;
            }

        } else {
            for (int i = 0; i < IMG_HEIGHT; i++) {
                for (int j = 0; j < IMG_WIDTH; j++) {
                    for (int k = 0; k < 1; k++) {

                        // /* Add pure symbolic byte. */
                        // byte byteValue = Debug.makeSymbolicByte("sym_" + i + "_" + j + "_" + k);
                        //
                        // /* Normalize value from [-128,127] to be in range [0, 1] */
                        // double value = (byteValue + 128) / 255.0;
                        //
                        // /* Add double Value */
                        // a[i][j][k] = value;

                        a[i][j][k] = Debug.makeSymbolicReal("sym_" + i + "_" + j + "_" + k);

                    }
                }
            }

        }

        /* Read internal data. */
        InternalData internalData = new InternalData();
        internalData.biases0 = Debug.getBiases0();
        internalData.biases2 = Debug.getBiases2();
        internalData.biases6 = Debug.getBiases6();
        internalData.biases8 = Debug.getBiases8();
        internalData.weights0 = Debug.getWeights0();
        internalData.weights2 = Debug.getWeights2();
        internalData.weights6 = Debug.getWeights6();
        internalData.weights8 = Debug.getWeights8();
        
        DNNt dnn = new DNNt(internalData);
        int res = dnn.run(a);

    }
    
}
