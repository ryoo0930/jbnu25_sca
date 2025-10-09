package org.newdawn.spaceinvaders.entity.boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.BossShotEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

import java.util.Random;

public class HardBossEntity extends AlienEntity {
    private Game game;
    private long lastAttackTime = 0;
    private long attackCooldown = 2000; // 2초마다 공격
    private int attackPattern = 0;
    private Random random = new Random();
    private int health = 5000; // 보스 체력

    // 움직임 관련
    private long lastMoveTime = 0;
    private long moveInterval = 1500; // 1.5초마다 움직임 변경
    private double targetX, targetY;


    public HardBossEntity(Game game, int x, int y) {
        super(game, x, y);
        this.game = game;
        this.sprite = game.getSpriteStore().getSprite("sprites/alien2.gif"); // 임시 보스 스프라이트
        this.x = x;
        this.y = y;
        setRandomTargetPosition();
    }

    private void setRandomTargetPosition() {
        targetX = 100 + random.nextInt(600);
        targetY = 50 + random.nextInt(150);
    }

    @Override
    public void move(long delta) {
        long currentTime = System.currentTimeMillis();

        // 위치 이동
        if (currentTime - lastMoveTime > moveInterval) {
            lastMoveTime = currentTime;
            setRandomTargetPosition();
        }

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 1) {
            this.dx = (dx / distance) * 100; // 이동 속도
            this.dy = (dy / distance) * 100;
        } else {
            this.dx = 0;
            this.dy = 0;
        }

        super.move(delta);


        // 공격 패턴
        if (currentTime - lastAttackTime > attackCooldown) {
            lastAttackTime = currentTime;
            fireAttackPattern();
            attackPattern = (attackPattern + 1) % 3; // 3가지 패턴 순환
        }
    }

    private void fireAttackPattern() {
        switch (attackPattern) {
            case 0:
                patternCircleShot();
                break;
            case 1:
                patternSpiralShot();
                break;
            case 2:
                patternAimedBurst();
                break;
        }
    }

    // 패턴 1: 원형으로 총알 발사
    private void patternCircleShot() {
        int bulletCount = 20;
        for (int i = 0; i < bulletCount; i++) {
            double angle = 2 * Math.PI * i / bulletCount;
            double speed = 150;
            double shotDx = Math.cos(angle) * speed;
            double shotDy = Math.sin(angle) * speed;
            game.addEntity(new BossShotEntity(game, "sprites/shot2.gif", (int) x, (int) y, shotDx, shotDy));
        }
    }

    // 패턴 2: 나선형으로 총알 발사
    private void patternSpiralShot() {
        int bulletCount = 30;
        for (int i = 0; i < bulletCount; i++) {
            double angle = 0.1 * i * Math.PI;
            double speed = 150 + i * 5;
            double shotDx = Math.cos(angle) * speed;
            double shotDy = Math.sin(angle) * speed;
            game.addEntity(new BossShotEntity(game, "sprites/shot2.gif", (int) x, (int) y, shotDx, shotDy));
        }
    }

    // 패턴 3: 플레이어 조준 연사
    private void patternAimedBurst() {
        Entity player = null;
        for (Object entity : game.getEntities()) {
            if (entity instanceof ShipEntity) {
                player = (Entity) entity;
                break;
            }
        }

        if (player != null) {
            double dx = player.getX() - this.x;
            double dy = player.getY() - this.y;
            double angle = Math.atan2(dy, dx);
            double speed = 200;

            for (int i = -2; i <= 2; i++) {
                double adjustedAngle = angle + (i * 0.1);
                double shotDx = Math.cos(adjustedAngle) * speed;
                double shotDy = Math.sin(adjustedAngle) * speed;
                game.addEntity(new BossShotEntity(game, "sprites/shot2.gif", (int) x, (int) y, shotDx, shotDy));
            }
        }
    }


    @Override
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            game.removeEntity(this);
            game.notifyWin(); // 보스 처치 시 게임 승리
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity) {
            takeDamage(30); // 플레이어 총알에 맞았을 때
            game.removeEntity(other);
        }
    }
}