package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.EasyPassingAlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.HardPassingAlienEntity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.boss.EasyBossEntity;
import org.newdawn.spaceinvaders.entity.boss.EasyMidBossEntity;

import java.util.ArrayList;

public class EasyStage implements Stage {
    private boolean finalBossSpawned = false;

    private enum StagePhase {
        ALIEN_WAVES,
        WAIT_FOR_ALIENS_TO_CLEAR,
        PAUSE_BEFORE_MID_BOSS,
        MID_BOSS_RIGHT,
        PAUSE_BETWEEN_MID_BOSSES,
        MID_BOSS_LEFT,
        FINAL_BOSS
    }
    private StagePhase currentPhase;

    private int wavesCompleted = 0;
    private int pairsSpawnedInWave = 0;
    private long lastPairSpawnTime = 0;
    private final long pairSpawnDelay = 400;
    private long wavePauseStartTime = 0;
    private final long wavePauseDuration = 4000;
    private boolean isPausedBetweenWaves = false;
    private long phaseTransitionTime = 0;

    @Override
    public void initEntities(Game game, ArrayList<Entity> entities) {
        finalBossSpawned = false;
        currentPhase = StagePhase.ALIEN_WAVES;
        wavesCompleted = 0;
        pairsSpawnedInWave = 0;
        isPausedBetweenWaves = false;
        lastPairSpawnTime = 0;
    }

    @Override
    public int getAlienCount() { return 0; }

    private boolean isEntityOnScreen(Game game, Class<? extends Entity> entityType) {
        for (Object entity : game.getEntities()) {
            if (entityType.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void update(Game game) {
        long currentTime = System.currentTimeMillis();

        switch (currentPhase) {
            case ALIEN_WAVES:
                updateAlienWaves(game, currentTime);
                if (wavesCompleted >= 2) {
                    currentPhase = StagePhase.WAIT_FOR_ALIENS_TO_CLEAR;
                }
                break;
            case WAIT_FOR_ALIENS_TO_CLEAR:
                if (!isEntityOnScreen(game, HardPassingAlienEntity.class) && !isEntityOnScreen(game, EasyPassingAlienEntity.class)) {
                    phaseTransitionTime = currentTime;
                    currentPhase = StagePhase.PAUSE_BEFORE_MID_BOSS;
                }
                break;
            case PAUSE_BEFORE_MID_BOSS:
                if (currentTime - phaseTransitionTime > 2000) {
                    game.addEntity(new EasyMidBossEntity(game, 850, 100, EasyMidBossEntity.Origin.RIGHT));
                    currentPhase = StagePhase.MID_BOSS_RIGHT;
                }
                break;
            case MID_BOSS_RIGHT:
                if (!isEntityOnScreen(game, EasyMidBossEntity.class)) {
                    phaseTransitionTime = currentTime;
                    currentPhase = StagePhase.PAUSE_BETWEEN_MID_BOSSES;
                }
                break;
            case PAUSE_BETWEEN_MID_BOSSES:
                if (currentTime - phaseTransitionTime > 2000) {
                    game.addEntity(new EasyMidBossEntity(game, -100, 100, EasyMidBossEntity.Origin.LEFT));
                    currentPhase = StagePhase.MID_BOSS_LEFT;
                }
                break;
            case MID_BOSS_LEFT:
                if (!isEntityOnScreen(game, EasyMidBossEntity.class)) {
                    currentPhase = StagePhase.FINAL_BOSS;
                }
                break;
            case FINAL_BOSS:
                if (!finalBossSpawned) {
                    java.util.List entities = game.getEntities();
                    for (int i = entities.size() - 1; i >= 0; i--) {
                        Object entity = entities.get(i);
                        if (!(entity instanceof ShipEntity)) {
                            game.removeEntity(entity);
                        }
                    }
                    game.addEntity(new EasyBossEntity(game, 350, 50));
                    finalBossSpawned = true;
                }
                break;
        }
    }

    private void updateAlienWaves(Game game, long currentTime) {
        if (wavesCompleted >= 2) return;

        if (isPausedBetweenWaves) {
            if (currentTime - wavePauseStartTime > wavePauseDuration) {
                isPausedBetweenWaves = false;
                pairsSpawnedInWave = 0;
            }
            return;
        }

        if (pairsSpawnedInWave < 5) {
            if (currentTime - lastPairSpawnTime > pairSpawnDelay) {
                lastPairSpawnTime = currentTime;
                game.addEntity(new EasyPassingAlienEntity(game, 150, -50, EasyPassingAlienEntity.Origin.LEFT));
                game.addEntity(new EasyPassingAlienEntity(game, 650, -50, EasyPassingAlienEntity.Origin.RIGHT));
                pairsSpawnedInWave++;
            }
        } else {
            wavesCompleted++;
            if (wavesCompleted < 2) {
                isPausedBetweenWaves = true;
                wavePauseStartTime = currentTime;
            }
        }
    }
}
