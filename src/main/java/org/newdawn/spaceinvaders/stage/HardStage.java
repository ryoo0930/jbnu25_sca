package org.newdawn.spaceinvaders.stage;

import java.util.ArrayList;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.boss.HardBossEntity;
import org.newdawn.spaceinvaders.entity.Entity;

public class HardStage implements Stage {
    private int alienCount;

    @Override
    public void initEntities(Game game, ArrayList<Entity> entities) {
        alienCount = 0;
        // 기존 외계인 생성 코드 대신 보스 생성
        Entity boss = new HardBossEntity(game, 400, 100);
        entities.add(boss);
        alienCount++;
    }

    @Override
    public int getAlienCount() {
        return alienCount;
    }
}