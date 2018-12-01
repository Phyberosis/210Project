package entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public abstract class MyEnt {
    int x, y;

    int radius;

    //60ups
//    long actionDelay = 16000000;
//    long lastActionTime;

    String imageSrc;
    Entities type;
    boolean active = true;

    public MyEnt(int radius, String imgSrc, Entities type){
        this.radius = radius;
        imageSrc = imgSrc;
        this.type = type;
    }

    public boolean isActive(){
        boolean ret;
        synchronized (this){
            ret = active;
        }
        return ret;
    }

    public void setActive(boolean act){
        synchronized (this){
            active = act;
        }
    }

    public Entities getType() {
        return type;
    }

    public abstract void update();

    public abstract void collided(MyEnt ent, long now);

    public abstract void render(GraphicsContext g);

    public boolean isIntersect(int x, int y, int r){
        return sqr(this.x - x) + sqr(this.y - y) < sqr(r+radius);
    }

    public boolean isIntersect(int[] b){
        return isIntersect(b[0], b[1], b[2]);
    }

    public int[] getBounds(){
        return new int[] {x, y, radius};
    }

    void drawPicture(GraphicsContext g, Image img){
        g.drawImage(img, (int)(x-radius), (int)(y-radius));
    }

    void drawCircle(GraphicsContext g){
        g.setStroke(Color.BLUE);
        g.fillOval(x-radius, y-radius , radius*2, radius*2);
    }

    Image loadImage(){
        return new Image(imageSrc, radius*2, radius*2, false, false);
    }

    Image loadImagePlr(){
        return new Image(imageSrc, radius*2.2, radius*2, false, false);
    }

//    Image scale(int r){
//        return new Image(imageSrc, r, r, false, false);
//    }

    private int sqr(int a){
        return a*a;
    }

    public String printLoc(){
        return Integer.toString(x)+", "+Integer.toString(y);
    }
}
