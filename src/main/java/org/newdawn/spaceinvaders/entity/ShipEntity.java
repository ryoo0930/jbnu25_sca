package org.newdawn.spaceinvaders.entity;

import java.awt.Graphics;
import java.awt.Rectangle;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.utility.SpriteStore;

/**
 * The entity that represents the players ship
 * 
 * @author Kevin Glass
 */
public class ShipEntity extends Entity {
	/** The game in which the ship exists */
	private Game game;

	/** 히트박스 스프라이트와 충돌 판정을 위한 객체 */
	private Sprite hitboxSprite;
	private Rectangle hitbox;

	/**
	 * Create a new entity to represent the players ship
	 *  
	 * @param game The game in which the ship is being created
	 * @param ref The reference to the sprite to show for the ship
	 * @param x The initial x location of the player's ship
	 * @param y The initial y location of the player's ship
	 */
	public ShipEntity(Game game,String ref,int x,int y) {
		super(ref,x,y);
		this.game = game;

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
		// apply the movement
		super.move(delta);

		// check boundaries and clamp position
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
		// TODO Auto-generated method stub
		super.draw(g);
		if(hitboxSprite != null) {
			hitboxSprite.draw(g, hitbox.x, hitbox.y);
		}
	}


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
		if (other instanceof AlienEntity) {
			game.notifyDeath();
		}
	}
}