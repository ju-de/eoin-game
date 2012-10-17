package dmcigd;

import dmcigd.core.*;
import java.awt.*;
import java.awt.event.*;

//Catches input and manages game loop objects
public class Main implements Runnable, KeyListener {
	
	//Initialize Demo Thread (temporary, delete when game is ready)
	Demo demo;
	
	//Initializing gameState variable - decides which object to interact with
	private GameState gameState;
	
	public Main() {
		
		//Set gameState variable (currently at 3 for Demo Screen, should be changed to 1 for real game to initiate start menu)
		gameState = GameState.DEMO;
		
		//Start Demo Thread (temporary, remove when game is ready)
		demo = new Demo();
		
		//Initializes Thread
		Thread th = new Thread(this);
		th.start();
	}
	
	//Passes game state to rendering thread
	public GameState getGameState() {
		return gameState;
	}
	
	public void run() {
		while(true) {
			//Temporary game state, remove when done
			if(gameState == GameState.DEMO) {
				demo.step();
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException ex) {}
		}
	}
	
	public void keyPressed(KeyEvent e) {
		//Catch core keypresses such as Pause/Unpause
		
		//Pass keypresses to appropriate object
	}
	
	public void keyReleased(KeyEvent e) {
		
	}
	
	public void keyTyped(KeyEvent e) {
		
	}
}