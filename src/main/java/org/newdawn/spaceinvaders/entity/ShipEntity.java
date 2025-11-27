package org.newdawn.spaceinvaders.entity;

import java.awt.Graphics;
import java.awt.Rectangle;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.utility.SpriteStore;
import org.newdawn.spaceinvaders.entity.ItemEntity;
import org.newdawn.spaceinvaders.event.EventBus;
import org.newdawn.spaceinvaders.event.PlayerHitEvent;


/**
 * 플레이어가 직접 조종하는 기체 엔티티
 * 이동, 화면 경계 클램핑, 히트 박스 기반 충돌 판정을 담당한다.
 * @author Kevin Glass
 */
public class ShipEntity extends Entity {
	/** The game in which the ship exists */
	private Game game;

	/** 히트박스 스프라이트와 충돌 판정을 위한 객체 */
	private Sprite hitboxSprite;
	private Rectangle hitbox;


    /**
     *
     * @param gamePlay  The GamePlay instance the ship belongs to
     * @param ref       The reference to the sprite to show for the ship
     * @param x         The initial x location of the player's ship
     * @param y         The initial y location of the player's ship
     */
	public ShipEntity(GamePlay gamePlay,String ref,int x,int y) {
		super(ref,x,y);
		this.game = gamePlay.getGame();

        // 기체 중앙에 위치하는 작은 히트박스 스프라이트 로드
		this.hitboxSprite = SpriteStore.get().getSprite("sprites/hitbox.gif");
		this.hitbox = new Rectangle();
		updateHitboxPosition();
	}

	/** 히트박스의 위치를 기체 중앙으로 갱신하는 메소드 */
	private void updateHitboxPosition() {
		if (this.sprite == null || this.hitboxSprite == null)
			return;

		int hitboxX = (int) (this.x + (this.sprite.getWidth() - this.hitboxSprite.getWidth()) / 2);
		int hitboxY = (int) (this.y + (this.sprite.getHeight() - this.hitboxSprite.getHeight()) / 2);
		this.hitbox.setBounds(hitboxX, hitboxY, this.hitboxSprite.getWidth(), this.hitboxSprite.getHeight());
	}

	/**
	 * Request that the ship move itself based on an elapsed ammount of
	 * time
	 * 
	 * @param delta The time that has elapsed since last move (ms)
	 */
	public void move(long delta) {
		// 기본 이동 처리
		super.move(delta);

		// 화면 경계 밖으로 나가지 않도록 위치를 클램핑
		if (x < 10) {
			x = 10;
		}
		if (x > 750) {
			x = 750;
		}
		if (y < 10) {
			y = 10;
		}
		if (y > 550) {
			y = 550;
		}

		// 움직임에 따른 히트박스 업데이트
		updateHitboxPosition();
	}

	@Override
	public void draw(Graphics g) {
		// 기체 스프라이트를 먼저 그린뒤,
		super.draw(g);
        // 히트 박스 시각화
		if(hitboxSprite != null) {
			hitboxSprite.draw(g, hitbox.x, hitbox.y);
		}
	}

    // ShipEntity는 기본 사격형이 아닌 별도의 작은 히트박스를 기준으로 충돌을 판정한다.
	@Override
	public boolean collidesWith(Entity other) {
		// 다른 엔티티의 경계 사각형을 가져옵니다.
		Rectangle otherBounds = new Rectangle(
				(int) other.x,
				(int) other.y,
				other.sprite.getWidth(),
				other.sprite.getHeight());
		// 기체 전체가 아닌, 작은 hitbox와 충돌했는지 검사합니다.
		return this.hitbox.intersects(otherBounds);
	}

	public Rectangle getHitbox() {
		return this.hitbox;
	}
	/**
	 * Notification that the player's ship has collided with something
	 * 
	 * @param other The entity with which the ship has collided
	 */
	public void collidedWith(Entity other) {
		// if its an alien, notify the game that the player
		// is dead
		if (other instanceof AlienEntity || other instanceof org.newdawn.spaceinvaders.entity.BossSkill.BossLaserEntity || other instanceof org.newdawn.spaceinvaders.entity.BossShotEntity) {
			EventBus.getInstance().publish(new PlayerHitEvent());
		}

        // 아이템과 충돌시 각 아이템 타입에 따른 효과 적용
		if (other instanceof ItemEntity) {
			ItemEntity item = (ItemEntity) other;
			switch (item.getType()) {
				case HEALTH:
					game.getGamePlay().increaseLife();
					break;
				case LASER:
					game.getGamePlay().increaseLaserCharges();
					break;
				case BOMB:
					game.getGamePlay().increaseBombCharges();
					break;
			}
			game.getGamePlay().removeEntity(other);
		}
	}
}