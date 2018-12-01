package neuralNet.dynamicTopologyNet;

import neuralNet.Net;

import javax.print.attribute.standard.DocumentName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

class Neuron {

    private HashMap<Neuron, Float> axons;
    private DynamicNet parentNet;

    private int activations;
    private float activationScheme; // one, all, or value between

    Neuron(DynamicNet n){
        parentNet = n;
        axons = new HashMap<>();
    }

    void setActivationScheme(float scheme){
        activationScheme = scheme;
        if(activationScheme > axons.size()){
            activationScheme = axons.size();
        }
    }

    float getActivationScheme(){
        return activationScheme;
    }

    void mutate(){
        for(Neuron key : axons.keySet()){
            float w = axons.get(key);
            w = parentNet.mutateWeight(w);
            axons.replace(key, w);
        }
//        activationScheme = axons.size() * parentNet.mutateScheme(this, activationScheme);
    }

    @Override
    public int hashCode(){
        return Math.round(activationScheme*1000);
    }

    @Override
    public boolean equals(Object o){
        if(o==null)
            return false;

        if(o.getClass() != this.getClass() || o.hashCode() != hashCode()){
            return false;
        }

        return true;
    }
}