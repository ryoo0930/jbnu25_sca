package org.newdawn.spaceinvaders;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.JPanel;

import javax.sound.sampled.Clip;

// 리팩토링을 위한 Import
import org.newdawn.spaceinvaders.command.GamePlayCommand;
import org.newdawn.spaceinvaders.command.MainMenuCommand;
import org.newdawn.spaceinvaders.input.InputMapper;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.ui.DifficultyMenu;
import org.newdawn.spaceinvaders.ui.MainMenu;
import org.newdawn.spaceinvaders.ui.NicknameInputScreen;
import org.newdawn.spaceinvaders.ui.OptionScreen;
import org.newdawn.spaceinvaders.ui.ScoreScreen;
import org.newdawn.spaceinvaders.utility.ScoreManager;
import org.newdawn.spaceinvaders.utility.SoundManager;
import org.newdawn.spaceinvaders.utility.SpriteStore;

public class Game extends Canvas {
    // (추가) Serializable 경고 해결을 위한 serialVersionUID
    private static final long serialVersionUID = 1L;

    /** 게임 상태. */
    private enum GameState {
        MAIN_MENU, DIFFICULTY_MENU, GAME_PLAY, NICKNAME_INPUT, SCORE, OPTION, EXIT
    }

    /** The stragey that allows us to use accelerate page flipping */
    private transient BufferStrategy strategy;
    /** True if the game is currently "running", i.e. the game loop is looping */
    private boolean gameRunning = true;


    /** The last time at which we recorded the frame rate */
    private long lastFpsTime;
    /** The current number of frames recorded */
    private int fps;
    /** The normal title of the game window */
    private String windowTitle = "Space Invaders 102";
    /** The game window that we'll update with the frame count */
    private transient JFrame container; // (수정) transient 추가

    // (수정) transient 추가
    private transient MainMenu mainMenu;
    private transient DifficultyMenu difficultyMenu;
    private transient GamePlay gamePlay;
    private transient NicknameInputScreen nicknameInputScreen;
    private transient ScoreScreen scoreScreen;
    private transient OptionScreen optionScreen;
    private int scoreToSave;
    private int lastDifficulty;

    private transient Clip mainMenuSound; // (수정) transient 추가
    private boolean soundLoop = false;
    private transient SpriteStore spriteStore; // (수정) transient 추가

    /** 게임 시작 시 처음으로 보여줄 화면 */
    private GameState currentGameState = GameState.MAIN_MENU;

    // --- (추가) InputMapper 필드 (모두 transient) ---
    private transient InputMapper currentInputMapper;
    private transient InputMapper mainMenuMapper;
    private transient InputMapper difficultyMenuMapper;
    private transient InputMapper gamePlayMapper;
    private transient InputMapper nicknameInputMapper;
    private transient InputMapper scoreScreenMapper;
    private transient InputMapper optionScreenMapper;
    // --- (추가 끝) ---

    /**
     * Construct our game and set it running.
     */
    public Game() {
        // create a frame to contain our game
        container = new JFrame("Space Invaders 102");

        // get hold the content of the frame and set up the resolution of the game
        JPanel panel = (JPanel) container.getContentPane();
        panel.setPreferredSize(new Dimension(800, 600));
        panel.setLayout(null);

        // setup our canvas size and put it into the content of the frame
        setBounds(0, 0, 800, 600);
        panel.add(this);

        // Tell AWT not to bother repainting our canvas since we're
        // going to do that our self in accelerated mode
        setIgnoreRepaint(true);

        // finally make the window visible
        container.pack();
        container.setResizable(false);
        container.setVisible(true);

        // add a listener to respond to the user closing the window. If they
        // do we'd like to exit the game
        container.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // (수정) KeyInputHandler 자체는 유지
        addKeyListener(new KeyInputHandler());

        // request the focus so key events come to us
        requestFocus();

        // create the buffering strategy which will allow AWT
        // to manage our accelerated graphics
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        // UI 객체 및 유틸리티 초기화
        mainMenu = new MainMenu();
        difficultyMenu = new DifficultyMenu();
        scoreScreen = new ScoreScreen();
        optionScreen = new OptionScreen();
        nicknameInputScreen = new NicknameInputScreen(); // 닉네임 화면도 미리 초기화

        mainMenuSound = SoundManager.get().getMusic("sounds/mainMenuSound.wav");
        spriteStore = SpriteStore.get(); // (수정) SpriteStore.get() 호출

        // (추가) 매퍼 초기화 및 시작 매퍼 설정
        initializeMappers();
        currentInputMapper = mainMenuMapper;
    }

    /**
     * (추가) 모든 InputMapper를 초기화하고 커맨드를 매핑하는 헬퍼 메소드
     */
    private void initializeMappers() {
        // 1. 메인 메뉴 매퍼
        mainMenuMapper = new InputMapper();
        mainMenuMapper.mapAction(KeyEvent.VK_UP,
                new MainMenuCommand(mainMenu, MainMenuCommand.Action.MOVE_UP));
        mainMenuMapper.mapAction(KeyEvent.VK_DOWN,
                new MainMenuCommand(mainMenu, MainMenuCommand.Action.MOVE_DOWN));
        mainMenuMapper.mapAction(KeyEvent.VK_ENTER,
                new MainMenuCommand(this, MainMenuCommand.Action.SELECT));
        mainMenuMapper.mapAction(KeyEvent.VK_Z,
                new MainMenuCommand(this, MainMenuCommand.Action.SELECT));

        // 2. 난이도 선택 매퍼 (람다식 사용)
        difficultyMenuMapper = new InputMapper();
        difficultyMenuMapper.mapAction(KeyEvent.VK_UP, () -> difficultyMenu.moveUp());
        difficultyMenuMapper.mapAction(KeyEvent.VK_DOWN, () -> difficultyMenu.moveDown());
        difficultyMenuMapper.mapAction(KeyEvent.VK_ENTER, () -> selectDifficultyOption());
        difficultyMenuMapper.mapAction(KeyEvent.VK_Z, () -> selectDifficultyOption());

        // 3. 점수 화면 매퍼
        scoreScreenMapper = new InputMapper();
        scoreScreenMapper.mapAction(KeyEvent.VK_Z, () -> returnToMainMenu());
        scoreScreenMapper.mapAction(KeyEvent.VK_SPACE, () -> returnToMainMenu());

        // 4. 옵션 화면 매퍼
        optionScreenMapper = new InputMapper();
        optionScreenMapper.mapAction(KeyEvent.VK_UP, () -> optionScreen.moveUp());
        optionScreenMapper.mapAction(KeyEvent.VK_DOWN, () -> optionScreen.moveDown());
        optionScreenMapper.mapAction(KeyEvent.VK_LEFT, () -> optionScreen.decreaseVolume());
        optionScreenMapper.mapAction(KeyEvent.VK_RIGHT, () -> optionScreen.increaseVolume());
        optionScreenMapper.mapAction(KeyEvent.VK_ENTER, () -> {
            if (optionScreen.getSelection() == 2) { // "Back" 선택 시
                returnToMainMenu();
            }
        });
        optionScreenMapper.mapAction(KeyEvent.VK_Z, () -> {
            if (optionScreen.getSelection() == 2) { // "Back" 선택 시
                returnToMainMenu();
            }
        });

        // 5. 닉네임 입력 매퍼
        nicknameInputMapper = new InputMapper();
        nicknameInputMapper.mapAction(KeyEvent.VK_UP, () -> nicknameInputScreen.moveUp());
        nicknameInputMapper.mapAction(KeyEvent.VK_DOWN, () -> nicknameInputScreen.moveDown());
        nicknameInputMapper.mapAction(KeyEvent.VK_LEFT, () -> nicknameInputScreen.moveLeft());
        nicknameInputMapper.mapAction(KeyEvent.VK_RIGHT, () -> nicknameInputScreen.moveRight());
        nicknameInputMapper.mapAction(KeyEvent.VK_Z, () -> {
            if (nicknameInputScreen == null) return;
            boolean isDone = nicknameInputScreen.processSelection();
            if (isDone) {
                String finalNickname = nicknameInputScreen.getNickname();
                ScoreManager.addScore(finalNickname, scoreToSave, lastDifficulty);
                returnToMainMenu(); // (상태 및 매퍼 변경)
            }
        });

        // 6. gamePlayMapper는 startGameWithDifficulty()에서 생성됩니다.
    }

    /**
     * (수정) 게임 종료 시 닉네임 입력 상태 및 매퍼로 변경
     */
    public void endGame() {
        if (gamePlay != null) {
            this.scoreToSave = gamePlay.getScore();
            this.lastDifficulty = gamePlay.getDifficulty();

            nicknameInputScreen = new NicknameInputScreen(); // 닉네임 초기화
            currentGameState = GameState.NICKNAME_INPUT;
            currentInputMapper = nicknameInputMapper; // (매퍼 교체)
            gamePlay = null;
        }
    }

    /**
     * (수정) 메인 메뉴로 복귀 시 매퍼 교체
     */
    public void returnToMainMenu() {
        currentGameState = GameState.MAIN_MENU;
        currentInputMapper = mainMenuMapper; // (매퍼 교체)
        gamePlay = null;
    }

    // --- (addScore, updateLogic 등 GamePlay 중계 메소드들은 그대로 유지) ---
    public void addScore(int score) {
        if (gamePlay != null) {
            gamePlay.addScore(score);
        }
    }
    public void updateLogic() {
        if (gamePlay != null)
            gamePlay.updateLogic();
    }
    public void removeEntity(Object entity) {
        if (gamePlay != null)
            gamePlay.removeEntity((Entity) entity);
    }
    public void increaseLife() {
        if (gamePlay != null) gamePlay.increaseLife();
    }
    public void increaseLaserCharges() {
        if (gamePlay != null) gamePlay.increaseLaserCharges();
    }
    public void increaseBombCharges() {
        if (gamePlay != null) gamePlay.increaseBombCharges();
    }
    public void addEntity(Entity entity) {
        if (gamePlay != null) gamePlay.addEntity(entity);
    }
    public java.util.List getEntities() {
        if (gamePlay != null) return gamePlay.getEntities();
        return java.util.Collections.emptyList();
    }
    public void notifyDeath() {
        if (gamePlay != null) {
            gamePlay.loseLifeAndRespawn();
            if (gamePlay.getLifes() <= 0) {
                // (수정) Game Over 메시지 대기 상태로 진입
                // (이 상태의 입력은 KeyInputHandler에서 예외 처리)
            }
        }
    }
    public void notifyWin() {
        if (gamePlay != null)
            gamePlay.notifyWin();
    }
    public void notifyAlienKilled(Entity alien) {
        if (gamePlay != null)
            gamePlay.notifyAlienKilled(alien);
    }
    public void tryToFire() {
        if (gamePlay != null)
            gamePlay.tryToFire();
    }
    public SpriteStore getSpriteStore(){
        return this.spriteStore;
    }
    // --- (중계 메소드 끝) ---


    /**
     * (수정) GameLoop에서 handleInput 호출 제거
     */
    public void gameLoop() {
        long lastLoopTime = SystemTimer.getTime();

        // keep looping round til the game ends
        while (gameRunning) {
            // work out how long its been since the last update, this
            // will be used to calculate how far the entities should
            // move this loop
            long delta = SystemTimer.getTime() - lastLoopTime;
            lastLoopTime = SystemTimer.getTime();

            // update the frame counter
            lastFpsTime += delta;
            fps++;

            // update our FPS counter if a second has passed since
            // we last recorded
            if (lastFpsTime >= 1000) {
                container.setTitle(windowTitle + " (FPS: " + fps + ")");
                lastFpsTime = 0;
                fps = 0;
            }

            // Get hold of a graphics context for the accelerated
            // surface and blank it out
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            g.setColor(Color.black);
            g.fillRect(0, 0, 800, 600);

            if (currentGameState != GameState.GAME_PLAY && currentGameState != GameState.NICKNAME_INPUT) {
                if (!soundLoop) {
                    mainMenuSound.loop(Clip.LOOP_CONTINUOUSLY);
                    soundLoop = true;
                }
            } else {
                if (soundLoop) {
                    mainMenuSound.stop();
                    soundLoop = false;
                }
            }

            // 화면 상태 변경
            switch (currentGameState) {
                case MAIN_MENU:
                    mainMenu.update();
                    mainMenu.draw(g);
                    break;
                case DIFFICULTY_MENU:
                    difficultyMenu.draw(g);
                    break;
                case GAME_PLAY:
                    if (gamePlay != null) {
                        gamePlay.update(delta); // update가 내부적으로 handleInput을 호출
                    }
                    // After update, gamePlay might be null, so we check again before drawing.
                    if (gamePlay != null) {
                        gamePlay.draw(g);

                        if (gamePlay.isWaitingForKeyPress()) {
                            g.setColor(Color.white);
                            String msg = gamePlay.getMessage();
                            String prompt = "Press Z or Space to return to the main menu";
                            g.drawString(msg, (800 - g.getFontMetrics().stringWidth(msg)) / 2, 250);
                            g.drawString(prompt, (800 - g.getFontMetrics().stringWidth(prompt)) / 2, 300);
                        }

                        // (삭제) gamePlay.handleInput(...) 호출 제거
                    }
                    break;
                case NICKNAME_INPUT:
                    if (nicknameInputScreen != null) {
                        nicknameInputScreen.draw(g);
                    }
                    break;
                case SCORE:
                    scoreScreen.draw(g);
                    break;
                case OPTION:
                    optionScreen.draw(g);
                    break;
            }

            // finally, we've completed drawing so clear up the graphics
            // and flip the buffer over
            g.dispose();
            strategy.show();

            // we want each frame to take 10 milliseconds, to do this
            // we've recorded when we started the frame. We add 10 milliseconds
            // to this and then factor in the current time to give
            // us our final value to wait for
            SystemTimer.sleep(lastLoopTime + 10 - SystemTimer.getTime());
        }
    }

    /** (수정) 메인 메뉴 선택 시 매퍼 교체 */
    public void selectMenuOption() {
        int selection = mainMenu.getSelection();
        switch (selection) {
            case 0:
                currentGameState = GameState.DIFFICULTY_MENU;
                currentInputMapper = difficultyMenuMapper; // (매퍼 교체)
                break;
            case 1:
                currentGameState = GameState.SCORE;
                currentInputMapper = scoreScreenMapper; // (매퍼 교체)
                break;
            case 2:
                System.out.println("option");
                currentGameState = GameState.OPTION;
                currentInputMapper = optionScreenMapper; // (매퍼 교체)
                break;
            case 3:
                System.exit(0);
                break;
        }
    }

    /** (수정) 난이도 선택 시 매퍼 교체 */
    private void selectDifficultyOption() {
        int difficulty = difficultyMenu.getSelection();
        startGameWithDifficulty(difficulty);
    }

    /** (수정) 게임 시작 시 GamePlay 매퍼 생성 및 교체 */
    private void startGameWithDifficulty(int difficulty) {
        gamePlay = new GamePlay(this, difficulty);

        // (추가) GamePlay 매퍼 생성 및 매핑 (1번 방식 적용)
        gamePlayMapper = new InputMapper();
        gamePlayMapper.mapState(KeyEvent.VK_UP,
                new GamePlayCommand(gamePlay, GamePlayCommand.Action.MOVE_UP));
        gamePlayMapper.mapState(KeyEvent.VK_DOWN,
                new GamePlayCommand(gamePlay, GamePlayCommand.Action.MOVE_DOWN));
        gamePlayMapper.mapState(KeyEvent.VK_LEFT,
                new GamePlayCommand(gamePlay, GamePlayCommand.Action.MOVE_LEFT));
        gamePlayMapper.mapState(KeyEvent.VK_RIGHT,
                new GamePlayCommand(gamePlay, GamePlayCommand.Action.MOVE_RIGHT));
        gamePlayMapper.mapState(KeyEvent.VK_SHIFT,
                new GamePlayCommand(gamePlay, GamePlayCommand.Action.SHIFT));
        gamePlayMapper.mapState(KeyEvent.VK_Z,
                new GamePlayCommand(gamePlay, GamePlayCommand.Action.FIRE));
        gamePlayMapper.mapState(KeyEvent.VK_X,
                new GamePlayCommand(gamePlay, GamePlayCommand.Action.LASER));
        gamePlayMapper.mapState(KeyEvent.VK_C,
                new GamePlayCommand(gamePlay, GamePlayCommand.Action.BOMB));

        gamePlay.startGame();
        currentGameState = GameState.GAME_PLAY;
        currentInputMapper = gamePlayMapper; // (매퍼 교체)
    }

    /**
     * (수정) KeyInputHandler를 대폭 수정
     */
    private class KeyInputHandler extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
            // (예외적 상황(게임 오버 메시지)을 먼저 처리
            // 이 상태는 GAME_PLAY 상태에 속하지만 입력 방식이 다름
            if (currentGameState == GameState.GAME_PLAY && gamePlay != null && gamePlay.isWaitingForKeyPress()) {
                if (e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_SPACE) {
                    endGame(); // 닉네임 입력 상태로 전환
                    return; // 매퍼가 실행되지 않도록 즉시 반환
                }
            }

            // 현재 활성화된 매퍼에게 키 입력을 위임
            if (currentInputMapper != null) {
                currentInputMapper.handleKeyPress(e.getKeyCode());
            }
        }

        /**
         * KeyReleased도 매퍼에게 위임
         */
        public void keyReleased(KeyEvent e) {

            if (currentInputMapper != null) {
                currentInputMapper.handleKeyReleased(e.getKeyCode());
            }
        }

        /**
         * KeyTyped는 ESC 종료 기능으로 유지
         */
        public void keyTyped(KeyEvent e) {
            // if we hit escape, then quit the game
            if (e.getKeyChar() == 27) {
                System.exit(0);
            }
        }
    }

    /**
     * The entry point into the game. We'll simply create an
     * instance of class which will start the display and game
     * loop.
     * * @param argv The arguments that are passed into our game
     */
    public static void main(String argv[]) {
        Game g = new Game();

        // Start the main game loop, note: this method will not
        // return until the game has finished running. Hence we are
        // using the actual main thread to run the game.
        g.gameLoop();
    }
}