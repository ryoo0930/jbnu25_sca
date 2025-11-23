package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;

public class NormalMidBossEntity extends Entity {
    private final Game game;
    private int health = 720; // 24 hits

    public enum Origin { LEFT, RIGHT }
    private final Origin origin;

    private enum State { ENTERING, ATTACKING, EXITING }
    private State currentState = State.ENTERING;

    private final long stayDuration = 10000;
    private long stateStartTime;

    private long lastSpiralShotTime = 0;
    private final long spiralShotInterval = 150; // Slower
    private double spiralAngle = 0;

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

        if (origin == Origin.LEFT) this.dx = 100;
        else this.dx = -100;
    }

    @Override
    public void move(long delta) {
        float deltaSeconds = delta / 1000.0f;

        if (currentState == State.ENTERING || currentState == State.EXITING) {
            x += (dx * deltaSeconds);
        }

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

    private void handleExitingState(long currentTime) {
        if (origin == Origin.LEFT && x + sprite.getWidth() < 0) {
            game.removeEntity(this);
        }
        if (origin == Origin.RIGHT && x > game.getWidth()) {
            game.removeEntity(this);
        }
    }

    private void fireSpiralShots(long currentTime) {
        if (currentTime - lastSpiralShotTime >= spiralShotInterval) {
            lastSpiralShotTime = currentTime;

            // ★ game.getShip() 대신 내부 helper 사용
            ShipEntity player = findPlayer();
            if (player == null) return;

            double centerX = x + sprite.getWidth() / 2.0;
            double centerY = y + sprite.getHeight() / 2.0;

            double angleToPlayer = Math.atan2(player.getY() - centerY, player.getX() - centerX);

            double spiralSpeed = 250;
            double spiralAngleStep = Math.toRadians(15);

            double angle1 = angleToPlayer + spiralAngle;
            double angle2 = angleToPlayer - spiralAngle;

            double shotDx1 = Math.cos(angle1) * spiralSpeed;
            double shotDy1 = Math.sin(angle1) * spiralSpeed;
            double shotDx2 = Math.cos(angle2) * spiralSpeed;
            double shotDy2 = Math.sin(angle2) * spiralSpeed;

            // ★ GuidedBossShotEntity 생성자에 sprite 인자 추가
            game.addEntity(new GuidedBossShotEntity(
                    game, "sprites/...", (int) centerX, (int) centerY, shotDx1, shotDy1));
            game.addEntity(new GuidedBossShotEntity(
                    game, "sprites/...", (int) centerX, (int) centerY, shotDx2, shotDy2));

            spiralAngle += spiralAngleStep;
            if (spiralAngle >= Math.PI) {
                spiralAngle -= Math.PI;
            }
        }
    }

    private void fireBurstShots(long currentTime) {
        long timeSinceBurstStart = currentTime - lastBurstStartTime;

        if (timeSinceBurstStart >= burstCycleDuration) {
            lastBurstStartTime = currentTime;
            shotsToFireInBurst = totalShotsInBurst;
            lastShotInBurstTime = currentTime;
            timeSinceBurstStart = 0;
        }

        if (shotsToFireInBurst > 0 && (currentTime - lastShotInBurstTime) >= shotInBurstInterval) {
            lastShotInBurstTime = currentTime;

            // ★ game.getShip() → findPlayer()
            ShipEntity player = findPlayer();
            if (player == null) return;

            double centerX = x + sprite.getWidth() / 2.0;
            double centerY = y + sprite.getHeight() / 2.0;

            double dxToPlayer = player.getX() - centerX;
            double dyToPlayer = player.getY() - centerY;
            double distance = Math.sqrt(dxToPlayer * dxToPlayer + dyToPlayer * dyToPlayer);
            if (distance == 0) distance = 1;

            double baseSpeed = 300;
            double baseDx = (dxToPlayer / distance) * baseSpeed;
            double baseDy = (dyToPlayer / distance) * baseSpeed;

            double spreadAngle = Math.toRadians(10);

            for (int i = -1; i <= 1; i++) {
                double angle = Math.atan2(baseDy, baseDx) + (i * spreadAngle);
                double shotDx = Math.cos(angle) * baseSpeed;
                double shotDy = Math.sin(angle) * baseSpeed;

                // ★ 여기서도 sprite 인자 추가
                game.addEntity(new GuidedBossShotEntity(
                        game, "sprites/...", (int) centerX, (int) centerY, shotDx, shotDy));
            }

            shotsToFireInBurst--;
        }
    }
    private ShipEntity findPlayer() {
        for (Object entity : game.getEntities()) {
            if (entity instanceof ShipEntity) {
                return (ShipEntity) entity;
            }
        }
        return null;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.removeEntity(this);
            game.addScore(4000);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity) {
            takeDamage(30);
            game.removeEntity(other);
        }
    }
}
