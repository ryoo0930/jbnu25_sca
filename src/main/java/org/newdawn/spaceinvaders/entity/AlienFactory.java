package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.stage.Difficulty;




public final class AlienFactory {

    private AlienFactory() {
        // 유틸리티 클래스이므로 인스턴스화 예방
    }

    /**
     * 난이도에 따라 서로 다른 PassingAlienEntity를 생성하는 팩토리 메서드.
     *
     * @param difficulty 난이도 (EASY / NORMAL / HARD / LUNATIC)
     * @param game       Game 인스턴스
     * @param x          스폰 X 좌표
     * @param y          스폰 Y 좌표
     * @param fromLeft   true면 왼쪽에서 등장, false면 오른쪽에서 등장
     * @return 생성된 PassingAlienEntity (Entity 타입으로 반환)
     */
    public static Entity createPassingAlien(
            Difficulty difficulty,
            Game game,
            int x,
            int y,
            boolean fromLeft
    ) {
        switch (difficulty) {
            case EASY:
                return new EasyPassingAlienEntity(
                        game,
                        x,
                        y,
                        fromLeft
                                ? EasyPassingAlienEntity.Origin.LEFT
                                : EasyPassingAlienEntity.Origin.RIGHT
                );
            case NORMAL:
                return new NormalPassingAlienEntity(
                        game,
                        x,
                        y,
                        fromLeft
                                ? NormalPassingAlienEntity.Origin.LEFT
                                : NormalPassingAlienEntity.Origin.RIGHT
                );
            case HARD:
                return new HardPassingAlienEntity(
                        game,
                        x,
                        y,
                        fromLeft
                                ? HardPassingAlienEntity.Origin.LEFT
                                : HardPassingAlienEntity.Origin.RIGHT
                );
            default:
                throw new IllegalArgumentException("Unknown difficulty: " + difficulty);
        }
    }
}

