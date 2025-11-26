package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.entity.EasyPassingAlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.HardPassingAlienEntity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.boss.EasyBossEntity;
import org.newdawn.spaceinvaders.entity.boss.EasyMidBossEntity;
import org.newdawn.spaceinvaders.entity.AlienFactory;

public class EasyStage extends AbstractStage {

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
                if (!isEntityOnScreen(gamePlay, HardPassingAlienEntity.class) && !isEntityOnScreen(gamePlay, EasyPassingAlienEntity.class)) {
                    phaseTransitionTime = currentTime;
                    currentPhase = StagePhase.PAUSE_BEFORE_MID_BOSS;
                }
                break;
            case PAUSE_BEFORE_MID_BOSS:
                if (currentTime - phaseTransitionTime > 2000) {
                    gamePlay.addEntity(new EasyMidBossEntity(gamePlay.getGame(), 850, 100, EasyMidBossEntity.Origin.RIGHT));
                    currentPhase = StagePhase.MID_BOSS_RIGHT;
                }
                break;
            case MID_BOSS_RIGHT:
                if (!isEntityOnScreen(gamePlay, EasyMidBossEntity.class)) {
                    phaseTransitionTime = currentTime;
                    currentPhase = StagePhase.PAUSE_BETWEEN_MID_BOSSES;
                }
                break;
            case PAUSE_BETWEEN_MID_BOSSES:
                if (currentTime - phaseTransitionTime > 2000) {
                    gamePlay.addEntity(new EasyMidBossEntity(gamePlay.getGame(), -100, 100, EasyMidBossEntity.Origin.LEFT));
                    currentPhase = StagePhase.MID_BOSS_LEFT;
                }
                break;
            case MID_BOSS_LEFT:
                if (!isEntityOnScreen(gamePlay, EasyMidBossEntity.class)) {
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
                    gamePlay.addEntity(new EasyBossEntity(gamePlay.getGame(), 350, 50));
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

                Entity left = AlienFactory.createPassingAlien(
                        Difficulty.EASY,
                        gamePlay.getGame(),
                        150,
                        -50,
                        true   // 왼쪽에서 등장
                );
                gamePlay.addEntity(left);

                Entity right = AlienFactory.createPassingAlien(
                        Difficulty.EASY,
                        gamePlay.getGame(),
                        650,
                        -50,
                        false  // 오른쪽에서 등장
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
