package tetris;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.security.Key;

public class OutputToKeystrokes {

    private Robot sharedRobot;

    public OutputToKeystrokes(Robot r){
        sharedRobot = r;
    }

    public void executeMoves(float[] moves){
        int col = -1;
        int rot = -1;

        int rotDataIndex = 10;
        for(int x=0; x<rotDataIndex; x++){
            if(moves[x] == 1f) {
                col = x;
                break;
            }
        }
        for(int r=rotDataIndex; r<rotDataIndex+4; r++){
            if(moves[r] == 1f) {
                rot = r-rotDataIndex;
                break;
            }
        }

        if(col == -1 || rot == -1){
            System.out.println("bad command");
            return;
        }

        System.out.println(col+" "+rot);
        sendKeys(col, rot);
    }

    private void setX(int calibrateDir, int dir, int x, long delay){
        for (int i=0; i<4; i++){
            sharedRobot.keyPress(calibrateDir);
            myWait(delay);
            sharedRobot.keyRelease(calibrateDir);
            myWait(delay);

        }

        for (int i=0; i<x; i++){
            sharedRobot.keyPress(dir);
            myWait(delay);
            sharedRobot.keyRelease(dir);
            myWait(delay);
        }
    }

    private void sendKeys(int col, int rot) {
        long delay = 20;
        long altDelay = 100;

        for (int i=0; i<rot; i++){
            sharedRobot.keyPress(KeyEvent.VK_UP);
            myWait(delay);
            sharedRobot.keyRelease(KeyEvent.VK_UP);
            myWait(delay);
        }

        myWait(altDelay);

        int calDir = KeyEvent.VK_LEFT;
        int dir = KeyEvent.VK_RIGHT;
        setX(calDir, dir, col, delay);

        myWait(altDelay);

        sharedRobot.keyPress(KeyEvent.VK_SPACE);
        myWait(delay);
        sharedRobot.keyRelease(KeyEvent.VK_SPACE);
        myWait(delay);

        sharedRobot.waitForIdle();
    }

    private void myWait(long delay){
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            //ignored
        }
    }
}
