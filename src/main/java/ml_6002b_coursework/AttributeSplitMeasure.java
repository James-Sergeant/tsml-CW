package ml_6002b_coursework;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

import java.io.FileReader;
import java.util.Enumeration;

/**
 * Interface for alternative attribute split measures for Part 2.2 of the coursework
 */
public abstract class AttributeSplitMeasure {

    public abstract double computeAttributeQuality(Instances data, Attribute att) throws Exception;

    /**
     * Splits a dataset according to the values of a nominal attribute.
     *
     * @param data the data which is to be split
     * @param att the attribute to be used for splitting
     * @return the sets of instances produced by the split
     */
    public Instances[] splitData(Instances data, Attribute att) {
        Instances[] splitData = new Instances[att.numValues()];
        for (int i = 0; i < att.numValues(); i++) {
            splitData[i] = new Instances(data, data.numInstances());
        }

        for (Instance inst: data) {
            splitData[(int) inst.value(att)].add(inst);
        }

        for (Instances split : splitData) {
            split.compactify();
        }

        return splitData;
    }

    //How to split:
        //Sort Data
        //Make data nominal
        // Split and find sum of squares error: https://stats.stackexchange.com/questions/220350/regression-trees-how-are-splits-decided
        // Lowest SSE is picked.

    //https://stats.stackexchange.com/questions/220350/regression-trees-how-are-splits-decided
    //https://medium.com/analytics-vidhya/regression-trees-decision-tree-for-regression-machine-learning-e4d7525d8047
    public Instances[] splitDataOnNumeric(Instances data, Attribute att) throws Exception {
        GiniAttributeSplitMeasure giniSplitMeasure = new GiniAttributeSplitMeasure(); //Ues to check split

        //Data sorted by attribute
        data.sort(att);

        //Make data nominal
        NumericToNominal nominal = new NumericToNominal();
        nominal.setInputFormat(data);
        int[] indexArray = {att.index()};
        nominal.setAttributeIndicesArray(indexArray);

        double currentBestGini = Double.MAX_VALUE; //Big value as first is needs to be best.
        double currentBestValue = 0.0d;

        for(Instance i: data){
            double split = i.value(att);
            Instances nomData = new Instances(data);

            //Change the values
            for(Instance j: nomData){
                if(j.value(att) <= split){ //If less than split value = 0;
                    j.setValue(att, 0);
                }else{
                    j.setValue(att, 1); //Else set to 1
                }
            }

            //Filter the new nom data.
            nominal.setInputFormat(nomData);
            nomData = Filter.useFilter(nomData, nominal);

            //Check to see if split is better
            double gini = giniSplitMeasure.computeAttributeQuality(nomData, nomData.attribute(att.index()));

            if(gini < currentBestGini){
                currentBestValue = i.value(att);
                currentBestGini = gini;
            }
        }

        Instances[] split = new Instances[2];
        split[0] = new Instances(data, data.numInstances());
        split[1] = new Instances(data, data.numInstances());


       //pick the best split.
        for (Instance i: data){
            if(i.value(att) <= currentBestValue){
                split[0].add(i);
            }else{
                split[1].add(i);
            }
        }

        //Compact data as has changed so size is equal to num instances.
        split[0].compactify();
        split[1].compactify();
        return split;
    }
    public static void main(String[] args) throws Exception {
        // Load chinaTown data from file
        FileReader reader = new FileReader("./src/main/java/ml_6002b_coursework/test_data/Chinatown.arff");
        Instances chinaTownData = new Instances(reader);
        chinaTownData.setClassIndex(chinaTownData.numAttributes()-1);

        IGAttributeSplitMeasure splitMeasure = new IGAttributeSplitMeasure();
        System.out.println(splitMeasure.computeAttributeQuality(chinaTownData, chinaTownData.attribute(0)));

    }
}
