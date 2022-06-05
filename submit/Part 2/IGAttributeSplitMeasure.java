package ml_6002b_coursework;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileReader;


public class IGAttributeSplitMeasure extends AttributeSplitMeasure {

    private boolean useGain = false;

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

        if(useGain){
            return AttributeMeasures.measureInformationGainRatio(table);
        }else{
            return AttributeMeasures.measureInformationGain(table);
        }
    }

    public void setUseGain(boolean useGain) {
        this.useGain = useGain;
    }

    /**
     * Main method.
     *
     * @param args the options for the split measure main
     */
    public static void main(String[] args) throws Exception {

        //Load in data
        Instances whiskeyData = new Instances(new FileReader("./src/main/java/ml_6002b_coursework/test_data/WhiskyRegion.arff"));
        whiskeyData.setClassIndex(whiskeyData.numAttributes()-1);

        // Test IGAttributeSplitMeasure
        IGAttributeSplitMeasure measure = new IGAttributeSplitMeasure();

        // Measure Info gain
        for(int attr = 0; attr < whiskeyData.numAttributes() - 1; attr++){
            String attribName = whiskeyData.attribute(attr).name();
            double infoGain = measure.computeAttributeQuality(whiskeyData, whiskeyData.attribute(attr));
            System.out.println("measure 'IG' for attribute" +attribName+ "splitting diagnosis ="+infoGain);
        }

        // Measure Info gain ratio
        measure.useGain = true;
        for(int attr = 0; attr < whiskeyData.numAttributes() - 1; attr++){
            String attribName = whiskeyData.attribute(attr).name();
            double infoGain = measure.computeAttributeQuality(whiskeyData, whiskeyData.attribute(attr));
            System.out.println("measure 'IG ratio' for attribute" +attribName+ "splitting diagnosis ="+infoGain);
        }
    }

}
