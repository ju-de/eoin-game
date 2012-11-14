package dmcigd.core.room;

import dmcigd.core.enums.*;
import dmcigd.core.objects.*;
import dmcigd.core.objects.interfaces.*;
import dmcigd.core.objects.maps.*;
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
	private String targetLevel,targetRoom;
	
	//Objects and object lists
	private Player player;
	private BlockMap blockMap = new BlockMap();
	private EnvironmentMap environmentMap = new EnvironmentMap();
	private DialogueHandler dialogueHandler = new DialogueHandler();
	private ArrayList<ObjectImage> visibleObjects;
	private ArrayList<TextLabel> textLabels = new ArrayList<TextLabel>();
	
	//Public Getters
	
	//Booleans
	public boolean isReady() {
		return ready;
	}
	public boolean isDead() {
		return isDead;
	}
	public boolean inDialogue() {
		return dialogueHandler.inDialogue();
	}
	
	//Strings
	public String getTargetLevel() {
		return targetLevel;
	}
	public String getTargetRoom() {
		return targetRoom;
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
	
	public void fetchVisibleObjects() {
		
		visibleObjects = new ArrayList<ObjectImage>();

		for (Region i : getRegions()) {
			if(i.isVisible(player.getX(), player.getY())) {
				visibleObjects.add(i.getObjectImage(player.getX(), player.getY()));
			}
		}
		
		for (SolidObject i : getSolidObjects()) {
			if(i.isVisible(player.getX(), player.getY())) {
				visibleObjects.add(i.getObjectImage(player.getX(), player.getY()));
			}
		}
		
		visibleObjects.add(player.getObjectImage(player.getX(), player.getY()));
		
		if(player.heldItem == null) {
			//Draw sword if player is not holding an object
			visibleObjects.add(player.sword.getObjectImage(player.getX(), player.getY()));
		}
		
		for (Item i : getItems()) {
			if(i.isVisible(player.getX(), player.getY())) {
				visibleObjects.add(i.getObjectImage(player.getX(), player.getY()));
			}
		}
		
		for (SolidObject i : getProjectiles()) {
			if(i.isVisible(player.getX(), player.getY())) {
				visibleObjects.add(i.getObjectImage(player.getX(), player.getY()));
			}
		}
	}
	
	public void step() {
		
		stepGameObjects();
		
		if(player.isDead || player.isDestroyed) {
			isDead = true;
		}
		
		//Check for level advancement
		if(player.getRoom() != null) {
			targetLevel = player.getLevel();
			targetRoom = player.getRoom();
		}
		
		fetchVisibleObjects();
		
	}
	
	public void run() {
		
		blockMap.loadBlockMap(codeBase, levelName, roomName);
		environmentMap.loadEnvironmentMap(codeBase, levelName, roomName);
		
		player = new Player(blockMap.getSpawnX() * 32 + 6, blockMap.getSpawnY() * 32, blockMap, getSolidObjects(), getItems(), getRegions());
		
		initializeSolidObjects();
		
		addSolidObject(player);
		
		initializeNonsolidObjects();

		fetchVisibleObjects();
		
		ready = true;
		
	}
	
	//Hussain, please abstract out input handling. We're starting to get a bad case of god classes
	
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		switch(keyCode) {
			case KeyEvent.VK_UP:
				player.climbUp(true);
				break;
			case KeyEvent.VK_DOWN:
				player.keyDown(true);
				break;
			case KeyEvent.VK_LEFT:
				player.walk(true, Direction.LEFT);
				break;
			case KeyEvent.VK_RIGHT:
				player.walk(true, Direction.RIGHT);
				break;
			case KeyEvent.VK_Z:
				player.jump(true);
				break;
			case KeyEvent.VK_X:
				if(inDialogue()) {
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
				player.climbUp(false);
				break;
			case KeyEvent.VK_DOWN:
				player.keyDown(false);
				break;
			case KeyEvent.VK_LEFT:
				player.walk(false, Direction.LEFT);
				break;
			case KeyEvent.VK_RIGHT:
				player.walk(false, Direction.RIGHT);
				break;
			case KeyEvent.VK_Z:
				player.jump(false);
				break;
			default:
				break;
		}
	}
}