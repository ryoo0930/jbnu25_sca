package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.GamePlay;

/**
 * 플레이어 및 적 양쪽에서 공통으로 사용하는 탄 엔티티
 * 위/아래 또는 임의 방향으로 이동하며, 에일리언 엔티티와 충돌시 데미지를 입힘
 */
public class ShotEntity extends Entity {
    private double moveSpeed = -300;
    private Game game;
    private GamePlay gamePlay;
    private Entity owner;

    // GamePlay 컨텍스트에서 사용하는 기본 탄 생성자
    public ShotEntity(GamePlay gamePlay, String sprite, int x, int y, Entity owner) {
        super(sprite, x, y);
        this.gamePlay = gamePlay;
        this.owner = owner;
        this.dy = moveSpeed;
    }

    // Game 컨텍스트와 임의 방향을 가지는 탄 생성자
    public ShotEntity(Game game, String sprite, int x, int y, double dx, double dy) {
        super(sprite, x, y);
        this.game = game;
        this.dx = dx;
        this.dy = dy;
    }

    // 기본 스프라이트를 사용하는 축약 생성자
    public ShotEntity(Game game, int x, int y, double dx, double dy) {
        this(game, "sprites/shot.gif", x, y, dx, dy);
    }

    // 발사 주체를 함께 넘길 수 있는 축약 생성자
    public ShotEntity(Game game, int x, int y, double dx, double dy, Entity owner) {
        this(game, "sprites/shot.gif", x, y, dx, dy);
        this.owner = owner;
    }

    // 이 탄을 발사한 엔티티를 반환
    public Entity getOwner() {
        return owner;
    }

    // 일정 높이 위로 벗어나면 화면 밖 처리로 제거
    @Override
    public void move(long delta) {
        super.move(delta);
        if (y < -100) {
            if (gamePlay != null) {
                gamePlay.removeEntity(this);
            } else if (game != null) {
                game.getGamePlay().removeEntity(this);
            }
        }
    }

    /**
     * 에일리언 엔티티와 충돌할 경우
     * 탄을 제거하고 외계인 체력을 감소시키며 체력이 0 이하면 외계인을 제거하고 점수/상태를 갱신
     * @param other The entity with which this entity collided.
     */
    @Override
    public void collidedWith(Entity other) {
        if (other instanceof AlienEntity) {
            // 탄 제거
            if (gamePlay != null) {
                gamePlay.removeEntity(this);
            } else if (game != null) {
                game.getGamePlay().removeEntity(this);
            }

            // 외계인 데미지 처리
            AlienEntity alien = (AlienEntity) other;
            alien.takeDamage(30);

            // 체력이 0 이하가 되면 외계인 제거 및 킬 알림
            if (alien.getHealth() <= 0) {
                if (gamePlay != null) {
                    gamePlay.notifyAlienKilled(other);
                    gamePlay.removeEntity(other);
                } else if (game != null) {
                    game.getGamePlay().notifyAlienKilled(other);
                    game.getGamePlay().removeEntity(other);
                }
            }
        }
    }
}
