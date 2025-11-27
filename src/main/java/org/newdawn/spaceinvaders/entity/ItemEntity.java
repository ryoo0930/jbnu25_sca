package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

public class ItemEntity extends Entity {
    public enum ItemType {
        HEALTH,
        LASER,
        BOMB
    }

    private final Game game;
    private final ItemType type;

    public ItemEntity(Game game, String ref, int x, int y, ItemType type) {
        super(ref, x, y);
        this.game = game;
        this.type = type;
        this.dy = 150; // Move downwards
    }

    public ItemType getType() {
        return type;
    }

    public void move(long delta) {
        super.move(delta);
        if (y > 650) {
            game.getGamePlay().removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // Collision logic is handled by ShipEntity
    }
}
