package org.newdawn.spaceinvaders;

import java.awt.Graphics2D;
import java.util.ArrayList;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.entity.playerSkill.BombEntity;
import org.newdawn.spaceinvaders.stage.EasyStage;
import org.newdawn.spaceinvaders.stage.HardStage;
import org.newdawn.spaceinvaders.stage.LunaticStage;
import org.newdawn.spaceinvaders.stage.NormalStage;
import org.newdawn.spaceinvaders.stage.Stage;
import org.newdawn.spaceinvaders.utility.SoundManager;


public class GamePlay {

    /** The list of all the entities that exist in our game */
    private ArrayList<Entity> entities = new ArrayList<>();
    /** The list of entities that need to be removed from the game this loop */
    private ArrayList<Entity> removeList = new ArrayList<>();
    /** The list of entities that need to be added to the game this loop */
    private ArrayList<Entity> addList = new ArrayList<>(); // 새로 추가된 엔티티를 위한 리스트
    /** The entity representing the player */
    private Entity ship;
    /** The speed at which the player's ship should move (pixels/sec) */
    private double moveSpeed = 300;
    /** The time at which last fired a shot */
    private long lastFire = 0;
    /** The interval between our players shot (ms) */
    private long firingInterval = 90;
    /** The number of aliens left on the screen */
    private int alienCount;
    private int score = 0;

    /** Extra Life 추가 */
    private int lifes = 3;
    private boolean invincible = false;
    private long invincibilityEndTime = 0;

    private long lastBombTime = 0L;
    private static final long BOMB_COOLDOWN_MS = 1500L;



    /** The message to display which waiting for a key press */
    private String message = "";
    /** True if we're holding up game play until a key has been pressed */
    private boolean waitingForKeyPress = true;
    /**
     * True if game logic needs to be applied this loop, normally as a result of a
     * game event
     */
    private boolean logicRequiredThisLoop = false;
    private boolean gameWon = false;

    // Entity 생성 시 필요.
    private Game game;
    private Stage stage;

    private int difficulty;

    // Laser 관리
    private org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity laser;
    private final long LASER_DURATION = 3000;
    private boolean laserButtonLatched = false; // 중첩방지

    public GamePlay(Game game, int difficulty) {
        this.game = game;
        this.difficulty = difficulty;
        setStage(difficulty);
        initEntities();
    }

    private void setStage(int difficulty) {
        switch (difficulty) {
            case 0: // Easy
                this.stage = new EasyStage();
                break;
            case 1: // Normal
                this.stage = new NormalStage();
                break;
            case 2: // Hard
                this.stage = new HardStage();
                break;
            case 3: // Lunatic
                this.stage = new LunaticStage();
                break;
        }
    }

    // Game 클래스가 상태를 조회할 수 있도록 메서드 제공
    public boolean isWaitingForKeyPress() {
        return waitingForKeyPress;
    }

    public String getMessage() {
        return message;
    }
    public int getScore() {
        return this.score;
    }

    public int getDifficulty() {
        return this.difficulty;
    }

    public void loseLifeAndRespawn() {
        if (invincible) return; // Already invincible, do nothing

        lifes--;
        if (lifes > 0) {
            ship.setPosition(370, 550);
            invincible = true;
            invincibilityEndTime = System.currentTimeMillis() + 2000; // 1 second of invincibility
        }
    }

    public int getLifes() {
        return lifes;
    }

    /**
     * Start a fresh game, this should clear out any old data and
     * create a new set.
     */
    public void startGame() {
        // clear out any existing entities and intialise a new set
        entities.clear();
        initEntities();

        waitingForKeyPress = false;
        gameWon = false;
    }

    /**
     * Initialise the starting state of the entities (ship and aliens). Each
     * entitiy will be added to the overall list of entities in the game.
     */
    private void initEntities() {
        // create the player ship and place it roughly in the center of the screen
        ship = new ShipEntity(game, "sprites/ship.gif", 370, 550);
        entities.add(ship);

        stage.initEntities(game, entities);
        alienCount = stage.getAlienCount();
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

    public void addEntity(Entity entity) {
        addList.add(entity); // entities.add(entity) 대신 addList에 추가
    }

    public java.util.List getEntities() {
        return entities;
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
        gameWon = true;
    }

    /**
     * Notification that an alien has been killed
     */
    public void notifyAlienKilled(Entity alien) {
        if(removeList.contains(alien)) return;

        this.score += 100;

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
        SoundManager.get().playSound("sounds/alienshoot2.wav");
    }
    private void fireBombIfReady() {
        long now = System.currentTimeMillis();
        if (now - lastBombTime < BOMB_COOLDOWN_MS) return; //연속발사 방지
        lastBombTime = now;

        int sx = (int) ship.getX();
        int sy = (int) ship.getY();
        int startX = (int)(ship.getX() + 3);
        int startY = (int)(ship.getY() - 30);
        Entity bomb = new BombEntity(
                game,"sprites/Boom.gif",
                startX, startY, 0, -250);
        addEntity(bomb); // addEntity 사용
    }


    /**
     * Game 클래스의 메인 루프에서 호출되어 게임 상태를 업데이트합니다.
     *
     * @param delta 마지막 프레임 이후 경과 시간
     */
    public void update(long delta) {
        if (invincible && System.currentTimeMillis() > invincibilityEndTime) {
            invincible = false;
        }

        if (!waitingForKeyPress) {
            // 엔티티 이동
            for (Entity entity : entities) {
                entity.move(delta);
            }

            // 레이저 관통 타격: 레이저와 겹치는 모든 Alien에 타격
            if (laser != null && !waitingForKeyPress) {
                for (int i = 0; i < entities.size(); i++) {
                    Entity e = entities.get(i);
                    if (e instanceof AlienEntity) {
                        if (laser.collidesWith(e)) {
                            laser.collidedWith(e);
                            e.collidedWith(laser);
                        }
                    }
                }
            }


            // 충돌 검사
            for (int p = 0; p < entities.size(); p++) {
                for (int s = p + 1; s < entities.size(); s++) {
                    Entity me = entities.get(p);
                    Entity him = entities.get(s);

                    // if ship is invincible, skip collision with it
                    if ((me instanceof ShipEntity && invincible) || (him instanceof ShipEntity && invincible)) {
                        continue;
                    }

                    if (me instanceof org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity ||
                            him instanceof org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity) {
                        continue;
                    }

                    if (me.collidesWith(him) || him.collidesWith(me)) {
                        me.collidedWith(him);
                        him.collidedWith(me);
                    }
                }
            }
        }

        // 제거할 엔티티 정리
        entities.removeAll(removeList);
        removeList.clear();

        // 추가할 엔티티 정리
        entities.addAll(addList);
        addList.clear();

        // 레이저 수명 확인 및 정리
        if (laser != null && laser.isExpired()) {
            removeEntity(laser);
            laser = null;
        }

        // 추가 로직 실행
        if (logicRequiredThisLoop) {
            for (Entity entity : entities) {
                entity.doLogic();
            }
            logicRequiredThisLoop = false;
        }
    }

    /**
     * Game 클래스에서 키 입력 상태를 받아와 처리.
     */
    public void handleInput(boolean up, boolean down, boolean left, boolean right, boolean space, boolean shift, boolean z, boolean x, boolean c) {
        if (!waitingForKeyPress) {
            ship.setHorizontalMovement(0);
            ship.setVerticalMovement(0);
            if ((up) && (!down))
                ship.setVerticalMovement(-moveSpeed);
            if ((down) && (!up))
                ship.setVerticalMovement(moveSpeed);
            if ((left) && (!right))
                ship.setHorizontalMovement(-moveSpeed);
            if ((right) && (!left))
                ship.setHorizontalMovement(moveSpeed);
            if(shift)
                moveSpeed = 150;
            if(!shift)
                moveSpeed = 300;
            if (z) {
                tryToFire();
            }
            if (c) {
                fireBombIfReady();
            }
            // 레이저 발사 트리거: X키 누르면 3초 지속
            if (x && !laserButtonLatched && laser == null) {
                laser = new org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity(
                        game,
                        (org.newdawn.spaceinvaders.entity.ShipEntity) ship,
                        LASER_DURATION
                );
                addEntity(laser); // addEntity 사용
                laserButtonLatched = true;
            }
            if (!x) {
                laserButtonLatched = false;
            }
        }
    }

    /**
     * Game 클래스에서 호출되어 모든 엔티티를 화면에 그리기.
     */
    public void draw(Graphics2D g) {
        for (Entity entity : entities) {
            //
            if (entity instanceof org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity) {
                continue;
            }
            if (entity instanceof ShipEntity) {
                if (invincible) {
                    // Blink the ship every 100ms
                    if ((System.currentTimeMillis() / 100) % 2 == 0) {
                        continue;
                    }
                }
            }
            entity.draw(g);
        }
        // 레이저는 최상단 위에 한 번에 그리기
        if (laser != null) {
            laser.draw(g);
        }

        g.setColor(java.awt.Color.WHITE);
        g.drawString("Score: " + this.score, 700, 50);
        g.drawString("Lives: " + (this.lifes > 0 ? this.lifes -1 : 0), 10, 50);
    }
}