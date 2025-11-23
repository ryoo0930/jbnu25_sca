package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.GamePlay;

public class ShotEntity extends Entity {
    private double moveSpeed = -300;
    private Game game;
    private GamePlay gamePlay;
    private Entity owner;

    public ShotEntity(GamePlay gamePlay, String sprite, int x, int y, Entity owner) {
        super(sprite, x, y);
        this.gamePlay = gamePlay;
        this.owner = owner;
        this.dy = moveSpeed;
    }

    public ShotEntity(Game game, String sprite, int x, int y, double dx, double dy) {
        super(sprite, x, y);
        this.game = game;
        this.dx = dx;
        this.dy = dy;
    }

    public ShotEntity(Game game, int x, int y, double dx, double dy) {
        this(game, "sprites/shot.gif", x, y, dx, dy);
    }

    public ShotEntity(Game game, int x, int y, double dx, double dy, Entity owner) {
        this(game, "sprites/shot.gif", x, y, dx, dy);
        this.owner = owner;
    }

    public Entity getOwner() {
        return owner;
    }

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

    public void collidedWith(Entity other) {
        if (other instanceof AlienEntity) {
            if (gamePlay != null) {
                gamePlay.removeEntity(this);
            } else if (game != null) {
                game.getGamePlay().removeEntity(this);
            }

            AlienEntity alien = (AlienEntity) other;
            alien.takeDamage(30);

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
