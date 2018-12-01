package managers;

import java.util.Observable;
import java.util.Observer;

public class Log implements Observer {

    private static Log instance = null;

    private Log(){
    }

    public static Log getInstance(){
        if(instance == null){
            instance = new Log();
        }

        return instance;
    }

    public static void print(Object o, String msg){
        System.out.println(o.getClass().getName() + ": " + msg);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(arg instanceof String){
            print(o, (String) arg);
        }
    }
}
