package org.newdawn.spaceinvaders.stage;

import java.util.ArrayList;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;

public class EasyStage implements Stage {
    private int alienCount;

    @Override
    public void initEntities(Game game, ArrayList<Entity> entities) {
        alienCount = 0;
        Entity alien = new AlienEntity(game, 400, 50);
        entities.add(alien);
        alienCount++;
    }

    @Override
    public int getAlienCount() {
        return alienCount;
    }

    @Override
    public void update(Game game) {
        // Easy stage has no dynamic spawning
    }
}
