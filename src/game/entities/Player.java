package game.entities;

import game.EatGame;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Player extends MyEnt{
    Image myPicture;

    private Location mouseLocation;
    Location mouse;

    private boolean eaten = false;
    public Player(){
        super(1, "face.PNG", Entities.PLAYER);
        myPicture = loadImagePlr();
        mouseLocation = new Location(0,0);
        mouse = new Location(-radius, -radius);

        x = -radius;
        y = -radius;

        makeMe();
        centerMe();
    }

    void makeMe(){
        radius = 60;
        myPicture = loadImagePlr();
    }

    void centerMe(){
        x = EatGame.WIDTH/2;
        y = EatGame.HEIGHT/2;
    }

    public void giveMouseLocation(int x, int y){
        synchronized (this){
//            System.out.println(x + "," +y);
            mouseLocation.x = x;
            mouseLocation.y = y;
        }
    }

    private int move(int a, int target){
        double rate = 10;
        int da = (int) Math.round(((target - a) / rate));
//        if(Math.abs(target - a) < rate/10 && target-a != 0){
//            da = Math.abs(target - a)/(target-a);
//        }
        return a+da;
    }

    public void grow(){
        radius+=6;
        synchronized (this){
            myPicture = loadImage();
        }
    }

    public void render(GraphicsContext g){
        synchronized (this){
//            System.out.println("player render");
            drawPicture(g, myPicture);
//            drawCircle(g);
        }
    }

    @Override
    public void update() {
        followMouse();
//        System.out.println(now-lastActionTime + "/"+actionDelay);
    }

    void followMouse(){
        mouse = mouseLocation;
        synchronized (this){
            x = move(x, mouse.x);
            y = move(y, mouse.y);
        }
    }

    void eaten(){
        eaten = true;
    }

    public boolean isEaten(){
        return eaten;
    }

    @Override
    public void collided(MyEnt ent, long now) {

        switch (ent.getType()){
            case FOOD:
//            System.out.println("grow!" + now);
                Food f = (Food) ent;
                f.eat(now);
                grow();
                break;
            case SHARK:
                if(getBounds()[2] > ent.getBounds()[2]) {
                    Shark s = (Shark) ent;
                    s.eaten();
                }
        }
    }
}