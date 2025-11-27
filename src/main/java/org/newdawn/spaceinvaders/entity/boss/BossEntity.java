package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.entity.Damageable;
import org.newdawn.spaceinvaders.entity.Entity;

/**
 * 보스 캐릭터의 공통 기능을 담당하는 추상 클래스입니다.
 * 체력, 점수, 콜백 처리 등 각 보스가 공통적으로 가지는 기본적인 속성과 동작을 정의합니다.
 * 구체적인 보스 구현체(Easy, Normal, Hard 등)는 이 클래스를 상속하여 개별 패턴이나 행동을 구현하게 됩니다.
 */
public abstract class BossEntity extends Entity implements Damageable {

    // 보스의 현제 체력
    protected int hp;
    // 보스의 최대 체력
    protected int maxHealth;
    // 보스를 처치하였을 때 점수
    protected int score;
    // 보스 이벤트(피해, 사망)를 전달하기 위한 콜백 인터페이스 (리팩토링한 것)
    private BossCallbacks callbacks;

    public BossEntity(String ref, int x, int y) {
        super(ref, x, y);
    }

    /**
     * 보스 이벤트를 전달받을 콜백 객체 설정
     * @param callbacks  이벤트 수신 콜백
     */
    public void setCallbacks(BossCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    /**
     * 보스가 피해를 받았을 때 호출되는 메서드
     * 체력을 감소시키고, 콜백이 등록되어 있다면 피해 이벤트 및 사망 이벤트를 순차적으로 전달
     * @param damage 입힌 피해량
     */
    @Override
    public void takeDamage(int damage) {
        this.hp -= damage;

        // 보스가 피해를 입었다는 이벤트 전달
        if (callbacks != null) {
            callbacks.onBossDamaged(this, damage);
        }

        // 체력이 0 이하라면 사망 처리 콜백 호출
        if (hp <= 0) {
            if (callbacks != null) {
                callbacks.onBossDead(this);
            }
        }
    }

    /**
     * 현재 보스의 체력을 반환
     * Game Play UI에서 체력 표시를 위해 사용됨
     */
    public int getHealth() {
        return hp;
    }
   // 보스의 최대 체력을 반환
    public int getMaxHealth() {
        return maxHealth;
    }

    // 보스를 죽였을 경우 얻은 점수를 반환
    public int getScore() {
        return score;
    }
}
