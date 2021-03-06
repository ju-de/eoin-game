package dmcigd.core.room;

import dmcigd.core.enums.*;
import dmcigd.core.objects.*;
import dmcigd.core.objects.interfaces.*;
import dmcigd.core.objects.maps.*;
import dmcigd.core.objects.particles.Particle;
import dmcigd.core.objects.player.*;

import java.awt.event.*;
import java.net.*;
import java.util.*;

public abstract class Room extends GameObjectHandler implements Runnable {
	
	//Stores codeBase string to be passed for file loading
	private URL codeBase;
	
	//Default level information
	private String levelName,roomName,tileSet;
	
	//Passes variables to be read by Main Game Loop
	private boolean ready,isDead = false;
	private String targetRoom;
	
	//Objects and object lists
	private Player player;
	private BlockMap blockMap = new BlockMap();
	private PhysicsHandler physicsHandler;
	private EnvironmentMap environmentMap = new EnvironmentMap();
	private DialogueHandler dialogueHandler = new DialogueHandler();
	public ArrayList<ObjectImage> visibleObjects;
	private ArrayList<TextLabel> textLabels = new ArrayList<TextLabel>();
        
	//Public Getters
	
	//Booleans
	public boolean isReady() {
		return ready;
	}
	public boolean isDead() {
		return isDead;
	}
	
	//Strings
	public String getTargetRoom() {
		return targetRoom;
	}
	public String getLevelName() {
		return levelName;
	}
	public String getRoomName() {
		return roomName;
	}
	public String getTileSet() {
		return tileSet;
	}
	
	//Special Objects
	public Player getPlayer() {
		return player;
	}
	public BlockMap getBlockMap() {
		return blockMap;
	}
	public PhysicsHandler getPhysicsHandler() {
		return physicsHandler;
	}
	public EnvironmentMap getEnvironmentMap() {
		return environmentMap;
	}
	public DialogueHandler getDialogueHandler() {
		return dialogueHandler;
	}
	
	//Linked Lists
	public ArrayList<ObjectImage> getVisibleObjects() {
		return visibleObjects;
	}
	public ArrayList<TextLabel> getTextLabels() {
		return textLabels;
	}
	
	//Public Setters
	public void addTextLabel(TextLabel textLabel) {
		textLabels.add(textLabel);
	}
        
	public Room(URL codeBase, String levelName, String roomName, String tileSet) {
		this.codeBase = codeBase;
		this.levelName = levelName;
		this.roomName = roomName;
		this.tileSet = tileSet;
		
		//Initializes Thread
		Thread th = new Thread(this);
		th.start();
		
	}
	
	public void addVisibleObject(GameObject i) {
		if(i.isVisible((int) player.getX(), (int) player.getY())) {
			visibleObjects.add(i.getObjectImage((int) player.getX(), (int) player.getY()));
		}
	}
	
	public void fetchVisibleObjects() {
		
		visibleObjects = new ArrayList<ObjectImage>();

		//Add Background Objects
		for (VisibleObject i : getBackgroundObjects()) {
			if(i.isVisible((int) player.getX(), (int) player.getY())) {
				visibleObjects.add(i.getObjectImage((int) player.getX(), (int) player.getY()));
			}
		}
		
		//Add Regions
		for (GameObject i : getRegions()) {
			addVisibleObject(i);
		}
                
		 // add visible particles
                for (Particle p: getParticles()){
                    //System.out.println("trying to render particle" + p.getX() + " " + p.getY());
                    if(p.isVisible((int) player.getX(), (int) player.getY())) {
                        visibleObjects.add(p.getObjectImage((int) player.getX(), (int) player.getY()));
                    }
                }
		
		//Add Solid Objects
		for (GameObject i : getSolidObjects()) {
			addVisibleObject(i);
		}

		//Draw sword only if player is not holding an object
		if(player.heldItem == null) {
			//Add Sword
			visibleObjects.add(player.sword.getObjectImage((int) player.getX(), (int) player.getY()));
		}
		
		//Add Items
		for (GameObject i : getItems()) {
			addVisibleObject(i);
		}

		//Add Projectiles
		for (GameObject i : getProjectiles()) {
			addVisibleObject(i);
		}
                
               
		//Add Foreground Objects
		for (VisibleObject i : getForegroundObjects()) {
			if(i.isVisible((int) player.getX(), (int) player.getY())) {
				visibleObjects.add(i.getObjectImage((int) player.getX(), (int) player.getY()));
			}
		}
	}
	
	public void step() {
		
		stepGameObjects();
		
		//Checks for player death
		if(player.isDead || player.isDestroyed) {
			isDead = true;
		}
		
		//Check for level advancement
		if(player.getRoom() != null) {
			targetRoom = player.getRoom();
		}
		
		fetchVisibleObjects();
		
	}
	
        @Override
	public void run() {
		
		blockMap.loadBlockMap(codeBase, levelName, roomName);
		environmentMap.loadEnvironmentMap(codeBase, levelName, roomName);
		
		physicsHandler = new PhysicsHandler(blockMap, getSolidObjects());
		
		player = new Player(blockMap.getSpawnX() * 32 + 6, blockMap.getSpawnY() * 32, this);

		addImageResource("playerrun.gif");
		addImageResource("playerjump.gif");
		
		initializeRoom();
		
		addSolidObject(player);

		fetchVisibleObjects();
		
		ready = true;
		
	}
	
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		switch(keyCode) {
			case KeyEvent.VK_UP:
				player.climb(true, Direction.UP);
				break;
			case KeyEvent.VK_DOWN:
				player.climb(true, Direction.DOWN);
				break;
			case KeyEvent.VK_LEFT:
				player.walk(true, Direction.LEFT);
				break;
			case KeyEvent.VK_RIGHT:
				player.walk(true, Direction.RIGHT);
				break;
			case KeyEvent.VK_SHIFT:
				player.sprint(true);
				break;
			case KeyEvent.VK_Z:
				player.jump(true);
				break;
			case KeyEvent.VK_X:
				if(dialogueHandler.inDialogue()) {
					dialogueHandler.advance();
				} else {
					player.interact();
				}
				break;
			default:
				break;
		}
	}
	
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		switch(keyCode) {
			case KeyEvent.VK_UP:
				player.climb(false, Direction.UP);
				break;
			case KeyEvent.VK_DOWN:
				player.climb(false, Direction.DOWN);
				break;
			case KeyEvent.VK_LEFT:
				player.walk(false, Direction.LEFT);
				break;
			case KeyEvent.VK_RIGHT:
				player.walk(false, Direction.RIGHT);
				break;
			case KeyEvent.VK_SHIFT:
				player.sprint(false);
			case KeyEvent.VK_Z:
				player.jump(false);
				break;
			default:
				break;
		}
	}
}
