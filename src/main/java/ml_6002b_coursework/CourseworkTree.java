package ml_6002b_coursework;

import weka.classifiers.AbstractClassifier;
import weka.core.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * A basic decision tree classifier for use in machine learning coursework (6002B).
 */
public class CourseworkTree extends AbstractClassifier {

    /** Measure to use when selecting an attribute to split the data with. */
    private AttributeSplitMeasure attSplitMeasure = new IGAttributeSplitMeasure();

    /** Maxiumum depth for the tree. */
    private int maxDepth = Integer.MAX_VALUE;

    /** The root node of the tree. */
    private TreeNode root;

    /**
     * Sets the attribute split measure for the classifier.
     *
     * @param attSplitMeasure the split measure
     */
    public void setAttSplitMeasure(AttributeSplitMeasure attSplitMeasure) {
        this.attSplitMeasure = attSplitMeasure;
    }

    public void setOptions(String options){
        if(options.contains("informationGain")){
            setAttSplitMeasure(new IGAttributeSplitMeasure());
        }

        if(options.contains("informationGainRatio")){
            IGAttributeSplitMeasure measure = new IGAttributeSplitMeasure();
            measure.setUseGain(true);
            setAttSplitMeasure(measure);
        }

        if(options.contains("chiSquared")){
            setAttSplitMeasure(new ChiSquaredAttributeSplitMeasure());
        }

        if(options.contains("gini")){
            setAttSplitMeasure(new GiniAttributeSplitMeasure());
        }

    }

    /**
     * Sets the max depth for the classifier.
     *
     * @param maxDepth the max depth
     */
    public void setMaxDepth(int maxDepth){
        this.maxDepth = maxDepth;
    }

    /**
     * Returns default capabilities of the classifier.
     *
     * @return the capabilities of this classifier
     */
    @Override
    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.disableAll();

        // attributes
        result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);

        // class
        result.enable(Capabilities.Capability.NOMINAL_CLASS);

        //instances
        result.setMinimumNumberInstances(2);

        return result;
    }

    /**
     * Builds a decision tree classifier.
     *
     * @param data the training data
     */
    @Override
    public void buildClassifier(Instances data) throws Exception {
        if (data.classIndex() != data.numAttributes() - 1) {
            throw new Exception("Class attribute must be the last index.");
        }

        root = new TreeNode();
        root.buildTree(data, 0);
    }

    /**
     * Classifies a given test instance using the decision tree.
     *
     * @param instance the instance to be classified
     * @return the classification
     */
    @Override
    public double classifyInstance(Instance instance) {
        double[] probs = distributionForInstance(instance);

        int maxClass = 0;
        for (int n = 1; n < probs.length; n++) {
            if (probs[n] > probs[maxClass]) {
                maxClass = n;
            }
        }

        return maxClass;
    }

    /**
     * Computes class distribution for instance using the decision tree.
     *
     * @param instance the instance for which distribution is to be computed
     * @return the class distribution for the given instance
     */
    @Override
    public double[] distributionForInstance(Instance instance) {
        return root.distributionForInstance(instance);
    }

    /**
     * Class representing a single node in the tree.
     */
    private class TreeNode {

        /** Attribute used for splitting, if null the node is a leaf. */
        Attribute bestSplit = null;

        /** Best gain from the splitting measure if the node is not a leaf. */
        double bestGain = 0;

        /** Depth of the node in the tree. */
        int depth;

        /** The node's children if it is not a leaf. */
        TreeNode[] children;

        /** The class distribution if the node is a leaf. */
        double[] leafDistribution;

        /**
         * Recursive function for building the tree.
         * Builds a single tree node, finding the best attribute to split on using a splitting measure.
         * Splits the best attribute into multiple child tree node's if they can be made, else creates a leaf node.
         *
         * @param data Instances to build the tree node with
         * @param depth the depth of the node in the tree
         */

        private double mean = 0;

        void buildTree(Instances data, int depth) throws Exception {
            this.depth = depth;

            // Loop through each attribute, finding the best one.
            for (int i = 0; i < data.numAttributes() - 1; i++) {
                double gain = attSplitMeasure.computeAttributeQuality(data, data.attribute(i));

                if (gain > bestGain) {
                    bestSplit = data.attribute(i);
                    bestGain = gain;
                }
            }

            // If we found an attribute to split on, create child nodes.
            if (bestSplit != null) {
                if(bestSplit.isNumeric()){
                    mean = data.attributeStats(bestSplit.index()).numericStats.mean;
                }
                Instances[] split;
                if(bestSplit.isNumeric()) split = attSplitMeasure.splitDataOnNumeric(data, bestSplit);
                else split = attSplitMeasure.splitData(data, bestSplit);
                children = new TreeNode[split.length];

                // Create a child for each value in the selected attribute, and determine whether it is a leaf or not.
                for (int i = 0; i < children.length; i++){
                    children[i] = new TreeNode();

                    boolean leaf = split[i].numDistinctValues(data.classIndex()) == 1 || depth + 1 == maxDepth;

                    if (split[i].isEmpty()) {
                        children[i].buildLeaf(data, depth + 1);
                    } else if (leaf) {
                        children[i].buildLeaf(split[i], depth + 1);
                    } else {
                        children[i].buildTree(split[i], depth + 1);
                    }
                }
            // Else turn this node into a leaf node.
            } else {
                leafDistribution = classDistribution(data);
            }
        }

        /**
         * Builds a leaf node for the tree, setting the depth and recording the class distribution of the remaining
         * instances.
         *
         * @param data remaining Instances to build the leafs class distribution
         * @param depth the depth of the node in the tree
         */
        void buildLeaf(Instances data, int depth) {
            this.depth = depth;
            leafDistribution = classDistribution(data);
        }

        /**
         * Recursive function traversing node's of the tree until a leaf is found. Returns the leafs class distribution.
         *
         * @return the class distribution of the first leaf node
         */
        double[] distributionForInstance(Instance inst) {
            // If the node is a leaf return the distribution, else select the next node based on the best attributes
            // value.
            if (bestSplit == null) {
                return leafDistribution;
            } else {
                if(bestSplit.isNumeric()){
                    if(inst.value(bestSplit) > mean) return children[0].distributionForInstance(inst);
                    else return children[1].distributionForInstance(inst);
                }
                return children[(int) inst.value(bestSplit)].distributionForInstance(inst);
            }
        }

        /**
         * Returns the normalised version of the input array with values summing to 1.
         *
         * @return the class distribution as an array
         */
        double[] classDistribution(Instances data) {
            double[] distribution = new double[data.numClasses()];
            for (Instance inst : data) {
                distribution[(int) inst.classValue()]++;
            }

            double sum = 0;
            for (double d : distribution){
                sum += d;
            }

            if (sum != 0){
                for (int i = 0; i < distribution.length; i++) {
                    distribution[i] = distribution[i] / sum;
                }
            }

            return distribution;
        }

        /**
         * Summarises the tree node into a String.
         *
         * @return the summarised node as a String
         */
        @Override
        public String toString() {
            String str;
            if (bestSplit == null){
                str = "Leaf," + Arrays.toString(leafDistribution) + "," + depth;
            } else {
                str = bestSplit.name() + "," + bestGain + "," + depth;
            }
            return str;
        }
    }

    /**
     * Main method.
     *
     * @param args the options for the classifier main
     */
    public static void main(String[] args) throws Exception {
        runData("./src/main/java/ml_6002b_coursework/test_data/optdigits.arff");
        runData("./src/main/java/ml_6002b_coursework/test_data/Chinatown.arff");
    }

    public static void runData(String file) throws Exception {
        FileReader reader = new FileReader(file);
        Instances data = new Instances(reader);
        data.setClassIndex(data.numAttributes() -1);

        //Split the data
        data.randomize(new Random());
        int trainingDataLength = (int) (0.6d * data.numInstances());
        int testingDataLength = data.numInstances() - trainingDataLength;
        Instances trainingData = new Instances(data, 0, trainingDataLength);
        Instances testingData = new Instances(data, trainingDataLength, testingDataLength);

        //IG
        CourseworkTree igTree = new CourseworkTree();
        igTree.setOptions("informationGain");
        igTree.buildClassifier(trainingData);
        //IG ratio
        CourseworkTree igRatioTree = new CourseworkTree();
        igRatioTree.setOptions("informationGainRatio");
        igRatioTree.buildClassifier(trainingData);
        //Chi
        CourseworkTree chiTree = new CourseworkTree();
        chiTree.setOptions("chiSquared");
        chiTree.buildClassifier(trainingData);
        //Gini
        CourseworkTree giniTree = new CourseworkTree();
        giniTree.setOptions("gini");
        giniTree.buildClassifier(trainingData);

        int igCount = 0, igRatioCount = 0, chiCount = 0, giniCount = 0;

        for(Instance i: testingData){
            if(igTree.classifyInstance(i) == i.classValue()) igCount++;
            if(igRatioTree.classifyInstance(i) == i.classValue()) igRatioCount++;
            if(chiTree.classifyInstance(i) == i.classValue()) chiCount++;
            if(giniTree.classifyInstance(i) == i.classValue()) giniCount++;
        }

        double igAccuracy = (double) igCount/(double) testingData.numInstances(),
                igRatioAccuracy = (double) igRatioCount/(double) testingData.numInstances(),
                chiAccuracy = (double) chiCount/(double) testingData.numInstances(),
                giniAccuracy = (double) giniCount/(double) testingData.numInstances();

        System.out.println(
                "DT using measure Info Gain on "+data.relationName()+" problem has test accuracy = "+igAccuracy*100+"%\n" +
                "DT using measure Info Gain Ratio on "+data.relationName()+" problem has test accuracy = "+igRatioAccuracy*100+"%\n" +
                "DT using measure Chi on "+data.relationName()+" problem has test accuracy = "+chiAccuracy*100+"%\n" +
                "DT using measure Gini Gain on "+data.relationName()+" problem has test accuracy = "+giniAccuracy*100+"%\n"
        );
    }
}