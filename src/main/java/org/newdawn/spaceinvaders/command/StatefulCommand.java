package org.newdawn.spaceinvaders.command;

/**
 * 키를 누르는(onPress) 상태와 떼는(onRelease) 상태를 구분하는 커맨드.
 */
public interface StatefulCommand {
    void onPress();
    void onRelease();
}
