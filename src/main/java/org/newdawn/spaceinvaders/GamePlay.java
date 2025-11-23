package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.*;
import org.newdawn.spaceinvaders.entity.BossSkill.GuidedBossShotEntity;
import org.newdawn.spaceinvaders.entity.playerSkill.BombEntity;
import org.newdawn.spaceinvaders.event.Event;
import org.newdawn.spaceinvaders.event.EventBus;
import org.newdawn.spaceinvaders.event.EventListener;
import org.newdawn.spaceinvaders.event.PlayerHitEvent;
import org.newdawn.spaceinvaders.stage.*;
import org.newdawn.spaceinvaders.system.CollisionSystem;
import org.newdawn.spaceinvaders.system.EntityManager;
import org.newdawn.spaceinvaders.system.MovementSystem;
import org.newdawn.spaceinvaders.system.RenderSystem;
import org.newdawn.spaceinvaders.utility.SoundManager;

import java.awt.*;
import java.util.List;

public class GamePlay implements EventListener {

    private final EntityManager entityManager = new EntityManager();
    private final MovementSystem movementSystem = new MovementSystem();
    private final CollisionSystem collisionSystem = new CollisionSystem();
    private final RenderSystem renderSystem = new RenderSystem();

    private Entity ship;
    private double moveSpeed = 300;
    private long lastFire = 0;
    private long firingInterval = 90;
    private int alienCount;
    private int score = 0;
    private int lifes = 3;
    private boolean invincible = false;
    private long invincibilityEndTime = 0;
    private int laserCharges = 2;
    private int bombCharges = 2;
    private long lastBombTime = 0L;
    private static final long BOMB_COOLDOWN_MS = 1500L;
    private String message = "";
    private boolean waitingForKeyPress = true;
    private boolean logicRequiredThisLoop = false;
    private Game game;
    private Stage stage;
    private int difficulty;
    private org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity laser;
    private final long LASER_DURATION = 3000;
    private boolean laserButtonLatched = false;

    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean shiftPressed = false;
    private boolean zPressed = false;
    private boolean xPressed = false;
    private boolean cPressed = false;

    public GamePlay(Game game, int difficulty) {
        this.game = game;
        this.difficulty = difficulty;
        setStage(difficulty);

        if (difficulty == 3) { // Lunatic Mode Bonus
            this.lifes = 101;
            this.laserCharges = 20;
            this.bombCharges = 20;
        }

        initEntities();
        EventBus.getInstance().register(this);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof PlayerHitEvent) {
            loseLifeAndRespawn();
        }
    }

    private void setStage(int difficulty) {
        switch (difficulty) {
            case 0: this.stage = new EasyStage(); break;
            case 1: this.stage = new NormalStage(); break;
            case 2: this.stage = new HardStage(); break;
            case 3: this.stage = new LunaticStage(); break;
        }
    }

    private void initEntities() {
        ship = new ShipEntity(this, "sprites/ship.gif", 370, 550);
        entityManager.addEntity(ship);
        stage.initEntities(this);
        alienCount = stage.getAlienCount();
    }

    public void update(long delta) {
        handleInput();

        if (invincible && System.currentTimeMillis() > invincibilityEndTime) {
            invincible = false;
        }

        if (!waitingForKeyPress) {
            stage.update(this);
            movementSystem.update(delta, entityManager);

            // Special laser collision logic
            if (laser != null) {
                for (Entity e : entityManager.getEntities()) {
                    if (e instanceof AlienEntity || e instanceof BossShotEntity || e instanceof GuidedBossShotEntity) {
                        if (laser.collidesWith(e)) {
                            laser.collidedWith(e);
                            e.collidedWith(laser);
                        }
                    }
                }
            }
            collisionSystem.checkCollisions(entityManager, invincible);
        }

        entityManager.updateLists();

        if (laser != null && laser.isExpired()) {
            removeEntity(laser);
            laser = null;
        }

        if (logicRequiredThisLoop) {
            for (Entity entity : entityManager.getEntities()) {
                entity.doLogic();
            }
            logicRequiredThisLoop = false;
        }
    }

    public void draw(Graphics2D g) {
        renderSystem.draw(g, entityManager, invincible, score, lifes, laserCharges, bombCharges);
    }

    private void handleInput() {
        if (waitingForKeyPress) return;

        ship.setHorizontalMovement(0);
        ship.setVerticalMovement(0);

        double currentMoveSpeed = shiftPressed ? 150 : 300;

        if (upPressed && !downPressed) ship.setVerticalMovement(-currentMoveSpeed);
        if (downPressed && !upPressed) ship.setVerticalMovement(currentMoveSpeed);
        if (leftPressed && !rightPressed) ship.setHorizontalMovement(-currentMoveSpeed);
        if (rightPressed && !leftPressed) ship.setHorizontalMovement(currentMoveSpeed);

        if (zPressed) tryToFire();
        if (cPressed) fireBombIfReady();

        if (xPressed && !laserButtonLatched && laser == null && laserCharges > 0) {
            laserCharges--;
            laser = new org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity(this, (ShipEntity) ship, LASER_DURATION);
            addEntity(laser);
            laserButtonLatched = true;
        }
        if (!xPressed) {
            laserButtonLatched = false;
        }
    }

    public void loseLifeAndRespawn() {
        if (invincible) return;
        lifes--;
        if (lifes > 0) {
            ship.setPosition(370, 550);
            invincible = true;
            invincibilityEndTime = System.currentTimeMillis() + 2000;
        } else {
            notifyDeath();
        }
    }

    public void notifyAlienKilled(Entity alien) {
        score += 100;
        alienCount--;
        if (alienCount == 0) {
            notifyWin();
        }
        for (Entity entity : entityManager.getEntities()) {
            if (entity instanceof AlienEntity) {
                entity.setHorizontalMovement(entity.getHorizontalMovement() * 1.02);
            }
        }
    }

    public void tryToFire() {
        if (System.currentTimeMillis() - lastFire < firingInterval) return;
        lastFire = System.currentTimeMillis();
        ShotEntity shot = new ShotEntity(this, "sprites/shot.gif", ship.getX() + 10, ship.getY() - 30, ship);
        entityManager.addEntity(shot);
        SoundManager.get().playSound("sounds/alienshoot2.wav");
    }

    private void fireBombIfReady() {
        if (bombCharges <= 0) return;
        long now = System.currentTimeMillis();
        if (now - lastBombTime < BOMB_COOLDOWN_MS) return;
        lastBombTime = now;
        bombCharges--;
        Entity bomb = new BombEntity(this, "sprites/Boom.gif", (int) (ship.getX() + 3), (int) (ship.getY() - 30), 0, -250);
        addEntity(bomb);
    }

    public void startGame() {
        entityManager.clear();
        initEntities();
        waitingForKeyPress = false;
    }

    public void addEntity(Entity entity) { entityManager.addEntity(entity); }
    public void removeEntity(Entity entity) { entityManager.removeEntity(entity); }
    public List<Entity> getEntities() { return entityManager.getEntities(); }
    public void notifyDeath() { message = "Oh no! They got you, try again?"; waitingForKeyPress = true; }
    public void notifyWin() { message = "Well done! You Win!"; waitingForKeyPress = true; }
    public void updateLogic() { logicRequiredThisLoop = true; }
    public void addScore(int amount) { this.score += amount; }
    public void increaseLife() { this.lifes++; }
    public void increaseLaserCharges() { this.laserCharges++; }
    public void increaseBombCharges() { this.bombCharges++; }
    public Game getGame() { return game; }
    public boolean isWaitingForKeyPress() { return waitingForKeyPress; }
    public String getMessage() { return message; }
    public int getScore() { return this.score; }
    public int getDifficulty() { return this.difficulty; }
    public int getLifes() { return lifes; }
    public void setMoveUp(boolean pressed) { this.upPressed = pressed; }
    public void setMoveDown(boolean pressed) { this.downPressed = pressed; }
    public void setMoveLeft(boolean pressed) { this.leftPressed = pressed; }
    public void setMoveRight(boolean pressed) { this.rightPressed = pressed; }
    public void setShift(boolean pressed) { this.shiftPressed = pressed; }
    public void setFire(boolean pressed) { this.zPressed = pressed; }
    public void setLaser(boolean pressed) { this.xPressed = pressed; }
    public void setBomb(boolean pressed) { this.cPressed = pressed; }
}