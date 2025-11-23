package org.newdawn.spaceinvaders.command.implement;

import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.command.StatefulCommand;

public class FireCommand implements StatefulCommand {
    private GamePlay receiver;

    public FireCommand(GamePlay receiver) {
        this.receiver = receiver;
    }

    @Override
    public void onPress() {
        if (receiver != null) {
            receiver.setFire(true);
        }
    }

    @Override
    public void onRelease() {
        if (receiver != null) {
            receiver.setFire(false);
        }
    }
}
