package mechanics;

import entities.MyEnt;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;

import java.util.LinkedList;

public class GraphicsHandler extends AnimationTimer {
    private GraphicsContext graphics;
    private LinkedList<MyEnt> ents;

    private int w, h;

    public GraphicsHandler (GraphicsContext g, LinkedList<MyEnt> ents, int width, int height){
        graphics = g;
        this.ents = ents;

        w = width;
        h = height;
    }

    public void setGraphics(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    @Override
    public void handle(long now) {
//        System.out.println(now);
        graphics.clearRect(0, 0, w, h);

        for(MyEnt e : ents){
            e.render(graphics);
        }
    }
}
