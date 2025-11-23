package org.newdawn.spaceinvaders.system;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.boss.BossEntity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Handles rendering all entities and game-related UI to the screen.
 */
public class RenderSystem {
    public void draw(Graphics2D g, EntityManager entityManager, boolean isInvincible, int score, int lives, int laserCharges, int bombCharges) {
        List<Entity> entities = entityManager.getEntities();

        // Draw all entities
        for (Entity entity : entities) {
            if (entity instanceof org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity) {
                continue; // Laser is drawn last, on top of everything
            }
            if (entity instanceof ShipEntity) {
                if (isInvincible) {
                    // Blink the ship every 100ms
                    if ((System.currentTimeMillis() / 100) % 2 == 0) {
                        continue;
                    }
                }
            }
            entity.draw(g);
        }

        // Draw laser on top
        for (Entity entity : entities) {
            if (entity instanceof org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity) {
                entity.draw(g);
                break; // There should be only one player laser
            }
        }

        // Draw boss health bar
        for (Entity entity : entities) {
            if (entity instanceof BossEntity) {
                BossEntity boss = (BossEntity) entity;
                int maxHealth = boss.getMaxHealth();
                int currentHealth = boss.getHealth();
                int barWidth = 500;
                int barHeight = 20;
                int barX = (800 - barWidth) / 2;
                int barY = 30;

                g.setColor(Color.GRAY);
                g.fillRect(barX, barY, barWidth, barHeight);

                float healthPercentage = (float) currentHealth / maxHealth;
                g.setColor(Color.RED);
                g.fillRect(barX, barY, (int) (barWidth * healthPercentage), barHeight);
                break; // Assume only one boss at a time
            }
        }

        // Draw UI text
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 700, 50);
        g.drawString("Lives: " + (lives > 0 ? lives - 1 : 0), 10, 50);
        g.drawString("Laser: " + laserCharges, 10, 70);
        g.drawString("Bomb: " + bombCharges, 10, 90);
    }
}
