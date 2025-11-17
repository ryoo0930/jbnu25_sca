package org.newdawn.spaceinvaders.command.implement;

import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.command.StatefulCommand;

public class MoveLeftCommand implements StatefulCommand {
    private GamePlay receiver;

    public MoveLeftCommand(GamePlay receiver) {
        this.receiver = receiver;
    }

    @Override
    public void onPress() {
        if (receiver != null) {
            receiver.setMoveLeft(true);
        }
    }

    @Override
    public void onRelease() {
        if (receiver != null) {
            receiver.setMoveLeft(false);
        }
    }
}
