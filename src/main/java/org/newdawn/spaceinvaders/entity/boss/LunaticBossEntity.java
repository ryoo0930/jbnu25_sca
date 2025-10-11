package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;
import org.newdawn.spaceinvaders.entity.BossSkill.LaserWarningLineEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ItemEntity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity;

import java.util.Random;
import java.awt.Rectangle;
import org.newdawn.spaceinvaders.entity.BossSkill.BossLaserEntity;

public class LunaticBossEntity extends BossEntity {
    private Game game;
    private Random random = new Random();
    private int health = 30000;
    private int maxHealth = 30000;
    private long lastLaserHitTime = 0;

    private org.newdawn.spaceinvaders.Sprite[] sprites;
    private int currentFrame = 0;
    private long lastFrameChange = 0;
    private long frameDuration = 150;

    private int phase = 1;
    private enum BossState { ATTACKING, RESTING }
    private BossState currentState = BossState.ATTACKING;
    private long stateChangeTime = 0;
    private long attackDuration = 8000;

    // Phase 1
    private double p1_sunflowerAngle1 = 0;
    private double p1_sunflowerAngle2 = Math.PI;
    private long p1_lastRainTime = 0;

    // Phase 2
    private double p2_wheelAngle = 0;
    private long p2_lastWheelExpandTime = 0;
    private long p2_lastHomingTime = 0;

    // Phase 3
    private long p3_lastGridLaserTime = 0;
    private int p3_laserStep = 0;
    private long p3_lastCollapseTime = 0;


    public LunaticBossEntity(Game game, int x, int y) {
        super("sprites/Boss1.gif", x, y);
        this.game = game;
        this.sprites = new org.newdawn.spaceinvaders.Sprite[3];
        this.sprites[0] = sprite;
        this.sprites[1] = game.getSpriteStore().getSprite("sprites/Boss2.gif");
        this.sprites[2] = game.getSpriteStore().getSprite("sprites/Boss3.gif");
        this.x = x;
        this.y = y;
        this.dx = 0;
        this.stateChangeTime = System.currentTimeMillis();
    }

    @Override
    public int getHealth() { return health; }
    @Override
    public int getMaxHealth() { return maxHealth; }

    private long restStartTime = 0;
    private long restDuration = 2000;

    @Override
    public void move(long delta) {
        long currentTime = System.currentTimeMillis();

        switch (currentState) {
            case ATTACKING:
                switch (phase) {
                    case 1: phase1Attack(currentTime); break;
                    case 2: phase2Attack(currentTime); break;
                    case 3: phase3Attack(currentTime); break;
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

        int currentPhase = phase;
        if (health > 20000) phase = 1;
        else if (health > 10000) phase = 2;
        else phase = 3;

        if (phase != currentPhase) {
            for (int i = -2; i <= 2; i++) {
                game.addEntity(new ItemEntity(game, "sprites/H.gif", (int)this.x + i*30, (int)this.y, ItemEntity.ItemType.HEALTH));
            }
            currentState = BossState.RESTING;
            restStartTime = currentTime;
        }
    }

    // Phase 1: Sunflower Spirals + Random Rain
    private void phase1Attack(long currentTime) {
        p1_sunflowerAngle1 += Math.PI / 60;
        p1_sunflowerAngle2 -= Math.PI / 60;
        fireCircular(p1_sunflowerAngle1, 4, 160, "sprites/GuidedShot2.gif"); // 6 -> 4 bullets
        fireCircular(p1_sunflowerAngle2, 4, 160, "sprites/GuidedShot4.gif"); // 6 -> 4 bullets

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
            fireCircular(p2_wheelAngle, 8, 200, "sprites/GuidedShot3.gif"); // 12 -> 8 bullets
        }
        fireAtAngleFromPoint(x - wheelRadius, y, p2_wheelAngle, 150, "sprites/GuidedShot2.gif");
        fireAtAngleFromPoint(x + wheelRadius, y, -p2_wheelAngle, 150, "sprites/GuidedShot4.gif");

        if (currentTime - p2_lastHomingTime > 2200) {
            p2_lastHomingTime = currentTime;
            Entity player = getPlayer();
            if (player != null) {
                double angle = Math.atan2(player.getY() - this.y, player.getX() - this.x);
                fireFan(angle, 1, 120, Math.PI / 12, "sprites/GuidedShot4.gif"); // 3 -> 1 bullet
            }
        }
    }

    // Phase 3: Laser Grid + Collapse
    private void phase3Attack(long currentTime) {
        if (currentTime - p3_lastGridLaserTime > 1000) {
            p3_lastGridLaserTime = currentTime;
            double angle = (p3_laserStep % 10) * (Math.PI / 9) - Math.PI/2;
            game.addEntity(new LaserWarningLineEntity(game, this, angle, 1000));
            game.addEntity(new BossLaserEntity(game, this, "sprites/BossLaser2.gif", 1000, 100, 1, angle));
            p3_laserStep++;
        }

        if (currentTime - p3_lastCollapseTime > 3500) {
            p3_lastCollapseTime = currentTime;
            int bulletCount = 12; // 18 -> 12 bullets
            for (int i = 0; i < bulletCount; i++) {
                double angle = 2 * Math.PI * i / bulletCount;
                double spawnX = x + Math.cos(angle) * 500;
                double spawnY = y + Math.sin(angle) * 500;
                Entity collapseShot = new GuidedBossShotEntity(game, "sprites/GuidedShot.gif", (int)spawnX, (int)spawnY, 0, 0);
                double dx = x - spawnX;
                double dy = y - spawnY;
                double norm = Math.sqrt(dx*dx + dy*dy);
                collapseShot.setHorizontalMovement(dx/norm * 90);
                collapseShot.setVerticalMovement(dy/norm * 90);
                game.addEntity(collapseShot);
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
            if (System.currentTimeMillis() - lastLaserHitTime > 100) {
                takeDamage(100);
                lastLaserHitTime = System.currentTimeMillis();
            }
        }
    }
}
