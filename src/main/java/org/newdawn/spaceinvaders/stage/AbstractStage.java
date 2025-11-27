package org.newdawn.spaceinvaders.stage;

public abstract class AbstractStage implements Stage {
    // Stage 인터페이스의 메서드(update, initEntities, getAlienCount)는
    // 하위 클래스에서 그대로 구현하게 두면 됨.

    // stage별 공통 플래그
    protected boolean finalBossSpawned = false;

    // stage별 공통 페이즈 enum
    protected enum StagePhase {
        ALIEN_WAVES,
        WAIT_FOR_ALIENS_TO_CLEAR,
        PAUSE_BEFORE_MID_BOSS,
        MID_BOSS_RIGHT,
        PAUSE_BETWEEN_MID_BOSSES,
        MID_BOSS_LEFT,
        FINAL_BOSS
    }

    // stage별 공통 상태 필드
    protected StagePhase currentPhase;

    protected int wavesCompleted = 0;
    protected int pairsSpawnedInWave = 0;
    protected long lastPairSpawnTime = 0;
    protected long pairSpawnDelay = 400;
    protected long wavePauseStartTime = 0;
    protected long wavePauseDuration = 4000;
    protected boolean isPausedBetweenWaves = false;
    protected long phaseTransitionTime = 0;

}
