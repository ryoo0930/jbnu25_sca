package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;

public class EasyMidBossEntity extends Entity {
    private final Game game;
    private int health = 360;

    public enum Origin { LEFT, RIGHT }
    private final Origin origin;

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

        if (origin == Origin.LEFT) this.dx = 80;
        else this.dx = -80;
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

    private long lastBurstTime = 0;
    private final long burstInterval = 2000; // Fire burst every 2 seconds

    @Override
    public void move(long delta) {
        float deltaSeconds = delta / 1000.0f;

        if (currentState == State.ENTERING || currentState == State.EXITING) {
            x += dx * deltaSeconds;
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
                if (currentTime - stateStartTime >= 1500) {
                    currentState = State.ATTACKING;
                    stateStartTime = currentTime;
                }
                break;

            case ATTACKING:
                if (currentTime - stateStartTime >= stayDuration) {
                    currentState = State.EXITING;
                    dx = (origin == Origin.LEFT ? -100 : 100);
                    return;
                }
                fireSpiralShots(currentTime);
                fireBurstShots(currentTime);
                break;

            case EXITING:
                if (x < -100 || x > 900) game.getGamePlay().removeEntity(this);
                break;
        }
    }

    private void fireSpiralShots(long currentTime) {
        if (currentTime - lastShotTime >= shotInterval) {
            lastShotTime = currentTime;
            fireSpiralShot();
        }
    }

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