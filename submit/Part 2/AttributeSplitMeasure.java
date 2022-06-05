package ml_6002b_coursework;

import weka.core.Attribute;
import weka.core.AttributeStats;
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
        //System.out.println(att.numValues() +" "+ att.index() );
        for (Instance inst: data) {
            //System.out.println(splitData.length + "val: "+inst.value(att));
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
        double mean = data.attributeStats(att.index()).numericStats.mean;
        Instances instances1 = new Instances(data, 0);
        Instances instances2 = new Instances(data, 0);
        for(Instance i: data){
            if(i.value(att) > mean){
                instances1.add(i);
            }else {
                instances2.add(i);
            }
        }
        return new Instances[] {instances1, instances2};
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
