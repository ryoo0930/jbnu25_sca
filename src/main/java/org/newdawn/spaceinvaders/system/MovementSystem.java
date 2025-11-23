package org.newdawn.spaceinvaders.system;

import org.newdawn.spaceinvaders.entity.Entity;

import java.util.List;

/**
 * Handles the movement of all entities.
 */
public class MovementSystem {
    public void update(long delta, EntityManager entityManager) {
        List<Entity> entities = entityManager.getEntities();
        for (Entity entity : entities) {
            entity.move(delta);
        }
    }
}
