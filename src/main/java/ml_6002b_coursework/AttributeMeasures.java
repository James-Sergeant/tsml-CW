package ml_6002b_coursework;

/**
 * Empty class for Part 2.1 of the coursework.
 */

//Gain(x, a) entropyRoot - sum( casesY/casesX entropyNode)
public class AttributeMeasures {
    private static double logBase2(double val){
        return val == 0 ? 0: Math.log(val) / Math.log(2);
    }

    public static double measureInformationGain(int[][] table){
        final int CLASS_COUNT = table[0].length; //How many outcomes i.e Islay Speyside
        final int ATTRIBUTE_COUNT = table.length; //Number of options i.e yes or no

        int[] entropyArray = new int[ATTRIBUTE_COUNT];
        int[] proportionArray = new int[ATTRIBUTE_COUNT];

        //1) Find the root node's entropy
        double rootEntropy = 0;
            //1) Find the total for each class
            //2) Find the total of all classes
            //3) Calculate the root entropy.

        //Finds the total for each class and all classes
        int totalElementsInRootNode = 0;
        int[] classTotalsInRootNode = new int[CLASS_COUNT];

        for(int[] attribute : table){
            for(int classIndex = 0; classIndex < CLASS_COUNT; classIndex++){
                classTotalsInRootNode[classIndex] += attribute[classIndex];
                totalElementsInRootNode += attribute[classIndex];
            }
        }

        //Finds the root entropy.
        for(int classIndex = 0; classIndex < CLASS_COUNT; classIndex++){
            double prob = (double)classTotalsInRootNode[classIndex]/totalElementsInRootNode;
            rootEntropy += prob * logBase2(prob);
        }







        //2) Find each node's
            //1) Attribute Entropy
            //2) Weighting
            //3)

        return 0.0d;
    }

    /**
     * Main method.
     *
     * @param args the options for the attribute measure main
     */
    public static void main(String[] args) {
        System.out.println(logBase2(0));
        logBase2(4);
    }

}
