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
    
    private ShipEntity findPlayer() {
        if (game.getGamePlay() == null) return null;
        for (Object entity : game.getGamePlay().getEntities()) {
            if (entity instanceof ShipEntity) {
                return (ShipEntity) entity;
            }
        }
        return null;
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
        if ((origin == Origin.LEFT && x + sprite.getWidth() < 0) || (origin == Origin.RIGHT && x > game.getWidth())) {
             if(game.getGamePlay() != null) game.getGamePlay().removeEntity(this);
        }
    }
    
    private void fireSpiralShots(long currentTime) {
        if (currentTime - lastSpiralShotTime > spiralShotInterval) {
            fireSpiralShot();
            lastSpiralShotTime = currentTime;
        }
    }

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
            double spreadAngle = Math.PI / 18;

            for (int i = -1; i <= 1; i++) { // 3 shots
                double angle = centerAngle + (i * spreadAngle);
                double shotDx = Math.cos(angle) * speed;
                double shotDy = Math.sin(angle) * speed;
                game.getGamePlay().addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot3.gif", (int)(x + sprite.getWidth()/2), (int)(y + sprite.getHeight()/2), shotDx, shotDy));
            }
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.getGamePlay().removeEntity(this);
            game.getGamePlay().addScore(4000);
        }
    }

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