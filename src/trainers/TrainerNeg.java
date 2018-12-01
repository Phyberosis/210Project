package trainers;

public class TrainerNeg extends Trainer {

    @Override
    public float[][] getTrainingSet() {
        float[][] set = new float[2][];
        for(int i=0; i<2; i++) {
            set[i] = new float[1];
            set[i][0] = (float) i;
        }
        return set;
    }

    @Override
    public float[] getAnswer(float[] input) {
        float[] ans = new float[1];
        if(input[0] == 1) {
            ans[0] = 0;
        }else{
            ans[0] = 1;
        }
        return ans;
    }
}
