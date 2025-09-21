package org.newdawn.spaceinvaders.entity.strategy;

import org.newdawn.spaceinvaders.entity.Entity;

public class DownThenRightMovementStrategy implements InnerMovementStrategy {
    private enum State {
        MOVING_DOWN,
        MOVING_RIGHT
    }

    private State currentState = State.MOVING_DOWN;
    private final double moveSpeed;

    public DownThenRightMovementStrategy(double moveSpeed){
        this.moveSpeed = moveSpeed;
    }

    @Override
    public void move(Entity entity, long delta){
        switch (currentState) {
            case MOVING_DOWN:
                if(entity.getVerticalMovement() == 0){
                    entity.setVerticalMovement(moveSpeed);
                    entity.setHorizontalMovement(0);
                }
                if(entity.getY() > 200){
                    currentState = State.MOVING_RIGHT;
                    entity.setVerticalMovement(0);
                }
                break;
        
            case MOVING_RIGHT:
                if(entity.getHorizontalMovement() == 0){
                    entity.setHorizontalMovement(moveSpeed);
                    entity.setVerticalMovement(0);
                }
                break;
        }
    }
}
