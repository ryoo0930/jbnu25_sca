package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

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

    public EasyMidBossEntity(Game game, int x, int y, Origin origin) {
        super("sprites/Boss1.gif", x, y);
        this.game = game;
        this.origin = origin;
        this.stateStartTime = System.currentTimeMillis();

        if (origin == Origin.LEFT) this.dx = 80;
        else this.dx = -80;
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
                fireShots(currentTime);
                break;

            case EXITING:
                if (origin == Origin.LEFT && x + sprite.getWidth() < 0) {
                    game.removeEntity(this);
                }
                if (origin == Origin.RIGHT && x > game.getWidth()) {
                    game.removeEntity(this);
                }
                break;
        }
    }

    private void fireShots(long currentTime) {
        if (currentTime - lastShotTime >= shotInterval) {
            lastShotTime = currentTime;

            ShipEntity player = findPlayer();
            if (player == null) return;

            double dxToPlayer = player.getX() - (x + sprite.getWidth() / 2.0);
            double dyToPlayer = player.getY() - (y + sprite.getHeight() / 2.0);
            double distance = Math.sqrt(dxToPlayer * dxToPlayer + dyToPlayer * dyToPlayer);
            if (distance == 0) distance = 1;

            double speed = 180;
            double vx = (dxToPlayer / distance) * speed;
            double vy = (dyToPlayer / distance) * speed;

            game.addEntity(new ShotEntity(game, (int)x + sprite.getWidth()/2, (int)y + sprite.getHeight()/2, vx, vy));
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
            game.addScore(2000);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity) {
            takeDamage(20);
            game.removeEntity(other);
        }
    }
}
