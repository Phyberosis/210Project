package tetris;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.LinkedList;

/**
 * type "begin" to start tetris player, will alt-tab on its own
 * make sure ScreenToInput has the correct screen coordinates for the game
 * there is a commented out section in the run method that saves the captured image
 * to a file -> this may help
 */

public class TetrisGUI extends Application {
    private final String TITLE = "A Tetris Player";

    private static final Object syncObject = new Object();

    private static String OutputExternalRef = "";
    private static LinkedList<String> OutputAni = new LinkedList<>();
    private static String OutputExternalAppend = "";

    private static TetrisPlayer tPlayer;

    @FXML
    private TextArea FXOutput;
    @FXML
    private TextField FXInput;
    @FXML
    private Button FXButtonEnter;
//    @FXML
//    StringProperty FXConsoleText;

    public static void main(String[] args) {
        launch(args);
    }

    @FXML
    private void initialize(){
        // I run second!
        // @FXML fields are filled!

        //demo
        new Thread(() -> {
            final long DEF_WAIT = 60;
            long wait = DEF_WAIT;
            while (true){
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    //
                }
                synchronized (syncObject){
                    if(FXOutput.getText().contains("try")){
                        try {
                            Thread.sleep(800);
                        } catch (InterruptedException e) {
                            //
                        }
                        System.exit(0);
                    }

                    FXOutput.setText(OutputExternalRef);
                    if(!OutputExternalRef.isEmpty())
                        FXOutput.appendText("\n");
                    FXOutput.appendText(OutputExternalAppend);
                    if(!OutputAni.isEmpty()){
                        String toAdd = OutputAni.removeFirst();
                        if(toAdd.equals(".") || toAdd.equals(",")){
                            wait = 500;

                        }else if(toAdd.equals("@")){
                            wait = 1400;
                            toAdd = "";
                        }else{
                            wait = DEF_WAIT;
                        }

                        OutputExternalAppend += toAdd;
                    }
                }

//                Platform.runLater(() ->FXOutput.setText(OutputExternalRef));
            }
        }).start();

        //demo
        FXOutput.setFont(Font.font(java.awt.Font.MONOSPACED, 25));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // I run first!
        // @FXML fields are all null!

        String fxml = "TetrisGUI.fxml";
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Scene scene = new Scene(root);

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(primaryScreenBounds.getMinX() + 800);
        primaryStage.setY(primaryScreenBounds.getMinY()+170);

        tPlayer = new TetrisPlayer(this);

        primaryStage.setScene(scene);
        primaryStage.setTitle(TITLE);
        primaryStage.show();

    }

    @Override
    public void stop(){
        System.exit(0);
    }

    public void handleEnterButton(MouseEvent mouseEvent) {
        KeyEvent ke = new KeyEvent(null, "", "", KeyCode.ENTER, false, false, false, false);
        handleKey(ke);
    }

    //demo
    private int stage = 0;
    private void filmDemo(){
        String myTag = "\n\njust a chat bot\t: ";
        switch (stage){
            case 0:
                appendAnimate("@");
                appendStraight(myTag);
                appendAnimate("hi, wassup?\n");
                stage++;
                break;
            case 1:
                stage++;
                break;
            case 2:
                appendAnimate("@");
                appendStraight(myTag);
                appendAnimate("analyzing code ..... \n\t\t  wow you suck, let me try");
                stage++;
                break;
            case 3:
                appendAnimate("@");
                appendStraight(myTag);
                appendAnimate("just a sec.....");
                Platform.runLater(()->{
                    TetrisPlayer plr = new TetrisPlayer(this);
                    plr.initTetrisWindow();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        //
                    }
                    FXOutput.setFont(Font.font(java.awt.Font.MONOSPACED, 18));
                    plr.initAndRun();
                });
                stage++;
                break;
        }
    }

    public void handleKey(KeyEvent keyEvent) {
        KeyCode kc = keyEvent.getCode();
        switch (kc){
            case ENTER:
                String newIn = FXInput.getText().toLowerCase();
                switch (newIn){
                    case "begin":
                        appendTextArea("you: \""+newIn+"\"");
                        new Thread(() -> {
                            appendTextArea("tetris player started");
//                        tPlayer.altTabAndHitSpace(2);
                            tPlayer.initAndRun();
                            FXOutput.setFont(Font.font(java.awt.Font.MONOSPACED, 18));
                        }).start();
                        break;
                    case "capture":
                        String file = tPlayer.capture();
                        appendTextArea("you: \""+newIn+"\"");
                        appendTextArea("game area captured, check file "+file);
                        break;
                    case "help":
                        appendTextArea("\"begin\"\t- starts player, updates on new block\n" +
                                "\"capture\"\t- takes snapshot of current game area I see for config");
                        break;

                        default:
                            //demo
                            appendTextAreaNoLn("\nrando 210 guy\t: "+newIn);
                            if(newIn.contains("now what?"))
                                stage=3;
                            filmDemo();
                }
                FXInput.setText("");
                break;
            case BACK_SPACE:
                String text = FXInput.getText();
                text = text.substring(0, text.length());
                FXInput.setText(text);
                if(!text.equals(""))
                     FXInput.positionCaret(text.length());
                break;
        }
    }

    public void setTextArea(String text){
        synchronized (syncObject){
//            FXOutput.setText(text);
            OutputExternalRef = text;
            OutputExternalAppend = "";
        }
    }

    //demo
    private void appendStraight(String Message){
        synchronized (syncObject){
            OutputAni.add(Message);
        }
    }

    //demo
    private void appendAnimate(String brokenMessage){
        synchronized (syncObject){
            for(char c : brokenMessage.toCharArray()){
                OutputAni.add(String.valueOf(c));
            }
        }
    }

    public void appendTextArea(String text) {
        synchronized (syncObject) {
            OutputExternalAppend += text + "\n";
        }
    }

    public void appendTextAreaNoLn(String text){
        synchronized (syncObject){
            OutputExternalAppend += text;
        }
    }
}
