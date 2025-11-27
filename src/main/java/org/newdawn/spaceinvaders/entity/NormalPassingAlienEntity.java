package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;
import org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity;

/**
 * 일반 난이도에서 등장하는 패싱하는 적 엔티티
 * 일정 y 지점에서 방향을 틀고, 짧은 탄막을 발사한 뒤 화면 밖으로 이동한다.
 */
public class NormalPassingAlienEntity extends Entity implements Damageable {
    private final Game game;
    private int health = 120; // 4 hits

    // 3프레임 애니메이션
    private final Sprite[] sprites;
    private int currentFrame = 0;
    private long lastFrameChange = 0;
    private final long frameDuration = 150;

    // 이동 및 동작 상태
    public enum Origin { LEFT, RIGHT }
    private enum State { DESCENDING, EXITING }
    private State currentState = State.DESCENDING;
    private final Origin origin;
    private final int turnY = 200;  // 여기까지 내려온 뒤 방향 전환

    // 발사 패턴
    private boolean isFiringBurst = false;
    private int burstsFired = 0;
    private final int totalBursts = 3;
    private long lastBurstTime = 0;
    private final long burstInterval = 300;

    public NormalPassingAlienEntity(Game game, int x, int y, Origin origin) {
        super("sprites/alien1.1.gif", x, y);
        this.game = game;
        this.origin = origin;

        // 애니메이션 프레임 로드
        this.sprites = new Sprite[3];
        this.sprites[0] = sprite;
        this.sprites[1] = game.getSpriteStore().getSprite("sprites/alien1.2.gif");
        this.sprites[2] = game.getSpriteStore().getSprite("sprites/allen1.3.gif");

        // 초기 이동
        this.dx = 0;
        this.dy = 100;
    }

    @Override
    public void move(long delta) {
        // 특정 위치까지 내려오면 방향 전환 + 단발 연사 시작
        if (currentState == State.DESCENDING && y > turnY) {
            currentState = State.EXITING;
            isFiringBurst = true;
            if (origin == Origin.LEFT) setHorizontalMovement(-150);
            else setHorizontalMovement(150);
            setVerticalMovement(0);
        }

        super.move(delta);

        // 애니메이션 업데이트
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameChange > frameDuration) {
            lastFrameChange = currentTime;
            currentFrame = (currentFrame + 1) % sprites.length;
            this.sprite = sprites[currentFrame];
        }

        // 지정된 횟수만큼 단발 발사
        if (isFiringBurst && burstsFired < totalBursts && currentTime - lastBurstTime > burstInterval) {
            lastBurstTime = currentTime;
            fireFanShot();
            burstsFired++;
        }

        // 화면 밖으로 나가면 삭제
        if (x > 850 || x < -50 || y > 650 || y < -50) {
            game.getGamePlay().removeEntity(this);
        }
    }

    // 플레이어 위치를 기준으로 단발 조준탄 발사
    private void fireFanShot() {
        Entity player = null;
        for (Object entity : game.getGamePlay().getEntities()) {
            if (entity instanceof ShipEntity) {
                player = (Entity) entity;
                break;
            }
        }

        if (player != null) {
            double targetDx = player.getX() - this.x;
            double targetDy = player.getY() - this.y;
            double centerAngle = Math.atan2(targetDy, targetDx);
            double speed = 250;

            // Fire a single shot
            double shotDx = Math.cos(centerAngle) * speed;
            double shotDy = Math.sin(centerAngle) * speed;
            game.getGamePlay().addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot1.gif", (int)(x + sprite.getWidth()/2), (int)y, shotDx, shotDy));
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.getGamePlay().removeEntity(this);
            game.getGamePlay().addScore(300);
        }
    }

    // 플레이어 탄에 피격되면 데미지를 받고 삭제됨
    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity) {
            takeDamage(30);
            game.getGamePlay().removeEntity(other);
        }
    }
}
