package org.newdawn.spaceinvaders.stage;

import java.util.ArrayList;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.movementStrategy.PatrolMovementStrategy;
import org.newdawn.spaceinvaders.entity.shotStrategy.DefaultShotStrategy;

public class EasyStage implements Stage {
    private int alienCount;

    @Override
    public void initEntities(Game game, ArrayList<Entity> entities) {
        alienCount = 0;

        PatrolMovementStrategy patrolMovementStrategy = new PatrolMovementStrategy(500);
        DefaultShotStrategy defaultShotStrategy = new DefaultShotStrategy(game, 2000);
        Entity alien = new AlienEntity(game, 400 , 80, patrolMovementStrategy, defaultShotStrategy);
        entities.add(alien);
        alienCount++;
    }

    @Override
    public int getAlienCount() {
        return alienCount;
    }
}
