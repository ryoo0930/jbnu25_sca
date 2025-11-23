package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.HardPassingAlienEntity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.boss.HardBossEntity;
import org.newdawn.spaceinvaders.entity.boss.MidBossEntity;

import java.util.ArrayList;

public class HardStage implements Stage {
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

    // Alien wave management
    private int wavesCompleted = 0;
    private int pairsSpawnedInWave = 0;
    private long lastPairSpawnTime = 0;
    private final long pairSpawnDelay = 400;
    private long wavePauseStartTime = 0;
    private final long wavePauseDuration = 4000;
    private boolean isPausedBetweenWaves = false;

    // Phase timer
    private long phaseTransitionTime = 0;

    @Override
    public void initEntities(GamePlay gamePlay) {
        finalBossSpawned = false;
        currentPhase = StagePhase.ALIEN_WAVES;

        wavesCompleted = 0;
        pairsSpawnedInWave = 0;
        isPausedBetweenWaves = false;
        lastPairSpawnTime = 0;
    }

    @Override
    public int getAlienCount() { return 0; }

    private boolean isEntityOnScreen(GamePlay gamePlay, Class<? extends Entity> entityType) {
        for (Object entity : gamePlay.getEntities()) {
            if (entityType.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void update(GamePlay gamePlay) {
        long currentTime = System.currentTimeMillis();

        // --- Phase Logic ---
        switch (currentPhase) {
            case ALIEN_WAVES:
                updateAlienWaves(gamePlay, currentTime);
                if (wavesCompleted >= 2) {
                    currentPhase = StagePhase.WAIT_FOR_ALIENS_TO_CLEAR;
                }
                break;

            case WAIT_FOR_ALIENS_TO_CLEAR:
                if (!isEntityOnScreen(gamePlay, HardPassingAlienEntity.class)) {
                    phaseTransitionTime = currentTime;
                    currentPhase = StagePhase.PAUSE_BEFORE_MID_BOSS;
                }
                break;

            case PAUSE_BEFORE_MID_BOSS:
                if (currentTime - phaseTransitionTime > 2000) { // 2-second pause
                    gamePlay.addEntity(new MidBossEntity(gamePlay.getGame(), 850, 100, MidBossEntity.Origin.RIGHT));
                    currentPhase = StagePhase.MID_BOSS_RIGHT;
                }
                break;

            case MID_BOSS_RIGHT:
                if (!isEntityOnScreen(gamePlay, MidBossEntity.class)) {
                    phaseTransitionTime = currentTime;
                    currentPhase = StagePhase.PAUSE_BETWEEN_MID_BOSSES;
                }
                break;

            case PAUSE_BETWEEN_MID_BOSSES:
                if (currentTime - phaseTransitionTime > 2000) { // 2-second pause
                    gamePlay.addEntity(new MidBossEntity(gamePlay.getGame(), -100, 100, MidBossEntity.Origin.LEFT));
                    currentPhase = StagePhase.MID_BOSS_LEFT;
                }
                break;

            case MID_BOSS_LEFT:
                if (!isEntityOnScreen(gamePlay, MidBossEntity.class)) {
                    // Transition directly to the final boss
                    currentPhase = StagePhase.FINAL_BOSS;
                }
                break;

            case FINAL_BOSS:
                if (!finalBossSpawned) {
                    // Clear any remaining projectiles before the final boss
                    java.util.List entities = gamePlay.getEntities();
                    for (int i = entities.size() - 1; i >= 0; i--) {
                        Object entity = entities.get(i);
                        if (!(entity instanceof ShipEntity)) {
                            gamePlay.removeEntity((Entity) entity);
                        }
                    }
                    gamePlay.addEntity(new HardBossEntity(gamePlay.getGame(), 350, 50));
                    finalBossSpawned = true;
                }
                break;
        }
    }

    private void updateAlienWaves(GamePlay gamePlay, long currentTime) {
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
                gamePlay.addEntity(new HardPassingAlienEntity(gamePlay.getGame(), 150, -50, HardPassingAlienEntity.Origin.LEFT));
                gamePlay.addEntity(new HardPassingAlienEntity(gamePlay.getGame(), 650, -50, HardPassingAlienEntity.Origin.RIGHT));
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
