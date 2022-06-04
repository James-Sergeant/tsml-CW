package ml_6002b_coursework;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileReader;

public class GiniAttributeSplitMeasure extends AttributeSplitMeasure{
    @Override
    public double computeAttributeQuality(Instances data, Attribute att) throws Exception {
        int[][] table = new int[att.numValues()][data.numClasses()];

        Instances[] split;
        if(att.isNumeric()) {
            split = splitDataOnNumeric(data, att); //Uses my method to make the data nom
            table = new int[2][data.numClasses()]; //Create new table
            for(int i =0; i < 2; i++){
                for(Instance j: split[i]){
                    table[i][(int)j.classValue()]++;
                }
            }
        }else{
            for(Instance instance : data){
                table[(int) instance.value(att)][(int) instance.classValue()]++;
            }
        }
        return AttributeMeasures.measureGini(table);
    }

    public static void main(String[] args) throws Exception {

        //Load in data
        Instances whiskeyData = new Instances(new FileReader("./src/main/java/ml_6002b_coursework/test_data/WhiskyRegion.arff"));
        whiskeyData.setClassIndex(whiskeyData.numAttributes()-1);

        // Test ChiSquaredAttributeSplitMeasure
        GiniAttributeSplitMeasure measure = new GiniAttributeSplitMeasure();

        // Measure Chi Squared
        for(int attr = 0; attr < whiskeyData.numAttributes() - 1; attr++){
            String attribName = whiskeyData.attribute(attr).name();
            double infoGain = measure.computeAttributeQuality(whiskeyData, whiskeyData.attribute(attr));
            System.out.println("measure 'Gini' for attribute" +attribName+ "splitting diagnosis ="+infoGain);
        }
    }
}
