package neuralNet.dynamicTopologyNet;

import exceptions.NegativeCostException;
import neuralNet.Net;

import java.util.LinkedList;
import java.util.Map;

/**
 * this was an unrealized dream...
 */

public class DynamicNet implements Net{
    LinkedList<Neuron> inputs, outputs;
    LinkedList<Neuron> hiddens;

    private final float MUT_TOPOLOGY_CHANCE = 0.2f;
    private final float MUT_CONNECTION_CHANCE = 0.1f;
    private final float MUT_RANGE = 1f;
    private final float MAX_WEIGHT = 50f;

    private int id;
    private float cost;

    public DynamicNet(int ins, int outs, int id) {
        this.id = id;
        this.cost = Float.MAX_VALUE;

        inputs = new LinkedList<>();
        for(int i=0; i<ins; i++){
            inputs.add(new Neuron(this));
        }

        outputs = new LinkedList<>();
        for(int i=0; i<outs; i++){
            outputs.add(new Neuron(this));
        }

        hiddens = new LinkedList<>();
    }

    @Override
    public void init(){
        for(Neuron n : inputs){
//            n.mutateConnections(1, MAX_WEIGHT, MAX_WEIGHT);
        }

        for(Neuron n : hiddens){
//            n.mutateConnections(1, MAX_WEIGHT, MAX_WEIGHT);
        }
    }

    public DynamicNet breed(DynamicNet partner){
        for(Neuron n : hiddens){
            if(!selectTrue())
                break;

            Neuron overWrite;
            do{
                overWrite = partner.selectRandNeuron();
            }while(n.equals(overWrite));

            n.setActivationScheme(overWrite.getActivationScheme());
        }

        return this;
    }

    private boolean selectTrue(){
        int a = (int) Math.round(Math.random()*2d);
        return a == 0;
    }

    private Neuron selectRandNeuron(){
        int randIdx = (int) Math.round(Math.random()*(hiddens.size()-1));

        return hiddens.get(randIdx);
    }

    float mutateWeight(float w){
        return w + (float)( Math.random()*MUT_RANGE - (MUT_RANGE/2.0) );
    }

    @Override
    public void mutate(){
        for(Neuron n : inputs){
//            n.mutateConnections(MUT_CONNECTION_CHANCE, MUT_RANGE, MAX_WEIGHT);
//            n.mutateTopology(MUT_TOPOLOGY_CHANCE, hiddens, MAX_WEIGHT);
        }

        for(Neuron n : hiddens){
//            n.mutateConnections(MUT_CONNECTION_CHANCE, MUT_RANGE, MAX_WEIGHT);
//            n.mutateTopology(MUT_TOPOLOGY_CHANCE, hiddens, MAX_WEIGHT);
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setCost(float cost) throws NegativeCostException {
        if(cost < 0)
            throw new NegativeCostException();
        this.cost = cost;
    }

    @Override
    public float getCost() {
        return cost;
    }

    @Override
    public void run(float[] input, boolean log) {
        for(Neuron n : inputs){
//            n.propagateSignal();
        }
    }

    @Override
    public float[] getOutput() {
        return new float[0];
    }
}
