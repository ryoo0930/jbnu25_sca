package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;
import org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity;

/**
 * Easy 난이도에서 등장하는 패싱형 에일리언
 * 일정 y위치까지 내려온 뒤 방향을 꺾어 이동하며, 이동 중 플레이어를 향해 몇 번의 단발 사격을 수행한다.
 */
public class EasyPassingAlienEntity extends Entity implements Damageable {

    // 게임 인스턴스
    private final Game game;
    // 총 3회 피격시 제거
    private int health = 90; // 3 hits

    // 3프레임 애니메이션
    private final Sprite[] sprites;
    private int currentFrame = 0;
    private long lastFrameChange = 0;
    private final long frameDuration = 150;

    // 등장 방향
    public enum Origin { LEFT, RIGHT }
    // 이동 상태
    private enum State { DESCENDING, EXITING }
    private State currentState = State.DESCENDING;
    private final Origin origin;
    // 내려갈 y 위치
    private final int turnY = 200;

    // 방향전환 후 3번의 사격 버스트
    private boolean isFiringBurst = false;
    private int burstsFired = 0;
    private final int totalBursts = 3;
    private long lastBurstTime = 0;
    private final long burstInterval = 300;

    public EasyPassingAlienEntity(Game game, int x, int y, Origin origin) {
        super("sprites/alien1.1.gif", x, y);
        this.game = game;
        this.origin = origin;

        // 간단한 프레임 애니메이션 설정
        this.sprites = new Sprite[3];
        this.sprites[0] = sprite;
        this.sprites[1] = game.getSpriteStore().getSprite("sprites/alien1.2.gif");
        this.sprites[2] = game.getSpriteStore().getSprite("sprites/allen1.3.gif");

        // 기본 이동
        this.dx = 0;
        this.dy = 100;
    }

    @Override
    public void move(long delta) {
        // 특정 Y 지점 도달 시 방향 전환
        if (currentState == State.DESCENDING && y > turnY) {
            currentState = State.EXITING;
            isFiringBurst = true;
            if (origin == Origin.LEFT) setHorizontalMovement(-150);
            else setHorizontalMovement(150);
            setVerticalMovement(0);
        }

        super.move(delta);

        // 애니메이션 처리
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameChange > frameDuration) {
            lastFrameChange = currentTime;
            currentFrame = (currentFrame + 1) % sprites.length;
            this.sprite = sprites[currentFrame];
        }

        // 단발 사격 3회 실행
        if (isFiringBurst && burstsFired < totalBursts && currentTime - lastBurstTime > burstInterval) {
            lastBurstTime = currentTime;
            fireFanShot();
            burstsFired++;
        }

        // 화면 밖으로 나가면 제거
        if (x > 850 || x < -50 || y > 650 || y < -50) {
            game.getGamePlay().removeEntity(this);
        }
    }

    // 플레이러를 향해 한 번의 조준 사격을 수행
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

            // 단일 조주탄 발사
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

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity) {
            takeDamage(30);
            game.getGamePlay().removeEntity(other);
        }
    }
}
