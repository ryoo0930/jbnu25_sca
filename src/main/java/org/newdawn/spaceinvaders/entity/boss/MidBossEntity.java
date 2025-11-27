package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;

/**
 * 스테이지 중간에서 등장하는 Mid Boss 엔티티
 * 좌/우 에서 진입한 뒤 일정 시간 동안 플레이러르  향해 공격한 후 퇴장한다.
 */
public class MidBossEntity extends Entity {
    private final Game game;
    private int health = 720;

    // 어느 쪽에서 등장했는지 구분
    public enum Origin { LEFT, RIGHT }
    private final Origin origin;

    // 간단한 상태 머신 : 진입 -> 공격 -> 퇴장
    private enum State { ENTERING, ATTACKING, EXITING }
    private State currentState = State.ENTERING;

    // 필드에 머무르는 시간 (ATTACKING 상태 유지 시간)
    private final long stayDuration = 10000;
    private long stateStartTime;

    // 연사 간격 관리
    private long lastShotTime = 0;
    private final long shotInterval = 500;
    
    private double spiralAngle = 0;

    public MidBossEntity(Game game, int x, int y, Origin origin) {
        super("sprites/Boss2.gif", x, y);
        this.game = game;
        this.origin = origin;
        this.stateStartTime = System.currentTimeMillis();

        // 등장 방향에 따라 초기 이동 방향 설정
        if (origin == Origin.LEFT) this.dx = 100;
        else this.dx = -100;
    }

    // 현재 필드에서 플레이어를 찾아 반환
    private ShipEntity findPlayer() {
        if (game.getGamePlay() == null) return null;
        for (Object entity : game.getGamePlay().getEntities()) {
            if (entity instanceof ShipEntity) {
                return (ShipEntity) entity;
            }
        }
        return null;
    }

    /**
     * Mid Boss의 상태에 따라 이동과 공격을 처리한다.
     */
    @Override
    public void move(long delta) {
        float deltaSeconds = delta / 1000.0f;

        // 진입/ 퇴장 상태일 때만 좌우 이동
        if (currentState == State.ENTERING || currentState == State.EXITING) {
            x += dx * deltaSeconds;
        }

        // 화면 좌우 경계에서 멈추도록 제한
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
                // 일정 시간 진입 후 ATTACKING 상태로 전환
                if (currentTime - stateStartTime >= 2000) {
                    currentState = State.ATTACKING;
                    stateStartTime = currentTime;
                }
                break;

            case ATTACKING:
                // 공격 유지 시간이 끝나면 퇴장 상태로 전환
                if (currentTime - stateStartTime >= stayDuration) {
                    currentState = State.EXITING;
                    dx = (origin == Origin.LEFT ? -100 : 100);
                    return;
                }
                fireShots(currentTime);
                break;

            case EXITING:
                // 화면 밖으로 나가면 엔티티 제거
                if (x < -100 || x > 900) {
                    game.getGamePlay().removeEntity(this);
                }
                break;
        }
    }

    // 일정 간격으로 플레이어를 향해 직선 탄을 발사한다.
    private void fireShots(long currentTime) {
        if (currentTime - lastShotTime >= shotInterval) {
            lastShotTime = currentTime;

            ShipEntity player = findPlayer();
            if (player == null) return;

            double dxToPlayer = player.getX() - (x + sprite.getWidth() / 2.0);
            double dyToPlayer = player.getY() - (y + sprite.getHeight() / 2.0);
            double distance = Math.sqrt(dxToPlayer * dxToPlayer + dyToPlayer * dyToPlayer);
            if (distance == 0) distance = 1;

            double speed = 200;
            double vx = (dxToPlayer / distance) * speed;
            double vy = (dyToPlayer / distance) * speed;

            game.getGamePlay().addEntity(new ShotEntity(game, (int)x + sprite.getWidth()/2, (int)y + sprite.getHeight()/2, vx, vy, this));
        }
    }

    // 사용하지 않는 패턴 예시 (스파이럴 탄막)
    private void fireSpiralShot() {
        double speed = 200;
        double shotDx = Math.cos(spiralAngle) * speed;
        double shotDy = Math.sin(spiralAngle) * speed;
        game.getGamePlay().addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot2.gif", (int)(x + sprite.getWidth()/2), (int)(y + sprite.getHeight()/2), shotDx, shotDy));
        spiralAngle += Math.PI / 6;
    }

    // 사용하지 않는 패턴 예시 (부채꼴 유도탄)
    private void fireGuidedFanShot() {
        ShipEntity player = findPlayer();

        if (player != null) {
            double targetDx = player.getX() - this.x;
            double targetDy = player.getY() - this.y;
            double centerAngle = Math.atan2(targetDy, targetDx);
            double speed = 300;
            double spreadAngle = Math.PI / 18; // 약 10도 간격

            // 중심 각도를 기준으로 5발 부채꼴 발사
            for (int i = -2; i <= 2; i++) {
                double angle = centerAngle + (i * spreadAngle);
                double shotDx = Math.cos(angle) * speed;
                double shotDy = Math.sin(angle) * speed;
                game.getGamePlay().addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot3.gif", (int)(x + sprite.getWidth()/2), (int)(y + sprite.getHeight()/2), shotDx, shotDy));
            }
        }
    }

    // Mid Boss가 피해를 받았을 때 체력을 감소시키고, 체력이 0 이하가 되면 제거 및 점수 부여를 수행
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.getGamePlay().removeEntity(this);
            game.getGamePlay().addScore(5000);
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