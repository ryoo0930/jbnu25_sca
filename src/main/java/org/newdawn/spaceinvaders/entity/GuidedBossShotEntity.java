package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class GuidedBossShotEntity extends Entity {
    private Game game;
    private double angle = 0; // 총알의 회전 각도

    public GuidedBossShotEntity(Game game, String sprite, int x, int y, double dx, double dy) {
        super(sprite, x, y);
        this.game = game;
        this.dx = dx;
        this.dy = dy;
        // 속도 벡터를 기반으로 각도 계산
        this.angle = Math.atan2(dy, dx);
    }

    @Override
    public void move(long delta) {
        super.move(delta);
        if (y < -100 || y > 700 || x < -100 || x > 900) {
            game.removeEntity(this);
        }
    }

    @Override
    public void draw(Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g.create(); // 그래픽 컨텍스트 복사
            // 회전의 중심을 엔티티의 중심으로 설정
            int centerX = (int) (x + sprite.getWidth() / 2);
            int centerY = (int) (y + sprite.getHeight() / 2);
            // 회전 적용 (라디안 단위). 이미지 방향을 고려하여 90도(Math.PI / 2)를 더함
            g2d.rotate(angle + Math.PI / 2, centerX, centerY);
            sprite.draw(g2d, (int) x, (int) y);
            g2d.dispose(); // 복사된 그래픽 컨텍스트 해제
        } else {
            // Graphics2D가 아닌 경우, 회전 없이 그림
            super.draw(g);
        }
    }


    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShipEntity) {
            ShipEntity ship = (ShipEntity) other;
            Rectangle myBounds = new Rectangle((int) this.x, (int) this.y, this.sprite.getWidth(), this.sprite.getHeight());

            if (myBounds.intersects(ship.getHitbox())) {
                game.removeEntity(this);
                game.notifyDeath();
            }
        }
    }
}
