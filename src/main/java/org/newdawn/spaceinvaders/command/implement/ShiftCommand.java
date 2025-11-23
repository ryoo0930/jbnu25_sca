package org.newdawn.spaceinvaders.command.implement;

import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.command.StatefulCommand;

public class ShiftCommand implements StatefulCommand {
    private GamePlay receiver;

    public ShiftCommand(GamePlay receiver) {
        this.receiver = receiver;
    }

    @Override
    public void onPress() {
        if (receiver != null) {
            receiver.setShift(true);
        }
    }

    @Override
    public void onRelease() {
        if (receiver != null) {
            receiver.setShift(false);
        }
    }
}
