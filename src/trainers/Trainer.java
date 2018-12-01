package trainers;

import exceptions.LengthMismatchException;
import exceptions.NegativeCostException;
import managers.Log;
import neuralNet.Net;

import java.util.Arrays;

public abstract class Trainer {

    public abstract float[][] getTrainingSet();

    public float[][] getAnswerSet(float[][] inputSet) {
        float[][] ansSet = new float[inputSet.length][];
        for(int i=0; i<inputSet.length; i++) {
            ansSet[i] = getAnswer(inputSet[i]);
        }
        return ansSet;
    }

    private float getAvgSqrError (float[] outs, float[] ans) {
        float error = 0;
        for(int i=0; i<outs.length; i++) {
            error += sqr(outs[i] - ans[i]);
        }
        error /= (float)outs.length;
        return error;
    }

    public abstract float[] getAnswer(float[] input);

    public void testNet(Net net, boolean shouldLog) {
        StringBuilder log = new StringBuilder();
        log.append("\ntesting staticNet: ").append(net.getId());

        float[][] inputSet = getTrainingSet();
        float[][] outputSet = new float[inputSet.length][];
        float[][] answerSet = getAnswerSet(inputSet);
        float[] errorSet = new float[inputSet.length];
        float cost = 0;

        for(int i=0; i<inputSet.length; i++) {
            try {
                net.run(inputSet[i], false);
            } catch (LengthMismatchException e) {
                log("internal exception when running net");
                e.printStackTrace();
                cost = Float.MAX_VALUE;
                break;
            }
            outputSet[i] = net.getOutput();
            errorSet[i] = getAvgSqrError(outputSet[i], answerSet[i]);
            cost += errorSet[i];
            if(shouldLog){
                log.append("\nin: ").append(Arrays.toString(inputSet[i]))
                        .append(", out: ").append(Arrays.toString(outputSet[i]))
                        .append(", answer: ").append(Arrays.toString(answerSet[i]))
                        .append(", error: ").append(errorSet[i]);
            }
        }

        if(shouldLog) {
            log(log.append("\ncost: ").append(cost).toString());
        }

        try {
            net.setCost(cost);
        } catch (NegativeCostException e) {
            log("cost was somehow neg!");
            e.printStackTrace();
            System.exit(0);
        }
    }

    private float sqr(float base) {
        return base*base;
    }

    void log(String msg) {
        Log.print(this, msg);
    }
}
