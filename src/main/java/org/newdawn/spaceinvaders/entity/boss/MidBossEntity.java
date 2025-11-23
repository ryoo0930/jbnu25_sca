package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

public class MidBossEntity extends Entity {
    private final Game game;
    private int health = 720;

    public enum Origin { LEFT, RIGHT }
    private final Origin origin;

    private enum State { ENTERING, ATTACKING, EXITING }
    private State currentState = State.ENTERING;

    private final long stayDuration = 10000;
    private long stateStartTime;

    private long lastShotTime = 0;
    private final long shotInterval = 500;

    public MidBossEntity(Game game, int x, int y, Origin origin) {
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
                if (currentTime - stateStartTime >= 2000) {
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
                fireShots(currentTime);
                break;

            case EXITING:
                if (x < -100 || x > 900) {
                    game.getGamePlay().removeEntity(this);
                }
                break;
        }
    }

    // 발사 패턴 (원래 기능 유지)
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

            game.addEntity(new ShotEntity(game, (int)x + sprite.getWidth()/2, (int)y + sprite.getHeight()/2, vx, vy));
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
        return null;
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
