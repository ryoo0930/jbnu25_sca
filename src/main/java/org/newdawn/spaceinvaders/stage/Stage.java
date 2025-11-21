package org.newdawn.spaceinvaders.stage;

import java.util.ArrayList;
import org.newdawn.spaceinvaders.Game; // (수정) GamePlay로 변경되므로 이 import는 불필요해짐
import org.newdawn.spaceinvaders.GamePlay;
import org.newdawn.spaceinvaders.entity.Entity;

public interface Stage {
    void initEntities(GamePlay gamePlay);
    int getAlienCount();
    void update(GamePlay gamePlay);
}