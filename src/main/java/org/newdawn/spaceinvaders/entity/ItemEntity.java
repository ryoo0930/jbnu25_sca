package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

/**
 * 플레이어가 획득할 수 있는 아이템을 나타내는 엔티티
 * 화면 아래로 떨어지며, ShipEntity가 충돌 시 실제 획득 처리함
 */
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

    @Override
    public void move(long delta) {
        super.move(delta);
        // 화면 아래로 벗어나면 제거
        if (y > 650) {
            game.getGamePlay().removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // Collision logic is handled by ShipEntity
    }
}
