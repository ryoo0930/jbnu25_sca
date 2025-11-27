package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;

/**
 * Normal 난이도의 Mid Boss 엔티티
 * 좌/우에서 진입한 뒤 일정 시간 동안 스파이럴 탄 + 유도 부채꼴 탄을 사용
 */
public class NormalMidBossEntity extends Entity {
    private final Game game;
    private int health = 720; // 24 hits

    // 어느 쪽에서 등장 했는지 구분
    public enum Origin { LEFT, RIGHT }
    private final Origin origin;

    // 상태 머신 (진입 -> 공격 -> 퇴장)
    private enum State { ENTERING, ATTACKING, EXITING }
    private State currentState = State.ENTERING;

    // 필드에 머무는 시간 (ATTACKING 상태 유지 시간)
    private final long stayDuration = 10000;
    private long stateStartTime;

    // 스파이럴 패턴 관련 타이밍
    private long lastSpiralShotTime = 0;
    private final long spiralShotInterval = 150; // Hard 보다 느리게 조정
    private double spiralAngle = 0;

    // 유도 부채꼴 탄(버스트) 관련 타이밍
    private long lastBurstStartTime = 0;
    private final long shotInBurstInterval = 300;
    private final long burstCooldown = 1000;
    private final long burstCycleDuration = (shotInBurstInterval * 2) + burstCooldown;
    private int shotsToFireInBurst = 0;
    private static final int totalShotsInBurst = 3;
    private long lastShotInBurstTime = 0;

    public NormalMidBossEntity(Game game, int x, int y, Origin origin) {
        super("sprites/Boss2.gif", x, y);
        this.game = game;
        this.origin = origin;
        this.stateStartTime = System.currentTimeMillis();

        // 등장 방향에 따라 초기 이동 방향 설정
        if (origin == Origin.LEFT) this.dx = 100;
        else this.dx = -100;
    }

    // 현재 필드에서 플레이어를 검색
    private ShipEntity findPlayer() {
        if (game.getGamePlay() == null) return null;
        for (Object entity : game.getGamePlay().getEntities()) {
            if (entity instanceof ShipEntity) {
                return (ShipEntity) entity;
            }
        }
        return null;
    }

    // 상태에 따라 이동 및 패턴을 업데이트 한다.
    @Override
    public void move(long delta) {
        float deltaSeconds = delta / 1000.0f;

        // 진입/ 퇴장 상태일 때만 좌우 이동
        if (currentState == State.ENTERING || currentState == State.EXITING) {
            x += (dx * deltaSeconds);
        }

        // 화면 경계에서 멈추도록 제한
        if (x <= 50) {
            x = 50;
            dx = 0;
        } else if (x >= game.getWidth() - 50 - sprite.getWidth()) {
            x = game.getWidth() - 50 - sprite.getWidth();
            dx = 0;
        }

        long currentTime = System.currentTimeMillis();
        switch (currentState) {
            case ENTERING:
                handleEnteringState(currentTime);
                break;
            case ATTACKING:
                handleAttackingState(currentTime);
                break;
            case EXITING:
                handleExitingState(currentTime);
                break;
        }
    }

    // ENTERING 상태 처리 : 일정 시간 후 ATTACKING으로 전환 및 타이머 초기화
    private void handleEnteringState(long currentTime) {
        if (currentTime - stateStartTime >= 2000) {
            currentState = State.ATTACKING;
            stateStartTime = currentTime;
            lastSpiralShotTime = currentTime;
            lastBurstStartTime = currentTime;
            lastShotInBurstTime = currentTime;
            shotsToFireInBurst = totalShotsInBurst;
            spiralAngle = 0;
        }
    }

    // ATTACKING 상태 처리 : 스파이럴 + 버스트 패턴 실행, 일정 시간 경과시 퇴장
    private void handleAttackingState(long currentTime) {
        long elapsedTime = currentTime - stateStartTime;

        if (elapsedTime >= stayDuration) {
            currentState = State.EXITING;
            dx = (origin == Origin.LEFT) ? -100 : 100;
            return;
        }

        fireSpiralShots(currentTime);
        fireBurstShots(currentTime);
    }

    // EXITING 상태 처리 : 화면 밖으로 나가면 엔티티 제거
    private void handleExitingState(long currentTime) {
        if ((origin == Origin.LEFT && x + sprite.getWidth() < 0) || (origin == Origin.RIGHT && x > game.getWidth())) {
             if(game.getGamePlay() != null) game.getGamePlay().removeEntity(this);
        }
    }

    // 일정 간격으로 스파이럴 탄을 한 발씩 발사
    private void fireSpiralShots(long currentTime) {
        if (currentTime - lastSpiralShotTime > spiralShotInterval) {
            fireSpiralShot();
            lastSpiralShotTime = currentTime;
        }
    }

    // 버스트 (짧은 유도 부채꼴 연사) 패턴 관리
    private void fireBurstShots(long currentTime) {
        if (shotsToFireInBurst > 0) {
            if (currentTime - lastShotInBurstTime > shotInBurstInterval) {
                fireGuidedFanShot();
                shotsToFireInBurst--;
                lastShotInBurstTime = currentTime;
            }
        } else {
            if (currentTime - lastBurstStartTime > burstCycleDuration) {
                shotsToFireInBurst = totalShotsInBurst;
                lastBurstStartTime = currentTime;
            }
        }
    }

    // 스파이렅 탄막 1발 발사
    private void fireSpiralShot() {
        double speed = 200;
        double shotDx = Math.cos(spiralAngle) * speed;
        double shotDy = Math.sin(spiralAngle) * speed;
        game.getGamePlay().addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot2.gif", (int)(x + sprite.getWidth()/2), (int)(y + sprite.getHeight()/2), shotDx, shotDy));
        spiralAngle += Math.PI / 6;
    }

    // 플레이어 방향을 기준으로 3발 부채꼴 유도탄 발사
    private void fireGuidedFanShot() {
        ShipEntity player = findPlayer();
        if (player != null) {
            double targetDx = player.getX() - this.x;
            double targetDy = player.getY() - this.y;
            double centerAngle = Math.atan2(targetDy, targetDx);
            double speed = 300;
            double spreadAngle = Math.PI / 18;

            for (int i = -1; i <= 1; i++) { // 3 shots
                double angle = centerAngle + (i * spreadAngle);
                double shotDx = Math.cos(angle) * speed;
                double shotDy = Math.sin(angle) * speed;
                game.getGamePlay().addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot3.gif", (int)(x + sprite.getWidth()/2), (int)(y + sprite.getHeight()/2), shotDx, shotDy));
            }
        }
    }

    // Normal Mid Boss가 피해를 받았을 때 체력을 감소시키고 체력이 0 이하가 되면 제거 및 점수를 부여
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.getGamePlay().removeEntity(this);
            game.getGamePlay().addScore(4000);
        }
    }

    // 플레이어 탄과 충돌했을 때 데미지를 적용한다.
    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity) {
            Entity owner = ((ShotEntity) other).getOwner();
            if (owner instanceof ShipEntity) {
                takeDamage(30);
                game.getGamePlay().removeEntity(other);
            }
        }
    }
}