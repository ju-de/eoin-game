package dmcigd.core.objects;

import dmcigd.core.*;

public class MovingObject extends BlockCollision {
	
	//Initialize position properties (position, velocity, and acceleration)
	private int x = 0;
	private int y = 0;
	private float vx = 0;
	private float vy = 2;
	private float aUp = 0;
	private float aDown = 0;
	private float aLeft = 0;
	private float aRight = 0;
	private float tUp = 32;
	private float tDown = 32;
	private float tLeft = 32;
	private float tRight = 32;
	
	//Initialize hitGround and isFalling property to be passed for child manipulation
	public boolean hitGround,isFalling;
	
	//Initialize blockLoader
	private BlockLoader blockLoader;
	
	//Public getters
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public float getVX() {
		return vx;
	}
	public float getVY() {
		return vy;
	}
	
	//Public setters
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	public void setVX(float vx) {
		this.vx = vx;
	}
	public void setVY(float vy) {
		this.vy = vy;
	}
	public void setAUp(float aUp) {
		this.aUp = aUp;
	}
	public void setADown(float aDown) {
		this.aDown = aDown;
	}
	public void setALeft(float aLeft) {
		this.aLeft = aLeft;
	}
	public void setARight(float aRight) {
		this.aRight = aRight;
	}
	public void setTUp(float tUp) {
		this.tUp = tUp;
	}
	public void setTDown(float tDown) {
		this.tDown = tDown;
	}
	public void setTLeft(float tLeft) {
		this.tLeft = tLeft;
	}
	public void setTRight(float tRight) {
		this.tRight = tRight;
	}
	public void setBlockLoader(BlockLoader blockLoader) {
		this.blockLoader = blockLoader;
	}
	public void setGravity() {
		accelerate(0.4f, 5.0f, Direction.DOWN);
	}
	public void accelerate(float rate, float terminal, Direction direction) {
		switch(direction) {
			case UP:
				aUp = rate;
				tUp = terminal;
				break;
			case DOWN:
				aDown = rate;
				tDown = terminal;
				break;
			case LEFT:
				aLeft = rate;
				tLeft = terminal;
				break;
			case RIGHT:
				aRight = rate;
				tRight = terminal;
				break;
			default:
				break;
		}
	}
	
	public void move() {
		
		hitGround = false;
		isFalling = false;
		
		int hShift = 0;
		char[][] immediateBlocks = blockLoader.getImmediateBlocks(x,y);
		
		//Calculate Acceleration
		vx = vx + aRight - aLeft;
		vy = vy + aDown - aUp;
		
		//Cap all speeds at 30px per tick
		if(vx + aRight - aLeft > tRight) {
			vx = tRight;
		}else if(vx + aRight - aLeft < -tLeft) {
			vx = -tLeft;
		}
		if(vy + aDown - aUp > tDown) {
			vy = tDown;
		}else if(vy + aDown - aUp < -tUp) {
			vy = -tUp;
		}
		
		if((int) vx > 0) {
			//Move Right
			
			//Compare column of right edge to column of destination's right edge
			//If both are in the same column, there is no possibility of intersection with a block
			if(tileCol(x, Direction.RIGHT) == tileCol(x + (int) vx, Direction.RIGHT)) {
				
				//If left edge crosses, account for new block position
				if(tileCol(x, Direction.LEFT) != tileCol(x + (int) vx, Direction.LEFT)) {
					hShift = 1;
				}
				
				//Move the object over
				x = x + (int) vx;
				
			} else {
				
				//Determine the tile offset (1 or 2)
				//Compare the column of the left edge (the center)
				//against the right edge of the destination (the tile to be checked)
				int col = tileCol(x + (int) vx, Direction.RIGHT) - tileCol(x, Direction.LEFT) + 1;
				
				//Check if tile to the immediate right is clear
				if(!isSolid(immediateBlocks[1][col])) {
					
					//Check if object is between two rows
					if(!betweenRows(y)) {
						
						//If left edge crosses, account for new block position
						if(tileCol(x, Direction.LEFT) != tileCol(x + (int) vx, Direction.LEFT)) {
							hShift = 1;
						}

						
						//Move the object over
						x = x + (int) vx;
						
					} else {
						
						//Check if tile to the bottom right is clear
						if(!isSolid(immediateBlocks[2][col])) {
							
							//If left edge crosses, account for new block position
							if(tileCol(x, Direction.LEFT) != tileCol(x + (int) vx, Direction.LEFT)) {
								hShift = 1;
							}

							//Move the object over
							x = x + (int) vx;
							
						} else {
							
							//Move the object to right edge of column
							x = colEdge(x, Direction.RIGHT);
						}
					}
				} else {
					//Move the object to right edge of column
					x = colEdge(x, Direction.RIGHT);
				}
			}
			
		}else if((int) vx < 0) {
			//Move left
			
			//Check if object is crossing a tile
			if(tileCol(x, Direction.LEFT) == tileCol(x + (int) vx, Direction.LEFT)) {
				
				//Move the object over
				x = x + (int) vx;
				
			} else {
				
				//Check if tile to the immediate left is clear
				if(!isSolid(immediateBlocks[1][0])) {
					
					//Check if object is between two rows
					if(!betweenRows(y)) {
						
						//Account for shift in tile position
						hShift = -1;

						//Move the object over
						x = x + (int) vx;
						
					} else {
						
						//Check if tile to the bottom left is clear
						if(!isSolid(immediateBlocks[2][0])) {
							
							//Account for shift in tile position
							hShift = -1;

							//Move the object over
							x = x + (int) vx;
							
						} else {
							
							//Move the object to left edge of column
							x = colEdge(x, Direction.LEFT);
						}
					}
				} else {
					
					//Move the object to left edge of column
					x = colEdge(x, Direction.LEFT);
				}
			}
		}
		
		if((int) vy >= 0) {
			//Move Down
			
			//Check if object is crossing a tile
			if(tileRow(y, Direction.DOWN) == tileRow(y + (int) vy, Direction.DOWN)) {

				//Move the object over
				y = y + (int) vy;
				
				if(y == rowEdge(y, Direction.DOWN) && isSolid(immediateBlocks[1][1 + hShift])) {
					hitGround = true;
				} else {
					isFalling = true;
				}
				
			} else {
				
				//Determine the row offset (1 or 2)
				int row = tileRow(y + (int) vy, Direction.DOWN) - tileRow(y, Direction.UP) + 1;
				
				//Check if tile to the immediate bottom is clear
				if(!isSolid(immediateBlocks[row][1 + hShift])) {
					
					if(!betweenCols(x)) {
						
						//Move the object over
						y = y + (int) vy;
						isFalling = true;
						
					} else {
						
						//Check the object to the bottom right is clear
						if(!isSolid(immediateBlocks[row][2 + hShift])) {

							//Move the object over
							y = y + (int) vy;
							isFalling = true;
							
						} else {
							//Move object to bottom row edge
							y = rowEdge(y, Direction.DOWN);
							hitGround = true;
						}
					}
				} else {
					//Move object to bottom row edge
					y = rowEdge(y, Direction.DOWN);
					hitGround = true;
				}
			}
		}else if((int) vy < 0) {
			//Move Up
			
			//Check if object is crossing a tile
			if(tileRow(y, Direction.UP) == tileRow(y + (int) vy, Direction.UP)) {
				//Move the object over
				y = y + (int) vy;
			} else {
				
				//Check if tile to the immediate top is clear
				if(!isSolid(immediateBlocks[0][1 + hShift])) {
					
					if(!betweenCols(x)) {
						//Move the object over
						y = y + (int) vy;
					} else {
						
						//Check the object to the top right is clear
						if(!isSolid(immediateBlocks[0][2 + hShift])) {
							//Move the object over
							y = y + (int) vy;
						} else {
							
							//Move object to bottom top edge
							y = rowEdge(y, Direction.UP);
						}
					}
				} else {
					//Move object to bottom top edge
					y = rowEdge(y, Direction.UP);
				}
			}
		}
		
		if(hitGround) {
			vy = 0;
		}
	}
}
