package neuralNet.staticTopologyNet;

import exceptions.LengthMismatchException;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import managers.Log;
import neuralNet.Net;
import neuralNet.Stringable;

import java.util.Observer;

public class StaticNet extends java.util.Observable implements Stringable, Net {

    private static final float LEARN_RATE = 0.001f;
    private static final float MIN_LEARN_RATE = 0.1f*LEARN_RATE;
    private static final float MUT_RATE = 0.1f;

    private float getCost;
    private int id;

    private Layer[] Layers;

    private Observer observer;

    public StaticNet(int id, int[] neuronCounts) {
        log(" building net with "+neuronCounts.length+" Layers");

        buildEmptyNet(id, neuronCounts);

        // so that each neuron in input layer has non-empty weights
        float[][] inputBiasWeights = new float[neuronCounts[0]][1];
        for (int i=0; i<neuronCounts[0]; i++) {
            inputBiasWeights[i][0] = 1f;
        }
        Layers[0].shiftWeights(inputBiasWeights, false);
        //Log(" built");
    }

    public StaticNet(String saveData){
        String[] lines = saveData.split("\n");
        int[] idLoc = getStartEnd(lines[0], "net: ", ",");
        int[] layersLoc = getStartEnd(lines[0], "Layers: ");

        try{
            //make net from save
            int id = Integer.parseInt(lines[0].substring(idLoc[0], idLoc[1]));

            int layerCount = Integer.parseInt(lines[0].substring(layersLoc[0], layersLoc[1]));
            int[] neuronCounts = new int[layerCount];

            for(int i=1; i<lines.length; i++){
                if(lines[i].contains("layer")){
                    int[] layCountLoc = getStartEnd(lines[i], "Neurons: ");
                    neuronCounts[i] = Integer.parseInt(lines[0].substring(layCountLoc[0], layCountLoc[1]));
                }
            }

            buildEmptyNet(id, neuronCounts);
        }catch (Exception e) {
            e.printStackTrace();
        }

        try{
            //set weights from save
            int layerIndex = 0, neuronIndex = 0;
            for(int i=1; i<lines.length; i++){
                //if line contains weight data
                if(lines[i].contains("[")){
                    float[] weights = getWeightsFromString(lines[i]);
                    if(Layers[layerIndex].Neurons.length == neuronIndex){
                        neuronIndex = 0;
                        layerIndex++;
                    }
                    Layers[layerIndex].Neurons[neuronIndex].setWeights(weights, false);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void buildEmptyNet(int id, int[] neuronCounts){
        this.id = id;

        Layers = new Layer[neuronCounts.length];
        int prevLayerSize = 0;
        for(int layerIndex = 0; layerIndex < neuronCounts.length; layerIndex++) {
            log(" creating layer "+(layerIndex+1)+" of "+neuronCounts.length);
            Layers[layerIndex] = new Layer(layerIndex, neuronCounts[layerIndex], prevLayerSize);
            prevLayerSize = Layers[layerIndex].Neurons.length;
        }
    }

    @Override
    public void init() {
        //input layer should stay at 1 for their weights
        for(int layerIndex = 1; layerIndex< Layers.length; layerIndex++) {
            Layers[layerIndex].randomize();
        }

        addObserver(Log.getInstance());
    }

    @Override
    public void run(float[] rawIn, boolean log) throws LengthMismatchException {
        if(rawIn.length != Layers[0].Neurons.length) {
            log("*** input size mismatch: given " + rawIn.length + ", requires " + Layers[0].Neurons.length);
            throw new LengthMismatchException();
        }

        if(log){
            setChanged();
            StringBuilder in = new StringBuilder();
            for(float f : rawIn){
                in.append(f).append(", ");
            }
            int last = in.lastIndexOf(",");
            in.delete(last, last+2);
            notifyObservers("running with inputs: "+in);
        }

        //sets input
        for(int i=0; i<rawIn.length; i++) {
            Neuron Neuron = Layers[0].Neurons[i];
            Neuron.reset();
            Neuron.fire(0, rawIn[i]);
        }
        Layers[0].update();

        boolean first = true;
        Layer prevLayer = null;
        for(Layer Layer : Layers){

            if(first) {
                first = false;
                prevLayer = Layer;
                continue;
            }

            for (int targetIndex = 0; targetIndex< Layer.Neurons.length; targetIndex++) {
                Neuron Neuron = Layer.Neurons[targetIndex];
                Neuron.reset();
                //first weight is bias and should be fired with strength 1
                Neuron.fire(0, 1);
                for (int callerIndex = 1; callerIndex<= prevLayer.Neurons.length; callerIndex++) {
                    Neuron.fire(callerIndex, prevLayer.Neurons[callerIndex-1].getActivation());
                }
            }
            Layer.update();
            prevLayer = Layer;
        }
    }

    @Override
    public float getCost() {
        return getCost;
    }

    public void setWeights(StaticNet srcStaticNet) {
        for(int layerIndex = 0; layerIndex< Layers.length; layerIndex++) {
            Layers[layerIndex].setWeights(srcStaticNet.Layers[layerIndex]);
        }
    }

    public void shiftWeights(float[][][] dWeights, boolean shouldLog) {
        if(shouldLog) {
            log(" shifting weights");
        }

        for(int layerIndex = 0; layerIndex< Layers.length; layerIndex++) {
            //usually layer 0 from bkp not changing input layer's weights (cuz they do nothing)
            if(dWeights[layerIndex]==null) {
                continue;
            }
            Layers[layerIndex].shiftWeights(dWeights[layerIndex], shouldLog);
        }
    }

    public float[][][] getDesiredChanges(float[] desiredOut) {
        if(desiredOut.length != Layers[Layers.length-1].Neurons.length){
            log("back propagation needs "+ Layers[Layers.length-1].Neurons.length
                    +" answers, given "+desiredOut.length);
            return null;
        }

        float[][][] changes = new float[Layers.length][][];
        float[] dErrPerRaw_fwd = new float[desiredOut.length];
        float[] dErrPerRaw;
        boolean isOutLayer = true;
        for(int layerIndex = Layers.length-1; layerIndex>0; layerIndex--) {
            Layer currLayer = Layers[layerIndex];
            Layer bckLayer = Layers[layerIndex-1];

            changes[layerIndex] = new float[currLayer.Neurons.length][];
            dErrPerRaw = new float[currLayer.Neurons.length];
            if(isOutLayer) {
                for(int i=0; i<desiredOut.length; i++) {
                    Neuron out = currLayer.Neurons[i];
                    dErrPerRaw_fwd[i] = out.getActivation() - desiredOut[i];
                }
            }

            //get desired weight and bckLayer activation changes for each neuron
            for(int neuronIndex = 0; neuronIndex< currLayer.Neurons.length; neuronIndex++) {
                Neuron Neuron = currLayer.Neurons[neuronIndex];
                changes[layerIndex][neuronIndex] = new float[Neuron.getWeights().length];

                float[][] extractableInfo;
                if (isOutLayer) {
                    //see method definition
                    extractableInfo = getExtractableInfo(bckLayer, dErrPerRaw_fwd[neuronIndex]);
                }else{
                    extractableInfo = getExtractableInfo(bckLayer,
                            getdErrPerRaw(Neuron, dErrPerRaw_fwd, Layers[layerIndex+1]) );
                }

                //store weight changes and Neuron's dErr / dRaw
                changes[layerIndex][neuronIndex] = extractableInfo[0];
                dErrPerRaw[neuronIndex] = extractableInfo[1][0];
            }
            isOutLayer = false;

            dErrPerRaw_fwd = new float[currLayer.Neurons.length];
            System.arraycopy(dErrPerRaw, 0, dErrPerRaw_fwd, 0, dErrPerRaw.length);
        }

        return changes;
    }

    // dErrPerRaw_fwd: dErr/dRaw for each neuron of forward 1 layer
    // layerWeights_fwd: weights for forward 1 layer in [neuron][weight]
    private float getdErrPerRaw(Neuron target, float[] dErrPerRaw_fwd, Layer layerFwd) {
        float dErrPerAct = 0;
        for(int i = 0; i< layerFwd.Neurons.length; i++) {
            dErrPerAct += dErrPerRaw_fwd[i] * layerFwd.Neurons[i].getWeights()[target.getId()+1];
        }
        return dErrPerAct * target.getdActPerRaw();
    }

    // gets dError / dWeight for each of target's weights and also returns its dErr / dRaw
    private float[][] getExtractableInfo( Layer bckLayer, float dErrPerRaw) {
        float[] dErrPerWeight = new float[bckLayer.Neurons.length+1];

        // bias
        dErrPerWeight[0] = -dErrPerRaw * LEARN_RATE;
//        if(Math.abs(dErrPerWeight[0]) < MIN_LEARN_RATE) {
//            dErrPerWeight[0] *= MIN_LEARN_RATE / Math.abs(dErrPerWeight[0]);
//        }

        // i is weight index
        Neuron bckNeuron;
        for(int i = 0; i< bckLayer.Neurons.length; i++) {
            bckNeuron = bckLayer.Neurons[i];

            //chain rule for dCost / dWeight
            dErrPerWeight[i+1] = LEARN_RATE * -dErrPerRaw * bckNeuron.getActivation();

//            if(Math.abs(dErrPerWeight[i+1]) < MIN_LEARN_RATE) {
//                dErrPerWeight[i+1] *= MIN_LEARN_RATE / Math.abs(dErrPerWeight[i+1]);
//            }
        }

        return new float[][]{ dErrPerWeight, new float[]{dErrPerRaw} };
    }

    private void mutate(float range){
        //input layer should stay at 1 for their weights
        for(int layerIndex = 1; layerIndex< Layers.length; layerIndex++) {
            Layers[layerIndex].mutate(range);
        }
    }

    @Override
    public void mutate() {
        mutate(MUT_RATE);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setCost(float cost) {
        this.getCost = cost;
    }

    @Override
    public float[] getOutput(){
        Layer outputLayer = Layers[Layers.length-1];
        float[] output = new float[outputLayer.Neurons.length];
        for(int i=0; i<output.length; i++) {
            output[i] = outputLayer.Neurons[i].getActivation();
        }
        return output;
    }

    @Override
    public String toString(){
        StringBuilder myString = new StringBuilder();
        myString.append("net: ").append(id).append(", Layers: ").append(Layers.length);
        for(Layer Layer : Layers) {
            myString.append(Layer.toString());
        }
        myString.append("\nend net: ").append(id);
        return myString.toString();
    }

    private int[] getStartEnd(String whole, String startFlag){
        return getStartEnd(whole, startFlag, "\0");
    }

    private int[] getStartEnd(String whole, String startFlag, String endFlag){
        int[] out = new int[2];

        out[0] = whole.indexOf(startFlag);
        out[1] = whole.indexOf(endFlag, out[0]);

        return out;
    }

    private float[] getWeightsFromString(String str){
        String[] elements = str
                .replace(" ", "")
                .replace("[", "")
                .replace("]", "")
                .split(",");

        float[] ret = new float[elements.length];
        for(int i=0; i<elements.length; i++){
            ret[i] = Float.parseFloat(elements[i]);
        }

        return ret;
    }

    private void log(String msg) {
        Log.print(this,id+msg);
    }

//    public float[][][] getDesiredChanges(float[] desiredOut) {
//        if(desiredOut.length != Layers[Layers.length-1].Neurons.length){
//            Log("back propagation needs "+Layers[Layers.length-1].Neurons.length
//                    +" answers, given "+desiredOut.length);
//            return null;
//        }
//
//        float[][][] changes = new float[Layers.length][][];
//        float[] desiredActivations = desiredOut;
//        for(int layerIndex=Layers.length-1; layerIndex>0; layerIndex--) {
//            Layer currLayer = Layers[layerIndex];
//            changes[layerIndex] = new float[currLayer.Neurons.length][];
//            float[][][] desiredChanges = new float[currLayer.Neurons.length][2][];  //see use for why 2
//            Layer bckLayer = Layers[layerIndex-1];
//
//            //get desired weight and bckLayer activation changes for each neuron
//            for(int neuronIndex=0; neuronIndex<currLayer.Neurons.length; neuronIndex++) {
//                Neuron neuron = currLayer.Neurons[neuronIndex];
//                changes[layerIndex][neuronIndex] = new float[neuron.getWeights().length];
//
//                //returned array arr; arr[0] is weight changes, arr[1] is act changes
//                desiredChanges[neuronIndex] = getChangesForNeuron(neuron, bckLayer,
//                        desiredActivations[neuronIndex]);
//
//                //store desired weight changes to be averaged over example set
//                changes[layerIndex][neuronIndex] = desiredChanges[neuronIndex][0];
//            }
//
//            //sum desired activation changes and store for bckLayer (next iteration)
//            desiredActivations = new float[bckLayer.Neurons.length];
//            for (float[][] desiredChange : desiredChanges) {
//                for (int bckIndex = 0; bckIndex < bckLayer.Neurons.length; bckIndex++) {
//                    desiredActivations[bckIndex] += desiredChange[1][bckIndex];
//                }
//            }
//        }
//
//        return changes;
//    }
}
