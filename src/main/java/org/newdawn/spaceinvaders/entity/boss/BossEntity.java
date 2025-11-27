package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.entity.Damageable;
import org.newdawn.spaceinvaders.entity.Entity;

public abstract class BossEntity extends Entity implements Damageable {

    protected int hp;
    protected int maxHealth;
    protected int score;

    private BossCallbacks callbacks;

    public BossEntity(String ref, int x, int y) {
        super(ref, x, y);
    }

    public void setCallbacks(BossCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void takeDamage(int damage) {
        this.hp -= damage;

        if (callbacks != null) {
            callbacks.onBossDamaged(this, damage);
        }

        if (hp <= 0) {
            if (callbacks != null) {
                callbacks.onBossDead(this);
            }
        }
    }

    // ★ 반드시 필요: GamePlay에서 호출
    public int getHealth() {
        return hp;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getScore() {
        return score;
    }
}
