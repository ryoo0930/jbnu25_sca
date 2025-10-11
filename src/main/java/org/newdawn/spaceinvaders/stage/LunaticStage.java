package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.boss.LunaticBossEntity;

import java.util.ArrayList;

public class LunaticStage implements Stage {

    private boolean bossSpawned = false;

    @Override
    public void initEntities(Game game, ArrayList<Entity> entities) {
        // Spawn the Lunatic Boss immediately
        game.addEntity(new LunaticBossEntity(game, 350, 50));
        bossSpawned = true;
    }

    @Override
    public int getAlienCount() {
        // Alien count is not relevant for this boss-only stage
        return 1;
    }

    @Override
    public void update(Game game) {
        // No additional logic needed, the boss handles itself.
    }
}
