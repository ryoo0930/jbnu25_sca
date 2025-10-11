package org.newdawn.spaceinvaders.entity.BossSkill;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.boss.HardBossEntity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class LaserWarningLineEntity extends Entity {
    private final Game game;
    private final HardBossEntity boss;
    private final double angle;
    private final long endTimeMillis;

    public LaserWarningLineEntity(Game game, HardBossEntity boss, double angle, long durationMillis) {
        super("sprites/shot.gif", (int) boss.getX(), (int) boss.getY()); // Placeholder sprite
        this.game = game;
        this.boss = boss;
        this.angle = angle;
        this.endTimeMillis = System.currentTimeMillis() + durationMillis;
    }

    @Override
    public void move(long delta) {
        // Automatically remove after duration
        if (System.currentTimeMillis() > endTimeMillis) {
            game.removeEntity(this);
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);

        // Line starts from the center of the boss
        int startX = boss.getX() + boss.getSprite().getWidth() / 2;
        int startY = boss.getY() + boss.getSprite().getHeight() / 2;

        // Line extends far off-screen in the direction of the angle
        int endX = startX + (int) (Math.cos(angle) * 1000);
        int endY = startY + (int) (Math.sin(angle) * 1000);

        g2d.drawLine(startX, startY, endX, endY);
    }

    @Override
    public void collidedWith(Entity other) {
        // The warning line does not interact with other entities
    }
}
