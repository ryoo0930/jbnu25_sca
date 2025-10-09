package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

public class BossShotEntity extends Entity {
    private Game game;

    public BossShotEntity(Game game, String sprite, int x, int y, double dx, double dy) {
        super(sprite, x, y);
        this.game = game;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void move(long delta) {
        super.move(delta);
        if (y > 600 || x < -100 || x > 800) {
            game.removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShipEntity) {
            game.removeEntity(this);
            game.notifyDeath();
        }
    }
}