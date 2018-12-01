package mechanics;

import entities.Player;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class MouseHandler implements EventHandler<MouseEvent> {

    private Player player;
    public MouseHandler(Player pl){
        player = pl;
    }

    @Override
    public void handle(MouseEvent event) {
        int x = (int)Math.round(event.getSceneX());
        int y = (int)Math.round(event.getSceneY());
        player.giveMouseLocation(x, y);
    }
}
