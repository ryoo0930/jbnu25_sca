package org.newdawn.spaceinvaders.command.implement;

import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.command.StatefulCommand;

public class LaserCommand implements StatefulCommand {
    private GamePlay receiver;

    public LaserCommand(GamePlay receiver) {
        this.receiver = receiver;
    }

    @Override
    public void onPress() {
        if (receiver != null) {
            receiver.setLaser(true);
        }
    }

    @Override
    public void onRelease() {
        if (receiver != null) {
            receiver.setLaser(false);
        }
    }
}
