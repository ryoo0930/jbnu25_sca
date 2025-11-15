package org.newdawn.spaceinvaders.command;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.ui.MainMenu;

public class MainMenuCommand implements Command{
    /** MainMenuCommand가 수행할 수 있는 목록 */
    public enum Action { MOVE_UP, MOVE_DOWN, SELECT }
    
    private MainMenu mainMenuReceiver;
    private Game gameReceiver;
    private Action action;

    /** MAINMENU의 동작(위 / 아래)을 위한 생성자 */
    public MainMenuCommand(MainMenu receiver, Action action) { this.mainMenuReceiver = receiver; this.action = action; }
    /** GAME의 동작(선택)을 위한 생성자 */
    public MainMenuCommand(Game receiver, Action action) { this.gameReceiver = receiver; this.action = action; }

    @Override
    public void execute() {
        switch (action) {
            case MOVE_UP:
                if(mainMenuReceiver != null) mainMenuReceiver.moveUp();
                break;
            case MOVE_DOWN:
                if(mainMenuReceiver != null) mainMenuReceiver.moveDown();
                break;
            case SELECT:
                if(gameReceiver != null) gameReceiver.selectMenuOption();
                break;
            default:
                break;
        }
    }
}
