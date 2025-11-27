package org.newdawn.spaceinvaders.entity.BossSkill;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.event.EventBus;
import org.newdawn.spaceinvaders.event.PlayerHitEvent;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * 보스가 사용하는 유도탄(또는 방향성 탄막) 엔티티
 * dx, dy 속도 벡터를 기반으로 회전된 스프라이트를 그리며, 플레이어와 충돌 시 피격 이벤트를 발생시킨다.
 */
public class GuidedBossShotEntity extends Entity {
    private Game game;
    // 화면에 그릴 때 사용할 총알의 회전 각도
    private double angle = 0;
    // 이미 충돌 처리된 탄인지 여부 (중복 피격 방지)
    private boolean used = false;

    /**
     *
     * @param game      게임 인스턴스
     * @param sprite    탄막 스프라이트 경로
     * @param x         시작 x 좌표
     * @param y         시작 y 좌표
     * @param dx        x 방향 속도
     * @param dy        y 방향 속도
     */
    public GuidedBossShotEntity(Game game, String sprite, int x, int y, double dx, double dy) {
        super(sprite, x, y);
        this.game = game;
        this.dx = dx;
        this.dy = dy;
        // 속도 벡터를 기반으로 화면 상에서의 회전 각도 계산
        this.angle = Math.atan2(dy, dx);
    }

    // 탄 이동 및 화면 밖으로 나간 경우 제거 처리
    @Override
    public void move(long delta) {
        super.move(delta);
        if (y < -100 || y > 700 || x < -100 || x > 900) {
            game.getGamePlay().removeEntity(this);
        }
    }

    /** 속도 방향에 맞춰서 스프라이트를 회전시켜 그린다.
     * Graphics2D가 아닐 경우에는 회전 없이 기본 draw를 사용한다.
     */
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


    // 플레이어와 충돌했을 때 한 번만 판정하고 탄을 제거한 뒤 PlayHitEvnet를 발행한다.
    @Override
    public void collidedWith(Entity other) {
        if (used) {
            return;
        }
        if (other instanceof ShipEntity) {
            ShipEntity ship = (ShipEntity) other;
            Rectangle myBounds = new Rectangle((int) this.x, (int) this.y, this.sprite.getWidth(), this.sprite.getHeight());

            if (myBounds.intersects(ship.getHitbox())) {
                used = true;
                game.getGamePlay().removeEntity(this);
                EventBus.getInstance().publish(new PlayerHitEvent());
            }
        }
    }
}
