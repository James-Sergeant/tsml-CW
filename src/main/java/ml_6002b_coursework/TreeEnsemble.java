package ml_6002b_coursework;

import utilities.ClassifierTools;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Debug;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.RandomSubset;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

public class TreeEnsemble extends AbstractClassifier {

    private int numTrees = 50;
    private CourseworkTree[] randomTrees = new CourseworkTree[numTrees];
    private double attributeProportion = 0.5d;
    private int seeds[] = new int[numTrees];
    private boolean averageDistributions = false;

    public TreeEnsemble(){};

    public TreeEnsemble(int numTrees, double attributeProportion, boolean averageDistributions) {
        this.numTrees = numTrees;
        this.attributeProportion = attributeProportion;
        this.averageDistributions = averageDistributions;
    }

    @Override
    public void buildClassifier(Instances data) throws Exception {
        //Random for picking the spltting mesure
        Random random = new Random(1);

        //Build x number of random trees:
        for(int i = 0; i < numTrees; i++){

            //Init tree and build classifier.
            randomTrees[i] = new CourseworkTree();

            //get a random splitting value:
            String[] splitters = {"informationGain", "informationGainRatio", "chiSquared","gini"};
            String splitter = splitters[random.nextInt(4)];
            randomTrees[i].setOptions(splitter);

            //Selected random attributes if setting is set.
            RandomSubset randomSubset = new RandomSubset();
            int seed = random.nextInt();
            seeds[i] = seed;
            randomSubset.setSeed(seed);
            //Select random attributes:
            int num = (int) (attributeProportion * data.numAttributes());
            randomSubset.setNumAttributes(num);
            randomSubset.setInputFormat(data);
            Instances randomData = randomSubset.process(data);

            //Build classifier
            randomTrees[i].buildClassifier(randomData);

        }
    }

    @Override
    public double classifyInstance(Instance instance) throws Exception {
        int[] totalVotes = new int[instance.numClasses()];
        int topPredictedClass = 0;

        if(!averageDistributions){
            int i =0;
            for(CourseworkTree tree: randomTrees){

                //Change to random subset
                RandomSubset randomSubset = new RandomSubset();
                randomSubset.setSeed(seeds[i]);
                //Select random attributes:
                int num = (int) (attributeProportion * instance.numAttributes());
                randomSubset.setNumAttributes(num);
                randomSubset.setInputFormat(instance.dataset());
                randomSubset.input(instance);
                Instance randomInstance = randomSubset.output();

                int predictedClass = (int) tree.classifyInstance(randomInstance);
                totalVotes[predictedClass]++;
                topPredictedClass = totalVotes[predictedClass] > totalVotes[topPredictedClass] ? predictedClass:topPredictedClass;
                i++;
            }
        }else{
            int i = 0;
            for(CourseworkTree tree: randomTrees){
                //Change to random subset
                RandomSubset randomSubset = new RandomSubset();
                randomSubset.setSeed(seeds[i]);
                //Select random attributes:
                int num = (int) (attributeProportion * instance.numAttributes());
                randomSubset.setNumAttributes(num);
                randomSubset.setInputFormat(instance.dataset());
                randomSubset.input(instance);
                Instance randomInstance = randomSubset.output();

                double[] dis = tree.distributionForInstance(randomInstance);
                for(int j = 0; j < instance.numClasses(); j++){
                    totalVotes[j] += dis[j];
                    topPredictedClass = totalVotes[j] > totalVotes[topPredictedClass] ? j:topPredictedClass;
                }
                i++;
            }
        }
        return topPredictedClass;
    }

    @Override
    public double[] distributionForInstance(Instance instance) throws Exception {
        int votesForClass[] = new int[instance.numClasses()];

        int i = 0;
        for(CourseworkTree tree : randomTrees){

            //Change to random subset
            RandomSubset randomSubset = new RandomSubset();
            randomSubset.setSeed(seeds[i]);
            //Select random attributes:
            int num = (int) (attributeProportion * instance.numAttributes());
            randomSubset.setNumAttributes(num);
            randomSubset.setInputFormat(instance.dataset());
            randomSubset.input(instance);
            Instance randomInstance = randomSubset.output();

            int prediction = (int) tree.classifyInstance(randomInstance);
            votesForClass[prediction]++;
            i++;
        }

        double[] r = new double[instance.numClasses()];
        int count = 0;
        for(int val: votesForClass) {
            r[count] = (double) val/(double)numTrees;
            count++;
        }
        return r;
    }

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

        TreeEnsemble treeEnsemble = new TreeEnsemble();
        treeEnsemble.buildClassifier(trainingData);

        double a = ClassifierTools.accuracy(testingData, treeEnsemble);
        System.out.println("Problem: "+testingData.relationName());
        System.out.println("test accuracy: "+ a);
        for(int i = 0; i < 5; i++){
            double[] prob = treeEnsemble.distributionForInstance(testingData.get(i));
            System.out.println("Instance "+i);
            for(int j = 0; j < prob.length; j++){
                System.out.print("\tProbability for class "+j+" "+prob[j] * 100+"% \n");
            }
        }
    }
}
