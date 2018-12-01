package trainers;

public class TrainerXOR extends Trainer {

    @Override
    public float[][] getTrainingSet() {
        return new float[][]{{0, 0}, {0, 1}, {1, 0}, {1, 1}};
    }

    @Override
    public float[] getAnswer(float[] input) {
        float[] out = new float[1];
        out[0] = xor((int)input[0], (int)input[1]);
        return out;
    }

    private int xor(int a, int b) {
        if(a!=b) {
            return 1;
        }else{
            return 0;
        }
    }
}
