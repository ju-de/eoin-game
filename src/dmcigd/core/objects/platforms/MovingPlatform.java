package dmcigd.core.objects.platforms;

import dmcigd.core.enums.CollisionType;
import dmcigd.core.objects.*;
import dmcigd.core.objects.interfaces.*;

public class MovingPlatform extends MovingObject implements RestableObject {
	
	//NOTE:
	//It wasn't worth making an enumerator for a two-value integer
	//That only exists for this class
	
	//Type 0: Horizontal movement
	//Type 1: Vertical movement
	
	private int objectClock,clockReset = 0;
	
	public boolean isDestroyed() { return false; }
	
	public MovingPlatform(int x, int y, int type, int width, float speed, int travelDistance) {
		
		setX(x);
		setY(y);
		setHeight(22);
		setWidth(width * 16);
		setImageHeight(32);
		setImageWidth(width * 16);
		
		setSequence(1);
		setFrame(0);
		
		setCollisionType(CollisionType.PLATFORM);
		
		if(type == 0) {
			setVX(speed);
		} else {
			setVY(speed);
		}
		
		setImagePath("objects.gif");
		
		clockReset = (int) (travelDistance * 16 / speed);
	}
	
	public void step() {
		
		if(objectClock < clockReset) {
			objectClock++;
		} else {
			setVX(-getVX());
			setVY(-getVY());
			objectClock = 0;
		}
		
		addX(getVX());
		addY(getVY());
		
	}
}
