package org.newdawn.spaceinvaders.entity;

import java.awt.Rectangle;

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
        // 충돌한 대상이 ShipEntity인지 확인합니다.
        if (other instanceof ShipEntity) {
            ShipEntity ship = (ShipEntity) other;
            // 이제 기체 전체가 아닌, ShipEntity의 작은 히트박스와 충돌했는지 검사합니다.
            Rectangle myBounds = new Rectangle((int) this.x, (int) this.y, this.sprite.getWidth(), this.sprite.getHeight());

            if (myBounds.intersects(ship.getHitbox())) {
                game.removeEntity(this);
                game.notifyDeath();
            }
        }
    }
}