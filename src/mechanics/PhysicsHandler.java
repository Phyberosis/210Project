package mechanics;

import entities.MyEnt;

import java.util.HashMap;
import java.util.LinkedList;

public class PhysicsHandler {

    public PhysicsHandler(){
    }

    private boolean isColliding(MyEnt a, MyEnt b){
        return a.isIntersect(b.getBounds());
    }

    public void checkCollisions(LinkedList<MyEnt> entities, long now){
        HashMap<MyEnt, MyEnt> collidingEnts = new HashMap<>();

        for(int checkIndex=0; checkIndex<entities.size() - 1; checkIndex++){
            for(int i=checkIndex + 1; i<entities.size(); i++){
                MyEnt a, b;
                a = entities.get(checkIndex);
                b = entities.get(i);

                if(!a.isActive() || !b.isActive()){
                    continue;
                }

                boolean collided = isColliding(a , b);
                if(collided){
                    collidingEnts.put(a, b);
                }
            }
        }

        for(MyEnt a : collidingEnts.keySet()){
            MyEnt b = collidingEnts.get(a);
            a.collided(b, now);
            b.collided(a, now);
        }
    }
}
