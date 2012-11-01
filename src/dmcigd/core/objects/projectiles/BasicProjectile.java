package dmcigd.core.objects.projectiles;

import java.awt.Rectangle;
import java.util.ArrayList;

import dmcigd.core.objects.*;
import dmcigd.core.objects.interfaces.*;
import dmcigd.core.enums.EntityType;
import dmcigd.core.objects.maps.BlockMap;

public class BasicProjectile extends Entity {
	
	public boolean objectsCollide(Rectangle boundingBox, SolidObject object) {
		
		//Overrides objectsCollide method to disregard boundary case
		//Of inherent object overlap. ALL collisions are counted.
		
		if(boundingBox.intersects(object.getBounds())) {
			return true;
		} else {
			return false;
		}
	}

	public BasicProjectile(int x, int y, int speed, int angle, boolean flipped, BlockMap blockMap, ArrayList<SolidObject> solidObjects) {
		
		setX(x);
		setY(y);
		
		//Flips angle if necessary, default facing right
		if(flipped) {
			angle = 180 - angle;
		}
		
		//Converts to radians
		double radians = Math.toRadians(angle);
		
		//Determines component vectors
		double vx = speed * Math.cos(radians);
		setVX((float) vx);
		
		double vy = -speed * Math.sin(radians);
		setVY((float) vy);
		
		//If specific collision attributes are necessary, override the EntityType in the constructor
		setEntityType(EntityType.PROJECTILE);
		setBlockMap(blockMap);
		setSolidObjects(solidObjects);
			
	}
	
	public BasicProjectile(int x, int y, int speed, int angle, BlockMap blockMap, ArrayList<SolidObject> solidObjects) {
		this(x,y,speed,angle,false,blockMap,solidObjects);
	}
	
	public void step() {
		move();
	}
}