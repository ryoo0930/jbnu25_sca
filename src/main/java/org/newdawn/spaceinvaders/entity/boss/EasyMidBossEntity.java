package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;

public class EasyMidBossEntity extends Entity {
    private final Game game;
    private int health = 450; // 15 hits

    public enum Origin { LEFT, RIGHT }
    private final Origin origin;

    private enum State { ENTERING, ATTACKING, EXITING }
    private State currentState = State.ENTERING;

    private final long stayDuration = 10000;
    private long stateStartTime;

    private long lastSpiralShotTime = 0;
    private final long spiralShotInterval = 200; // Slower
    private double spiralAngle = 0;

    private long lastBurstStartTime = 0;
    private final long shotInBurstInterval = 300;
    private final long burstCooldown = 1000;
    private final long burstCycleDuration = (shotInBurstInterval * 2) + burstCooldown;
    private int shotsToFireInBurst = 0;
    private static final int totalShotsInBurst = 3;
    private long lastShotInBurstTime = 0;

    public EasyMidBossEntity(Game game, int x, int y, Origin origin) {
        super("sprites/Boss2.gif", x, y);
        this.game = game;
        this.origin = origin;
        this.stateStartTime = System.currentTimeMillis();

        if (origin == Origin.LEFT) this.dx = 100;
        else this.dx = -100;
    }

    @Override
    public void move(long delta) {
        long currentTime = System.currentTimeMillis();
        switch (currentState) {
            case ENTERING:
                if ((origin == Origin.LEFT && x >= 100) || (origin == Origin.RIGHT && x <= 600)) {
                    this.dx = 0;
                    currentState = State.ATTACKING;
                    stateStartTime = currentTime;
                }
                break;
            case ATTACKING:
                if (currentTime - stateStartTime > stayDuration) {
                    currentState = State.EXITING;
                    if (origin == Origin.LEFT) this.dx = -100;
                    else this.dx = 100;
                }
                break;
            case EXITING:
                if (x < -100 || x > 900) game.removeEntity(this);
                break;
        }
        super.move(delta);
        if (currentState == State.ATTACKING) handleAttacks(currentTime);
    }

    private void handleAttacks(long currentTime) {
        if (currentTime - lastSpiralShotTime > spiralShotInterval) {
            lastSpiralShotTime = currentTime;
            fireSpiralShot();
        }

        if (shotsToFireInBurst == 0 && currentTime - lastBurstStartTime > burstCycleDuration) {
            shotsToFireInBurst = totalShotsInBurst;
            lastBurstStartTime = currentTime;
        }

        if (shotsToFireInBurst > 0 && currentTime - lastShotInBurstTime > shotInBurstInterval) {
            lastShotInBurstTime = currentTime;
            fireGuidedFanShot();
            shotsToFireInBurst--;
        }
    }

    private void fireSpiralShot() {
        double speed = 200;
        double shotDx = Math.cos(spiralAngle) * speed;
        double shotDy = Math.sin(spiralAngle) * speed;
        game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot2.gif", (int)(x + sprite.getWidth()/2), (int)(y + sprite.getHeight()/2), shotDx, shotDy));
        spiralAngle += Math.PI / 6;
    }

    private void fireGuidedFanShot() {
        Entity player = null;
        for (Object entity : game.getEntities()) {
            if (entity instanceof ShipEntity) {
                player = (Entity) entity;
                break;
            }
        }
        if (player != null) {
            double targetDx = player.getX() - this.x;
            double targetDy = player.getY() - this.y;
            double centerAngle = Math.atan2(targetDy, targetDx);
            double speed = 300;

            // Fire a single shot
            double shotDx = Math.cos(centerAngle) * speed;
            double shotDy = Math.sin(centerAngle) * speed;
            game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot3.gif", (int)(x + sprite.getWidth()/2), (int)(y + sprite.getHeight()/2), shotDx, shotDy));
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.removeEntity(this);
            game.addScore(2500);
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
