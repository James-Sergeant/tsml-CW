package ml_6002b_coursework;


/**
 * Empty class for Part 2.1 of the coursework.
 */

//Gain(x, a) entropyRoot - sum( casesY/casesX entropyNode)
public class AttributeMeasures {
    private static double log2(double x){
        if (x != 0){
            return Math.log(x) / Math.log(2);
        }else{
            return 0;
        }
    }

    public static double measureInformationGain(int[][] contingencyTable) {
        int CLASS_COUNT = contingencyTable[0].length;
        int ATTRIBUTE_COUNT = contingencyTable.length;

        double[] entropyArray = new double[ATTRIBUTE_COUNT];
        double[] proportionArray = new double[ATTRIBUTE_COUNT];
        int rootNodeClassTotal = 0;

        //Sum of root node
        int[] rootClassTotals = new int[CLASS_COUNT];
        for (int[] attr : contingencyTable) {
            for (int i = 0; i < CLASS_COUNT; i++) {
                rootClassTotals[i] += attr[i];
                rootNodeClassTotal += attr[i];
            }
        }

        //Runs through all classes and attributes within
        for(int node = 0; node < ATTRIBUTE_COUNT; node++) {
            int classTotal = 0;
            for (int classAmount : contingencyTable[node]) {
                classTotal += classAmount;
            }

            // Finds ratio for each class
            double[] ratioArray = new double[CLASS_COUNT];
            for(int i = 0; i < CLASS_COUNT; i++){
                ratioArray[i] = (double) contingencyTable[node][i] / (double) classTotal;
            }

            // finds class entropy
            double entropy = 0;
            for (double prob : ratioArray) {
                entropy += log2(prob) * prob;
            }
            entropyArray[node] = -1 * entropy;

            // finds node proportion
            proportionArray[node] = (double) classTotal / (double) rootNodeClassTotal;
        }


        // finds ratios for root node
        double[] probList = new double[CLASS_COUNT];
        for(int i = 0; i < CLASS_COUNT; i++){
            probList[i] = (double) rootClassTotals[i] / (double) rootNodeClassTotal;
        }
        // Calculate Entropy of root node
        double rootNodeEntropy = 0;
        for (double prob : probList) {
            rootNodeEntropy += log2(prob) * prob;
        }
        rootNodeEntropy *= -1;


        // finds info gain
        double informationGain = rootNodeEntropy;
        for(int node = 0; node < contingencyTable.length; node++) {
            informationGain -= proportionArray[node] * entropyArray[node];
        }
        return informationGain;
    }


    public static double measureInformationGainRatio(int[][] table){
        double informationGain = measureInformationGain(table);

        final int CLASS_COUNT = table[0].length; //How many outcomes i.e Islay Speyside
        final int ATTRIBUTE_COUNT = table.length; //Number of options i.e yes or no

        double[] proportionArray = new double[ATTRIBUTE_COUNT];
        int totalElementsInRootNode = 0;

        //Find root node total.
        for(int[] attribute : table){
            for(int classIndex = 0; classIndex < CLASS_COUNT; classIndex++){
                totalElementsInRootNode += attribute[classIndex];
            }
        }

        //Find the class weights.
        for(int attrib = 0; attrib < ATTRIBUTE_COUNT; attrib++ ){
            //Total elements in node.
            int totalElementsInNode = 0;
            for(int classValue: table[attrib]) {
                totalElementsInNode += classValue;
            }
            proportionArray[attrib] = (double) totalElementsInNode / (double) totalElementsInRootNode;
        }

        //Find split
        double split = 0;
        for (double proportion: proportionArray) {
            split += proportion * log2(proportion);
        }
        split *= -1;

        double ratio = informationGain / split;
        return ratio;
    }

    public static double measureGini(int[][] contingencyTable) {
        int CLASS_COUNT = contingencyTable[0].length;
        int ATTRIBUTE_COUNT = contingencyTable.length;

        double impurityOfRoot = 0.0d;
        int totalElementsInRootNode = 0;
        double[] impurityArray = new double[ATTRIBUTE_COUNT];


        // Finds total for root node
        int[] rootClassTotals = new int[CLASS_COUNT];
        for (int[] i : contingencyTable) {
            for (int outcome = 0; outcome < CLASS_COUNT; outcome++) {
                rootClassTotals[outcome] += i[outcome];
                totalElementsInRootNode += i[outcome];
            }
        }

        // finds the root nodes imp
        for(int i = 0; i < CLASS_COUNT; i++){
            double fraction = (double) rootClassTotals[i] / (double) totalElementsInRootNode;
            impurityOfRoot += Math.pow(fraction, 2);
        }
        impurityOfRoot = 1 - impurityOfRoot;


        // Gets the gini for each class
        for(int node = 0; node < ATTRIBUTE_COUNT; node++) {
            int nodeTotle = 0;
            for (int classAmount : contingencyTable[node]) {
                nodeTotle += classAmount;
            }

            // Calculate  impurity
            double imp = 0.0d;
            for(int i = 0; i < CLASS_COUNT; i++){
                double fraction = (double) contingencyTable[node][i] / (double) nodeTotle;
                imp += Math.pow(fraction, 2);
            }
            imp = 1 - imp;
            impurityArray[node] = imp;
        }

        double gini = impurityOfRoot;
        for(int i = 0; i < ATTRIBUTE_COUNT; i++) {
            int totalInNode = 0;
            for (int classAmount : contingencyTable[i]) {
                totalInNode += classAmount;
            }

            gini -= ((double) totalInNode / (double) totalElementsInRootNode) * impurityArray[i];
        }

        return gini;
    }

    public static double measureChiSquared(int[][] table){
        final int CLASS_COUNT = table[0].length; //How many outcomes i.e Islay Speyside
        final int ATTRIBUTE_COUNT = table.length; //Number of options i.e yes or no

        double chi = 0.0d;
        int totalElementsInRootNode = 0;

        //Find root node total.
        int[] classTotalsRoot = new int[CLASS_COUNT];
        for(int[] attribute : table){
            for(int classIndex = 0; classIndex < CLASS_COUNT; classIndex++){
                classTotalsRoot[classIndex] += attribute[classIndex];
                totalElementsInRootNode += attribute[classIndex];
            }
        }

        //Find root prob for classes
        double[] classProb = new double[CLASS_COUNT];
        for(int i = 0; i < CLASS_COUNT; i++){
            classProb[i] = (double) classTotalsRoot[i]/ (double) totalElementsInRootNode;
        }

        for(int i = 0; i < ATTRIBUTE_COUNT; i++){

            //find total
            int nodeTotal = 0;
            for (int classValue: table[i]){
                nodeTotal += classValue;
            }

            //find chi
            for(int j = 0; j < CLASS_COUNT; j++){
                double actual = table[i][j];
                double expected = classProb[j] * nodeTotal;
                chi += Math.pow(actual - expected, 2)/expected;
            }
        }



        return chi;
    }

    /**
     * Main method.
     *
     * @param args the options for the attribute measure main
     */
    public static void main(String[] args) {
        int[][] peatyContingencyTable = {
                {4, 0},
                {1, 5}
        };

        double infoGain = measureInformationGain(peatyContingencyTable);
        System.out.println("Info gain: "+infoGain);
        double ratio = measureInformationGainRatio(peatyContingencyTable);
        System.out.println("Info gain ratio: "+ratio);
        double gini = measureGini(peatyContingencyTable);
        System.out.println("Gini: "+gini);
        double chi = measureChiSquared(peatyContingencyTable);
        System.out.println("chi: "+chi);
    }

}
