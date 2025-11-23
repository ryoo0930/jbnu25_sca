package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;

public class MidBossEntity extends Entity {
    private final Game game;
    private int health = 900;

    public enum Origin { LEFT, RIGHT }
    private final Origin origin;

    private enum State { ENTERING, ATTACKING, EXITING }
    private State currentState = State.ENTERING;

    private final long stayDuration = 10000; // 10 seconds
    private long stateStartTime;

    // Spiral Attack Pattern
    private long lastSpiralShotTime = 0;
    private final long spiralShotInterval = 100;
    private double spiralAngle = 0;

    // Guided Fan Burst Attack Pattern
    private long lastBurstStartTime = 0;
    private final long shotInBurstInterval = 300;   // Time between each of the 3 fan shots
    private final long burstCooldown = 1000;        // 1-second pause after the burst
    private final long burstCycleDuration = (shotInBurstInterval * (totalShotsInBurst -1)) + burstCooldown; // Total time for one cycle
    private int shotsToFireInBurst = 0;
    private static final int totalShotsInBurst = 3;
    private long lastShotInBurstTime = 0;


    public MidBossEntity(Game game, int x, int y, Origin origin) {
        super("sprites/Boss2.gif", x, y);
        this.game = game;
        this.origin = origin;
        this.stateStartTime = System.currentTimeMillis();

        if (origin == Origin.LEFT) {
            this.dx = 100; // Move right to enter
        } else {
            this.dx = -100; // Move left to enter
        }
    }

    @Override
    public void move(long delta) {
        long currentTime = System.currentTimeMillis();

        // State transition logic
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
                    if (origin == Origin.LEFT) {
                        this.dx = -100; // Move left to exit
                    } else {
                        this.dx = 100; // Move right to exit
                    }
                }
                break;
            case EXITING:
                if (x < -100 || x > 900) {
                    game.getGamePlay().removeEntity(this);
                }
                break;
        }

        super.move(delta);

        if (currentState == State.ATTACKING) {
            handleAttacks(currentTime);
        }
    }

    private void handleAttacks(long currentTime) {
        // --- Continuous Spiral Shot ---
        if (currentTime - lastSpiralShotTime > spiralShotInterval) {
            lastSpiralShotTime = currentTime;
            fireSpiralShot();
        }

        // --- Guided Fan Burst Logic ---
        // Check if it's time to start a new 3-shot burst cycle
        if (shotsToFireInBurst == 0 && currentTime - lastBurstStartTime > burstCycleDuration) {
            shotsToFireInBurst = totalShotsInBurst;
            lastBurstStartTime = currentTime;
        }

        // Fire the shots that are part of the current burst
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
        game.getGamePlay().addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot2.gif", (int)(x + sprite.getWidth()/2), (int)(y + sprite.getHeight()/2), shotDx, shotDy));
        spiralAngle += Math.PI / 6;
    }

    private void fireGuidedFanShot() {
        Entity player = null;
        for (Object entity : game.getGamePlay().getEntities()) {
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
            double spreadAngle = Math.PI / 18; // 10 degrees spread between each shot

            // Create 5 shots in a fan
            for (int i = -2; i <= 2; i++) {
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
            game.getGamePlay().addScore(5000);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity) {
            takeDamage(30);
            game.getGamePlay().removeEntity(other);
        }
    }
}