package neuralNet.staticTopologyNet;

import managers.Log;
import neuralNet.Stringable;

import java.util.Arrays;

public class Neuron implements Stringable {
    private static final float MAX_WEIGHT = 50;

    private int id;
    private float[] weights;
    private float rawActivation = 0;
    private float activation = 0;
    private float dActPerRaw = 0;

    Neuron(int id, int prevLayerSize) {
        this.id = id;
        weights = new float[prevLayerSize+1]; //+1 for bias
        //bias ignored for now
        weights[0] = 0;
        for(int i=1; i<weights.length; i++) {
            weights[i] = 1;
        }
    }

    int getId() {
        return id;
    }

    void fire(int callerIndex, float strength){
        rawActivation += weights[callerIndex]*strength;
    }

    void shiftWeights(float[] dWeights, boolean shouldLog) {
        if(dWeights.length != weights.length) {
            log("shiftWeights error, given length: "+dWeights.length
                    +", required: "+weights.length);
            return;
        }

        if(shouldLog){
            log(Arrays.toString(dWeights));
        }

        for(int i=0; i<weights.length; i++) {
            weights[i] += dWeights[i];

            if(weights[i] > MAX_WEIGHT){
                weights[i] = MAX_WEIGHT;
            }else if(weights[i] < -MAX_WEIGHT) {
                weights[i] = -MAX_WEIGHT;
            }
        }

    }

    void setWeights(float[] newWeights, boolean shouldLog) {
        if(newWeights.length != weights.length) {
            log("setWeights error, given length: "+newWeights.length
                        +", required: "+weights.length);
            return;
        }
        for(int i=0; i<newWeights.length; i++) {
            if(newWeights[i] > MAX_WEIGHT){
                newWeights[i] = MAX_WEIGHT;
            }else if(newWeights[i] < -MAX_WEIGHT) {
                newWeights[i] = -MAX_WEIGHT;
            }
        }

        System.arraycopy(newWeights, 0, weights, 0, newWeights.length);
    }

    void setWeights(Neuron srcNeuron) {
        this.setWeights(srcNeuron.weights, false);
    }

    float[] getWeights () {
        return weights;
    }

    public void mutate(float range) {
        //bias ignored for now
        for(int weightIndex=1; weightIndex<weights.length; weightIndex++) {
            weights[weightIndex] = mutateValue(weights[weightIndex], range);
        }
    }

    public void randomize() {
        //bias ignored for now
        for(int weightIndex=1; weightIndex<weights.length; weightIndex++) {
            weights[weightIndex] = (float)(Math.random()*2d-1d)*MAX_WEIGHT;
        }
    }

    void reset(){
        rawActivation = 0;
    }

    void update() {
        activation = (float) ( 1.0 / (1.0 + Math.pow(Math.E, -rawActivation)) );
        dActPerRaw = activation*(1 - activation);
    }

    float getRawActivation() {
        return rawActivation;
    }

    float getActivation() {
        return activation;
    }

    // dAct / dRaw
    float getdActPerRaw() {
        return dActPerRaw;
    }

    private float mutateValue(float value, float range) {
        return value + powerOf((float)(Math.random()*2d-1d), 3)*range;
    }

    private float powerOf(float base, int power) {
        float ans = 1;
        try{
            while(power>0) {
                ans*=base;
                power--;
            }
            return ans;
        }catch (Exception ignored){
            if(ans>0){
                return Float.MAX_VALUE;
            }else{
                return Float.MIN_VALUE;
            }
        }
    }

    @Override
    public String toString() {
        return "\n  neuron: "+id+"\n   "+Arrays.toString(weights);
    }

    private void log(String msg) {
        Log.print(this,id+msg);
    }
}
