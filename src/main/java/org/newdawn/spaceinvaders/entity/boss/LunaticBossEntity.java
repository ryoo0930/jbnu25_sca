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

/**
 * Lunatic 난이도 보스 엔티티
 * 빠른 이동과 강력한 패턴을 사용한다.
 * 해바라기 탄막, 회전 바퀴, 유도탄
 */
public class LunaticBossEntity extends BossEntity {
    private Game game;
    private Random random = new Random();
    private int health = 15000; // Health reduced
    private int maxHealth = 15000;

    // 간단한 3프레임 애니메이션
    private org.newdawn.spaceinvaders.Sprite[] sprites;
    private int currentFrame = 0;
    private long lastFrameChange = 0;
    private long frameDuration = 150;

    private int phase = 1;
    // 이동, 공격, 휴식 상태
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

    // 보스가 좌우로 이동할 목표 위치 설정
    private void setNewTargetX() { targetX = 100 + random.nextInt(600); }

    private long restStartTime = 0;
    private long restDuration = 2000;

    // Lunatic 보스의 상태 머신(이동, 공격, 휴식)과 애니메이션을 갱신한다.
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
                // 페이즈별 패턴 실행
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

        // 보스 애니메이션 프레임 전환
        if (currentTime - lastFrameChange > frameDuration) {
            lastFrameChange = currentTime;
            currentFrame = (currentFrame + 1) % sprites.length;
            this.sprite = sprites[currentFrame];
        }

        super.move(delta);

        // Phase transition (체력 기반)
        int currentPhase = phase;
        if (health > 7500) { // Phase 2 at 1/2 health
            phase = 1;
        } else {
            phase = 2;
        }

        // 페이즈 변경 시 체력 아이템 다수 드랍 + 잠깐 휴식
        if (phase != currentPhase) {
            for (int i = -2; i <= 2; i++) {
                game.getGamePlay().addEntity(new ItemEntity(game, "sprites/H.gif", (int)this.x + i*30, (int)this.y, ItemEntity.ItemType.HEALTH));
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

        // 화면 상단에서 랜덤 위치로 떨어지는 직선 탄
        if (currentTime - p1_lastRainTime > 250) {
            p1_lastRainTime = currentTime;
            int rainX = 50 + random.nextInt(700);
            Entity rainShot = new GuidedBossShotEntity(game, "sprites/GuidedShot3.gif", rainX, -20, 0, 200);
            game.getGamePlay().addEntity(rainShot);
        }
    }

    // Phase 2: Blade Wheels + Homing Amulets
    private void phase2Attack(long currentTime) {
        p2_wheelAngle += Math.PI / 70;
        double wheelRadius = 150;

        // 일정 시간마다 원형 탄막 확산
        if(currentTime - p2_lastWheelExpandTime > 4000){
            p2_lastWheelExpandTime = currentTime;
            fireCircular(p2_wheelAngle, 8, 200, "sprites/GuidedShot3.gif");
        }

        // 좌/우 바퀴 위치에서 회전 탄막 발사
        fireAtAngleFromPoint(x - wheelRadius, y, p2_wheelAngle, 150, "sprites/GuidedShot2.gif");
        fireAtAngleFromPoint(x + wheelRadius, y, -p2_wheelAngle, 150, "sprites/GuidedShot4.gif");

        // 플레이어를 향한 유도성 탄막
        if (currentTime - p2_lastHomingTime > 2200) {
            p2_lastHomingTime = currentTime;
            Entity player = getPlayer();
            if (player != null) {
                double angle = Math.atan2(player.getY() - this.y, player.getX() - this.x);
                fireFan(angle, 1, 120, Math.PI / 12, "sprites/GuidedShot4.gif");
            }
        }
    }

    // 현재 필드에서 존재하는 플레이어 찾기
    private Entity getPlayer() {
        for (Object entity : game.getGamePlay().getEntities()) {
            if (entity instanceof ShipEntity) {
                return (Entity) entity;
            }
        }
        return null;
    }

    private void fireAtAngle(double angle, double speed, String sprite) {
        fireAtAngleFromPoint(x, y, angle, speed, sprite);
    }

    // 지정 좌표에서 각도/속도 기반으로 탄막을 발사
    private void fireAtAngleFromPoint(double startX, double startY, double angle, double speed, String sprite){
        int fireX = (int) (startX + this.sprite.getWidth() / 2);
        int fireY = (int) (startY + this.sprite.getHeight() / 2);
        double dx = Math.cos(angle) * speed;
        double dy = Math.sin(angle) * speed;
        game.getGamePlay().addEntity(new GuidedBossShotEntity(game, sprite, fireX, fireY, dx, dy));
    }

    // 원형으로 다수의 탄을 발사
    private void fireCircular(double angleOffset, int bulletCount, double speed, String sprite) {
        for (int i = 0; i < bulletCount; i++) {
            double angle = 2 * Math.PI * i / bulletCount + angleOffset;
            fireAtAngle(angle, speed, sprite);
        }
    }

    // 중심각도를 기준으로 부채꼴 탄막을 발사.
    private void fireFan(double centerAngle, int bulletCount, double speed, double spread, String sprite) {
        for (int i = 0; i < bulletCount; i++) {
            double angle = centerAngle + (i - (bulletCount - 1) / 2.0) * spread;
            fireAtAngle(angle, speed, sprite);
        }
    }

    // Lunatic 보스 피해 처리 및 사망 시 승리 처리
    public void takeDamage(int damage) {
        health -= damage;
        game.getGamePlay().addScore(damage * 20);
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

    // 플레이어 탄,레이저와 충돌 시 데미지 처리
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