package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.entity.Entity;

public abstract class BossEntity extends Entity {

    public BossEntity(String ref, int x, int y) {
        super(ref, x, y);
    }

    public abstract int getHealth();
    public abstract int getMaxHealth();
}
