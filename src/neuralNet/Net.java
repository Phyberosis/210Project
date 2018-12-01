package neuralNet;

import exceptions.LengthMismatchException;
import exceptions.NegativeCostException;

public interface Net {

    void init();

    void mutate();

    int getId();

    void setCost(float cost) throws NegativeCostException;

    float getCost();

    void run(float[] input, boolean log) throws LengthMismatchException;

    float[] getOutput();
}
