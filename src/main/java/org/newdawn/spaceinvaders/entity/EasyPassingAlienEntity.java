package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;
import org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity;

public class EasyPassingAlienEntity extends Entity implements Damageable {
    private final Game game;
    private int health = 90; // 3 hits

    private final Sprite[] sprites;
    private int currentFrame = 0;
    private long lastFrameChange = 0;
    private final long frameDuration = 150;

    public enum Origin { LEFT, RIGHT }
    private enum State { DESCENDING, EXITING }
    private State currentState = State.DESCENDING;
    private final Origin origin;
    private final int turnY = 200;

    private boolean isFiringBurst = false;
    private int burstsFired = 0;
    private final int totalBursts = 3;
    private long lastBurstTime = 0;
    private final long burstInterval = 300;

    public EasyPassingAlienEntity(Game game, int x, int y, Origin origin) {
        super("sprites/alien1.1.gif", x, y);
        this.game = game;
        this.origin = origin;

        this.sprites = new Sprite[3];
        this.sprites[0] = sprite;
        this.sprites[1] = game.getSpriteStore().getSprite("sprites/alien1.2.gif");
        this.sprites[2] = game.getSpriteStore().getSprite("sprites/allen1.3.gif");

        this.dx = 0;
        this.dy = 100;
    }

    @Override
    public void move(long delta) {
        if (currentState == State.DESCENDING && y > turnY) {
            currentState = State.EXITING;
            isFiringBurst = true;
            if (origin == Origin.LEFT) setHorizontalMovement(-150);
            else setHorizontalMovement(150);
            setVerticalMovement(0);
        }

        super.move(delta);

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameChange > frameDuration) {
            lastFrameChange = currentTime;
            currentFrame = (currentFrame + 1) % sprites.length;
            this.sprite = sprites[currentFrame];
        }

        if (isFiringBurst && burstsFired < totalBursts && currentTime - lastBurstTime > burstInterval) {
            lastBurstTime = currentTime;
            fireFanShot();
            burstsFired++;
        }

        if (x > 850 || x < -50 || y > 650 || y < -50) {
            game.removeEntity(this);
        }
    }

    private void fireFanShot() {
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
            double speed = 250;

            // Fire a single shot
            double shotDx = Math.cos(centerAngle) * speed;
            double shotDy = Math.sin(centerAngle) * speed;
            game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot1.gif", (int)(x + sprite.getWidth()/2), (int)y, shotDx, shotDy));
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.removeEntity(this);
            game.addScore(300);
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
