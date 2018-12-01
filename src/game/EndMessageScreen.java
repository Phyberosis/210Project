package game;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class EndMessageScreen extends Scene {

    public EndMessageScreen(String msg, Color color, int w, int h, Group root) {
        super(root);

        Canvas c = new Canvas(w, h);
        GraphicsContext g = c.getGraphicsContext2D();
        root.getChildren().add( c );

        Font f = new Font(h/6);
        g.setFont(f);
        g.setFill(color);
        g.fillText(msg,0, h*2/6);
        g.setFill(Color.BLACK);
        f = new Font(h/9);
        g.setFont(f);
        g.fillText("press R to reset", 0, h*6/7);
    }
}
