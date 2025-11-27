package org.newdawn.spaceinvaders.entity;

import java.awt.Rectangle;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.event.EventBus;
import org.newdawn.spaceinvaders.event.PlayerHitEvent;

public class BossShotEntity extends Entity {
    	private Game game;
    	/** True if this shot has been "used", i.e. its hit something */
    	private boolean used = false;
    public BossShotEntity(Game game, String sprite, int x, int y, double dx, double dy) {
        super(sprite, x, y);
        this.game = game;
        this.dx = dx;
        this.dy = dy;
    }

	public void move(long delta) {
		// proceed with normal move
		super.move(delta);
		
		// if we shot off the screen, remove ourselfs
		if (y < -100) {
			game.getGamePlay().removeEntity(this);
		}
	}
	
	/**
	 * Notification that this shot has collided with another
	 * entity
	 * 
	 * @parma other The other entity with which we've collided
	 */
	public void collidedWith(Entity other) {
		// prevents double kills, if we've already hit something,
		// don't collide
		if (used) {
			return;
		}
		
		// if we've hit an alien, kill it!
		if (other instanceof ShipEntity) {
			// remove the affected entities
			used = true;
			game.getGamePlay().removeEntity(this);
			EventBus.getInstance().publish(new PlayerHitEvent());
		}
	}
}