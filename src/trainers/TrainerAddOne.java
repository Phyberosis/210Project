package trainers;

public class TrainerAddOne extends Trainer{
    @Override
    public float[][] getTrainingSet() {

        float[][] set = new float[8][];

        int digits = 3;
        int setSize = powerOf(2, digits);
        for(int setIndex=0; setIndex<setSize; setIndex++) {
            set[setIndex] = toBinary(setIndex, digits);
        }
        return set;
    }

    private float[] toBinary(int in, int digits) {
        float[] out = new float[digits];

        final int base = 2;
        int power = 1;
        for(int i=digits-1; i>=0; i--) {
            int digit = in % powerOf(base, power);

            in -= digit;

            digit /= powerOf(base, power-1);
            out[i] = digit;

            power++;
        }

        return out;
    }

    private int powerOf(int base, int power) {
        int ans = 1;

        for(int i=0; i<power; i++) {
            ans *= base;
        }
        return ans;
    }

    @Override
    public float[] getAnswer(float[] input) {
        int length = input.length;
        float[] out = new float[length];
        System.arraycopy(input, 0, out, 0, length);

        for(int i=length-1; i>=0; i--) {
            if(input[i] == 0) {
                out[i] = 1;
                break;
            }else if(input[i] == 1) {
                out[i] = 0;
                if(i!=0){
                    out[i-1] = 1;
                }
            }else{
                log("invalid input: "+input[i]);
            }
        }
        return out;
    }
}
