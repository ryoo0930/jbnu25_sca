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
 * Easy 난이도 보스의 구체 구현 클래스
 * 체력, 페이즈 전환, 패턴 속도, 탄막 밀도 등이 Hard 보스보다 쉬운 버전
 * 움직, 공격, 휴식 상태를 오가며 페이즈(1~3)에 따라 서로 다른 패턴을 사용
 */
public class EasyBossEntity extends BossEntity {
    private Game game;
    private Random random = new Random();

    // Easy 보스 전용 체력 (Hard의 half)
    private int health = 7500; // Health: 1/2 of Hard
    private int maxHealth = 7500;

    // 보스 스프라이트 애니메이션 프레임
    private org.newdawn.spaceinvaders.Sprite[] sprites;
    private int currentFrame = 0;
    private long lastFrameChange = 0;
    private long frameDuration = 150;  // 프레임 전환 주기 (ms)

    // 페이즈 및 상태 관리
    private int phase = 1;
    private int phase1AttackStep = 0;
    private int attackCounter = 0;
    private long lastAttackTime = 0;
    private long attackCooldown = 3000;
    private long lastSubAttackTime = 0;
    private double phase1BurstAngle;

    // 간단한 상태 머신 (이동, 공격, 휴식)
    private enum BossState { MOVING, ATTACKING, RESTING }
    private BossState currentState = BossState.MOVING;
    private long stateChangeTime = 0;
    private long moveDuration = 1000;
    private long attackDuration = 4000;
    private double targetX;

    // Phase 2 관련 변수
    private int phase2AttackStep = 0;
    private double phase3BurstAngle; // Corrected variable
    private long warningStartTime = 0;
    private long lastPhase2SpiralTime = 0;
    private long phase2SpiralCooldown = 300; // Slower
    private int spiralAngleIndex = 0;

    // Phase 3 관련 변수 (탄막, 슈팅풍 패턴)
    private double p3_spiralAngle1 = 0;
    private long p3_lastRingTime = 0;
    private final long p3_ringCooldown = 2500; // Slower ring
    private long p3_lastSpiralTime = 0;
    private final long p3_spiralInterval = 100; // Fire every 100ms, making it much less dense

    public EasyBossEntity(Game game, int x, int y) {
        // Easy 보스 스프라이트 및 초기 위치 설정
        super("sprites/Boss1.gif", x, 100);
        this.game = game;
        setCallbacks(game);
        // 애니메이션용 스프라이트 배열 초기화
        this.sprites = new org.newdawn.spaceinvaders.Sprite[3];
        this.sprites[0] = sprite;
        this.sprites[1] = game.getSpriteStore().getSprite("sprites/Boss2.gif");
        this.sprites[2] = game.getSpriteStore().getSprite("sprites/Boss3.gif");
        this.x = x;
        this.y = 100;
        this.stateChangeTime = System.currentTimeMillis();
        setNewTargetX(); // 첫 이동 목표 설정
    }

    // Easy 보스 전용 체력 정보
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    // 이동 목표 X 좌표를 랜덤으로 갱신
    private void setNewTargetX() { targetX = 100 + random.nextInt(600); }

    private long restStartTime = 0;
    private long restDuration = 3000;

    /**
     * 보스의 이동 상태 (이동, 공격, 휴식) 업데이트
     * @param delta The ammount of time that has passed in milliseconds
     */
    @Override
    public void move(long delta) {
        long currentTime = System.currentTimeMillis();

        switch (currentState) {
            case MOVING:
                // 목표 x 좌표까지 좌/우로 이동
                double dx_vec = targetX - x;
                if (Math.abs(dx_vec) > 1) this.dx = Math.signum(dx_vec) * 100;
                else this.dx = 0;
                // 일정 시간 이동 후 공격 상태로 전환
                if (currentTime - stateChangeTime > moveDuration) {
                    currentState = BossState.ATTACKING;
                    stateChangeTime = currentTime;
                    this.dx = 0;
                    phase2AttackStep = 0;
                }
                break;
            case ATTACKING:
                // 현재 페이즈에 따라 서로 다른 패턴 실행
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
                // 공격 시간이 끝나면 다시 이동 상태로 전환
                if (currentTime - stateChangeTime > attackDuration) {
                    currentState = BossState.MOVING;
                    stateChangeTime = currentTime;
                    setNewTargetX();
                }
                break;
            case RESTING:
                // 페이즈 전환 후 잠시 쉬는 상태
                if (currentTime - restStartTime > restDuration) {
                    currentState = BossState.ATTACKING;
                    stateChangeTime = currentTime;
                }
                break;
        }

        // 스프라이트 애니메잉션 프레임 전환
        if (currentTime - lastFrameChange > frameDuration) {
            lastFrameChange = currentTime;
            currentFrame = (currentFrame + 1) % sprites.length;
            this.sprite = sprites[currentFrame];
        }

        // 실제 위치 갱신은 상위 Entity 로직을 사용함
        super.move(delta);

        // 체력 기준으로 페이즈 전환
        int currentPhase = phase;
        // Adjusted phase thresholds for 7500 HP
        if (health > 5000) phase = 1;
        else if (health > 2500) phase = 2;
        else phase = 3;

        // 페이즈가 바뀌면 아이템 드랍 + 상태 초기화 + 휴식
        if (phase != currentPhase) {
            int dropX = (int) this.x + this.sprite.getWidth() / 2;
            int dropY = (int) this.y + this.sprite.getHeight() / 2;

            // 체력, 레이저, 폭탄 아이템 드랍
            game.getGamePlay().addEntity(new ItemEntity(game, "sprites/H.gif", dropX - 30, dropY, ItemEntity.ItemType.HEALTH));
            game.getGamePlay().addEntity(new ItemEntity(game, "sprites/L.gif", dropX, dropY, ItemEntity.ItemType.LASER));
            game.getGamePlay().addEntity(new ItemEntity(game, "sprites/B.gif", dropX + 30, dropY, ItemEntity.ItemType.BOMB));

            // 패턴 관리 변수 리셋
            phase1AttackStep = 0;
            attackCounter = 0;
            lastAttackTime = currentTime;

            // 휴식 상태로 전환
            currentState = BossState.RESTING;
            restStartTime = currentTime;
            this.dx = 0;
        }
    }

    // 1페이즈 : 조준 3연발 + 원형 탄막 패턴
    private void phase1Attack(long currentTime) {
        // 1. 쿨다운 후 조준 방항 계산
        if (phase1AttackStep == 0 && currentTime - lastAttackTime > attackCooldown) {
            phase1AttackStep = 1;
            attackCounter = 3; // Reduced bursts
            // 플레이어 위치를 기반으로 조준 각도 계산
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

        // 2. 조준 3연발 패턴
        if (phase1AttackStep == 1 && attackCounter > 0 && currentTime - lastSubAttackTime > 150) { // Slower
            lastSubAttackTime = currentTime;
            patternAimedBurst(this.phase1BurstAngle);
            attackCounter--;

            // 3. 원형 탄막으로 전환
            if (attackCounter == 0) {
                phase1AttackStep = 2;
                attackCounter = 15; // Reduced shots
                lastSubAttackTime = currentTime + 500;
            }
        }
        // 3. 회전하는 원형 탄막
        else if (phase1AttackStep == 2 && attackCounter > 0 && currentTime - lastSubAttackTime > 150) { // Slower
            lastSubAttackTime = currentTime;
            patternCircleShot(attackCounter);
            attackCounter--;

            // 패턴 종료 후 다시 쿨다운
            if (attackCounter == 0) {
                phase1AttackStep = 0;
                lastAttackTime = currentTime;
            }
        }
    }

    // 플레이어 방향으로 푸채꼴 형태의 탄막 발사
    private void patternAimedBurst(double angle) {
        fireFan(angle, 3, 200, 0.1, "sprites/GuidedShot.gif"); // 3 shots, slower
    }

    // 회전하는 원형 탄막 패턴
    private void patternCircleShot(int shotIndex) {
        double rotationPerShot = Math.PI / 30.0;
        double angleOffset = (20 - shotIndex) * rotationPerShot;
        fireCircular(angleOffset, 16, 120, "sprites/GuidedShot.gif"); // Fewer bullets, slower
    }

    // 2페이즈 중 레이저 경고선+ 레이저 발사 로직
    private void phase2LaserAttack(long currentTime) {
        // 1. 플레이어 방향 계산 + 경고선 생성
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
                game.getGamePlay().addEntity(new LaserWarningLineEntity(game, this, this.phase3BurstAngle, 1500)); // Longer warning
                warningStartTime = currentTime;
                phase2AttackStep = 1;
            }
        }
        // 2. 경고 시간 이후 실제 레이저 발사 준비
        if (phase2AttackStep == 1 && currentTime - warningStartTime > 1500) {
            phase2AttackStep = 2;
            attackCounter = 30; // Fewer shots
            lastSubAttackTime = currentTime;
        }
        // 3. 일정 간격으로 레이저를 여러 번 발사
        if (phase2AttackStep == 2 && attackCounter > 0 && currentTime - lastSubAttackTime > 20) { // Slower
            lastSubAttackTime = currentTime;
            game.getGamePlay().addEntity(new BossLaserEntity(game, this, "sprites/BossLaser2.gif", 2000, 100, 1, this.phase3BurstAngle));
            attackCounter--;
            if (attackCounter == 0) phase2AttackStep = 3;
        }
    }

    // 2페이즈 추가 패턴 : 느린 스파이럴 탄막
    private void phase2SpiralAttack(long currentTime) {
        if (currentTime - lastPhase2SpiralTime > phase2SpiralCooldown) {
            lastPhase2SpiralTime = currentTime;
            double rotationOffset = spiralAngleIndex * (Math.PI / 16.0);
            fireCircular(rotationOffset, 4, 180, "sprites/GuidedShot.gif"); // Halved bullet count from 8 to 4
            spiralAngleIndex++;
        }
    }

    // 3페이즈 패턴 :스파이럴 + 링 탄막으로 구성된 탄막 슈핑풍 공격
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

    // 특정 각도 방향으로 탄 하나 발사
    private void fireAtAngle(double angle, double speed, String sprite) {
        int fireX = (int) (x + this.sprite.getWidth() / 2);
        int fireY = (int) (y + this.sprite.getHeight() / 2);
        double dx = Math.cos(angle) * speed;
        double dy = Math.sin(angle) * speed;
        game.getGamePlay().addEntity(new GuidedBossShotEntity(game, sprite, fireX, fireY, dx, dy));
    }

    // 원형으로 다수의 탄막 발사
    private void fireCircular(double angleOffset, int bulletCount, double speed, String sprite) {
        for (int i = 0; i < bulletCount; i++) {
            double angle = 2 * Math.PI * i / bulletCount + angleOffset;
            fireAtAngle(angle, speed, sprite);
        }
    }

    // 중심 각도를 기준으로 부채꼴 형태로 탄막 발사
    private void fireFan(double centerAngle, int bulletCount, double speed, double spread, String sprite) {
        for (int i = 0; i < bulletCount; i++) {
            double angle = centerAngle + (i - (bulletCount - 1) / 2.0) * spread;
            fireAtAngle(angle, speed, sprite);
        }
    }

    /**
     * Easy 보스 전용 피해 로직 처리
     * 체력을 감소시키고 그만큼 점수를 추가하며, 체력이 0이하가 되면 승리 처리 및 보스 제거
     * BossEntity의 takeDamage를 별도로 재정의한 버전임
      */

    public void takeDamage(int damage) {
        health -= damage;
        game.getGamePlay().addScore(damage * 10);
        if (health <= 0) {
            game.getGamePlay().removeEntity(this);
            game.getGamePlay().notifyWin();
        }
    }

    /**
     * 보스와 다른 엔티티의 충돌 판정
     * 실제 스프라이트보다 약간 작은 히트박스를 사용하여 난이도를 낮춤
     */
    public boolean collidesWith(Entity other) {
        Rectangle me = new Rectangle((int) (x + sprite.getWidth() * 0.125), (int) (y + sprite.getHeight() * 0.125), (int) (sprite.getWidth() * 0.75), (int) (sprite.getHeight() * 0.75));
        Rectangle him = new Rectangle((int) other.getX(), (int) other.getY(), other.getSprite().getWidth(), other.getSprite().getHeight());
        return me.intersects(him);
    }

    /**
     * 다른 엔티티와 실제로 충돌했을 때의 처리
     * 플레이어 탄과 충돌시 체력 30이 감소하고 탄을 제거한다.
     * 프렐리어 레이저와 충돌시 체력이 100 감소한다.
     */
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
