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

import org.newdawn.spaceinvaders.entity.Entity;

/**
 * The main hook of our game. This class with both act as a manager
 * for the display and central mediator for the game logic.
 * 
 * Display management will consist of a loop that cycles round all
 * entities in the game asking them to move and then drawing them
 * in the appropriate place. With the help of an inner class it
 * will also allow the player to control the main ship.
 * 
 * As a mediator it will be informed when entities within our game
 * detect events (e.g. alient killed, played died) and will take
 * appropriate game actions.
 * 
 * @author Kevin Glass
 */
public class Game extends Canvas {
	/** 게임 상태. 현재 Game class는 화면만 보여줄 뿐, GameState를 통해 화면의 구성을 변경시킵니다. */
	private enum GameState {
		MAIN_MENU, DIFFICULTY_MENU, GAME_PLAY, NICKNAME_INPUT, SCORE, OPTION, EXIT
	}

	/** The stragey that allows us to use accelerate page flipping */
	private BufferStrategy strategy;
	/** True if the game is currently "running", i.e. the game loop is looping */
	private boolean gameRunning = true;

	/** True if we're holding up game play until a key has been pressed */
	private boolean waitingForKeyPress = true;
	/** Ture if the up cursor key is currently pressed */
	private boolean upPressed = false;
	/** Ture if the down cursor key is currently pressed */
	private boolean downPressed = false;
	/** True if the left cursor key is currently pressed */
	private boolean leftPressed = false;
	/** True if the right cursor key is currently pressed */
	private boolean rightPressed = false;
	/** True if the space cursor key is currently pressed */
	private boolean spacePressed = false;
	/** True if the shift cursor key is currently pressed */
	private boolean shiftPressed = false;
	/** True if the Z cursor key is currently pressed */
	private boolean zPressed = false;
	private boolean xPressed = false;

	/**
	 * True if game logic needs to be applied this loop, normally as a result of a
	 * game event
	 */
	private boolean logicRequiredThisLoop = false;
	/** The last time at which we recorded the frame rate */
	private long lastFpsTime;
	/** The current number of frames recorded */
	private int fps;
	/** The normal title of the game window */
	private String windowTitle = "Space Invaders 102";
	/** The game window that we'll update with the frame count */
	private JFrame container;

	private MainMenu mainMenu;
	private DifficultyMenu difficultyMenu;
	private GamePlay gamePlay;
	private NicknameInputScreen nicknameInputScreen;
	private ScoreScreen scoreScreen;

	private int scoreToSave;
	private int lastDifficulty;

	/** 게임 시작 시 처음으로 보여줄 화면 */
	private GameState currentGameState = GameState.MAIN_MENU;

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

		// add a key input system (defined below) to our canvas
		// so we can respond to key pressed
		addKeyListener(new KeyInputHandler());

		// request the focus so key events come to us
		requestFocus();

		// create the buffering strategy which will allow AWT
		// to manage our accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();

		mainMenu = new MainMenu();
		difficultyMenu = new DifficultyMenu();
		scoreScreen = new ScoreScreen();
	}

	public void endGame() {
		if (gamePlay != null) {
			this.scoreToSave = gamePlay.getScore();
			this.lastDifficulty = gamePlay.getDifficulty();

			nicknameInputScreen = new NicknameInputScreen();
			currentGameState = GameState.NICKNAME_INPUT;
			gamePlay = null;
		}
	}

	public void returnToMainMenu() {
		currentGameState = GameState.MAIN_MENU;
		gamePlay = null;
	}

	public void updateLogic() {
		if (gamePlay != null)
			gamePlay.updateLogic();
	}

	public void removeEntity(Object entity) {
		if (gamePlay != null)
			gamePlay.removeEntity((Entity) entity);
	}

	public void notifyDeath() {
		if (gamePlay != null) {
			gamePlay.loseLifeAndRespawn();
			if (gamePlay.getLifes() <= 0) {
				endGame();
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

	/**
	 * The main game loop. This loop is running during all game
	 * play as is responsible for the following activities:
	 * <p>
	 * - Working out the speed of the game loop to update moves
	 * - Moving the game entities
	 * - Drawing the screen contents (entities, text)
	 * - Updating game events
	 * - Checking Input
	 * <p>
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
					if (gamePlay == null)
						break;
					gamePlay.update(delta);
					gamePlay.draw(g);

					if (gamePlay.isWaitingForKeyPress()) {
						g.setColor(Color.white);
						String msg = gamePlay.getMessage();
						String prompt = "Press Z or Space to return to the main menu";
						g.drawString(msg, (800 - g.getFontMetrics().stringWidth(msg)) / 2, 250);
						g.drawString(prompt, (800 - g.getFontMetrics().stringWidth(prompt)) / 2, 300);

					}

					gamePlay.handleInput(upPressed, downPressed, leftPressed, rightPressed, spacePressed, shiftPressed,
							zPressed, xPressed); // gamePlay에게 넘겨줄 키보드 키
					break;
				case NICKNAME_INPUT:
					if (nicknameInputScreen != null) {
						nicknameInputScreen.draw(g);
					}
					break;
				case SCORE:
					scoreScreen.draw(g);
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

	/** 메인 메뉴 선택 로직 추가 */
	private void selectMenuOption() {
		int selection = mainMenu.getSelection();
		switch (selection) {
			case 0:
				currentGameState = GameState.DIFFICULTY_MENU;
				break;
			case 1:
				currentGameState = GameState.SCORE;
				break;
			case 2:
				System.out.println("option");
				currentGameState = GameState.OPTION;
				break;
			case 3:
				System.exit(0);
				break;
		}
	}

	/** 난이도 선택 로직 추가 */
	private void selectDifficultyOption() {
		int difficulty = difficultyMenu.getSelection();
		startGameWithDifficulty(difficulty);
	}

	/** 난이도에 따른 게임 실행 로직 추가 */
	private void startGameWithDifficulty(int difficulty) {
		gamePlay = new GamePlay(this, difficulty);
		gamePlay.startGame();
		currentGameState = GameState.GAME_PLAY;
	}

	/**
	 * A class to handle keyboard input from the user. The class
	 * handles both dynamic input during game play, i.e. left/right
	 * and shoot, and more static type input (i.e. press any key to
	 * continue)
	 * 
	 * This has been implemented as an inner class more through
	 * habbit then anything else. Its perfectly normal to implement
	 * this as seperate class if slight less convienient.
	 * 
	 * @author Kevin Glass
	 */
	private class KeyInputHandler extends KeyAdapter {
		/** The number of key presses we've had while waiting for an "any key" press */
		private int pressCount = 1;

		/**
		 * Notification from AWT that a key has been pressed. Note that
		 * a key being pressed is equal to being pushed down but *NOT*
		 * released. Thats where keyTyped() comes in.
		 *
		 * @param e The details of the key that was pressed
		 */
		public void keyPressed(KeyEvent e) {
			switch (currentGameState) {
				case MAIN_MENU:
					if (e.getKeyCode() == KeyEvent.VK_UP) {
						mainMenu.moveUp();
					} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
						mainMenu.moveDown();
					} else if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_Z) {
						selectMenuOption();
					}
					break;
				case DIFFICULTY_MENU:
					if (e.getKeyCode() == KeyEvent.VK_UP) {
						difficultyMenu.moveUp();
					} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
						difficultyMenu.moveDown();
					} else if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_Z) {
						selectDifficultyOption();
					}
					break;
				case GAME_PLAY:
					if (gamePlay != null && gamePlay.isWaitingForKeyPress()) {
						if (e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_SPACE) {
							endGame();
						}
					} else {
						if (e.getKeyCode() == KeyEvent.VK_UP) {
							upPressed = true;
						} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
							downPressed = true;
						} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
							leftPressed = true;
						} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
							rightPressed = true;
						} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
							spacePressed = true;
						} else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
							shiftPressed = true;
						} else if (e.getKeyCode() == KeyEvent.VK_Z) {
							zPressed = true;
						} else if (e.getKeyCode() == KeyEvent.VK_X) {
							xPressed = true;
						}
					}
					break;
				case NICKNAME_INPUT:
					if (nicknameInputScreen == null)
						break;
					if (e.getKeyCode() == KeyEvent.VK_UP)
						nicknameInputScreen.moveUp();
					else if (e.getKeyCode() == KeyEvent.VK_DOWN)
						nicknameInputScreen.moveDown();
					else if (e.getKeyCode() == KeyEvent.VK_LEFT)
						nicknameInputScreen.moveLeft();
					else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
						nicknameInputScreen.moveRight();
					else if (e.getKeyCode() == KeyEvent.VK_Z) {
						boolean isDone = nicknameInputScreen.processSelection();
						if (isDone) {
							String finalNickname = nicknameInputScreen.getNickname();

							ScoreManager.addScore(finalNickname, scoreToSave, lastDifficulty);

							currentGameState = GameState.MAIN_MENU;
						}
					}
					break;
				case SCORE:
					if (e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_SPACE) {
						currentGameState = GameState.MAIN_MENU;
					}
					break;
			}
		}

		/**
		 * Notification from AWT that a key has been released.
		 *
		 * @param e The details of the key that was released
		 */
		public void keyReleased(KeyEvent e) {
			// if we're waiting for an "any key" typed then we don't
			// want to do anything with just a "released"
			if (waitingForKeyPress) {
				return;
			}

			if (e.getKeyCode() == KeyEvent.VK_UP) {
				upPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				downPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				leftPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				rightPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				spacePressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
				shiftPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_Z) {
				zPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_X) {
				xPressed = false;
			}
		}

		/**
		 * Notification from AWT that a key has been typed. Note that
		 * typing a key means to both press and then release it.
		 *
		 * @param e The details of the key that was typed.
		 */
		public void keyTyped(KeyEvent e) {
			// if we're waiting for a "any key" type then
			// check if we've recieved any recently. We may
			// have had a keyType() event from the user releasing
			// the shoot or move keys, hence the use of the "pressCount"
			// counter.
			if (waitingForKeyPress) {
				if (pressCount == 1) {
					// since we've now recieved our key typed
					// event we can mark it as such and start
					// our new game
					waitingForKeyPress = false;
					// gameStart();
					pressCount = 0;
				} else {
					pressCount++;
				}
			}

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
	 * 
	 * @param argv The arguments that are passed into our game
	 */
	public static void main(String argv[]) {
		Game g = new Game();

		// Start the main game loop, note: this method will not
		// return until the game has finished running. Hence we are
		// using the actual main thread to run the game.
		g.gameLoop();
	}
}
