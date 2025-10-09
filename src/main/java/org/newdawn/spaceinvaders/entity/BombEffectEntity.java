package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import java.util.HashMap;
import java.util.Map;
import java.awt.Graphics;

/**
 * 폭탄 적중 시 생성되는 범위 폭발 이펙트.
 * - lifeMs 동안 유지되며
 * - 반경 내 모든 적에게 hitIntervalMs 간격으로 최대 maxHitsPerTarget만큼 피해
 */
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

    /** 폭발 유지 및 범위 판정 */
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
            if (!(e instanceof AlienEntity)) continue;

            int ex = (int) (e.getX() + (e.sprite != null ? e.sprite.getWidth() : 0) / 2.0);
            int ey = (int) (e.getY() + (e.sprite != null ? e.sprite.getHeight() : 0) / 2.0);

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

    /** 실제 데미지/처치 처리 (보스 없음 경우 가정 일반 적 즉시 제거) */
        private void applyHit(Entity target) {
            game.notifyAlienKilled(target);
            game.removeEntity(target);
    }

        public void collidedWith(Entity other) {
        // 고정 이펙트: 추가 충돌 처리 없음
    }
}