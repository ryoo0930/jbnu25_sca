package org.newdawn.spaceinvaders.stage;

import java.util.ArrayList;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.strategy.DownThenRightMovementStrategy;

public class EasyStage implements Stage {
    private int alienCount;

    @Override
    public void initEntities(Game game, ArrayList<Entity> entities) {
        alienCount = 0;

        DownThenRightMovementStrategy downThenRightMove = new DownThenRightMovementStrategy(100);
        Entity alien = new AlienEntity(game, 300 , 50, downThenRightMove);
        entities.add(alien);
        alienCount++;
    }

    @Override
    public int getAlienCount() {
        return alienCount;
    }
}
