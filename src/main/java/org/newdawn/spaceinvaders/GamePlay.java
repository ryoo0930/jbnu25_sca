package org.newdawn.spaceinvaders;

import java.awt.Graphics2D;
import java.util.ArrayList;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

public class GamePlay {

    /** The list of all the entities that exist in our game */
	private ArrayList<Entity> entities = new ArrayList<>();
	/** The list of entities that need to be removed from the game this loop */
	private ArrayList<Entity> removeList = new ArrayList<>();
	/** The entity representing the player */
	private Entity ship;
	/** The speed at which the player's ship should move (pixels/sec) */
	private double moveSpeed = 300;
	/** The time at which last fired a shot */
	private long lastFire = 0;
	/** The interval between our players shot (ms) */
	private long firingInterval = 500;
	/** The number of aliens left on the screen */
	private int alienCount;

    /** The message to display which waiting for a key press */
	private String message = "";
	/** True if we're holding up game play until a key has been pressed */
	private boolean waitingForKeyPress = true;
    /** True if game logic needs to be applied this loop, normally as a result of a game event */
    private boolean logicRequiredThisLoop = false;
    
    // Game 객체에 대한 참조. Entity 생성 시 필요.
    private Game game;

    public GamePlay(Game game) {
        this.game = game;
        initEntities();
    }

    // Game 클래스가 상태를 조회할 수 있도록 getter 메서드 제공
    public boolean isWaitingForKeyPress() { return waitingForKeyPress; }
    public String getMessage() { return message; }
    
    /**
	 * Start a fresh game, this should clear out any old data and
	 * create a new set.
	 */
    public void startGame() {
        // clear out any existing entities and intialise a new set
        entities.clear();
        initEntities();

        waitingForKeyPress = false;
    }

    /**
	 * Initialise the starting state of the entities (ship and aliens). Each
	 * entitiy will be added to the overall list of entities in the game.
	 */
    private void initEntities() {
        // create the player ship and place it roughly in the center of the screen
        ship = new ShipEntity(game, "sprites/ship.gif", 370, 550);
        entities.add(ship);

        // create a block of aliens (5 rows, by 12 aliens, spaced evenly)
        alienCount = 0;
        for (int row = 0; row < 5; row++) {
            for (int x = 0; x < 12; x++) {
                Entity alien = new AlienEntity(game, 100 + (x * 50), (50) + row * 30);
                entities.add(alien);
                alienCount++;
            }
        }
    }

    /**
	 * Notification from a game entity that the logic of the game
	 * should be run at the next opportunity (normally as a result of some
	 * game event)
	 */
    public void updateLogic() {
        logicRequiredThisLoop = true;
    }

    /**
	 * Remove an entity from the game. The entity removed will
	 * no longer move or be drawn.
	 * 
	 * @param entity The entity that should be removed
	 */
    public void removeEntity(Entity entity) {
        removeList.add(entity);
    }

    /**
	 * Notification that the player has died. 
	 */
    public void notifyDeath() {
        message = "Oh no! They got you, try again?";
        waitingForKeyPress = true;
    }

    /**
	 * Notification that the player has won since all the aliens
	 * are dead.
	 */
    public void notifyWin() {
        message = "Well done! You Win!";
        waitingForKeyPress = true;
    }

    /**
	 * Notification that an alien has been killed
	 */
    public void notifyAlienKilled() {
        // reduce the alient count, if there are none left, the player has won!
        alienCount--;
        if (alienCount == 0) {
            notifyWin();
        }

        // if there are still some aliens left then they all need to get faster, so
		// speed up all the existing aliens
        for (Entity entity : entities) {
            if (entity instanceof AlienEntity) {
                entity.setHorizontalMovement(entity.getHorizontalMovement() * 1.02);
            }
        }
    }

    /**
	 * Attempt to fire a shot from the player. Its called "try"
	 * since we must first check that the player can fire at this 
	 * point, i.e. has he/she waited long enough between shots
	 */
    public void tryToFire() {
        // check that we have waiting long enough to fire
        if (System.currentTimeMillis() - lastFire < firingInterval) {
            return;
        }

        // if we waited long enough, create the shot entity, and record the time.
        lastFire = System.currentTimeMillis();
        ShotEntity shot = new ShotEntity(game, "sprites/shot.gif", ship.getX() + 10, ship.getY() - 30);
        entities.add(shot);
    }

    /**
     * Game 클래스의 메인 루프에서 호출되어 게임 상태를 업데이트합니다.
     * @param delta 마지막 프레임 이후 경과 시간
     */
    public void update(long delta) {
        if (!waitingForKeyPress) {
            // 엔티티 이동
            for (Entity entity : entities) {
                entity.move(delta);
            }

            // 충돌 검사
            for (int p = 0; p < entities.size(); p++) {
                for (int s = p + 1; s < entities.size(); s++) {
                    Entity me = entities.get(p);
                    Entity him = entities.get(s);
                    if (me.collidesWith(him)) {
                        me.collidedWith(him);
                        him.collidedWith(me);
                    }
                }
            }
        }

        // 제거할 엔티티 정리
        entities.removeAll(removeList);
        removeList.clear();

        // 추가 로직 실행
        if (logicRequiredThisLoop) {
            for (Entity entity : entities) {
                entity.doLogic();
            }
            logicRequiredThisLoop = false;
        }
    }
    
    /**
     * Game 클래스에서 키 입력 상태를 받아와 처리합니다.
     */
    public void handleInput(boolean up, boolean down, boolean left, boolean right, boolean fire) {
        if (!waitingForKeyPress) {
            ship.setHorizontalMovement(0);
            ship.setVerticalMovement(0);
            if ((up) && (!down)) ship.setVerticalMovement(-moveSpeed);
            if ((down) && (!up)) ship.setVerticalMovement(moveSpeed);
            if ((left) && (!right)) ship.setHorizontalMovement(-moveSpeed);
            if ((right) && (!left)) ship.setHorizontalMovement(moveSpeed);
            if (fire) { tryToFire(); }
        }
    }
    
    /**
     * Game 클래스에서 호출되어 모든 엔티티를 화면에 그립니다.
     */
    public void draw(Graphics2D g) {
        for (Entity entity : entities) {
            entity.draw(g);
        }
    }
}