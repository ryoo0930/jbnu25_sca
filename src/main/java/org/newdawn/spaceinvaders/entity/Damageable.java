package org.newdawn.spaceinvaders.entity;

/**
 * 피해를 받을 수 있는 엔티티가 구현된 인터페이스
 * 보스, 적, 플레이어 등 공통으로 데미지 처리 방식을 통일시키기 위해 리팩토링함
 */
public interface Damageable {
    /**
     * 지정된 데미지만큼 체력을 감소시키는 메서드.
     * @param damage 적용할 데미지 값
     */
    void takeDamage(int damage);
}
