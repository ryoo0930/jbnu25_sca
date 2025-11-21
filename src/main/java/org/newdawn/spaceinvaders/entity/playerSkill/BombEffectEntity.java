package org.newdawn.spaceinvaders.entity.playerSkill;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.Damageable;

import java.util.HashMap;
import java.util.Map;
import java.awt.Graphics;

public class BombEffectEntity extends Entity {
    private Game game;

    private long lifeMs;
    private int maxHitsPerTarget;
    private long hitIntervalMs;

    private long startTs;

    // 대상별 히트 카운트/시간
    private Map<Entity, Integer> hitsCount = new HashMap<Entity, Integer>();
    private Map<Entity, Long> lastHitTs = new HashMap<Entity, Long>();

    // 판정 반경
    private int radius;
    private double scale = 5.0;  // 폭발 크기
    private int damagePerHit = 2000;  // 폭발 데미지
    public BombEffectEntity(Game game, String spriteRef,
                            int centerX, int centerY,
                            long lifeMs, int maxHitsPerTarget, long hitIntervalMs) {
        super(spriteRef, centerX, centerY);
        this.game = game;
        this.lifeMs = lifeMs;
        this.maxHitsPerTarget = maxHitsPerTarget;
        this.hitIntervalMs = hitIntervalMs;

        this.startTs = System.currentTimeMillis();

        // 스프라이트 중심 기준 배치
        int w = (sprite != null ? sprite.getWidth() : 0);
        int h = (sprite != null ? sprite.getHeight() : 0);
        this.x = centerX - (w * scale) / 2.0;
        this.y = centerY - (h * scale) / 2.0;

        // 판정 반경 스케일 반영
        this.radius = (int) Math.round((Math.max(w, h) / 2.0) * scale);

        this.dx = 0;
        this.dy = 0;
    }

    // 폭발 유지 및 범위 판정
    public void move(long delta) {
        long now = System.currentTimeMillis();

        // 수명 종료
        if (now - startTs >= lifeMs) {
            game.removeEntity(this);
            return;
        }

        // 중심 좌표
        int cx = (int) (x + (sprite != null ? sprite.getWidth() : 0) * scale / 2.0);
        int cy = (int) (y + (sprite != null ? sprite.getHeight() : 0) * scale / 2.0);

        // 게임의 엔티티 전체 순회 (Game.getEntities 필요)
        java.util.List list = game.getEntities();
        for (int i = 0; i < list.size(); i++) {
            Entity e = (Entity) list.get(i);
            if (e == this) continue;
            if (!(e instanceof Damageable)) continue;

            int ex = (int) (e.getX() + (e.getSprite() != null ? e.getSprite().getWidth() : 0) / 2.0);
            int ey = (int) (e.getY() + (e.getSprite() != null ? e.getSprite().getHeight() : 0) / 2.0);

            int dx = ex - cx;
            int dy = ey - cy;

            int dstW   = (int) Math.round((sprite != null ? sprite.getWidth()  : 0) * scale);
            int dstH   = (int) Math.round((sprite != null ? sprite.getHeight() : 0) * scale);
            int left   = (int) x;
            int top    = (int) y;
            int right  = left + dstW;
            int bottom = top  + dstH;


            if (ex >= left && ex < right && ey >= top && ey < bottom) {
                Integer c = hitsCount.get(e);
                if (c == null) c = 0;
                if (c >= maxHitsPerTarget) continue;

                Long last = lastHitTs.get(e);
                if (last == null) last = 0L;

                if (now - last >= hitIntervalMs) {
                    applyHit(e);
                    hitsCount.put(e, c + 1);
                    lastHitTs.put(e, now);
                }
            }
        }
    }

    public void draw(Graphics g) {
        if (sprite == null) return;
        int srcW = sprite.getWidth();
        int srcH = sprite.getHeight();
        if (srcW <= 0 || srcH <= 0) return;

        int dstW = (int) Math.round(srcW * scale);
        int dstH = (int) Math.round(srcH * scale);

        int startX = (int) x;
        int startY = (int) y;
        int endX = startX + dstW;
        int endY = startY + dstH;

        int stepX = Math.max(1, srcW / 2);
        int stepY = Math.max(1, srcH / 2);

        for (int yy = startY; yy < endY; yy += stepY) {
            for (int xx = startX; xx < endX; xx += stepX) {
                sprite.draw(g, xx, yy);
            }
        }
    }

    //데미지 처리
    private void applyHit(Entity target) {
        //  체력0 이하 시 처치/삭제까지 수행
        if (!(target instanceof Damageable)) {
            return;
        }
        Damageable d = (Damageable) target;
            try {
                d.takeDamage(damagePerHit);
        } catch (Throwable ignore) {}

        // Alien인 경우에만 사망/점수 처리까지 수행
        if (target instanceof AlienEntity) {
            AlienEntity a = (AlienEntity) target;
            if (a.getHealth() <= 0) {
                game.notifyAlienKilled(a);
                game.removeEntity(a);
            }
        }
    }


    public void collidedWith(Entity other) {
        // 고정 이펙트: 추가 충돌 처리 없음 (기존 유지)
    }
}