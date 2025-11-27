package org.newdawn.spaceinvaders.entity.BossSkill;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.boss.HardBossEntity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * 보스 레이저 발사 전에 경고용으로 표시되는 선이다.
 * 실제 충돌 판정은 없으며, 일정 시간 동안만 화면에 표시된다.
 */
public class LaserWarningLineEntity extends Entity {
    private final Game game;
    // 레이저를 발사할 기준이 되는 보스 엔티티
    private final Entity boss;
    // 경고선이 가리키는 방향
    private final double angle;
    // 경고선 표시가 끝나는 시간
    private final long endTimeMillis;

    /**
     *
     * @param game              게임 인스턴스
     * @param boss              경고선을 기준으로 할 보스 엔티티
     * @param angle             레이저가 날아갈 방향
     * @param durationMillis    경고선이 유지될 시간
     */
    public LaserWarningLineEntity(Game game, Entity boss, double angle, long durationMillis) {
        // 스프라이트는 실제로 사용하지 않는 placeholder
        super("sprites/shot.gif", (int) boss.getX(), (int) boss.getY()); // Placeholder sprite
        this.game = game;
        this.boss = boss;
        this.angle = angle;
        this.endTimeMillis = System.currentTimeMillis() + durationMillis;
    }

    // 수명만 관리하고 시간이 지나면 스스로 제거됨
    @Override
    public void move(long delta) {
        // Automatically remove after duration
        if (System.currentTimeMillis() > endTimeMillis) {
            game.getGamePlay().removeEntity(this);
        }
    }

    // 보스의 중심에서 angle 방향으로 뻗는 하얀 선을 그린다.
    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);

        // 선의 시작점 : 보스 스프라이트의 중앙
        int startX = boss.getX() + boss.getSprite().getWidth() / 2;
        int startY = boss.getY() + boss.getSprite().getHeight() / 2;

        // 선의 끝점 : angle 방향으로 화면 밖까지 연장
        int endX = startX + (int) (Math.cos(angle) * 1000);
        int endY = startY + (int) (Math.sin(angle) * 1000);

        g2d.drawLine(startX, startY, endX, endY);
    }

    // 경고선은 다른 엔티티와 상호작용하지 않는다.
    @Override
    public void collidedWith(Entity other) {
    }
}
