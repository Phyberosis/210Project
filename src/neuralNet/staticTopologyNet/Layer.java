package neuralNet.staticTopologyNet;

import managers.Log;
import neuralNet.Stringable;

class Layer implements Stringable {
    private int id;
    Neuron[] Neurons;

    Layer(int id, int neuronCount, int prevLayerSize) {
        this.id = id;
        log(" building "+neuronCount+" Neurons");

        Neurons = new Neuron[neuronCount];

        for(int neuronIndex = 0; neuronIndex < neuronCount; neuronIndex++) {
            //Log(" creating neuron "+(neuronIndex)+" of "+neuronCount);
            Neurons[neuronIndex] = new Neuron(neuronIndex, prevLayerSize);
        }
    }

    void setWeights(Layer srcLayer) {
        for(int neuronIndex = 0; neuronIndex< srcLayer.Neurons.length; neuronIndex++) {
            Neurons[neuronIndex].setWeights(srcLayer.Neurons[neuronIndex]);
        }
    }

    void shiftWeights(float[][] dWeights, boolean shouldLog) {
        if(shouldLog) {
            log("");
        }
        for(int neuronIndex=0; neuronIndex<dWeights.length; neuronIndex++) {
            Neurons[neuronIndex].shiftWeights(dWeights[neuronIndex], shouldLog);
        }
    }

    public void mutate(float range) {
        for (Neuron Neuron : Neurons) {
            Neuron.mutate(range);
        }
    }

    public void randomize() {
        for(Neuron Neuron : Neurons) {
            Neuron.randomize();
        }
    }

    void update(){
        for (Neuron Neuron : Neurons) {
            Neuron.update();
        }
    }

    @Override
    public String toString() {
        StringBuilder myString = new StringBuilder();
        myString.append("\n layer: ").append(id).append(", Neurons: ").append(Neurons.length);
        for(Neuron Neuron : Neurons) {
            myString.append(Neuron.toString());
        }
        return myString.toString();
    }

    private void log(String msg) {
        Log.print(this,id+msg);
    }
}
