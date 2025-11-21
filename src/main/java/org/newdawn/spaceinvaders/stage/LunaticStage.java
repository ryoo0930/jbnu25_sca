package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.boss.LunaticBossEntity;

import java.util.ArrayList;

public class LunaticStage implements Stage {

    private boolean bossSpawned = false;

    @Override
    public void initEntities(GamePlay gamePlay) {
        // Spawn the Lunatic Boss immediately
        gamePlay.addEntity(new LunaticBossEntity(gamePlay.getGame(), 350, 50));
        bossSpawned = true;
    }

    @Override
    public int getAlienCount() {
        // Alien count is not relevant for this boss-only stage
        return 1;
    }

    @Override
    public void update(GamePlay gamePlay) {
        // No additional logic needed, the boss handles itself.
    }
}
