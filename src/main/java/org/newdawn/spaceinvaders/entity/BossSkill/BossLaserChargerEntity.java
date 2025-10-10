package org.newdawn.spaceinvaders.entity.BossSkill;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.boss.HardBossEntity;
import org.newdawn.spaceinvaders.utility.SpriteStore;

import java.awt.Graphics;

public class BossLaserChargerEntity extends Entity {
    private final Game game;
    private final HardBossEntity boss;

    private final long endTimeMillis;

    // 차지 끝난 뒤 쏠 레이저 파라미터
    private final String laserSpriteRef;      // "sprites/BossLaser2.gif"
    private final long   laserDurationMillis; // 레이저 지속
    private final long   laserDamageInterval; // 레이저 판정 주기
    private final int    laserDamagePerTick;  // 레이저 데미지

    // 위치 보정
    private final int xOffset = 0;
    private final int yOffset = 0;

    public BossLaserChargerEntity(
            Game game,
            HardBossEntity boss,
            String chargeSpriteRef,      // "sprites/BossLaser1.gif"
            long chargeMillis,           // 차지(대기) 시간
            String laserSpriteRef,       // "sprites/BossLaser2.gif"
            long laserDurationMillis,
            long laserDamageInterval,
            int  laserDamagePerTick
    ) {
        super(chargeSpriteRef, (int) boss.getX(), (int) boss.getY());
        this.game = game;
        this.boss = boss;

        this.endTimeMillis = System.currentTimeMillis() + chargeMillis;

        this.laserSpriteRef = laserSpriteRef;
        this.laserDurationMillis = laserDurationMillis;
        this.laserDamageInterval = laserDamageInterval;
        this.laserDamagePerTick = laserDamagePerTick;

        this.dx = 0; this.dy = 0;

        if (this.sprite == null) {
            this.sprite = SpriteStore.get().getSprite(chargeSpriteRef);
        }
    }

    private boolean isExpired() {
        return System.currentTimeMillis() > endTimeMillis;
    }

    public void move(long delta) {
        // 보스 위치를 따라감
        int bossW = (boss.getSprite() != null ? boss.getSprite().getWidth() : 0);
        int chargeW = (this.sprite != null ? this.sprite.getWidth() : 0);
        int centerOffset = (bossW - chargeW) / 2;

        this.x = boss.getX() + centerOffset + xOffset;
        this.y = boss.getY() + boss.getSprite().getHeight() + yOffset;

        if (isExpired()) {
            // 차지 끝 → 실제 레이저 생성
            BossLaserEntity beam = new BossLaserEntity(
                    game,
                    boss,
                    laserSpriteRef,
                    laserDurationMillis,
                    laserDamageInterval,
                    laserDamagePerTick
            );
            game.addEntity(beam);
            game.removeEntity(this);
        }
    }

    public void draw(Graphics g) {
        if (sprite == null) return;
        // 차지 스프라이트 1장
        sprite.draw(g, (int) this.x, (int) this.y);
    }

    public void collidedWith(Entity other) {
        // 차지 이펙트는 충돌 반응 없음
    }
}