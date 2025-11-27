package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.utility.SpriteStore;

/**
 * 기본 적 엔티티
 * 좌우로 이동하면서 화면 가장자리에 도달하면 방향을 바꾸고, 플레이어가 있는 쪽으로 조금씩 내려온다.
 */
public class AlienEntity extends Entity implements Damageable {
	// 적의 기본 이동 속도
	private double moveSpeed = 75;
	// 적이 속한 게임 인스턴스
	private Game game;
    // 애니메이션에 사용되는 프레임 배열
	private Sprite[] frames = new Sprite[4];
	// 마지막 프레임이 변경된 이후 경과 시간
	private long lastFrameChange;
	// 한 프레임이 유지되는 시간
	private long frameDuration = 250;
	// 현재 표시 중인 프레임 인덱스
	private int frameNumber;

	// 적 체력
	private int health;

    /**
     *  새로운 ALienEntity 생성
     * @param game  이 적이 속한 Game 인스턴스
     * @param x     적의 초기 x 좌표
     * @param y     적의 초기 y 좌표
     */
    public AlienEntity(Game game,int x,int y) {
		super("sprites/Boss1.gif",x,y);
		
		// 애니메이션 프레임 설정
		frames[0] = sprite;
		frames[1] = SpriteStore.get().getSprite("sprites/Boss1.gif");
		frames[2] = sprite;
		frames[3] = SpriteStore.get().getSprite("sprites/Boss1.gif");
		
		this.game = game;
		dx = -moveSpeed;
		this.health = 100; // 체력 초기화
	}

	// 데미지 처리 : 체력을 감소시킴
	public void takeDamage(int damage) {
		this.health -= damage;
	}
	
	public int getHealth() {
		return health;
	}

    // 적의 이동 및 애니메이션을 처리
    	public void move(long delta) {
		// 경과 시간을 기반으로 애니메이션 프레임 변경
		lastFrameChange += delta;

		if (lastFrameChange > frameDuration) {
			lastFrameChange = 0;

			frameNumber++;
			if (frameNumber >= frames.length) {
				frameNumber = 0;
			}
			
			sprite = frames[frameNumber];
		}

        // 왼쪽 끝에 도달하면 GamePlay에 로직 업데이트 요청
		if ((dx < 0) && (x < 10)) {
			if (game.getGamePlay() != null) {
				game.getGamePlay().updateLogic();
			}
		}
		// 오른쪽 끝에 도달하면 마찬가지로 로직 업데이트 요청
		if ((dx > 0) && (x > 750)) {
			if (game.getGamePlay() != null) {
				game.getGamePlay().updateLogic();
			}
		}
		
		// 기본 이동 처리
		super.move(delta);
	}
	

    // 외부에서 호출되는 Alien 전용 로직
    // 이동 방향을 반전시키고 화면 아래로 한 칸 내려간다.
    // 바닥에 도달하면 플레이어 사망 처리
	public void doLogic() {
		// 수평 이동 방향 반전 + 약간 아래로 이동
		dx = -dx;
		y += 10;
		
		// 화면 아래로 내려가면 플레이어 사망
		if (y > 570) {
			if (game.getGamePlay() != null) {
				game.getGamePlay().notifyDeath();
			}
		}
	}
	
	// 다른 엔티티와 충동했을 때 호출된다.
	public void collidedWith(Entity other) {
        // 플레이어 레이저 스킬과 충돌 시 체력 1 감소
		if (other instanceof org.newdawn.spaceinvaders.entity.playerSkill.LaserEntity) {
			takeDamage(1);
		}
	}
}