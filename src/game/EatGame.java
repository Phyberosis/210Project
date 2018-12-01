package game;

import entities.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mechanics.GraphicsHandler;
import mechanics.KeyHandler;
import mechanics.LogicHandler;
import mechanics.MouseHandler;

import java.util.HashMap;
import java.util.LinkedList;

public class EatGame extends Application {

    public static int WIDTH = 1600;
    public static int HEIGHT = 900;

    private final String TITLE = "Eat!!";
//    private final String[][] FXMLS = new String[][]
//        {
//                {"windWin.fxml", SceneIDs.WIN.toString()},
//                {"windLose.fxml", SceneIDs.LOSE.toString()}
//        };

    private GraphicsHandler graphicsHandler;
    private LogicHandler logicHandler;

    private LinkedList<MyEnt> ents;
    private Player plr;
    private Shark shark;

    private Stage stage;
    private Scene currentScene;
    private HashMap<SceneIDs, Scene> sceneMap;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        primaryStage.setTitle(TITLE);

        Group root = new Group();
        currentScene = new Scene( root );
        primaryStage.setScene(currentScene);

        primaryStage.setOnCloseRequest(event -> stop());

        sceneMap = new HashMap<>();
        sceneMap.put(SceneIDs.GAME, currentScene);
//        loadScenes();

        primaryStage.show();
        primaryStage.setFullScreen(true);
        WIDTH = (int) Screen.getPrimary().getBounds().getWidth();
        HEIGHT = (int) Screen.getPrimary().getBounds().getHeight();

        Canvas canvas = initGameScene();

        root.getChildren().add( canvas );
    }

    private Canvas initGameScene(){
        stage.setFullScreen(true);

        ents = new LinkedList<>();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext g = canvas.getGraphicsContext2D();
        graphicsHandler = new GraphicsHandler(g, ents, WIDTH, HEIGHT);
        logicHandler = new LogicHandler(ents, this);

        loadFoods();
        plr = new Player();
        ents.add(plr);
        shark = new Shark(WIDTH);
        ents.add(shark);

        currentScene.setOnMouseMoved(new MouseHandler(plr));
        currentScene.setOnKeyPressed(new KeyHandler(this));

        graphicsHandler.start();
        logicHandler.start();

        return canvas;
    }

    private void stopHandlers(){
        graphicsHandler.stop();
        logicHandler.stop();
    }

    public void changeScene(SceneIDs newScene){
        String msg;
        Color col;
        Group root = new Group();

        switch (newScene){
            case WIN:
                msg = "You ate the shark!\nYou Win!";
                col = Color.GREEN;
                stopHandlers();
                break;
            case LOSE:
                msg = "You got eaten!\nTry Again!";
                col = Color.RED;
                stopHandlers();
                break;
            case GAME:
                Canvas c = initGameScene();
                root.getChildren().add(c);
                stage.getScene().setRoot(root);
                return;
                default:
                    msg = "";
                    col = Color.BLACK;
                    break;
        }

        Scene s = new EndMessageScreen(msg, col, WIDTH*2/3, HEIGHT*2/3, root);
//        Scene s = sceneMap.get(newScene);
        Platform.runLater(() -> {
            currentScene = s;
            stage.setScene(s);
            s.setOnKeyPressed(new KeyHandler(EatGame.this));
        });
    }

    public void slowTime(){
        logicHandler.slowTime();
    }

    public void resetTime(){
        logicHandler.resetTime();
    }

    @Override
    public void stop(){
        logicHandler.stop();
        graphicsHandler.stop();
        System.exit(0);
    }

    private void loadFoods(){
        for(int i = 0; i<5; i++) {
            int choice = (int) Math.round(Math.random() * 3 + 1);
            String src = "candy.png";
            switch (choice) {
                case 2:
                    src = "broccoli.png";
                    break;
                case 3:
                    src = "pizza.png";
                    break;
            }

            ents.add(new Food(src));
        }
    }
}
