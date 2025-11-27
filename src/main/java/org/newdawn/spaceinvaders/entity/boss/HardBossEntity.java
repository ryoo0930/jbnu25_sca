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

/**
 * Hard 난이도 최종 보스 엔티티
 * 페이즈 1~3과 Enerage(보통/압축) 상태에 따라 탄막 패턴이 강화된다.
 */
public class HardBossEntity extends BossEntity {
    private Game game;
    private Random random = new Random();
    private int health = 15000;
    private int maxHealth = 15000;

    // 현재 사용 중인 스프라이트 배열 (일반, 압축 상태 포함)
    private org.newdawn.spaceinvaders.Sprite[] sprites; // The currently active sprite array for animation
    private org.newdawn.spaceinvaders.Sprite[] normalSprites;
    private org.newdawn.spaceinvaders.Sprite[] darkSprites;
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

    // 보스 이동, 공격, 휴식 상태 정의
    private enum BossState { MOVING, ATTACKING, RESTING }
    private BossState currentState = BossState.MOVING;
    private long stateChangeTime = 0;
    private long moveDuration = 1000;
    private long attackDuration = 4000;
    private double targetX;

    // Enrage (Dark Sprite) State
    private boolean isEnraged = false;
    private long lastEnrageToggleTime = 0;
    private final long ENRAGE_NORMAL_DURATION = 7000; // 7 seconds normal
    private final long ENRAGE_DARK_DURATION = 3000;   // 3 seconds dark

    // Phase 2 Patterns
    private int phase2AttackStep = 0;
    private double phase3BurstAngle; // This is used by phase 2's laser, name is from previous refactor
    private long warningStartTime = 0;
    private long lastPhase2SpiralTime = 0;
    private long phase2SpiralCooldown = 200;
    private int spiralAngleIndex = 0;

    // Phase 3 Patterns
    private double p3_spiralAngle1 = 0;
    private double p3_spiralAngle2 = 0;
    private long p3_lastRingTime = 0;
    private final long p3_ringCooldown = 2000;
    private long p3_lastAimedTime = 0;
    private final long p3_aimedCooldown = 1500;

    public HardBossEntity(Game game, int x, int y) {
        super("sprites/Boss1.gif", x, 100);
        this.game = game;
        setCallbacks(game);

        // Load normal sprites
        this.normalSprites = new org.newdawn.spaceinvaders.Sprite[3];
        this.normalSprites[0] = sprite; // From super constructor
        this.normalSprites[1] = game.getSpriteStore().getSprite("sprites/Boss2.gif");
        this.normalSprites[2] = game.getSpriteStore().getSprite("sprites/Boss3.gif");

        // Load dark sprites
        this.darkSprites = new org.newdawn.spaceinvaders.Sprite[3];
        this.darkSprites[0] = game.getSpriteStore().getSprite("sprites/BossDark.gif");
        this.darkSprites[1] = game.getSpriteStore().getSprite("sprites/BossDark2.gif");
        this.darkSprites[2] = game.getSpriteStore().getSprite("sprites/BossDark3.gif");

        // Set the initial active sprites
        this.sprites = this.normalSprites;

        this.x = x;
        this.y = 100;
        this.stateChangeTime = System.currentTimeMillis();
        this.lastEnrageToggleTime = System.currentTimeMillis();
        setNewTargetX();
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    private void setNewTargetX() { targetX = 100 + random.nextInt(600); }

    private long restStartTime = 0;
    private long restDuration = 3000;

    // 보스 상태(Enrage, 이동, 공격, 휴식 페이즈 전환) 및 애니메이션을 업데이트 한다.
    @Override
    public void move(long delta) {
        long currentTime = System.currentTimeMillis();

        // Handle Enrage state transition
        if (!isEnraged && currentTime - lastEnrageToggleTime > ENRAGE_NORMAL_DURATION) {
            isEnraged = true;
            lastEnrageToggleTime = currentTime;
            this.sprites = this.darkSprites;
        } else if (isEnraged && currentTime - lastEnrageToggleTime > ENRAGE_DARK_DURATION) {
            isEnraged = false;
            lastEnrageToggleTime = currentTime;
            this.sprites = this.normalSprites;
        }

        // State machine
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
                // 페이즈별 패턴 실행
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

        // Animation - uses the currently active 'sprites' array
        if (currentTime - lastFrameChange > frameDuration) {
            lastFrameChange = currentTime;
            currentFrame = (currentFrame + 1) % sprites.length;
            this.sprite = sprites[currentFrame];
        }

        super.move(delta);

        // Phase transition
        int currentPhase = phase;
        if (health > 10000) phase = 1;
        else if (health > 5000) phase = 2;
        else phase = 3;

        // 페이즈가 바뀔 때 아이템 드랍 및 상태 초기화
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

    // 페이즈1 : 조준 연발 샷 + 원형 탄막 패턴
    private void phase1Attack(long currentTime) {
        if (phase1AttackStep == 0 && currentTime - lastAttackTime > attackCooldown) {
            phase1AttackStep = 1;
            attackCounter = 5;
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

        if (phase1AttackStep == 1 && attackCounter > 0 && currentTime - lastSubAttackTime > 100) {
            lastSubAttackTime = currentTime;
            patternAimedBurst(this.phase1BurstAngle);
            attackCounter--;
            if (attackCounter == 0) {
                phase1AttackStep = 2;
                attackCounter = 20;
                lastSubAttackTime = currentTime + 500;
            }
        } else if (phase1AttackStep == 2 && attackCounter > 0 && currentTime - lastSubAttackTime > 100) {
            lastSubAttackTime = currentTime;
            patternCircleShot(attackCounter);
            attackCounter--;
            if (attackCounter == 0) {
                phase1AttackStep = 0;
                lastAttackTime = currentTime;
            }
        }
    }

    // 플레이어 방향으로 부채꼴 탄막 발사
    private void patternAimedBurst(double angle) {
        fireFan(angle, 5, 250, 0.1, "sprites/GuidedShot.gif");
    }

    // 회전하는 원형 탄막 패턴
    private void patternCircleShot(int shotIndex) {
        double rotationPerShot = Math.PI / 30.0;
        double angleOffset = (20 - shotIndex) * rotationPerShot;
        fireCircular(angleOffset, 28, 150, "sprites/GuidedShot.gif");
    }

    /**
     * 페이즈2 : 경고선 -> 레이저 연타 패턴
     */
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
                game.getGamePlay().addEntity(new LaserWarningLineEntity(game, this, this.phase3BurstAngle, 1000));
                warningStartTime = currentTime;
                phase2AttackStep = 1;
            }
        }
        if (phase2AttackStep == 1 && currentTime - warningStartTime > 1000) {
            phase2AttackStep = 2;
            attackCounter = 50;
            lastSubAttackTime = currentTime;
        }
        if (phase2AttackStep == 2 && attackCounter > 0 && currentTime - lastSubAttackTime > 10) {
            lastSubAttackTime = currentTime;
            game.getGamePlay().addEntity(new BossLaserEntity(game, this, "sprites/BossLaser2.gif", 2000, 100, 1, this.phase3BurstAngle));
            attackCounter--;
            if (attackCounter == 0) phase2AttackStep = 3;
        }
    }

    // 페이즈2: 스파이럴 탄막 (원형 발사 각도 회전)
    private void phase2SpiralAttack(long currentTime) {
        if (currentTime - lastPhase2SpiralTime > phase2SpiralCooldown) {
            lastPhase2SpiralTime = currentTime;
            double rotationOffset = spiralAngleIndex * (Math.PI / 16.0);
            fireCircular(rotationOffset, 12, 200, "sprites/GuidedShot.gif");
            spiralAngleIndex++;
        }
    }

    /**
     * 페이즈3: 양방향 스파이럴 + 링 탄막 + 조준 부채꼴 패턴
     */
    private void phase3TouhouAttack(long currentTime) {
        p3_spiralAngle1 += Math.PI / 30;
        p3_spiralAngle2 -= Math.PI / 30;
        fireAtAngle(p3_spiralAngle1, 180, "sprites/GuidedShot.gif");
        fireAtAngle(p3_spiralAngle2, 180, "sprites/GuidedShot.gif");

        if (currentTime - p3_lastRingTime > p3_ringCooldown) {
            p3_lastRingTime = currentTime;
            fireCircular(0, 20, 140, "sprites/GuidedShot.gif");
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
                fireFan(centerAngle, 3, 220, Math.PI / 16, "sprites/GuidedShot.gif");
            }
        }
    }

    // 특정 각도 방향으로 탄막 한 발 발사
    private void fireAtAngle(double angle, double speed, String sprite) {
        int fireX = (int) (x + this.sprite.getWidth() / 2);
        int fireY = (int) (y + this.sprite.getHeight() / 2);
        double dx = Math.cos(angle) * speed;
        double dy = Math.sin(angle) * speed;
        game.getGamePlay().addEntity(new GuidedBossShotEntity(game, sprite, fireX, fireY, dx, dy));
    }

    // 워녛ㅇ으로 여러 발 발사
    private void fireCircular(double angleOffset, int bulletCount, double speed, String sprite) {
        for (int i = 0; i < bulletCount; i++) {
            double angle = 2 * Math.PI * i / bulletCount + angleOffset;
            fireAtAngle(angle, speed, sprite);
        }
    }

    // 중심 각도를 기준으로 부채꼴 형태 발사
    private void fireFan(double centerAngle, int bulletCount, double speed, double spread, String sprite) {
        for (int i = 0; i < bulletCount; i++) {
            double angle = centerAngle + (i - (bulletCount - 1) / 2.0) * spread;
            fireAtAngle(angle, speed, sprite);
        }
    }

    // Hard 보스 피해 처리 및 사망 시 승리 처리
    public void takeDamage(int damage) {
        health -= damage;
        game.getGamePlay().addScore(damage * 10);
        if (health <= 0) {
            game.getGamePlay().removeEntity(this);
            game.getGamePlay().notifyWin();
        }
    }

    // 약간 축소된 히트박스를 사용한 충돌 판정
    @Override
    public boolean collidesWith(Entity other) {
        Rectangle me = new Rectangle((int) (x + sprite.getWidth() * 0.125), (int) (y + sprite.getHeight() * 0.125), (int) (sprite.getWidth() * 0.75), (int) (sprite.getHeight() * 0.75));
        Rectangle him = new Rectangle((int) other.getX(), (int) other.getY(), other.getSprite().getWidth(), other.getSprite().getHeight());
        return me.intersects(him);
    }

    // 플레이어 탄/레이저와 충돌 시 데미지 처리
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