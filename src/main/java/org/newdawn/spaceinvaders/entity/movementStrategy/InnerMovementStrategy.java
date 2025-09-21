package org.newdawn.spaceinvaders.entity.movementStrategy;

import org.newdawn.spaceinvaders.entity.Entity;

public interface InnerMovementStrategy {
    void move(Entity entity, long delta);
}