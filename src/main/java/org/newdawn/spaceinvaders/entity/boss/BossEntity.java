package org.newdawn.spaceinvaders.entity.boss;


import org.newdawn.spaceinvaders.entity.Damageable;
import org.newdawn.spaceinvaders.entity.Entity;

public abstract class BossEntity extends Entity implements Damageable {

    public BossEntity(String ref, int x, int y) {
        super(ref, x, y);
    }
    @Override
    public abstract void takeDamage(int damage);
    public abstract int getHealth();
    public abstract int getMaxHealth();
}
