package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;
import org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity;

/**
 * Hard 난이도에서 등장하는 패싱형 에일리언
 * 일정 y위치까지 내려온 뒤 방향을 꺾어 이동하면서 플레이어를 향해 3번의 부채꼴 탄막을 발사한다.
 */
public class HardPassingAlienEntity extends Entity implements Damageable {

    // 적이 속한 게임 인스턴스
    private final Game game;
    // 5회 피격정도를 가짐
    private int health = 150;

    // Animation
    private final Sprite[] sprites;
    private int currentFrame = 0;
    private long lastFrameChange = 0;
    private final long frameDuration = 150; // ms

    // Movement State
    public enum Origin { LEFT, RIGHT }
    private enum State { DESCENDING, EXITING }
    private State currentState = State.DESCENDING;
    private final Origin origin;
    private final int turnY = 200; // Y coordinate to turn at

    // Firing State
    private boolean isFiringBurst = false;
    private int burstsFired = 0;
    private final int totalBursts = 3;
    private long lastBurstTime = 0;
    private final long burstInterval = 300; // ms between each fan shot burst

    public HardPassingAlienEntity(Game game, int x, int y, Origin origin) {
        super("sprites/alien1.1.gif", x, y);
        this.game = game;
        this.origin = origin;

        // Setup animation frames
        this.sprites = new Sprite[3];
        this.sprites[0] = sprite; // The one from super constructor
        this.sprites[1] = game.getSpriteStore().getSprite("sprites/alien1.2.gif");
        this.sprites[2] = game.getSpriteStore().getSprite("sprites/allen1.3.gif");

        // Initial movement: straight down
        this.dx = 0;
        this.dy = 100;
    }

    @Override
    public void move(long delta) {
        // 일정 높이까지 내려온 후, 좌/우로 빠져나가는 패턴으로 전환
        if (currentState == State.DESCENDING && y > turnY) {
            currentState = State.EXITING;
            isFiringBurst = true; // Start the burst fire sequence
            if (origin == Origin.LEFT) {
                setHorizontalMovement(-150); // Move left
            } else {
                setHorizontalMovement(150); // Move right
            }
            setVerticalMovement(0); // Stop moving down
        }

        // 기본 위치 업데이트
        super.move(delta);

        // 애니메이션 프레임 업데이트
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameChange > frameDuration) {
            lastFrameChange = currentTime;
            currentFrame = (currentFrame + 1) % sprites.length;
            this.sprite = sprites[currentFrame];
        }

        // 부채꼴 탄막을 일정 간격으로 3번 발사
        if (isFiringBurst && burstsFired < totalBursts && currentTime - lastBurstTime > burstInterval) {
            lastBurstTime = currentTime;
            fireFanShot();
            burstsFired++;
        }

        // 화면 밖으로 완전히 나가면 엔티티 제거
        if (x > 850 || x < -50 || y > 650 || y < -50) {
            game.getGamePlay().removeEntity(this);
        }
    }

    // 플레이어를 향해 중심 각도를 잡고, 좌/우로 조금식 퍼지는 부채꼴 탄막을 발사
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
            double spreadAngle = Math.PI / 12; // 15 degrees spread

            for (int i = -1; i <= 1; i++) {
                double angle = centerAngle + (i * spreadAngle);
                double shotDx = Math.cos(angle) * speed;
                double shotDy = Math.sin(angle) * speed;
                game.getGamePlay().addEntity(new GuidedBossShotEntity(game, "sprites/GuidedShot1.gif", (int)(x + sprite.getWidth()/2), (int)y, shotDx, shotDy));
            }
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
        // 플레이어 탄에 피격되면 체력 감소 후 탄 제거
        if (other instanceof ShotEntity) {
            takeDamage(30);
            game.getGamePlay().removeEntity(other);
        }
    }
}