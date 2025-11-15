package org.newdawn.spaceinvaders.command;

import org.newdawn.spaceinvaders.GamePlay;

public class GamePlayCommand implements StatefulCommand{
    /** GamePlayCommand가 수행할 수 있는 목록 */
    public enum Action { MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT, SHIFT, FIRE, LASER, BOMB }
    
    private GamePlay receiver;
    private Action action;

    /** GAMEPLAY의 동작을 위한 생성자 */
    public GamePlayCommand(GamePlay receiver, Action action) { this.receiver = receiver; this.action = action; }

    @Override
    public void onPress() {
        switch (action) {
            case MOVE_UP: 
                if(receiver != null) receiver.setMoveUp(true);
                break;
            case MOVE_DOWN: 
                if(receiver != null) receiver.setMoveDown(true);
                break;
            case MOVE_LEFT: 
                if(receiver != null) receiver.setMoveLeft(true);
                break;
            case MOVE_RIGHT: 
                if(receiver != null) receiver.setMoveRight(true);
                break;
            case SHIFT: 
                if(receiver != null) receiver.setShift(true);
                break;
            case FIRE: 
                if(receiver != null) receiver.setFire(true);
                break;
            case LASER: 
                if(receiver != null) receiver.setLaser(true);
                break;
            case BOMB: 
                if(receiver != null) receiver.setBomb(true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onRelease() {
        switch (action) {
            case MOVE_UP: 
                if(receiver != null) receiver.setMoveUp(false);
                break;
            case MOVE_DOWN: 
                if(receiver != null) receiver.setMoveDown(false);
                break;
            case MOVE_LEFT: 
                if(receiver != null) receiver.setMoveLeft(false);
                break;
            case MOVE_RIGHT: 
                if(receiver != null) receiver.setMoveRight(false);
                break;
            case SHIFT: 
                if(receiver != null) receiver.setShift(false);
                break;
            case FIRE: 
                if(receiver != null) receiver.setFire(false);
                break;
            case LASER: 
                if(receiver != null) receiver.setLaser(false);
                break;
            case BOMB: 
                if(receiver != null) receiver.setBomb(false);
                break;
            default:
                break;
        }
    }  
}
