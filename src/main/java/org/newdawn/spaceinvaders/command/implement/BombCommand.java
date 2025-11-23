package org.newdawn.spaceinvaders.command.implement;

import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.command.StatefulCommand;

public class BombCommand implements StatefulCommand {
    private GamePlay receiver;

    public BombCommand(GamePlay receiver) {
        this.receiver = receiver;
    }

    @Override
    public void onPress() {
        if (receiver != null) {
            receiver.setBomb(true);
        }
    }

    @Override
    public void onRelease() {
        if (receiver != null) {
            receiver.setBomb(false);
        }
    }
}
