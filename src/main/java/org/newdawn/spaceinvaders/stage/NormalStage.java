package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.NormalPassingAlienEntity;
import org.newdawn.spaceinvaders.entity.HardPassingAlienEntity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.boss.NormalBossEntity;
import org.newdawn.spaceinvaders.entity.boss.NormalMidBossEntity;
import org.newdawn.spaceinvaders.entity.AlienFactory;

public class NormalStage extends AbstractStage {

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

        switch (currentPhase) {
            case ALIEN_WAVES:
                updateAlienWaves(gamePlay, currentTime);
                if (wavesCompleted >= 2) {
                    currentPhase = StagePhase.WAIT_FOR_ALIENS_TO_CLEAR;
                }
                break;
            case WAIT_FOR_ALIENS_TO_CLEAR:
                if (!isEntityOnScreen(gamePlay, HardPassingAlienEntity.class) && !isEntityOnScreen(gamePlay, NormalPassingAlienEntity.class)) {
                    phaseTransitionTime = currentTime;
                    currentPhase = StagePhase.PAUSE_BEFORE_MID_BOSS;
                }
                break;
            case PAUSE_BEFORE_MID_BOSS:
                if (currentTime - phaseTransitionTime > 2000) {
                    gamePlay.addEntity(new NormalMidBossEntity(gamePlay.getGame(), 850, 100, NormalMidBossEntity.Origin.RIGHT));
                    currentPhase = StagePhase.MID_BOSS_RIGHT;
                }
                break;
            case MID_BOSS_RIGHT:
                if (!isEntityOnScreen(gamePlay, NormalMidBossEntity.class)) {
                    phaseTransitionTime = currentTime;
                    currentPhase = StagePhase.PAUSE_BETWEEN_MID_BOSSES;
                }
                break;
            case PAUSE_BETWEEN_MID_BOSSES:
                if (currentTime - phaseTransitionTime > 2000) {
                    gamePlay.addEntity(new NormalMidBossEntity(gamePlay.getGame(), -100, 100, NormalMidBossEntity.Origin.LEFT));
                    currentPhase = StagePhase.MID_BOSS_LEFT;
                }
                break;
            case MID_BOSS_LEFT:
                if (!isEntityOnScreen(gamePlay, NormalMidBossEntity.class)) {
                    currentPhase = StagePhase.FINAL_BOSS;
                }
                break;
            case FINAL_BOSS:
                if (!finalBossSpawned) {
                    java.util.List entities = gamePlay.getEntities();
                    for (int i = entities.size() - 1; i >= 0; i--) {
                        Object entity = entities.get(i);
                        if (!(entity instanceof ShipEntity)) {
                            gamePlay.removeEntity((Entity) entity);
                        }
                    }
                    gamePlay.addEntity(new NormalBossEntity(gamePlay.getGame(), 350, 50));
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
//                lastPairSpawnTime = currentTime;
//                gamePlay.addEntity(new NormalPassingAlienEntity(gamePlay.getGame(), 150, -50, NormalPassingAlienEntity.Origin.LEFT));
//                gamePlay.addEntity(new NormalPassingAlienEntity(gamePlay.getGame(), 650, -50, NormalPassingAlienEntity.Origin.RIGHT));
//                pairsSpawnedInWave++;
                lastPairSpawnTime = currentTime;

                Entity left = AlienFactory.createPassingAlien(
                        Difficulty.NORMAL,
                        gamePlay.getGame(),
                        150,
                        -50,
                        true
                );
                gamePlay.addEntity(left);

                Entity right = AlienFactory.createPassingAlien(
                        Difficulty.NORMAL,
                        gamePlay.getGame(),
                        650,
                        -50,
                        false
                );
                gamePlay.addEntity(right);

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
