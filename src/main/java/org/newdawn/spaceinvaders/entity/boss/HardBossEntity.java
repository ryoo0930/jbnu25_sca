package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;

import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;


import org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity;

import java.util.Random;

import java.awt.Rectangle;

import org.newdawn.spaceinvaders.entity.BossSkill.BossLaserEntity;
import org.newdawn.spaceinvaders.entity.BossSkill.LaserWarningLineEntity;

public class HardBossEntity extends Entity {
    private Game game;
    private Random random = new Random();
    private int health = 15000; // 보스 체력
    private int maxHealth = 15000;
    private long lastLaserHitTime = 0;

    // Animation
    private org.newdawn.spaceinvaders.Sprite[] sprites;
    private int currentFrame = 0;
    private long lastFrameChange = 0;
    private long frameDuration = 150; // 150ms per frame

    private int phase = 1;
    private int phase1AttackStep = 0; // 0: ready, 1: guided shots, 2: circular shots
    private int attackCounter = 0;
    private long lastAttackTime = 0;
    private long attackCooldown = 3000; // Cooldown for phase 1 attack sequence
    private long lastSubAttackTime = 0;
    private double phase1BurstAngle; // Angle for the aimed burst

    // Movement & State
    private enum BossState {
        MOVING,
        ATTACKING
    }
    private BossState currentState = BossState.MOVING;
    private long stateChangeTime = 0;
    private long moveDuration = 1000; // 1 second
    private long attackDuration = 4000; // 4 seconds, enough for one attack sequence
    private double targetX;

    // BossLaser
    private long bossLaserChargeMillis   = 1000;  // 차지 시간
    private long bossLaserDurationMillis = 1000;  // 발사 지속 시간
    private long bossLaserDamageInterval = 1000;  // 1초에 1틱
    private int  bossLaserDamagePerTick  = 1;     // 틱당 1데미지
    private long lastBossLaserTime = 0;           // 마지막 시퀀스 시작 시간
    private long bossLaserCooldown  = 5000;       // 시퀀스 쿨다운
    private boolean bossLaserActive = false;      // 발사 진행 여부


    public HardBossEntity(Game game, int x, int y) {
        super("sprites/Boss1.gif", x, 100); // Set fixed Y position
        this.game = game;
        this.sprites = new org.newdawn.spaceinvaders.Sprite[3];
        this.sprites[0] = sprite; // Use the one already loaded
        this.sprites[1] = game.getSpriteStore().getSprite("sprites/Boss2.gif");
        this.sprites[2] = game.getSpriteStore().getSprite("sprites/Boss3.gif");
        this.x = x;
        this.y = 100; // Ensure Y is fixed

        // Start with moving
        this.stateChangeTime = System.currentTimeMillis();
        setNewTargetX();
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    private void setNewTargetX() {
        targetX = 100 + random.nextInt(600);
    }

    @Override
    public void move(long delta) {
        long currentTime = System.currentTimeMillis();

        // State machine logic
        switch (currentState) {
            case MOVING:
                // Move towards targetX
                double dx_vec = targetX - x;
                double distance = Math.abs(dx_vec);
                if (distance > 1) {
                    this.dx = (dx_vec / distance) * 100; // Movement speed
                } else {
                    this.dx = 0; // Reached destination
                }

                // Check for state transition
                if (currentTime - stateChangeTime > moveDuration) {
                    currentState = BossState.ATTACKING;
                    stateChangeTime = currentTime;
                    this.dx = 0; // Stop
                    phase3AttackStep = 0; // Reset attack for new ATTACKING state
                }
                break;

            case ATTACKING:
                // Perform attack logic based on phase
                switch (phase) {
                    case 1:
                        phase1Attack(currentTime);
                        break;
                    case 2:
                        phase2Attack(currentTime);
                        break;
                    case 3:
                        phase3Attack(currentTime);
                        break;
                }

                // Check for state transition
                if (currentTime - stateChangeTime > attackDuration) {
                    currentState = BossState.MOVING;
                    stateChangeTime = currentTime;
                    setNewTargetX();
                }
                break;
        }

        // Common logic for animation, actual movement, and phase transition
        // Animation
        if (currentTime - lastFrameChange > frameDuration) {
            lastFrameChange = currentTime;
            currentFrame = (currentFrame + 1) % sprites.length;
            this.sprite = sprites[currentFrame];
        }

        // Update position
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
            lastAttackTime = currentTime;
            // Force transition to attacking state to start new phase attack
            currentState = BossState.ATTACKING;
            stateChangeTime = currentTime;
            this.dx = 0;

            // 보스 레이자 상태 초기화
            bossLaserActive = false;
            lastBossLaserTime = currentTime;
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
        int fireX = (int) (x + sprite.getWidth() / 2);
        int fireY = (int) (y + sprite.getHeight() / 2);

        // Fire 5 shots in a fan shape
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
        // Each shot in the sequence rotates clockwise
        double rotationPerShot = Math.PI / 30.0; // Controls the speed of rotation
        double angleOffset = (20 - shotIndex) * rotationPerShot;

        for (int i = 0; i < bulletCount; i++) {
            double angle = 2 * Math.PI * i / bulletCount + angleOffset;
            double speed = 150;
            double shotDx = Math.cos(angle) * speed;
            double shotDy = Math.sin(angle) * speed;
            game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot.gif", fireX, fireY, shotDx, shotDy));
        }
    }

    private void patternSpiralShot() {
        int bulletCount = 30;
        int fireX = (int) (x + sprite.getWidth() / 2);
        int fireY = (int) (y + sprite.getHeight() / 2);
        for (int i = 0; i < bulletCount; i++) {
            double angle = 0.1 * i * Math.PI;
            double speed = 150 + i * 5;
            double shotDx = Math.cos(angle) * speed;
            double shotDy = Math.sin(angle) * speed;
            game.addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot.gif", fireX, fireY, shotDx, shotDy));
        }
    }
    private int phase3AttackStep = 0;
    private double phase3BurstAngle;

    private long warningStartTime = 0;

    private void phase3Attack(long currentTime) {
        long warningDuration = 1000; // 1초
        long burstInterval = 10; // 0.01초 간격 (더 빠름)

        if (phase3AttackStep == 0) { // 0: 조준 및 경고선 표시
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

                // 경고선 생성
                game.addEntity(new LaserWarningLineEntity(game, this, this.phase3BurstAngle, warningDuration));
                
                warningStartTime = currentTime;
                phase3AttackStep = 1; // 발사 대기 상태로 전환
            }
        }
        
        if (phase3AttackStep == 1) { // 1: 발사 대기
            if (currentTime - warningStartTime > warningDuration) {
                phase3AttackStep = 2; // 발사 중 상태로 전환
                attackCounter = 50; // 50발 발사
                lastSubAttackTime = currentTime;
            }
        }

        if (phase3AttackStep == 2) { // 2: 발사 중
            if (attackCounter > 0 && currentTime - lastSubAttackTime > burstInterval) {
                lastSubAttackTime = currentTime;

                game.addEntity(new BossLaserEntity(
                    game,
                    this,
                    "sprites/BossLaser2.gif",
                    2000,
                    100,
                    1,
                    this.phase3BurstAngle
                ));

                attackCounter--;
                if (attackCounter == 0) {
                    phase3AttackStep = 3; // 발사 완료, 대기 상태로 전환
                }
            }
        }
    }


        public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.removeEntity(this);
            game.notifyWin(); // Notify game win when boss is defeated
        }
    }

    @Override
    public boolean collidesWith(Entity other) {
        // Define a smaller hitbox for the boss (e.g., 75% of the sprite size)
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