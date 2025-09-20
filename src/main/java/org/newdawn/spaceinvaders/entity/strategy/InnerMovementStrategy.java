package org.newdawn.spaceinvaders.entity.strategy;

import org.newdawn.spaceinvaders.entity.Entity;

/** AlienEntity의 특정 움직임을 추가하기 위한 인터페이스 */
public interface InnerMovementStrategy {
    void move(Entity entity, long delta);

}