package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;
import org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity;

public class PassingAlienEntity extends Entity {
    private final Game game;
    private int health = 150;

    // Animation
    private final Sprite[] sprites;
    private int currentFrame = 0;
    private long lastFrameChange = 0;
    private final long frameDuration = 150; // ms

    // Movement State
    public enum Origin { LEFT, RIGHT }
    private enum State { DESCENDING, EXITING }
    private State currentState = State.DESCENDING;
    private final Origin origin;
    private final int turnY = 200; // Y coordinate to turn at

    // Firing State
    private boolean isFiringBurst = false;
    private int burstsFired = 0;
    private final int totalBursts = 3;
    private long lastBurstTime = 0;
    private final long burstInterval = 300; // ms between each fan shot burst

    public PassingAlienEntity(Game game, int x, int y, Origin origin) {
        super("sprites/alien1.1.gif", x, y);
        this.game = game;
        this.origin = origin;

        // Setup animation frames
        this.sprites = new Sprite[3];
        this.sprites[0] = sprite; // The one from super constructor
        this.sprites[1] = game.getSpriteStore().getSprite("sprites/alien1.2.gif");
        this.sprites[2] = game.getSpriteStore().getSprite("sprites/allen1.3.gif");

        // Initial movement: straight down
        this.dx = 0;
        this.dy = 100;
    }

    @Override
    public void move(long delta) {
        // State-based movement logic
        if (currentState == State.DESCENDING && y > turnY) {
            currentState = State.EXITING;
            isFiringBurst = true; // Start the burst fire sequence
            if (origin == Origin.LEFT) {
                setHorizontalMovement(-150); // Move left
            } else {
                setHorizontalMovement(150); // Move right
            }
            setVerticalMovement(0); // Stop moving down
        }

        super.move(delta);

        // Animate
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameChange > frameDuration) {
            lastFrameChange = currentTime;
            currentFrame = (currentFrame + 1) % sprites.length;
            this.sprite = sprites[currentFrame];
        }

        // Handle burst firing
        if (isFiringBurst && burstsFired < totalBursts && currentTime - lastBurstTime > burstInterval) {
            lastBurstTime = currentTime;
            fireFanShot();
            burstsFired++;
        }

        // Remove if it goes off screen
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
            double spreadAngle = Math.PI / 12; // 15 degrees spread

            // Define the three angles for the fan
            double[] angles = {
                centerAngle - spreadAngle,
                centerAngle,
                centerAngle + spreadAngle
            };

            // Create a shot for each angle
            for (double angle : angles) {
                double shotDx = Math.cos(angle) * speed;
                double shotDy = Math.sin(angle) * speed;
                game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot1.gif", (int)(x + sprite.getWidth()/2), (int)y, shotDx, shotDy));
            }
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.removeEntity(this);
            game.addScore(1000);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity) {
            takeDamage(30);
            game.removeEntity(other);
        } else if (other instanceof LaserEntity) {
            takeDamage(1);
        }
    }
}