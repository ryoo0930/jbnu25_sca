package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.entity.boss.BossEntity;

/**
 * 보스 관련 콜백을 처리하는 인터페이스 입니다.
 * 보스가 피해를 입히거나 사망하는 등의 이벤트가 발생할 때
 * 게임 로직, UI 등이 이 인터페이스를 통해 알림을 받을 수 있습니다.
 * 리팩토링 과정에서 인터페이스를 따로 만드는 것이 유지보수성에 더 높을 것이라 판단하여 추가하였습니다.
 */
public interface BossCallbacks {
    /** 보스가 피해를 입었을 때 호출됩니다.
     *
     * @param boss 피해를 입은 보스 Entity
     * @param damage 받은 피해량
     */
    void onBossDamaged(BossEntity boss, int damage);

    /**
     * 보스가 사망하였을 때 호출됩니다.
     * @param boss
     */
    void onBossDead(BossEntity boss);
}
