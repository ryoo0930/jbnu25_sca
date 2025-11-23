package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ItemEntity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity;

import java.util.Random;
import java.awt.Rectangle;

public class LunaticBossEntity extends BossEntity {
    private Game game;
    private Random random = new Random();
    private int health = 15000; // Health reduced
    private int maxHealth = 15000;

    private org.newdawn.spaceinvaders.Sprite[] sprites;
    private int currentFrame = 0;
    private long lastFrameChange = 0;
    private long frameDuration = 150;

    private int phase = 1;
    private enum BossState { MOVING, ATTACKING, RESTING }
    private BossState currentState = BossState.MOVING;
    private long stateChangeTime = 0;
    private long moveDuration = 1000;
    private long attackDuration = 8000;
    private double targetX;

    // Phase 1
    private double p1_sunflowerAngle1 = 0;
    private double p1_sunflowerAngle2 = Math.PI;
    private long p1_lastRainTime = 0;

    // Phase 2
    private double p2_wheelAngle = 0;
    private long p2_lastWheelExpandTime = 0;
    private long p2_lastHomingTime = 0;

    public LunaticBossEntity(Game game, int x, int y) {
        super("sprites/BossDark.gif", x, 100);
        this.game = game;
        setCallbacks(game);
        this.sprites = new org.newdawn.spaceinvaders.Sprite[3];
        this.sprites[0] = sprite;
        this.sprites[1] = game.getSpriteStore().getSprite("sprites/BossDark2.gif");
        this.sprites[2] = game.getSpriteStore().getSprite("sprites/BossDark3.gif");
        this.x = x;
        this.y = 100;
        this.stateChangeTime = System.currentTimeMillis();
        setNewTargetX();
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    private void setNewTargetX() { targetX = 100 + random.nextInt(600); }

    private long restStartTime = 0;
    private long restDuration = 2000;

    @Override
    public void move(long delta) {
        long currentTime = System.currentTimeMillis();

        // State machine
        switch (currentState) {
            case MOVING:
                double dx_vec = targetX - x;
                if (Math.abs(dx_vec) > 1) this.dx = Math.signum(dx_vec) * 150;
                else this.dx = 0;
                if (currentTime - stateChangeTime > moveDuration) {
                    currentState = BossState.ATTACKING;
                    stateChangeTime = currentTime;
                    this.dx = 0;
                }
                break;
            case ATTACKING:
                switch (phase) {
                    case 1: phase1Attack(currentTime); break;
                    case 2: phase2Attack(currentTime); break;
                }
                if (currentTime - stateChangeTime > attackDuration) {
                    currentState = BossState.MOVING;
                    stateChangeTime = currentTime;
                    setNewTargetX();
                }
                break;
            case RESTING:
                if (currentTime - restStartTime > restDuration) {
                    currentState = BossState.ATTACKING;
                    stateChangeTime = currentTime;
                }
                break;
        }

        if (currentTime - lastFrameChange > frameDuration) {
            lastFrameChange = currentTime;
            currentFrame = (currentFrame + 1) % sprites.length;
            this.sprite = sprites[currentFrame];
        }

        super.move(delta);

        // Phase transition
        int currentPhase = phase;
        if (health > 7500) { // Phase 2 at 1/2 health
            phase = 1;
        } else {
            phase = 2;
        }

        if (phase != currentPhase) {
            for (int i = -2; i <= 2; i++) {
                game.addEntity(new ItemEntity(game, "sprites/H.gif", (int)this.x + i*30, (int)this.y, ItemEntity.ItemType.HEALTH));
            }
            currentState = BossState.RESTING;
            restStartTime = currentTime;
            this.dx = 0;
        }
    }

    // Phase 1: Sunflower Spirals + Random Rain
    private void phase1Attack(long currentTime) {
        p1_sunflowerAngle1 += Math.PI / 60;
        p1_sunflowerAngle2 -= Math.PI / 60;
        fireCircular(p1_sunflowerAngle1, 4, 160, "sprites/GuidedShot2.gif");
        fireCircular(p1_sunflowerAngle2, 4, 160, "sprites/GuidedShot4.gif");

        if (currentTime - p1_lastRainTime > 250) {
            p1_lastRainTime = currentTime;
            int rainX = 50 + random.nextInt(700);
            Entity rainShot = new GuidedBossShotEntity(game, "sprites/GuidedShot3.gif", rainX, -20, 0, 200);
            game.addEntity(rainShot);
        }
    }

    // Phase 2: Blade Wheels + Homing Amulets
    private void phase2Attack(long currentTime) {
        p2_wheelAngle += Math.PI / 70;
        double wheelRadius = 150;
        if(currentTime - p2_lastWheelExpandTime > 4000){
            p2_lastWheelExpandTime = currentTime;
            fireCircular(p2_wheelAngle, 8, 200, "sprites/GuidedShot3.gif");
        }
        fireAtAngleFromPoint(x - wheelRadius, y, p2_wheelAngle, 150, "sprites/GuidedShot2.gif");
        fireAtAngleFromPoint(x + wheelRadius, y, -p2_wheelAngle, 150, "sprites/GuidedShot4.gif");

        if (currentTime - p2_lastHomingTime > 2200) {
            p2_lastHomingTime = currentTime;
            Entity player = getPlayer();
            if (player != null) {
                double angle = Math.atan2(player.getY() - this.y, player.getX() - this.x);
                fireFan(angle, 1, 120, Math.PI / 12, "sprites/GuidedShot4.gif");
            }
        }
    }

    private Entity getPlayer() {
        for (Object entity : game.getEntities()) {
            if (entity instanceof ShipEntity) {
                return (Entity) entity;
            }
        }
        return null;
    }

    private void fireAtAngle(double angle, double speed, String sprite) {
        fireAtAngleFromPoint(x, y, angle, speed, sprite);
    }

    private void fireAtAngleFromPoint(double startX, double startY, double angle, double speed, String sprite){
        int fireX = (int) (startX + this.sprite.getWidth() / 2);
        int fireY = (int) (startY + this.sprite.getHeight() / 2);
        double dx = Math.cos(angle) * speed;
        double dy = Math.sin(angle) * speed;
        game.addEntity(new GuidedBossShotEntity(game, sprite, fireX, fireY, dx, dy));
    }

    private void fireCircular(double angleOffset, int bulletCount, double speed, String sprite) {
        for (int i = 0; i < bulletCount; i++) {
            double angle = 2 * Math.PI * i / bulletCount + angleOffset;
            fireAtAngle(angle, speed, sprite);
        }
    }

    private void fireFan(double centerAngle, int bulletCount, double speed, double spread, String sprite) {
        for (int i = 0; i < bulletCount; i++) {
            double angle = centerAngle + (i - (bulletCount - 1) / 2.0) * spread;
            fireAtAngle(angle, speed, sprite);
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        game.addScore(damage * 20);
        if (health <= 0) {
            game.removeEntity(this);
            game.notifyWin();
        }
    }

    @Override
    public boolean collidesWith(Entity other) {
        Rectangle me = new Rectangle((int) (x + sprite.getWidth() * 0.125), (int) (y + sprite.getHeight() * 0.125), (int) (sprite.getWidth() * 0.75), (int) (sprite.getHeight() * 0.75));
        Rectangle him = new Rectangle((int) other.getX(), (int) other.getY(), other.getSprite().getWidth(), other.getSprite().getHeight());
        return me.intersects(him);
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity) {
            takeDamage(30);
            game.removeEntity(other);
        } else if (other instanceof LaserEntity) {
            takeDamage(100);
        }
    }
}