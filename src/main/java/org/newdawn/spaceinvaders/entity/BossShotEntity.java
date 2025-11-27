package org.newdawn.spaceinvaders.entity;

import java.awt.Rectangle;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.event.EventBus;
import org.newdawn.spaceinvaders.event.PlayerHitEvent;

/**
 * 보스가 발사하는 기본 탄막 엔티티
 * 화면 위쪽으로 사라질 때까지 이동하며, 플레이어와 충돌 시 한 번만 피격 판정을 한다.
 */
public class BossShotEntity extends Entity {
    	private Game game;
    	// 이미 충돌 처리된 탄인지 여부(중복 데미지 방지)
    	private boolean used = false;

    /**
     *
     * @param game      게임 인스턴스
     * @param sprite    탄막 스프라이트 경로
     * @param x         시작 x 좌표
     * @param y         시작 y 좌표
     * @param dx        x 방향 속도
     * @param dy        y 방향 속도
     */
    public BossShotEntity(Game game, String sprite, int x, int y, double dx, double dy) {
        super(sprite, x, y);
        this.game = game;
        this.dx = dx;
        this.dy = dy;
    }

    // 탄을 이동시키고, 화면 밖으로 벗어나면 제거한다.
    @Override
	public void move(long delta) {
		// 기본 이동 처리
		super.move(delta);
		
		// 화면 위로 벗어나면 엔티티 제거
		if (y < -100) {
			game.getGamePlay().removeEntity(this);
		}
	}

    /**
     * 다른 엔티티와 충돌했을 때 호출된다.
     * @param other The entity with which this entity collided.
     */
	public void collidedWith(Entity other) {
		// 이미 한 번 사용된 탄이면 더 이상 판정하지 않음
		if (used) {
			return;
		}
		
		// 플레이어와 충돌 시, 탄 제거 + 플레이어 피격 이벤트 발행
		if (other instanceof ShipEntity) {
			used = true;
			game.getGamePlay().removeEntity(this);
			EventBus.getInstance().publish(new PlayerHitEvent());
		}
	}
}