package dmcigd.core.objects.player;

import dmcigd.core.objects.*;
import dmcigd.core.objects.interfaces.*;

import java.util.ArrayList;

public class Sword extends ObjectCollision {
	
	private float baseX,baseY;
	private boolean attacking,onLadder = false;
	private int damage = 10;
	private ArrayList<SolidObject> solidObjects;
	private int killCount = 0;
	
	//Public Getters
	public int getKillCount() {
		return killCount;
	}
	
	//Public setters
	public void addKillCount(int killCount) {
		this.killCount += killCount;
	}
	public void setPosition(float x, float y) {
		baseX = x;
		baseY = y + 10;
	}
	public void setLadder(boolean onLadder) {
		this.onLadder = onLadder;
	}
	public void attack() {
		if(!onLadder && !attacking) {
			this.attacking = true;
			setSequence(1);
			setFrame(0);
		}
	}
	
	public Sword(int x, int y, ArrayList<SolidObject> solidObjects) {
		
		baseX = x;
		baseY = y + 10;
		
		setX(baseX - 4);
		setY(baseY);
		setHeight(16);
		setWidth(44);
		setImageHeight(30);
		setImageWidth(44);
		
		setImagePath("sword.gif");
		
		this.solidObjects = solidObjects;
		
		setSequence(0);
		setFrame(0);
		
		setFrameLimits(new int[] {2,6});
		setAnimationLoops(new boolean[] {false,false});
		setFrameSpeed(0.4f);
		
	}
	
	public void checkCollision() {
		//Loop through solid objects
		for(SolidObject i : solidObjects) {
			if(getBounds().intersects(i.getBounds())) {
				//Increments the KillCount
				if(i.onAttack(damage, flipped)) {
					killCount++;
				}
			}
		}
	}
	
	public void step() {
		if(onLadder) {
			
			setSequence(0);
			setFrame(1);
			
			setX(baseX - 12);
			setY(baseY + 2);
			
			attacking = false;
		}else if(attacking) {
			
			//Offset
			int offSet = getFrame() * getFrame();
			
			//Set Position
			if(flipped) {
				setX(baseX - offSet - 24);
			} else {
				setX(baseX + offSet - 4);
			}
			setY(baseY);
			
			//Attack all collided objects
			checkCollision();
			
			//Animate
			animate();
			if(getFrame() == 5) {
				attacking = false;
			}
		}else {
			setSequence(0);
			setFrame(0);
			
			if(flipped) {
				setX(baseX - 20);
			} else {
				setX(baseX - 4);
			}
			
			setY(baseY);
		}
	}
}
