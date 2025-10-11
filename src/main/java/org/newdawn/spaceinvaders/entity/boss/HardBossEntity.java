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

public class HardBossEntity extends Entity {
    private Game game;
    private Random random = new Random();
    private int health = 15000;
    private int maxHealth = 15000;
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

    // Phase 2: Touhou-style patterns
    private double p2_spiralAngle1 = 0;
    private double p2_spiralAngle2 = 0;
    private long p2_lastRingTime = 0;
    private final long p2_ringCooldown = 2000; // 2 seconds
    private long p2_lastAimedTime = 0;
    private final long p2_aimedCooldown = 1500; // 1.5 seconds

    private int phase3AttackStep = 0;
    private double phase3BurstAngle;
    private long warningStartTime = 0;
    private long lastPhase3SpiralTime = 0;
    private long phase3SpiralCooldown = 200;
    private int spiralAngleIndex = 0;

    public HardBossEntity(Game game, int x, int y) {
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

    private void setNewTargetX() {
        targetX = 100 + random.nextInt(600);
    }

    private long restStartTime = 0;
    private long restDuration = 3000;

    @Override
    public void move(long delta) {
        long currentTime = System.currentTimeMillis();

        switch (currentState) {
            case MOVING:
                double dx_vec = targetX - x;
                double distance = Math.abs(dx_vec);
                if (distance > 1) {
                    this.dx = (dx_vec / distance) * 100;
                } else {
                    this.dx = 0;
                }
                if (currentTime - stateChangeTime > moveDuration) {
                    currentState = BossState.ATTACKING;
                    stateChangeTime = currentTime;
                    this.dx = 0;
                    phase3AttackStep = 0;
                }
                break;
            case ATTACKING:
                switch (phase) {
                    case 1: phase1Attack(currentTime); break;
                    case 2: // Now runs the logic that was in phase 3
                        phase3LaserAttack(currentTime);
                        phase3SpiralAttack(currentTime);
                        break;
                    case 3: // Now runs the logic that was in phase 2
                        phase2Attack(currentTime);
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
        if (health > 10000) phase = 1;
        else if (health > 5000) phase = 2;
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
        long burstInterval = 100;
        long circularShotInterval = 100;

        if (phase1AttackStep == 0 && currentTime - lastAttackTime > attackCooldown) {
            phase1AttackStep = 1;
            attackCounter = 5;
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
                this.phase1BurstAngle = Math.atan2(targetDy, targetDx);
            } else {
                this.phase1BurstAngle = -Math.PI / 2;
            }
        }

        if (phase1AttackStep == 1) {
            if (attackCounter > 0 && currentTime - lastSubAttackTime > burstInterval) {
                lastSubAttackTime = currentTime;
                patternAimedBurst(this.phase1BurstAngle);
                attackCounter--;
                if (attackCounter == 0) {
                    phase1AttackStep = 2;
                    attackCounter = 20;
                    lastSubAttackTime = currentTime + 500;
                }
            }
        } else if (phase1AttackStep == 2) {
            if (attackCounter > 0 && currentTime - lastSubAttackTime > circularShotInterval) {
                lastSubAttackTime = currentTime;
                patternCircleShot(attackCounter);
                attackCounter--;
                if (attackCounter == 0) {
                    phase1AttackStep = 0;
                    lastAttackTime = currentTime;
                }
            }
        }
    }

    private void phase2Attack(long currentTime) {
        int fireX = (int) (x + sprite.getWidth() / 2);
        int fireY = (int) (y + sprite.getHeight() / 2);
        double speed = 180;

        // Pattern 1: Dual rotating spirals
        p2_spiralAngle1 += Math.PI / 30; // Clockwise
        p2_spiralAngle2 -= Math.PI / 30; // Counter-clockwise

        double dx1 = Math.cos(p2_spiralAngle1) * speed;
        double dy1 = Math.sin(p2_spiralAngle1) * speed;
        game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot.gif", fireX, fireY, dx1, dy1));

        double dx2 = Math.cos(p2_spiralAngle2) * speed;
        double dy2 = Math.sin(p2_spiralAngle2) * speed;
        game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot.gif", fireX, fireY, dx2, dy2));

        // Pattern 2: Expanding ring every 2 seconds
        if (currentTime - p2_lastRingTime > p2_ringCooldown) {
            p2_lastRingTime = currentTime;
            int bulletCount = 20;
            for (int i = 0; i < bulletCount; i++) {
                double angle = 2 * Math.PI * i / bulletCount;
                double ringDx = Math.cos(angle) * (speed * 0.8); // Slightly slower
                double ringDy = Math.sin(angle) * (speed * 0.8);
                game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot.gif", fireX, fireY, ringDx, ringDy));
            }
        }

        // Pattern 3: Aimed 3-shot burst every 1.5 seconds
        if (currentTime - p2_lastAimedTime > p2_aimedCooldown) {
            p2_lastAimedTime = currentTime;
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
                double spread = Math.PI / 16;
                for (int i = -1; i <= 1; i++) {
                    double angle = centerAngle + i * spread;
                    double aimedDx = Math.cos(angle) * (speed * 1.2); // Slightly faster
                    double aimedDy = Math.sin(angle) * (speed * 1.2);
                    game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot.gif", fireX, fireY, aimedDx, aimedDy));
                }
            }
        }
    }

    private void patternAimedBurst(double angle) {
        double speed = 250;
        int fireX = (int) (x + sprite.getWidth() / 2);
        int fireY = (int) (y + sprite.getHeight() / 2);
        for (int i = -2; i <= 2; i++) {
            double adjustedAngle = angle + (i * 0.1);
            double shotDx = Math.cos(adjustedAngle) * speed;
            double shotDy = Math.sin(adjustedAngle) * speed;
            game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot.gif", fireX, fireY, shotDx, shotDy));
        }
    }

    private void patternCircleShot(int shotIndex) {
        int bulletCount = 28;
        int fireX = (int) (x + sprite.getWidth() / 2);
        int fireY = (int) (y + sprite.getHeight() / 2);
        double rotationPerShot = Math.PI / 30.0;
        double angleOffset = (20 - shotIndex) * rotationPerShot;
        for (int i = 0; i < bulletCount; i++) {
            double angle = 2 * Math.PI * i / bulletCount + angleOffset;
            double speed = 150;
            double shotDx = Math.cos(angle) * speed;
            double shotDy = Math.sin(angle) * speed;
            game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot.gif", fireX, fireY, shotDx, shotDy));
        }
    }

    private void phase3LaserAttack(long currentTime) {
        long warningDuration = 1000;
        long burstInterval = 10;
        if (phase3AttackStep == 0) {
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
                this.phase3BurstAngle = Math.atan2(targetDy, targetDx);
                game.addEntity(new LaserWarningLineEntity(game, this, this.phase3BurstAngle, warningDuration));
                warningStartTime = currentTime;
                phase3AttackStep = 1;
            }
        }
        if (phase3AttackStep == 1) {
            if (currentTime - warningStartTime > warningDuration) {
                phase3AttackStep = 2;
                attackCounter = 50;
                lastSubAttackTime = currentTime;
            }
        }
        if (phase3AttackStep == 2) {
            if (attackCounter > 0 && currentTime - lastSubAttackTime > burstInterval) {
                lastSubAttackTime = currentTime;
                game.addEntity(new BossLaserEntity(game, this, "sprites/BossLaser2.gif", 2000, 100, 1, this.phase3BurstAngle));
                attackCounter--;
                if (attackCounter == 0) {
                    phase3AttackStep = 3;
                }
            }
        }
    }

    private void phase3SpiralAttack(long currentTime) {
        if (currentTime - lastPhase3SpiralTime > phase3SpiralCooldown) {
            lastPhase3SpiralTime = currentTime;
            patternPhase3SpiralShot();
        }
    }

    private void patternPhase3SpiralShot() {
        int bulletCount = 12;
        int fireX = (int) (x + sprite.getWidth() / 2);
        int fireY = (int) (y + sprite.getHeight() / 2);
        double rotationOffset = spiralAngleIndex * (Math.PI / 16.0);
        for (int i = 0; i < bulletCount; i++) {
            double angle = (2 * Math.PI * i / bulletCount) + rotationOffset;
            double speed = 200;
            double shotDx = Math.cos(angle) * speed;
            double shotDy = Math.sin(angle) * speed;
            game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot.gif", fireX, fireY, shotDx, shotDy));
        }
        spiralAngleIndex++;
    }

    public void takeDamage(int damage) {
        health -= damage;
        game.addScore(damage * 10);
        if (health <= 0) {
            game.removeEntity(this);
            game.notifyWin();
        }
    }

    @Override
    public boolean collidesWith(Entity other) {
        int hitboxWidth = (int) (sprite.getWidth() * 0.75);
        int hitboxHeight = (int) (sprite.getHeight() * 0.75);
        int hitboxX = (int) (x + (sprite.getWidth() - hitboxWidth) / 2);
        int hitboxY = (int) (y + (sprite.getHeight() - hitboxHeight) / 2);
        Rectangle me = new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
        Rectangle him = new Rectangle((int) other.getX(), (int) other.getY(), other.getSprite().getWidth(), other.getSprite().getHeight());
        return me.intersects(him);
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity) {
            takeDamage(30);
            game.removeEntity(other);
        } else if (other instanceof LaserEntity) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastLaserHitTime > 100) {
                takeDamage(100);
                lastLaserHitTime = currentTime;
            }
        }
    }
}