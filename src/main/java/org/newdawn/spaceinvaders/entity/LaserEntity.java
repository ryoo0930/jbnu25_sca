package org.newdawn.spaceinvaders.entity;

import java.awt.Graphics;
import java.awt.Rectangle;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

/**
 * 레이저(지속형): X키로 3초간 유지되며 Ship 앞에서 위로 뻗는 공격.
 * - Ship을 따라다님
 * - 비관통(동시에 한 기만 타격) (관통으로 바꿀 예정)
 * - ShotEntity와 동일한 효과로 적 처치
 */
public class LaserEntity extends Entity {
    private Game game;
    private ShipEntity ship;

    private long endTimeMillis;

    private Sprite tileSprite;
    private int tileW;
    private int tileH;

    // Ship 기준 위치 보정
    private int xOffset = 10;
    private int yOffset = -30;

    // 과다 타격 방지(비관통): 짧은 쿨타임
    private long lastHitTime = 0L;
    private long hitCooldown = 120L; // ms

    public LaserEntity(Game game, ShipEntity ship, long durationMillis) {
        super("sprites/laser.gif", (int) ship.getX(), (int) ship.getY());
        this.game = game;
        this.ship = ship;
        this.endTimeMillis = System.currentTimeMillis() + durationMillis;

        this.tileSprite = SpriteStore.get().getSprite("sprites/laser.gif");
        this.tileW = tileSprite.getWidth();
        this.tileH = tileSprite.getHeight();

        this.dx = 0;
        this.dy = 0;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > endTimeMillis;
    }

    // Ship 위치를 따라가도록 좌표만 갱신
    public void move(long delta) {
        int shipW = (ship.sprite != null ? ship.sprite.getWidth() : 0);
        int laserW = (this.tileSprite != null ? this.tileSprite.getWidth() : 0);
        int centerOffset = (shipW - laserW) / 2;


        this.x = ship.getX() + centerOffset;
        this.y = ship.getY() + yOffset;
    }

    // Ship 앞에서 화면 상단까지 laser.gif를 세로로 이어 붙여서 그림
    public void draw(Graphics g) {
        int startX = (int) this.x;
        int startY = (int) this.y;

        // Ship 앞에서 위쪽(0)까지 타일링
        for (int yy = startY; yy >= 0; yy -= tileH) {
            tileSprite.draw(g, startX, yy);
        }
    }

    // 레이저의 판정 영역(수직 기둥)
    private Rectangle getBeamRect() {
        // 화면 위쪽부터 ship 코앞까지 직사각형
        int bx = (int) this.x;

        // ship 중앙 정렬을 위해 오프셋 적용
        int shipW = (ship.sprite != null ? ship.sprite.getWidth() : 0);
        int laserW = (this.tileSprite != null ? this.tileSprite.getWidth() : 0);
        int centerOffset = (shipW - laserW) / 2;
        bx = (int) ship.getX() + centerOffset;
        this.x = ship.getX() + centerOffset + 1;

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

        int w = (other.sprite != null ? other.sprite.getWidth() : 1);
        int h = (other.sprite != null ? other.sprite.getHeight() : 1);
        Rectangle r = new Rectangle((int) other.getX(), (int) other.getY(), w, h);

        return beam.intersects(r);
    }

    // 적에 적중시관통
    public void collidedWith(Entity other) {
        if (!(other instanceof AlienEntity)) return;

        long now = System.currentTimeMillis();
        if (now - lastHitTime < hitCooldown) return;
        lastHitTime = now;


        // 적 체력 추가에 따른 수정된 적 제거 로직
        AlienEntity alien = (AlienEntity)other;
        alien.takeDamage(100);

        if(alien.getHealth() <= 0) {
            game.notifyAlienKilled(other);
            game.removeEntity(other);
        }
    }
}
