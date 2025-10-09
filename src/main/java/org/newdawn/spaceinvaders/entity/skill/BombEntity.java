package org.newdawn.spaceinvaders.entity.skill;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;

/**
 * C 키로 발사되는 폭탄 투사체.
 * - 위로 직진
 * - 적과 충돌하면 사라지고 BombEffectEntity(범위 폭발)를 생성
 * - @Override 사용하지 않음 (수업 수준)
 */
public class BombEntity extends Entity {
    private Game game;
    private boolean exploded;

    public BombEntity(Game game, String spriteRef, int x, int y, double vx, double vy) {
        super(spriteRef, x, y);
        this.game = game;
        this.dx = vx;
        this.dy = vy;
        this.exploded = false;
    }

    /** 이동 처리 */
    public void move(long delta) {
        if (exploded) return;

        // 기본 이동
        super.move(delta);

        // 화면 위로 벗어나면 제거 (ShotEntity와 유사)
        if (y < -100) {
            game.removeEntity(this);
        }
    }

    /** 충돌 처리 */
    public void collidedWith(Entity other) {
        if (exploded) return;

        // 적과 충돌 시 폭발 생성
        if (other instanceof AlienEntity) {
            explode();
        }
    }

    /** 폭발 생성 */
    private void explode() {
        exploded = true;

        int centerX = (int) (getX() + (sprite != null ? sprite.getWidth() : 0) / 2.0);
        int centerY = (int) (getY() + (sprite != null ? sprite.getHeight() : 0) / 2.0);

        // 폭발 이펙트 생성 (중앙 정렬은 이펙트 쪽에서 처리)
        BombEffectEntity boom = new BombEffectEntity(
                game,
                "sprites/BoomEffect.gif",
                centerX, centerY,
                100L,   // 지속(ms): 0.1초
                4,      // 대상별 최대 히트 수
                25L    // 대상별 히트 간격(ms):
        );

        // Game → GamePlay에 추가 위임 (Game.addEntity 필요)
        game.addEntity(boom);

        // 자신 제거
        game.removeEntity(this);
    }
}