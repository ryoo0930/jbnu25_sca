package org.newdawn.spaceinvaders.stage;

import java.util.ArrayList;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.strategy.PatrolMovementStrategy;

public class EasyStage implements Stage {
    private int alienCount;

    @Override
    public void initEntities(Game game, ArrayList<Entity> entities) {
        alienCount = 0;

        PatrolMovementStrategy patrolMovementStrategy = new PatrolMovementStrategy(500);
        Entity alien = new AlienEntity(game, 400 , 80, patrolMovementStrategy);
        entities.add(alien);
        alienCount++;
    }

    @Override
    public int getAlienCount() {
        return alienCount;
    }
}
