package testers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import trainers.TrainerXOR;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TesterTrainerXOR {

    private TrainerXOR trainerXOR;

    private final float[][] TRAINING_SET = new float[][]{{0, 0}, {0, 1}, {1, 0}, {1, 1}};
    private final float[] ANS_SET = new float[] {1, 0, 0, 1};

    @BeforeEach
    private void setup(){
        trainerXOR = new TrainerXOR();
    }

    @Test
    private void testGetTrainingSet(){

        // a "contains()" might work better here but this is ok for my purposes
        float[][] set = trainerXOR.getTrainingSet();
        for(int i=0; i<set.length; i++){
            for(int ii=0; ii<set[0].length; i++){
                if(set[i][ii] != TRAINING_SET[i][ii]){
                    fail("element mismatch at index: "+i+", "+ii);
                }
            }
        }
    }

    @Test
    private void testGetAnswer(){
        for(int i=0; i<TRAINING_SET.length; i++){
            float[] ans = trainerXOR.getAnswer(TRAINING_SET[i]);
            assertTrue(ans.length == 1);
            if(ans[0] != ANS_SET[i]){
                fail("answer mismatch at: "+i+"\nexpected: "+ANS_SET[i]+", received: "+ans[0]);
            }
        }
    }
}
