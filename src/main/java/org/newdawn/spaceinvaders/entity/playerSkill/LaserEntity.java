package org.newdawn.spaceinvaders.entity.playerSkill;

import java.awt.Graphics;
import java.awt.Rectangle;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.boss.HardBossEntity;
import org.newdawn.spaceinvaders.entity.boss.NormalBossEntity;
import org.newdawn.spaceinvaders.entity.boss.EasyBossEntity;
import org.newdawn.spaceinvaders.entity.EasyPassingAlienEntity;
import org.newdawn.spaceinvaders.entity.NormalPassingAlienEntity;
import org.newdawn.spaceinvaders.entity.HardPassingAlienEntity;
import org.newdawn.spaceinvaders.entity.boss.EasyMidBossEntity;
import org.newdawn.spaceinvaders.entity.boss.NormalMidBossEntity;
import org.newdawn.spaceinvaders.entity.boss.MidBossEntity;
import org.newdawn.spaceinvaders.utility.SpriteStore;

public class LaserEntity extends Entity {
    private Game game;
    private ShipEntity ship;

    private long endTimeMillis;

    private Sprite tileSprite;
    private int tileW;
    private int tileH;

    // Ship 기준 위치 보정
    private int yOffset = -30;

    // 레이저 지속시간/판정시간/데미지 제어
    private long lastDamageTick;               // 마지막으로 피해를 준 시간

    private long damageIntervalMillis = 100L;  // 판정 0.1초에 1번만 피해 적용
    private int damagePerTick = 100;          // 틱당 피해량: 현재 100

    public LaserEntity(GamePlay gamePlay, ShipEntity ship, long durationMillis) {
        super("sprites/laser.gif", (int) ship.getX(), (int) ship.getY());
        this.game = gamePlay.getGame();
        this.ship = ship;
        this.endTimeMillis = System.currentTimeMillis() + durationMillis;

        this.tileSprite = SpriteStore.get().getSprite("sprites/laser.gif");
        this.tileW = tileSprite.getWidth();
        this.tileH = tileSprite.getHeight();
        this.lastDamageTick = 0L;
        this.dx = 0;
        this.dy = 0;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > endTimeMillis;
    }

    // Ship 위치를 따라가도록 좌표만 갱신
    public void move(long delta) {
        int shipW = (ship.getSprite() != null ? ship.getSprite().getWidth() : 0);
        int laserW = (this.tileSprite != null ? this.tileSprite.getWidth() : 0);
        int centerOffset = (shipW - laserW) / 2;


        this.x = ship.getX() + centerOffset;
        this.y = ship.getY() + yOffset;

        // 지속시간 종료 시 제거
        if(isExpired()) {
            game.getGamePlay().removeEntity(this);
            return;
        }

        // 데미지는 프레임마다가 아니라 주기적으로만 적용
        long now = System.currentTimeMillis();
        if(now -lastDamageTick >=damageIntervalMillis) {
            lastDamageTick = now;

            // 기존 충돌 판정 그대로 사용
            for (Object o : game.getGamePlay().getEntities()) {
                Entity e = (Entity) o;
                if (e == this) continue;

                if (e instanceof AlienEntity) {
                    if (this.collidesWith(e)) {
                        ((AlienEntity) e).takeDamage(damagePerTick); // 에일리언 HP 기반 피해
                        AlienEntity a = (AlienEntity) e;
                        if (a.getHealth() <= 0) {                    // 체력 0이하가 될 경우 처치
                            game.getGamePlay().notifyAlienKilled(a);               // 점수 상태 갱신
                            game.getGamePlay().removeEntity(a);                    // 엔티티 제거
                            continue;                                // 제거된 엔티티는 이후 로직에서 생략
                        }
                    }
                }

                if (e instanceof HardBossEntity) {
                    if (this.collidesWith(e)) {
                        ((HardBossEntity) e).takeDamage(damagePerTick); // 보스 피해

                    }
                }
                if (e instanceof NormalBossEntity) {
                    if (this.collidesWith(e)) {
                        ((NormalBossEntity) e).takeDamage(damagePerTick);
                    }
                }
                if (e instanceof EasyBossEntity) {
                    if (this.collidesWith(e)) {
                        ((EasyBossEntity) e).takeDamage(damagePerTick);
                    }
                }
                if (e instanceof EasyPassingAlienEntity) {
                    if (this.collidesWith(e)) {
                        ((EasyPassingAlienEntity) e).takeDamage(damagePerTick);
                    }
                }
                if (e instanceof NormalPassingAlienEntity) {
                    if (this.collidesWith(e)) {
                        ((NormalPassingAlienEntity) e).takeDamage(damagePerTick);
                    }
                }
                if (e instanceof HardPassingAlienEntity) {
                    if (this.collidesWith(e)) {
                        ((HardPassingAlienEntity) e).takeDamage(damagePerTick);
                    }
                }
                if (e instanceof EasyMidBossEntity) {
                    if (this.collidesWith(e)) {
                        ((EasyMidBossEntity) e).takeDamage(damagePerTick);
                    }
                }
                if (e instanceof NormalMidBossEntity) {
                    if (this.collidesWith(e)) {
                        ((NormalMidBossEntity) e).takeDamage(damagePerTick);
                    }
                }
                if (e instanceof MidBossEntity) {
                    if (this.collidesWith(e)) {
                        ((MidBossEntity) e).takeDamage(damagePerTick);
                    }
                }
            }
        }
    }

    // Ship 앞에서 화면 상단까지 laser.gif를 세로로 이어 붙여서 그림
    public void draw(Graphics g) {
        if (isExpired()) return;

        int screenTop = 0;
        int tileHeight = (tileSprite != null ? tileSprite.getHeight() : 0);
        int yStart = (int) this.y;
        for (int ty = yStart; ty >= screenTop - tileHeight; ty -= tileHeight) {
            if (tileSprite != null) {
                tileSprite.draw(g, (int) this.x, ty);
            }
        }
    }

    // 레이저의 판정 영역(수직 기둥)
    private Rectangle getBeamRect() {
        // 화면 위쪽부터 ship 코앞까지 직사각형
        int bx = (int) this.x;

        // ship 중앙 정렬을 위해 오프셋 적용
        int shipW = (ship.getSprite() != null ? ship.getSprite().getWidth() : 0);
        int laserW = (this.tileSprite != null ? this.tileSprite.getWidth() : 0);
        int centerOffset = (shipW - laserW) / 2;
        bx = (int) ship.getX() + centerOffset;
    //    this.x = ship.getX() + centerOffset + 1;

        int byTop = 0;
        int byBottom = (int) this.y + tileH;
        int bWidth = Math.max(6, laserW > 0 ? laserW : tileW); // 폭을 6픽셀 이상으로 제한
        int bHeight = Math.max(0, byBottom - byTop);

        return new Rectangle(bx, byTop, bWidth, bHeight);
    }

    // 레이저-적 충돌 체크
    public boolean collidesWith(Entity other) {
        if (other instanceof ShipEntity) return false;

        Rectangle beam = getBeamRect();

        int w = (other.getSprite() != null ? other.getSprite().getWidth() : 1);
        int h = (other.getSprite() != null ? other.getSprite().getHeight() : 1);
        Rectangle r = new Rectangle((int) other.getX(), (int) other.getY(), w, h);

        return beam.intersects(r);
    }

    // 적에 적중시관통
    public void collidedWith(Entity other) {
    }
}
