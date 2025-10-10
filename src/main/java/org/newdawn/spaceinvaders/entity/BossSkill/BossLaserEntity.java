package org.newdawn.spaceinvaders.entity.BossSkill;


import java.awt.Graphics;
import java.awt.Rectangle;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.boss.HardBossEntity;
import org.newdawn.spaceinvaders.utility.SpriteStore;

public class BossLaserEntity extends Entity {
    private final Game game;
    private final HardBossEntity boss;

    private final long endTimeMillis;
    private final long damageIntervalMillis;
    private final int  damagePerTick;

    private long lastDamageTick = 0L;

    private final Sprite tileSprite;
    private final int tileW;
    private final int tileH;

    // 보스 스프라이트 기준 위치 보정
    private final int xOffset = 0;   // 레이저를 약간 좌/우로 이동
    private final int yOffset = 0;   // 보스 코앞/아래쪽 이동

    public BossLaserEntity(
            Game game,
            HardBossEntity boss,
            String laserSpriteRef,
            long durationMillis,
            long damageIntervalMillis,
            int damagePerTick
    ) {
        super(laserSpriteRef, (int) boss.getX(), (int) boss.getY());
        this.game = game;
        this.boss = boss;

        this.endTimeMillis = System.currentTimeMillis() + durationMillis;
        this.damageIntervalMillis = damageIntervalMillis;
        this.damagePerTick = damagePerTick;

        this.tileSprite = SpriteStore.get().getSprite(laserSpriteRef);
        this.tileW = tileSprite.getWidth();
        this.tileH = tileSprite.getHeight();

        this.dx = 0; this.dy = 0;
    }

    private boolean isExpired() {
        return System.currentTimeMillis() > endTimeMillis;
    }

    public void move(long delta) {
        // 보스 위치를 따라감
        int bossW = (boss.getSprite() != null ? boss.getSprite().getWidth() : 0);
        int laserW = (this.tileSprite != null ? this.tileSprite.getWidth() : 0);
        int centerOffset = (bossW - laserW) / 2;

        this.x = boss.getX() + centerOffset + xOffset;
        this.y = boss.getY() + boss.getSprite().getHeight() + yOffset; // 보스 아랫부분부터 쏘기

        if (isExpired()) {
            game.removeEntity(this);
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastDamageTick >= damageIntervalMillis) {
            lastDamageTick = now;

            // 플레이어에게만 데미지
            for (Object o : game.getEntities()) {
                Entity e = (Entity) o;
                if (e == this) continue;

                if (e instanceof ShipEntity) {
                    if (collidesWith(e)) {
                    }
                }
            }
        }
    }

    public void draw(Graphics g) {
        if (isExpired()) return;

        int screenBottom = game.getHeight(); // Game에 화면 높이를 돌려주는 메서드가 없다면 600 등 상수 사용
        int tileHeight = (tileSprite != null ? tileSprite.getHeight() : 0);

        // 보스 아랫부분(y)에서 화면 하단까지 아래 방향으로 이어붙이기
        int yStart = (int) this.y;
        for (int ty = yStart; ty <= screenBottom; ty += tileHeight) {
            if (tileSprite != null) {
                tileSprite.draw(g, (int) this.x, ty);
            }
        }
    }

    private Rectangle getBeamRect() {
        // 보스 아래에서 화면 하단까지
        int bx = (int) this.x;

        int bossW = (boss.getSprite() != null ? boss.getSprite().getWidth() : 0);
        int laserW = (this.tileSprite != null ? this.tileSprite.getWidth() : 0);
        int centerOffset = (bossW - laserW) / 2;
        bx = (int) boss.getX() + centerOffset + xOffset;

        int byTop = (int) (boss.getY() + boss.getSprite().getHeight() + yOffset);
        int byBottom = game.getHeight(); // 화면 하단까지
        int bWidth = Math.max(6, laserW > 0 ? laserW : tileW);
        int bHeight = Math.max(0, byBottom - byTop);

        return new Rectangle(bx, byTop, bWidth, bHeight);
    }

    public boolean collidesWith(Entity other) {
        if (!(other instanceof ShipEntity)) return false;

        Rectangle beam = getBeamRect();

        int w = (other.getSprite() != null ? other.getSprite().getWidth() : 1);
        int h = (other.getSprite() != null ? other.getSprite().getHeight() : 1);
        Rectangle r = new Rectangle((int) other.getX(), (int) other.getY(), w, h);

        return beam.intersects(r);
    }

    public void collidedWith(Entity other) {
    }
}