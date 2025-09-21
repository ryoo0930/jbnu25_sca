package org.newdawn.spaceinvaders.entity.movementStrategy;

import org.newdawn.spaceinvaders.entity.Entity;
import java.awt.Point;
import java.util.Random;

public class PatrolMovementStrategy implements InnerMovementStrategy {

    private enum State {
        INITIAL_WAIT,
        MOVING,
        IDLE
    }

    // --- ✨ 추가: 각 위치의 인덱스를 상수로 정의하여 가독성을 높였습니다. ---
    private static final int LEFT_POINT_INDEX = 0;
    private static final int CENTER_POINT_INDEX = 1;
    private static final int RIGHT_POINT_INDEX = 2;

    private State currentState = State.INITIAL_WAIT;
    private final long WAIT_DURATION = 5000; // 대기 시간
    private final long MOVE_DURATION = 2000; // 이동에 걸리는 시간 (2초)
    private long timeInCurrentState = 0;

    private Point[] targetPoints;
    private int currentTargetIndex;
    private final double moveSpeed;
    private Random random = new Random();

    // 이동 시작점과 끝점을 저장할 변수
    private Point startPoint;
    private Point endPoint;

    // --- ✨ 수정: 생성자에서 첫 목표를 항상 중앙으로 설정합니다. ---
    public PatrolMovementStrategy(double moveSpeed) {
        this.moveSpeed = moveSpeed;
        this.targetPoints = new Point[] {
            new Point(100, 50),   // 왼쪽 지점 (인덱스 0)
            new Point(400, 80),   // 중앙 지점 (인덱스 1)
            new Point(600, 50)    // 오른쪽 지점 (인덱스 2)
        };

        // 첫 번째 목표를 중앙 지점으로 강제하여, 어디서 생성되든 중앙으로 먼저 이동하게 합니다.
        this.currentTargetIndex = CENTER_POINT_INDEX;
        this.endPoint = targetPoints[this.currentTargetIndex];
    }

    @Override
    public void move(Entity entity, long delta) {
        timeInCurrentState += delta;

        switch (currentState) {
            case INITIAL_WAIT:
            case IDLE:
                // 대기 상태 (초기 대기 또는 도착 후 대기)
                if (timeInCurrentState >= WAIT_DURATION) {
                    // 대기가 끝나면, 새로운 이동을 준비
                    selectNewTarget(entity);
                    changeState(State.MOVING);
                }
                break;

            case MOVING:
                // 첫 이동 시 startPoint가 null일 수 있으므로, 현재 엔티티 위치로 초기화합니다.
                if (startPoint == null) {
                    startPoint = new Point((int)entity.getX(), (int)entity.getY());
                }

                // 이동 진행률 계산 (0.0 ~ 1.0)
                double progress = Math.min(1.0, (double) timeInCurrentState / MOVE_DURATION);

                // Cosine 함수를 이용해 부드러운 가속/감속 효과 생성 (이징 함수)
                double easedProgress = 0.5 * (1 - Math.cos(progress * Math.PI));

                // 시작점과 끝점 사이의 현재 위치를 계산 (선형 보간)
                double newX = startPoint.x + (endPoint.x - startPoint.x) * easedProgress;
                double newY = startPoint.y + (endPoint.y - startPoint.y) * easedProgress;

                // 엔티티의 위치를 직접 설정
                entity.setPosition(newX, newY);

                // 이동이 완료되면
                if (progress >= 1.0) {
                    entity.setPosition(endPoint.x, endPoint.y); // 오차 보정을 위해 위치를 정확히 맞춰줌
                    changeState(State.IDLE);
                }
                break;
        }
    }

    // 상태 변경 시 타이머 리셋
    private void changeState(State newState) {
        this.currentState = newState;
        this.timeInCurrentState = 0;
    }

    // --- ✨ 수정: 핵심 이동 로직을 변경한 부분입니다. ---
    private void selectNewTarget(Entity entity) {
        // 현재 위치를 다음 이동의 시작점으로 설정
        this.startPoint = new Point((int)entity.getX(), (int)entity.getY());
        
        int newIndex;

        // 현재 도착해 있는 지점(currentTargetIndex)에 따라 다음 목표를 결정합니다.
        if (currentTargetIndex == CENTER_POINT_INDEX) {
            // [현재 위치: 중앙] -> 다음 목표: 왼쪽 또는 오른쪽 중 무작위 선택
            newIndex = random.nextBoolean() ? LEFT_POINT_INDEX : RIGHT_POINT_INDEX;
        } else {
            // [현재 위치: 왼쪽 또는 오른쪽] -> 다음 목표: 무조건 중앙으로 이동
            newIndex = CENTER_POINT_INDEX;
        }
        
        // 다음 목표 인덱스와 끝점을 업데이트합니다.
        currentTargetIndex = newIndex;
        this.endPoint = targetPoints[currentTargetIndex];
    }
}