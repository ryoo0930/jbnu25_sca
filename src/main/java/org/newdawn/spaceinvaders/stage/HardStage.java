package org.newdawn.spaceinvaders.stage;

import java.util.ArrayList;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;

public class HardStage implements Stage {
    private int alienCount;

    @Override
    public void initEntities(Game game, ArrayList<Entity> entities) {
        alienCount = 0;
        for (int row = 0; row < 6; row++) {
            for (int x = 0; x < 15; x++) {
                Entity alien = new AlienEntity(game, 50 + (x * 45), (50) + row * 25);
                entities.add(alien);
                alienCount++;
            }
        }
    }

    @Override
    public int getAlienCount() {
        return alienCount;
    }
}
