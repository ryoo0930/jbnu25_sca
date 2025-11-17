package org.newdawn.spaceinvaders.input;

import java.util.HashMap;
import java.util.Map;

import org.newdawn.spaceinvaders.command.Command;
import org.newdawn.spaceinvaders.command.StatefulCommand;

public class InputMapper {
    private final Map<Integer, Command> pressMap = new HashMap<>();
    private final Map<Integer, StatefulCommand> stateMap = new HashMap<>();

    /** 단일 실행 커맨드 매핑 */
    public void mapAction(int keyCode, Command command) { pressMap.put(keyCode, command); }
    /** 상태 변경 커맨드 매핑 */
    public void mapState(int keyCode, StatefulCommand command) { stateMap.put(keyCode, command); }

    public void handleKeyPress(int keyCode) {
        if(pressMap.containsKey(keyCode)) pressMap.get(keyCode).execute();
        if(stateMap.containsKey(keyCode)) stateMap.get(keyCode).onPress();
    }
    public void handleKeyReleased(int keyCode) {
        if(stateMap.containsKey(keyCode)) stateMap.get(keyCode).onRelease();
    }
}
