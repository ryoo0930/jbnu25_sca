package org.newdawn.spaceinvaders.entity.BossSkill;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.boss.HardBossEntity;
import org.newdawn.spaceinvaders.event.EventBus;
import org.newdawn.spaceinvaders.event.PlayerHitEvent;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

/**
 * 보스가 발사하는 회전형 레이저 엔티티
 * 일정 시간 동안 유지되며, 회전된 히트박스로 플레이어와의 충돌을 판정한다.
 */

public class BossLaserEntity extends Entity {
    private final Game game;
    private final long endTimeMillis;
    private boolean used = false;
    private final double angle;

    /**
     *
     * @param game                  게임 인스턴스
     * @param boss                  레이저를 발사하는 보스
     * @param laserSpriteRef        레이저 스프라이트 경로
     * @param durationMillis        레이저 유지 시간
     * @param damageIntervalMillis  데미지 간격
     * @param damagePerTick         틱당 데미지
     * @param angle                 레이저 발사 각도
     */
    public BossLaserEntity(
            Game game,
            Entity boss,
            String laserSpriteRef,
            long durationMillis,
            long damageIntervalMillis,
            int damagePerTick,
            double angle
    ) {
        // 레이저의 논리적 위치를 보스 중앙으로 설정
        super(laserSpriteRef, (int) (boss.getX() + boss.getSprite().getWidth() / 2), (int) (boss.getY() + boss.getSprite().getHeight() / 2));
        this.game = game;
        this.endTimeMillis = System.currentTimeMillis() + durationMillis;
        this.angle = angle;

        // 각도 기반으로 레이저 진행 방향 설정
        double speed = 800;
        this.dx = (float) (Math.cos(angle) * speed);
        this.dy = (float) (Math.sin(angle) * speed);
    }

    // 레이저가 수명을 초과했는지 여부
    private boolean isExpired() {
        return System.currentTimeMillis() > endTimeMillis;
    }

    public void move(long delta) {
        super.move(delta);

        if (isExpired() || y > 600 || y < 0 || x < 0 || x > 800) {
            game.getGamePlay().removeEntity(this);
        }
    }

    // 스파라이트 중심이 (x, y)가 되도록 보정하고, 각도를 이용해 회전된 레이저를 그림
    @Override
    public void draw(Graphics g) {
        // 스프라이트의 중심이 (x, y)에 오도록 위치를 보정하여 그림
        int drawX = (int) x - sprite.getWidth() / 2;
        int drawY = (int) y - sprite.getHeight() / 2;
        sprite.draw(g, drawX, drawY, angle + Math.PI / 2);
    }

    // 회전된 레이저 히트박스와 ShipEntity의 히트박스를 Area 기반으로 판정
    @Override
    public boolean collidesWith(Entity other) {
        if (used || !(other instanceof ShipEntity)) {
            return false;
        }

        // 1. 자신의 회전된 히트박스 Area 생성
        // 히트박스를 (0,0) 기준으로 생성 후, 실제 위치와 각도로 변환
        int hitboxWidth = (int) (sprite.getWidth() * 0.5);
        int hitboxHeight = (int) (sprite.getHeight() * 0.8);
        Rectangle laserRect = new Rectangle(-hitboxWidth / 2, -hitboxHeight / 2, hitboxWidth, hitboxHeight);
        
        AffineTransform tx = new AffineTransform();
        tx.translate(x, y); // 실제 중심으로 이동
        tx.rotate(angle + Math.PI / 2); // 중심 기준으로 회전
        
        Area laserArea = new Area(laserRect);
        laserArea.transform(tx);

        // 2. 상대방의 히트박스 Area 생성 (ShipEntity는 회전하지 않음)
        Area otherArea = new Area(other.getHitbox());

        // 3. 충돌 검사(충돌 영역이 비어있지 않으면 충돌)
        laserArea.intersect(otherArea);
        return !laserArea.isEmpty();
    }

    // 플레이어와 실제로 충돌했을 때 한 번만 판정하고, 레이저를 제거한 뒤 PlayHitEvent를 발행한다.
    public void collidedWith(Entity other) {
        if (used) {
            return;
        }
        if (other instanceof ShipEntity) {
            used = true;
            game.getGamePlay().removeEntity(this);
            EventBus.getInstance().publish(new PlayerHitEvent());
        }
    }
}