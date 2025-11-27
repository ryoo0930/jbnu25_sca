package org.newdawn.spaceinvaders.system;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ItemEntity;
import org.newdawn.spaceinvaders.entity.ShipEntity;

import java.util.List;

/**
 * Handles collision detection between entities.
 */
public class CollisionSystem {
    public void checkCollisions(EntityManager entityManager, boolean isInvincible) {
        List<Entity> entities = entityManager.getEntities();
        int entityCount = entities.size();

        for (int p = 0; p < entityCount; p++) {
            for (int s = p + 1; s < entityCount; s++) {
                Entity me = entities.get(p);
                Entity him = entities.get(s);

                // if ship is invincible, skip collision with it, unless it's an item
                if ((me instanceof ShipEntity && isInvincible && !(him instanceof ItemEntity)) ||
                        (him instanceof ShipEntity && isInvincible && !(me instanceof ItemEntity))) {
                    continue;
                }

                // Lasers handle their own multi-target collision, so we skip standard collision checks.
                if (me instanceof org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity ||
                        him instanceof org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity) {
                    continue;
                }

                if (me.collidesWith(him) || him.collidesWith(me)) {
                    me.collidedWith(him);
                    him.collidedWith(me);
                }
            }
        }
    }
}
