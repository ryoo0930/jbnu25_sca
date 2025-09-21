package org.newdawn.spaceinvaders.entity.shotStrategy;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.AlienShotEntity;
import org.newdawn.spaceinvaders.entity.Entity;

public class DefaultShotStrategy implements InnerShotStrategy {
    private final Game game;
    private long lastFire = 0;
    private final long firingInterval;
    private final double shotSpeed = 300.0;

    public DefaultShotStrategy(Game game, long firingInterval){
        this.game = game;
        this.firingInterval = firingInterval;
    }

    @Override
    public void tryToFire(Entity alien) {
        if(System.currentTimeMillis() - lastFire < firingInterval){
            return;
        }
        
        Entity player = game.getShip();
        if(player == null) return;

        lastFire = System.currentTimeMillis();

        double targetX = player.getX() - alien.getX();
        double targetY = player.getY() - alien.getY();

        double distance = Math.sqrt(targetX*targetX + targetY*targetY);
        double dx = (targetX / distance);
        double dy = (targetY / distance);

        double finalDx = dx * shotSpeed;
        double finalDy = dy * shotSpeed;

        AlienShotEntity shot = new AlienShotEntity(game, "sprites/shot1.gif", alien.getX() + 10, alien.getY() + 30, finalDx, finalDy);
        game.addEntity(shot);
    }
}
