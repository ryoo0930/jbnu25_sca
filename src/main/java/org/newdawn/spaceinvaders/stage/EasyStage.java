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
        for (int row = 0; row < 3; row++) {
            for (int x = 0; x < 10; x++) {
                Entity alien = new AlienEntity(game, 100 + (x * 60), (50) + row * 40);
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
