package game.entities;

import game.EatGame;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class Shark extends MyEnt {
    int maxRange;

//    float speed;
    int dx, dy;
//    final float ACCEL = 5;
    final float MAX_SPEED = 25;
    int tackleRange;
    boolean isTackling = false;
    boolean isRepositioning = false;

    final int MAX_REPO_TICKS = 45;
    int repositioningTicks = 0;
    private long dormancy = 300;

    boolean eaten = false;

    private Location plrLoc;
    private Image me;

    private ImageView imageView;
    private SnapshotParameters imageViewParams;
    private boolean flip = false;

    public Shark(int gameWidth) {
        super(175, "shark.png", Entities.SHARK);

        plrLoc = new Location(0,0);
        x = EatGame.WIDTH/2;
        y = -EatGame.HEIGHT;

        me = loadImage();
        imageView = new ImageView(me);
        imageViewParams = new SnapshotParameters();
        maxRange = (gameWidth*3);
//        speed = 0;
        dx = 0;
        dy = 0;
        tackleRange = EatGame.HEIGHT;
    }

    private int getSqrRange(Location target){
        return sqr(target.x - x) + sqr(target.y - y);
    }

    public void givePlayerLocation(int[] bounds){
        synchronized (this){
//            System.out.println(x + "," +y);
            int x = bounds[0];
            int y = bounds[1];
            plrLoc.x = x;
            plrLoc.y = y;
        }
    }

    void eaten(){
        eaten = true;
    }

    public boolean isEaten(){
        return eaten;
    }

    @Override
    public void update() {
//            System.out.println(now - lastActionTime);

        if(dormancy > 0){
            dormancy--;
            return;
        }

        int sqrRange = getSqrRange(plrLoc);
        int sqrMax = sqr(tackleRange);

        //initialize tackle when in range and only stop when off screen
        if(sqrRange < sqrMax){
            isTackling = true;
//                speed = MAX_SPEED;
        }
        else if(isTackling && sqrRange > sqrMax){
//                System.out.println("here");
            isTackling = false;
            isRepositioning = true;
            repositioningTicks = 0;
//                speed = 0;
        }else if(isRepositioning){
            repositioningTicks++;
            if(repositioningTicks > MAX_REPO_TICKS)
                isRepositioning = false;

        }
//            System.out.println(sqrRange+", "+sqrMax);

        //stop tracking when tackling
        if(!isTackling){
            synchronized (this){
                int[] spd = getNewVector(plrLoc.x, plrLoc.y);
                dx = spd[0];
                dy = spd[1];
            }
        }

//            System.out.println(dx+", "+dy);
//            System.out.println(x+", "+y);
//            System.out.println(plrLoc.x+", "+plrLoc.y);
        //always need to move
        x += dx;
        y += dy;
    }

    @Override
    public void collided(MyEnt ent, long now) {
        switch (ent.getType()){
            case PLAYER:
                if(getBounds()[2] > ent.getBounds()[2]) {
                    Player p = (Player) ent;
                    p.eaten();
                }
        }
    }

    @Override
    public void render(GraphicsContext g) {
        Image rotatedImage = imageView.snapshot(imageViewParams, null);
        double width = rotatedImage.getWidth();
        double h = rotatedImage.getHeight();
        double xx = x;
        if(flip) {
            xx = x+width;
            width = -width;
        }
        g.drawImage(rotatedImage, xx - Math.abs(width/2), y-(h/2), width, h);

//        drawPicture(g, rotatedImage);
    }

    private int sqr(int a){
        return a*a;
    }

    private int[] getNewVector(int tx, int ty){
//        System.out.println(speed);

        double xDisp = tx - x;
        double yDisp = ty - y;

        double theta = Math.atan(yDisp/xDisp);
        if(xDisp >= 0){
            flip = true;
            double th = Math.PI - theta;
            imageView.setRotate(th*180/Math.PI + 45 + 180);
//            theta-=Math.PI;
        }
        else{
            flip = false;
            imageView.setRotate(theta*180/Math.PI + 45);
        }
//        System.out.println(theta);
        imageViewParams.setFill(Color.TRANSPARENT);

        double xSign = xDisp/Math.abs(xDisp);
        double ySign = yDisp/Math.abs(yDisp);

        int dx, dy;
        dx = (int) Math.abs(Math.round(Math.cos(theta) * MAX_SPEED));
        dy = (int) Math.abs(Math.round(Math.sin(theta) * MAX_SPEED));

        if(isRepositioning){
            int temp = dy;
            dy = -dx;
            dx = temp;
        }

        dx = (int)Math.round(xSign*dx);
        dy = (int)Math.round(ySign*dy);

        return new int[] {dx, dy};
    }
}
