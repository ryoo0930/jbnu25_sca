package org.newdawn.spaceinvaders.entity.playerSkill;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.boss.HardBossEntity;
import org.newdawn.spaceinvaders.entity.boss.NormalBossEntity;
import org.newdawn.spaceinvaders.entity.boss.EasyBossEntity;
import org.newdawn.spaceinvaders.entity.EasyPassingAlienEntity;
import org.newdawn.spaceinvaders.entity.NormalPassingAlienEntity;
import org.newdawn.spaceinvaders.entity.HardPassingAlienEntity;
import org.newdawn.spaceinvaders.entity.boss.EasyMidBossEntity;
import org.newdawn.spaceinvaders.entity.boss.NormalMidBossEntity;
import org.newdawn.spaceinvaders.entity.boss.MidBossEntity;

public class BombEntity extends Entity {
    private Game game;
    private boolean exploded;

    // 폭발 이펙트에 넘길 데미지
    private int   effectDamagePerHit = 5000;  // 수치당 데미지
    private long  effectDurationMs   = 100;  //  지속시간
    private long  effectIntervalMs   = 100;  // 판전 간격

    public BombEntity(GamePlay gamePlay, String sprite, int x, int y, int dx, int dy) {
        super(sprite, x, y);
        this.game = gamePlay.getGame();
    }

    // 이동 처리
    public void move(long delta) {
        if (exploded) return;

        // 기본 이동
        super.move(delta);

        // 화면 위로 벗어나면 제거 (ShotEntity와 유사)
        if (y < -100) {
            game.getGamePlay().removeEntity(this);
        }
    }

    // 충돌처리
    public void collidedWith(Entity other) {
        if (exploded) return;

        // 적과 충돌 시 폭발 생성
        if (other instanceof AlienEntity) {
            explode();
            return;
        }
        if (other instanceof HardBossEntity
                || other instanceof NormalBossEntity
                || other instanceof EasyBossEntity
                || other instanceof MidBossEntity
                || other instanceof NormalMidBossEntity
                || other instanceof EasyMidBossEntity
                || other instanceof HardPassingAlienEntity
                || other instanceof NormalPassingAlienEntity
                || other instanceof EasyPassingAlienEntity
        ) {
            explode();
            return;
        }
    }
    // 폭발 생성 (BombEffect로 변경, Bomb제거)
    private void explode() {
        exploded = true;

        int centerX = (int) (getX() + (sprite != null ? sprite.getWidth() : 0) / 2.0);
        int centerY = (int) (getY() + (sprite != null ? sprite.getHeight() : 0) / 2.0);

        // 폭발 이펙트 생성 (중앙 정렬은 이펙트 쪽에서 처리)
        BombEffectEntity boom = new BombEffectEntity(
                game,
                "sprites/BoomEffect.gif",
                centerX, centerY,
                effectDurationMs,  // 지속시간
                effectDamagePerHit,    // 데미지
                effectIntervalMs   // 판정 간격
        );

        // Game → GamePlay에 추가 위임 (Game.addEntity 필요)
        game.getGamePlay().addEntity(boom);
        // 자신 제거
        game.getGamePlay().removeEntity(this);
    }
}