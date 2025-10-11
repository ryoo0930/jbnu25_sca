package org.newdawn.spaceinvaders.entity.BossSkill;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.boss.HardBossEntity;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

public class BossLaserEntity extends Entity {
    private final Game game;
    private final long endTimeMillis;
    private boolean used = false;
    private final double angle;

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

        double speed = 800;
        this.dx = (float) (Math.cos(angle) * speed);
        this.dy = (float) (Math.sin(angle) * speed);
    }

    private boolean isExpired() {
        return System.currentTimeMillis() > endTimeMillis;
    }

    public void move(long delta) {
        super.move(delta);

        if (isExpired() || y > 600 || y < 0 || x < 0 || x > 800) {
            game.removeEntity(this);
        }
    }

    @Override
    public void draw(Graphics g) {
        // 스프라이트의 중심이 (x, y)에 오도록 위치를 보정하여 그림
        int drawX = (int) x - sprite.getWidth() / 2;
        int drawY = (int) y - sprite.getHeight() / 2;
        sprite.draw(g, drawX, drawY, angle + Math.PI / 2);
    }

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

        // 3. 충돌 검사
        laserArea.intersect(otherArea);
        return !laserArea.isEmpty();
    }

    public void collidedWith(Entity other) {
        if (used) {
            return;
        }
        if (other instanceof ShipEntity) {
            used = true;
            game.removeEntity(this);
            game.notifyDeath();
        }
    }
}