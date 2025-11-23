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

public class NormalBossEntity extends BossEntity {
    private Game game;
    private Random random = new Random();
    private int health = 12000; // Health: 4/5 of Hard
    private int maxHealth = 12000;

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
    private long phase2SpiralCooldown = 200;
    private int spiralAngleIndex = 0;

    // Phase 3
    private double p3_spiralAngle1 = 0;
    private double p3_spiralAngle2 = 0;
    private long p3_lastRingTime = 0;
    private final long p3_ringCooldown = 2000;
    private long p3_lastAimedTime = 0;
    private final long p3_aimedCooldown = 1500;
    private ShipEntity findPlayer() {
        for (Object entity : game.getGamePlay().getEntities()) {
            if (entity instanceof ShipEntity) {
                return (ShipEntity) entity;
            }
        }
        return null;
    }

    public NormalBossEntity(Game game, int x, int y) {
        super("sprites/Boss1.gif", x, 100);
        this.game = game;
        setCallbacks(game);
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
        // Adjusted phase thresholds for 12000 HP
        if (health > 8000) phase = 1;
        else if (health > 4000) phase = 2;
        else phase = 3;

        if (phase != currentPhase) {
            int dropX = (int) this.x + this.sprite.getWidth() / 2;
            int dropY = (int) this.y + this.sprite.getHeight() / 2;
            game.getGamePlay().addEntity(new ItemEntity(game, "sprites/H.gif", dropX - 30, dropY, ItemEntity.ItemType.HEALTH));
            game.getGamePlay().addEntity(new ItemEntity(game, "sprites/L.gif", dropX, dropY, ItemEntity.ItemType.LASER));
            game.getGamePlay().addEntity(new ItemEntity(game, "sprites/B.gif", dropX + 30, dropY, ItemEntity.ItemType.BOMB));
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
            attackCounter = 4; // 4 bursts
            Entity player = null;
            for (Object entity : game.getGamePlay().getEntities()) {
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

        if (phase1AttackStep == 1 && attackCounter > 0 && currentTime - lastSubAttackTime > 120) {
            lastSubAttackTime = currentTime;
            patternAimedBurst(this.phase1BurstAngle);
            attackCounter--;
            if (attackCounter == 0) {
                phase1AttackStep = 2;
                attackCounter = 18; // 18 shots
                lastSubAttackTime = currentTime + 500;
            }
        } else if (phase1AttackStep == 2 && attackCounter > 0 && currentTime - lastSubAttackTime > 120) {
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
        fireFan(angle, 3, 220, 0.1, "sprites/GuidedShot.gif"); // 3 shots
    }

    private void patternCircleShot(int shotIndex) {
        double rotationPerShot = Math.PI / 30.0;
        double angleOffset = (20 - shotIndex) * rotationPerShot;
        fireCircular(angleOffset, 22, 140, "sprites/GuidedShot.gif"); // 22 bullets
    }

    private void phase2LaserAttack(long currentTime) {
        if (phase2AttackStep == 0) {
            Entity player = null;
            for (Object entity : game.getGamePlay().getEntities()) {
                if (entity instanceof ShipEntity) {
                    player = (Entity) entity;
                    break;
                }
            }
            if (player != null) {
                this.phase3BurstAngle = Math.atan2(player.getY() - this.y, player.getX() - this.x);
                game.getGamePlay().addEntity(new LaserWarningLineEntity(game, this, this.phase3BurstAngle, 1200)); // Slightly longer warning
                warningStartTime = currentTime;
                phase2AttackStep = 1;
            }
        }
        if (phase2AttackStep == 1 && currentTime - warningStartTime > 1200) {
            phase2AttackStep = 2;
            attackCounter = 40; // 40 shots
            lastSubAttackTime = currentTime;
        }
        if (phase2AttackStep == 2 && attackCounter > 0 && currentTime - lastSubAttackTime > 15) {
            lastSubAttackTime = currentTime;
            game.getGamePlay().addEntity(new BossLaserEntity(game, this, "sprites/BossLaser2.gif", 2000, 100, 1, this.phase3BurstAngle));
            attackCounter--;
            if (attackCounter == 0) phase2AttackStep = 3;
        }
    }

    private void phase2SpiralAttack(long currentTime) {
        if (currentTime - lastPhase2SpiralTime > phase2SpiralCooldown) {
            lastPhase2SpiralTime = currentTime;
            double rotationOffset = spiralAngleIndex * (Math.PI / 16.0);
            fireCircular(rotationOffset, 10, 190, "sprites/GuidedShot.gif"); // 10 bullets
            spiralAngleIndex++;
        }
    }

    private void phase3TouhouAttack(long currentTime) {
        p3_spiralAngle1 += Math.PI / 35; // Slightly slower
        p3_spiralAngle2 -= Math.PI / 35;
        fireAtAngle(p3_spiralAngle1, 170, "sprites/GuidedShot.gif");
        fireAtAngle(p3_spiralAngle2, 170, "sprites/GuidedShot.gif");

        if (currentTime - p3_lastRingTime > p3_ringCooldown) {
            p3_lastRingTime = currentTime;
            fireCircular(0, 16, 130, "sprites/GuidedShot.gif"); // 16 bullets
        }

        if (currentTime - p3_lastAimedTime > p3_aimedCooldown) {
            p3_lastAimedTime = currentTime;
            Entity player = null;
            for (Object entity : game.getGamePlay().getEntities()) {
                if (entity instanceof ShipEntity) {
                    player = (Entity) entity;
                    break;
                }
            }
            if (player != null) {
                double centerAngle = Math.atan2(player.getY() - this.y, player.getX() - this.x);
                fireFan(centerAngle, 1, 220, Math.PI / 16, "sprites/GuidedShot.gif"); // 1 shot
            }
        }
    }

    private void fireAtAngle(double angle, double speed, String sprite) {
        int fireX = (int) (x + this.sprite.getWidth() / 2);
        int fireY = (int) (y + this.sprite.getHeight() / 2);
        double dx = Math.cos(angle) * speed;
        double dy = Math.sin(angle) * speed;
        game.getGamePlay().addEntity(new GuidedBossShotEntity(game, sprite, fireX, fireY, dx, dy));
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
        game.getGamePlay().addScore(damage * 10);
        if (health <= 0) {
            game.getGamePlay().removeEntity(this);
            game.getGamePlay().notifyWin();
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
            game.getGamePlay().removeEntity(other);
        } else if (other instanceof LaserEntity) {
            takeDamage(100);
        }
    }
}
