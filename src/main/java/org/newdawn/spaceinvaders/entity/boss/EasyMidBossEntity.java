package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;

/**
 * Easy 난이도의 중간보스를 나타내는 엔티티
 * 좌/우에서 진입한 뒤 일정 시간 동안 패턴 공격을 수행하고 퇴장한다.
 */
public class EasyMidBossEntity extends Entity {
    private final Game game;
    private int health = 360;

    // 어느 쪽에서 등장했는지 구분
    public enum Origin { LEFT, RIGHT }
    private final Origin origin;

    // 단순 상태 머신 : 진입 후 공격 한 후 퇴장
    private enum State { ENTERING, ATTACKING, EXITING }
    private State currentState = State.ENTERING;

    private final long stayDuration = 7000;
    private long stateStartTime;

    private long lastShotTime = 0;
    private final long shotInterval = 700;
    
    private double spiralAngle = 0;

    public EasyMidBossEntity(Game game, int x, int y, Origin origin) {
        super("sprites/Boss1.gif", x, y);
        this.game = game;
        this.origin = origin;
        this.stateStartTime = System.currentTimeMillis();

        // 등장 방향에 따라 초기 이동 방향 설정
        if (origin == Origin.LEFT) this.dx = 80;
        else this.dx = -80;
    }

    // 플레이어를 검색하는 유틸리티 메서드
    private ShipEntity findPlayer() {
        if (game.getGamePlay() == null) return null;
        for (Object entity : game.getGamePlay().getEntities()) {
            if (entity instanceof ShipEntity) {
                return (ShipEntity) entity;
            }
        }
        return null;
    }

    private long lastBurstTime = 0;
    private final long burstInterval = 2000; // Fire burst every 2 seconds

    @Override
    public void move(long delta) {
        float deltaSeconds = delta / 1000.0f;

        // 진입/퇴장 상태일 때만 좌우 이동
        if (currentState == State.ENTERING || currentState == State.EXITING) {
            x += dx * deltaSeconds;
        }

        // 화면 좌우에서 멈추도록 제한
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
                // 일정 시간 지나면 공격 상태로 전환
                if (currentTime - stateStartTime >= 1500) {
                    currentState = State.ATTACKING;
                    stateStartTime = currentTime;
                }
                break;

            case ATTACKING:
                // 정해진 시간 동안만 필드에 머무른 뒤 퇴장
                if (currentTime - stateStartTime >= stayDuration) {
                    currentState = State.EXITING;
                    dx = (origin == Origin.LEFT ? -100 : 100);
                    return;
                }
                fireSpiralShots(currentTime);
                fireBurstShots(currentTime);
                break;

            case EXITING:
                // 화면 밖으로 나가면 엔티티 제거
                if (x < -100 || x > 900) game.getGamePlay().removeEntity(this);
                break;
        }
    }

    // 일정 간격으로 스파이럴 탄막 발사
    private void fireSpiralShots(long currentTime) {
        if (currentTime - lastShotTime >= shotInterval) {
            lastShotTime = currentTime;
            fireSpiralShot();
        }
    }

    // 일정 간격으로 플레이어를 향한 유도탄 발사
    private void fireBurstShots(long currentTime) {
        if (currentTime - lastBurstTime >= burstInterval) {
            lastBurstTime = currentTime;
            fireGuidedFanShot();
        }
    }

    private void fireSpiralShot() {
        double speed = 200;
        double shotDx = Math.cos(spiralAngle) * speed;
        double shotDy = Math.sin(spiralAngle) * speed;
        game.getGamePlay().addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot2.gif", (int)(x + sprite.getWidth()/2), (int)(y + sprite.getHeight()/2), shotDx, shotDy));
        spiralAngle += Math.PI / 6;
    }

    private void fireGuidedFanShot() {
        ShipEntity player = findPlayer();
        if (player != null) {
            double targetDx = player.getX() - this.x;
            double targetDy = player.getY() - this.y;
            double centerAngle = Math.atan2(targetDy, targetDx);
            double speed = 300;

            // Fire a single shot
            double shotDx = Math.cos(centerAngle) * speed;
            double shotDy = Math.sin(centerAngle) * speed;
            game.getGamePlay().addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot3.gif", (int)(x + sprite.getWidth()/2), (int)(y + sprite.getHeight()/2), shotDx, shotDy));
        }
    }

    // 중간 보스가 피해를 받았을 때 처리 로직
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.getGamePlay().removeEntity(this);
            game.getGamePlay().addScore(2500);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity) {
            Entity owner = ((ShotEntity) other).getOwner();
            if(owner instanceof ShipEntity){
                takeDamage(30);
                game.getGamePlay().removeEntity(other);
            }
        }
    }
}