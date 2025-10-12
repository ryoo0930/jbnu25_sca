package org.newdawn.spaceinvaders.stage;

import java.util.ArrayList;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;

public interface Stage {
    void initEntities(Game game, ArrayList<Entity> entities);
    int getAlienCount();
    void update(Game game);
}
