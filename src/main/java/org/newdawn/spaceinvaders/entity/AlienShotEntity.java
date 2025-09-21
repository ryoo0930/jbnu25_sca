package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

public class AlienShotEntity extends Entity{
    private double moveSpeed = 300;
    private Game game;

    public AlienShotEntity(Game game, String sprite, int x, int y, double dx, double dy){
        super(sprite, x, y);

        this.game = game;
        this.dx = dx;
        this.dy = dy;
        
    }

    @Override
    public void move(long delta) {
        // TODO Auto-generated method stub
        super.move(delta);

        if(y > 600 || y < -100 || x > 800 || x < 0) {game.removeEntity(this);}
    }

    @Override
    public void collidedWith(Entity other) {
        // TODO Auto-generated method stub
        if(other instanceof AlienEntity){
            return;
        }

        if(other instanceof ShipEntity){
            game.removeEntity(this);
        }
    }
}
