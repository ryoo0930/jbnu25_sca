package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game; // (수정) GamePlay로 변경
import org.newdawn.spaceinvaders.GamePlay;

/**
 * An entity representing a shot fired by the player's ship
 * * @author Kevin Glass
 */
public class ShotEntity extends Entity {
	/** The vertical speed at which the players shot moves */
	private double moveSpeed = -300;
	/** The game in which this entity exists */
	// (수정) Game game -> GamePlay gamePlay
	private GamePlay gamePlay;
	
	/**
	 * Create a new shot from the player
	 * * @param gamePlay The game in which the shot has been created
	 * @param sprite The sprite representing this shot
	 * @param x The initial x location of the shot
	 * @param y The initial y location of the shot
	 */
	// (수정) Game game -> GamePlay gamePlay
	public ShotEntity(GamePlay gamePlay,String sprite,int x,int y) {
		super(sprite,x,y);
		
		// (수정) this.game = game -> this.gamePlay = gamePlay
		this.gamePlay = gamePlay;
		
		dy = moveSpeed;
	}

    public ShotEntity(Game game, String sprite, int x, int y, double dx, double dy) {
        super(sprite, x, y);
        this.game = game;

        this.dx = dx;   // Entity 쪽 필드 (이미 존재)
        this.dy = dy;
    }
    public ShotEntity(Game game, int x, int y, double dx, double dy) {
        super("sprites/shot.gif", x, y);   // 기본 스프라이트
        this.game = game;
        this.dx = dx;
        this.dy = dy;
    }

	/**
	 * Request that this shot moved based on time elapsed
	 * * @param delta The time that has elapsed since last move
	 */
	public void move(long delta) {
		// proceed with normal move
		super.move(delta);
		
		// if we shot off the screen, remove ourselfs
		if (y < -100) {
			// (수정) game.removeEntity -> gamePlay.removeEntity
			gamePlay.removeEntity(this);
		}
	}
	
	/**
	 * Notification that this shot has collided with another
	 * entity
	 * * @parma other The other entity with which we've collided
	 */
	public void collidedWith(Entity other) {
		// if we've hit an alien, kill it!
		if (other instanceof AlienEntity) {
			// notify the game that the alien has been killed

			// 충돌이 일어날 때 처리
			// (수정) game.removeEntity -> gamePlay.removeEntity
			gamePlay.removeEntity(this);

			AlienEntity alien = (AlienEntity) other;
			alien.takeDamage(30);

			if(alien.getHealth() <= 0) {
				// (수정) game.notifyAlienKilled -> gamePlay.notifyAlienKilled
				gamePlay.notifyAlienKilled(other);
				// (수정) game.removeEntity -> gamePlay.removeEntity
				gamePlay.removeEntity(other);
			}
		}
	}
}