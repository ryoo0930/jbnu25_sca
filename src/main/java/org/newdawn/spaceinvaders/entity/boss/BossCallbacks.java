package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.entity.boss.BossEntity;

public interface BossCallbacks {
    void onBossDamaged(BossEntity boss, int damage);
    void onBossDead(BossEntity boss);
}
