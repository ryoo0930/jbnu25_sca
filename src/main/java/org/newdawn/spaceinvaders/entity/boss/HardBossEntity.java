package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.GuidedBossShotEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

import org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity;

import java.util.Random;

public class HardBossEntity extends AlienEntity {
    private Game game;
    private Random random = new Random();
    private int health = 15000; // 보스 체력
    private int maxHealth = 15000;
    private long lastLaserHitTime = 0;

    private int phase = 1;
    private int phase1AttackStep = 0; // 0: ready, 1: guided shots, 2: circular shots
    private int attackCounter = 0;
    private long lastAttackTime = 0;
    private long attackCooldown = 3000; // Cooldown for phase 1 attack sequence
    private long lastSubAttackTime = 0;
    private double phase1BurstAngle; // Angle for the aimed burst

    // Movement
    private long lastMoveTime = 0;
    private long moveInterval = 1500; // 1.5 seconds to change movement
    private double targetX, targetY;

    public HardBossEntity(Game game, int x, int y) {
        super(game, x, y);
        this.game = game;
        this.sprite = game.getSpriteStore().getSprite("sprites/alien2.gif"); // Temporary boss sprite
        this.x = x;
        this.y = y;
        setRandomTargetPosition();
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    private void setRandomTargetPosition() {
        targetX = 100 + random.nextInt(600);
        targetY = 50 + random.nextInt(150);
    }

    @Override
    public void move(long delta) {
        long currentTime = System.currentTimeMillis();

        // Movement
        if (currentTime - lastMoveTime > moveInterval) {
            lastMoveTime = currentTime;
            setRandomTargetPosition();
        }

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 1) {
            this.dx = (dx / distance) * 100; // Movement speed
            this.dy = (dy / distance) * 100;
        } else {
            this.dx = 0;
            this.dy = 0;
        }

        super.move(delta);

        // Phase transition logic
        int currentPhase = phase;
        if (health > 10000) {
            phase = 1;
        } else if (health > 5000) {
            phase = 2;
        } else {
            phase = 3;
        }

        // Reset attack state on phase change
        if (phase != currentPhase) {
            phase1AttackStep = 0;
            attackCounter = 0;
            lastAttackTime = currentTime; // Start cooldown for the new phase's attacks
        }

        // Execute attacks based on phase
        switch (phase) {
            case 1:
                phase1Attack(currentTime);
                break;
            case 2:
                phase2Attack(currentTime);
                break;
            case 3:
                // No attacks in phase 3
                break;
        }
    }

    private void phase1Attack(long currentTime) {
        long burstInterval = 100; // 0.1s between bursts
        long circularShotInterval = 100; // 0.1s between circular shots

        // Start sequence after cooldown
        if (phase1AttackStep == 0 && currentTime - lastAttackTime > attackCooldown) {
            phase1AttackStep = 1; // Start aimed burst part
            attackCounter = 5; // 5 consecutive bursts

            // Target the player only at the beginning of the sequence
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
                this.phase1BurstAngle = -Math.PI / 2; // Default to firing downwards if no player found
            }
        }

        // Step 1: Fire 5 fan-shaped bursts
        if (phase1AttackStep == 1) {
            if (attackCounter > 0 && currentTime - lastSubAttackTime > burstInterval) {
                lastSubAttackTime = currentTime;
                patternAimedBurst(this.phase1BurstAngle);
                attackCounter--;
                if (attackCounter == 0) {
                    phase1AttackStep = 2; // Transition to circular shot part
                    attackCounter = 20; // 20 consecutive circular shots
                    lastSubAttackTime = currentTime + 500; // Add a small pause
                }
            }
        // Step 2: Fire 20 circular shots
        } else if (phase1AttackStep == 2) {
            if (attackCounter > 0 && currentTime - lastSubAttackTime > circularShotInterval) {
                lastSubAttackTime = currentTime;
                patternCircleShot(attackCounter);
                attackCounter--;
                if (attackCounter == 0) {
                    phase1AttackStep = 0; // End of sequence
                    lastAttackTime = currentTime; // Start main cooldown
                }
            }
        }
    }

    private void phase2Attack(long currentTime) {
        // Phase 2: Continuously fire bursts of 5 spiral shots.
        long timeBetweenBursts = 1000; // 1 second between each 5-shot burst.
        long timeBetweenShotsInBurst = 150; // 0.15 seconds between shots within the burst.

        if (attackCounter == 0 && currentTime - lastAttackTime > timeBetweenBursts) {
            attackCounter = 5; // Start a new burst.
        }

        if (attackCounter > 0 && currentTime - lastSubAttackTime > timeBetweenShotsInBurst) {
            lastSubAttackTime = currentTime;
            patternSpiralShot();
            attackCounter--;
            if (attackCounter == 0) {
                lastAttackTime = currentTime; // Mark the end of the burst to start the timer for the next one.
            }
        }
    }

    private void patternAimedBurst(double angle) {
        double speed = 250;

        // Fire 5 shots in a fan shape
        for (int i = -2; i <= 2; i++) {
            double adjustedAngle = angle + (i * 0.1);
            double shotDx = Math.cos(adjustedAngle) * speed;
            double shotDy = Math.sin(adjustedAngle) * speed;
            game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot.gif", (int) x, (int) y, shotDx, shotDy));
        }
    }

    private void patternCircleShot(int shotIndex) {
        int bulletCount = 28;
        // Each shot in the sequence rotates clockwise
        double rotationPerShot = Math.PI / 30.0; // Controls the speed of rotation
        double angleOffset = (20 - shotIndex) * rotationPerShot;

        for (int i = 0; i < bulletCount; i++) {
            double angle = 2 * Math.PI * i / bulletCount + angleOffset;
            double speed = 150;
            double shotDx = Math.cos(angle) * speed;
            double shotDy = Math.sin(angle) * speed;
            game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot.gif", (int) x, (int) y, shotDx, shotDy));
        }
    }

    private void patternSpiralShot() {
        int bulletCount = 30;
        for (int i = 0; i < bulletCount; i++) {
            double angle = 0.1 * i * Math.PI;
            double speed = 150 + i * 5;
            double shotDx = Math.cos(angle) * speed;
            double shotDy = Math.sin(angle) * speed;
            game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot.gif", (int) x, (int) y, shotDx, shotDy));
        }
    }

    @Override
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.removeEntity(this);
            game.notifyWin(); // Notify game win when boss is defeated
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity) {
            takeDamage(30); // Damage when hit by player's shot
            game.removeEntity(other);
        } else if (other instanceof LaserEntity) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastLaserHitTime > 100) { // 100ms 간격으로 데미지
                takeDamage(100);
                lastLaserHitTime = currentTime;
            }
        }
    }
}