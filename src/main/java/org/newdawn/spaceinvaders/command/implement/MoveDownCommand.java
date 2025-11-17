package org.newdawn.spaceinvaders.command.implement;

import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.command.StatefulCommand;

public class MoveDownCommand implements StatefulCommand {
    private GamePlay receiver;

    public MoveDownCommand(GamePlay receiver) {
        this.receiver = receiver;
    }

    @Override
    public void onPress() {
        if (receiver != null) {
            receiver.setMoveDown(true);
        }
    }

    @Override
    public void onRelease() {
        if (receiver != null) {
            receiver.setMoveDown(false);
        }
    }
}
