package org.newdawn.spaceinvaders.command.implement;

import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.command.StatefulCommand;

public class MoveRightCommand implements StatefulCommand {
    private GamePlay receiver;

    public MoveRightCommand(GamePlay receiver) {
        this.receiver = receiver;
    }

    @Override
    public void onPress() {
        if (receiver != null) {
            receiver.setMoveRight(true);
        }
    }

    @Override
    public void onRelease() {
        if (receiver != null) {
            receiver.setMoveRight(false);
        }
    }
}
