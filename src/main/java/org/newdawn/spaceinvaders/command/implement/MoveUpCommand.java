package org.newdawn.spaceinvaders.command.implement;

import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.command.StatefulCommand;

public class MoveUpCommand implements StatefulCommand {
    private GamePlay receiver;

    public MoveUpCommand(GamePlay receiver) {
        this.receiver = receiver;
    }

    @Override
    public void onPress() {
        if (receiver != null) {
            receiver.setMoveUp(true);
        }
    }

    @Override
    public void onRelease() {
        if (receiver != null) {
            receiver.setMoveUp(false);
        }
    }
}
