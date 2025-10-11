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

public class EasyBossEntity extends BossEntity {
    private Game game;
    private Random random = new Random();
    private int health = 7500; // Health: 1/2 of Hard
    private int maxHealth = 7500;
    private long lastLaserHitTime = 0;

    private org.newdawn.spaceinvaders.Sprite[] sprites;
    private int currentFrame = 0;
    private long lastFrameChange = 0;
    private long frameDuration = 150;

    private int phase = 1;
    private int phase1AttackStep = 0;
    private int attackCounter = 0;
    private long lastAttackTime = 0;
    private long attackCooldown = 3000;
    private long lastSubAttackTime = 0;
    private double phase1BurstAngle;

    private enum BossState { MOVING, ATTACKING, RESTING }
    private BossState currentState = BossState.MOVING;
    private long stateChangeTime = 0;
    private long moveDuration = 1000;
    private long attackDuration = 4000;
    private double targetX;

    // Phase 2
    private int phase2AttackStep = 0;
    private double phase3BurstAngle; // Corrected variable
    private long warningStartTime = 0;
    private long lastPhase2SpiralTime = 0;
    private long phase2SpiralCooldown = 300; // Slower
    private int spiralAngleIndex = 0;

    // Phase 3
    private double p3_spiralAngle1 = 0;
    private long p3_lastRingTime = 0;
    private final long p3_ringCooldown = 2500; // Slower ring
    private long p3_lastSpiralTime = 0;
    private final long p3_spiralInterval = 100; // Fire every 100ms, making it much less dense

    public EasyBossEntity(Game game, int x, int y) {
        super("sprites/Boss1.gif", x, 100);
        this.game = game;
        this.sprites = new org.newdawn.spaceinvaders.Sprite[3];
        this.sprites[0] = sprite;
        this.sprites[1] = game.getSpriteStore().getSprite("sprites/Boss2.gif");
        this.sprites[2] = game.getSpriteStore().getSprite("sprites/Boss3.gif");
        this.x = x;
        this.y = 100;
        this.stateChangeTime = System.currentTimeMillis();
        setNewTargetX();
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    private void setNewTargetX() { targetX = 100 + random.nextInt(600); }

    private long restStartTime = 0;
    private long restDuration = 3000;

    @Override
    public void move(long delta) {
        long currentTime = System.currentTimeMillis();

        switch (currentState) {
            case MOVING:
                double dx_vec = targetX - x;
                if (Math.abs(dx_vec) > 1) this.dx = Math.signum(dx_vec) * 100;
                else this.dx = 0;
                if (currentTime - stateChangeTime > moveDuration) {
                    currentState = BossState.ATTACKING;
                    stateChangeTime = currentTime;
                    this.dx = 0;
                    phase2AttackStep = 0;
                }
                break;
            case ATTACKING:
                switch (phase) {
                    case 1: phase1Attack(currentTime); break;
                    case 2:
                        phase2LaserAttack(currentTime);
                        phase2SpiralAttack(currentTime);
                        break;
                    case 3:
                        phase3TouhouAttack(currentTime);
                        break;
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

        int currentPhase = phase;
        // Adjusted phase thresholds for 7500 HP
        if (health > 5000) phase = 1;
        else if (health > 2500) phase = 2;
        else phase = 3;

        if (phase != currentPhase) {
            int dropX = (int) this.x + this.sprite.getWidth() / 2;
            int dropY = (int) this.y + this.sprite.getHeight() / 2;
            game.addEntity(new ItemEntity(game, "sprites/H.gif", dropX - 30, dropY, ItemEntity.ItemType.HEALTH));
            game.addEntity(new ItemEntity(game, "sprites/L.gif", dropX, dropY, ItemEntity.ItemType.LASER));
            game.addEntity(new ItemEntity(game, "sprites/B.gif", dropX + 30, dropY, ItemEntity.ItemType.BOMB));
            phase1AttackStep = 0;
            attackCounter = 0;
            lastAttackTime = currentTime;
            currentState = BossState.RESTING;
            restStartTime = currentTime;
            this.dx = 0;
        }
    }

    private void phase1Attack(long currentTime) {
        if (phase1AttackStep == 0 && currentTime - lastAttackTime > attackCooldown) {
            phase1AttackStep = 1;
            attackCounter = 3; // Reduced bursts
            Entity player = null;
            for (Object entity : game.getEntities()) {
                if (entity instanceof ShipEntity) {
                    player = (Entity) entity;
                    break;
                }
            }
            if (player != null) {
                this.phase1BurstAngle = Math.atan2(player.getY() - this.y, player.getX() - this.x);
            } else {
                this.phase1BurstAngle = -Math.PI / 2;
            }
        }

        if (phase1AttackStep == 1 && attackCounter > 0 && currentTime - lastSubAttackTime > 150) { // Slower
            lastSubAttackTime = currentTime;
            patternAimedBurst(this.phase1BurstAngle);
            attackCounter--;
            if (attackCounter == 0) {
                phase1AttackStep = 2;
                attackCounter = 15; // Reduced shots
                lastSubAttackTime = currentTime + 500;
            }
        } else if (phase1AttackStep == 2 && attackCounter > 0 && currentTime - lastSubAttackTime > 150) { // Slower
            lastSubAttackTime = currentTime;
            patternCircleShot(attackCounter);
            attackCounter--;
            if (attackCounter == 0) {
                phase1AttackStep = 0;
                lastAttackTime = currentTime;
            }
        }
    }

    private void patternAimedBurst(double angle) {
        fireFan(angle, 3, 200, 0.1, "sprites/GuidedShot.gif"); // 3 shots, slower
    }

    private void patternCircleShot(int shotIndex) {
        double rotationPerShot = Math.PI / 30.0;
        double angleOffset = (20 - shotIndex) * rotationPerShot;
        fireCircular(angleOffset, 16, 120, "sprites/GuidedShot.gif"); // Fewer bullets, slower
    }

    private void phase2LaserAttack(long currentTime) {
        if (phase2AttackStep == 0) {
            Entity player = null;
            for (Object entity : game.getEntities()) {
                if (entity instanceof ShipEntity) {
                    player = (Entity) entity;
                    break;
                }
            }
            if (player != null) {
                this.phase3BurstAngle = Math.atan2(player.getY() - this.y, player.getX() - this.x);
                game.addEntity(new LaserWarningLineEntity(game, this, this.phase3BurstAngle, 1500)); // Longer warning
                warningStartTime = currentTime;
                phase2AttackStep = 1;
            }
        }
        if (phase2AttackStep == 1 && currentTime - warningStartTime > 1500) {
            phase2AttackStep = 2;
            attackCounter = 30; // Fewer shots
            lastSubAttackTime = currentTime;
        }
        if (phase2AttackStep == 2 && attackCounter > 0 && currentTime - lastSubAttackTime > 20) { // Slower
            lastSubAttackTime = currentTime;
            game.addEntity(new BossLaserEntity(game, this, "sprites/BossLaser2.gif", 2000, 100, 1, this.phase3BurstAngle));
            attackCounter--;
            if (attackCounter == 0) phase2AttackStep = 3;
        }
    }

    private void phase2SpiralAttack(long currentTime) {
        if (currentTime - lastPhase2SpiralTime > phase2SpiralCooldown) {
            lastPhase2SpiralTime = currentTime;
            double rotationOffset = spiralAngleIndex * (Math.PI / 16.0);
            fireCircular(rotationOffset, 4, 180, "sprites/GuidedShot.gif"); // Halved bullet count from 8 to 4
            spiralAngleIndex++;
        }
    }

    private void phase3TouhouAttack(long currentTime) {
        // Slower spiral with cooldown
        if (currentTime - p3_lastSpiralTime > p3_spiralInterval) {
            p3_lastSpiralTime = currentTime;
            p3_spiralAngle1 += Math.PI / 45; // Slower rotation
            fireAtAngle(p3_spiralAngle1, 150, "sprites/GuidedShot.gif"); // Only one spiral
        }

        if (currentTime - p3_lastRingTime > p3_ringCooldown) { // Simplified timer
            p3_lastRingTime = currentTime;
            fireCircular(0, 6, 120, "sprites/GuidedShot.gif"); // Fewer bullets, slower
        }

        // No aimed shot in easy mode
    }

    private void fireAtAngle(double angle, double speed, String sprite) {
        int fireX = (int) (x + this.sprite.getWidth() / 2);
        int fireY = (int) (y + this.sprite.getHeight() / 2);
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
        game.addScore(damage * 10);
        if (health <= 0) {
            game.removeEntity(this);
            game.notifyWin();
        }
    }
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
