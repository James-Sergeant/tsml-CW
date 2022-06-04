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

        double[] entropyArray = new double[ATTRIBUTE_COUNT];
        double[] proportionArray = new double[ATTRIBUTE_COUNT];

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

        //For each attribute i.e yes or no find the entropy
        for (int attribNode = 0; attribNode < ATTRIBUTE_COUNT; attribNode++){
            //Get class totals for the node.
            int nodeClassTotal = 0;
            for(int x: table[attribNode]){
                nodeClassTotal += x;
            }

            //Find the Attribute probability for this node.
            double[] attributeProbability = new double[CLASS_COUNT];
            for(int classCount = 0; classCount<ATTRIBUTE_COUNT; classCount++){
                attributeProbability[classCount] = (double) table[attribNode][classCount] / (double) nodeClassTotal;
            }

            double attributeEntropy = 0;
            //Calculates the entropy for the attribute.
            for (double probability: attributeProbability){
                attributeEntropy += logBase2(probability) * probability;
            }entropyArray[attribNode] = -1 * attributeEntropy;

            proportionArray[attribNode] = (double) nodeClassTotal / (double) totalElementsInRootNode;
        }

        //Find the info gain
        double infoGain = rootEntropy;
        for(int attribute = 0; attribute < ATTRIBUTE_COUNT; attribute++){
            infoGain -= proportionArray[attribute] * entropyArray[attribute];
        }

        return infoGain;
    }

    /**
     * Main method.
     *
     * @param args the options for the attribute measure main
     */
    public static void main(String[] args) {
        int[][] peatyContingencyTable = {
                {4, 0}, // Yes (4 - islay, 0 - speyside)
                {1, 5}  // No  (1 - islay, 5 - speyside)
        };

        double infoGain = measureInformationGain(peatyContingencyTable);
        System.out.println(infoGain);
    }

}
