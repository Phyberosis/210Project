package tetris;

import trainers.TrainerTetris;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class TetrisPlayer implements Runnable{

    private final String INTERNET_APP = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
    private final String TETRIS_PATH = "https://tetris.Com/play-tetris/?utm_source=top_nav_link&utm_medium=webnav&utm_campaign=playNow_btm_tst&utm_content=text_play_now";

    private Thread thread;
    private boolean isRunning = false;
    private final long UPDATE_PERIOD = 750;
    public static final int OFFSET = 28;

    private TetrisGUI ui;

    private Robot robot;
    private ScreenToInput screenIn;

    public TetrisPlayer(TetrisGUI tetrisGUI){
        ui = tetrisGUI;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.err.println("check INTERNET_APP field and TETRIS_PATH field in TetrisPlayer" +
                    "\n TetrisPlayer NOT INITIALIZED");
        }
        screenIn = new ScreenToInput(robot);
    }

    public static void main(String[] args) {
        TetrisPlayer plr = new TetrisPlayer(null);
        plr.initAndRun();
    }

    public void initTetrisWindow() {
        try {
            Runtime.getRuntime().exec(new String[] {INTERNET_APP, TETRIS_PATH});
        } catch (IOException e) {
            //
        }
    }

    public void initAndRun(){
        if(thread == null){
            thread = new Thread(this);
            isRunning = true;
            thread.start();
        }
    }

    @Override
    public void run() {

        if(robot == null)
            return;

//        try {
//            initTetrisWindow();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }

        System.out.println("Initializing components");
        TrainerTetris trainerTetris = new TrainerTetris(ui);
        OutputToKeystrokes keyOut = new OutputToKeystrokes(robot);

        System.out.println("Beginning core process");
        float[] gameState;
        float[] neededMoves;
        long last = System.currentTimeMillis();
        while(isRunning){
            long now = System.currentTimeMillis();
//            if(now - last < UPDATE_PERIOD){
//                continue;
//            }else{
//                last = now;
//            }

            gameState = screenIn.extractGameState();
            if(isValidGameState(gameState)){
                trainerTetris.addToHistory(gameState);
                neededMoves = trainerTetris.getAnswer(gameState);
                keyOut.executeMoves(neededMoves);
            }
        }
    }

    //debug
    public String capture(){
        return screenIn.capture();
    }

    //debug
    public static boolean isValidGameState(float[] state){
        for(int i=0; i<OFFSET; i++){
            if(state[i] == 1f)
                return true;
        }

        return false;
    }

    public void stop(){
        isRunning = false;
    }

    public void altTabAndHitSpace(int tabs) {
        robot.keyPress(KeyEvent.VK_ALT);
        delay();

        for(int i = 0; i<tabs; i++){
            robot.keyPress(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_TAB);
            delay();
        }

        robot.keyRelease(KeyEvent.VK_ALT);
        delay();

        robot.keyPress(KeyEvent.VK_SPACE);
        robot.keyRelease(KeyEvent.VK_SPACE);

        robot.waitForIdle();
    }

    private void delay(){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            //
        }
    }
}
