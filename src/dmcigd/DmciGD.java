package dmcigd;

import dmcigd.core.*;
import dmcigd.core.enums.*;
import dmcigd.core.objects.*;
import dmcigd.levels.SaveCodeHandler;

import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

//Renders applet
@SuppressWarnings("serial")
public class DmciGD extends Applet implements Runnable {

    private URL codeBase;
    /** Synchronizes the applet (graphics) thread with the Main (game loop) thread. */
    private ThreadSync threadSync = new ThreadSync();
    /** The game manager object (which handles game loops). */
    private Main main;
    /** Decides which screen to paint (loading, dialogue, etc). */
    GameState gameState;
    /** Determines which tilesheet to display (decayed or not decayed?). */
    private int decayOffset;
    /** True if there is an overlay (eg dialogue). */
    private boolean paintOnce = false;
    /** Visible objects list*/
    private char[][] visibleBlocks = new char[12][22];
    /** Visible environment list*/
    private char[][] visibleEnvironment = new char[12][22];
    /** The list of visible text labels to draw (floating text)*/
    private ArrayList<TextLabel> textLabels = new ArrayList<TextLabel>();
    /** The list of visible objects to draw*/
    private ArrayList<ObjectImage> visibleObjects = new ArrayList<ObjectImage>();
    /** The current tilesheet*/
    private Image tileSheet, bgImage;
    /** the map of image names to images*/
    private Map<String, int[]> imageMap = new HashMap<String, int[]>();
    private Map<String, Image> objectImageMap = new HashMap<String, Image>();
    /** the player's current location*/
    private int playerX, playerY;
    /**Dialogue variables (speaker name, dialogue lines)*/
    private String name, line1, line2, line3;
    
    //Initialize the Double-buffering Variables
    private Image dbImage;
    private Graphics dbg;
    /**fonts*/
    Font f, fSmall;
    
    //Initialize loading animation
    private Image loadingImage;
    private int loadingClock = 0;

    /**
     * Initialize all objects and gets audio data
     */
    @Override
    public void init() {
        setSize(640, 320);
        resize(640, 320);
        codeBase = getCodeBase();
        main = new Main(threadSync, codeBase);
        gameState = main.getGameState();

        //Add listener to main thread
        addKeyListener(main);

        //Start new thread when applet loads
        Thread th = new Thread(this);
        th.start();

        //Set canvas background
        setBackground(new Color(141, 141, 141));

        buildImageMap();

        //Initialize Double-Buffers
        dbImage = createImage(this.getSize().width, this.getSize().height);
        dbg = dbImage.getGraphics();

        //Load loading image
        loadingImage = getImageFromPath("loading.gif");

        MediaTracker mt = new MediaTracker(this);
        mt.addImage(loadingImage, 0);

        //Preload constant sprites
        //	Player, Sword, Level Codes
        objectImageMap.put("sword.gif", getImageFromPath("sword.gif"));
        mt.addImage(objectImageMap.get("sword.gif"), 1);
        objectImageMap.put("player.gif", getImageFromPath("player.gif"));
        mt.addImage(objectImageMap.get("player.gif"), 2);
        objectImageMap.put("levelcode.gif", getImageFromPath("levelcode.gif"));
        mt.addImage(objectImageMap.get("levelcode.gif"), 3);

        try {
            mt.waitForAll();
        } catch (InterruptedException e) {
        }

        //Create font
            try {
				f = Font.createFont(Font.PLAIN, new URL(getCodeBase(), "../share/gfx/04B_03__.TTF").openStream()).deriveFont(16f);
	            fSmall = f.deriveFont(8f);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (FontFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    }
    
    //Stops background music from running after tab is closed.
    public void destroy() {
    	MidiPlayer.endSong();
    }

    //Repaints screen
    @Override
    public synchronized void run() {

        while (true) {

            //Tells Main thread to wait until finished grabbing variables
            //Listens for when new frame is fetched
            threadSync.consume();

            switch (main.getGameState()) {
                case GAMEPLAY:

                    if (gameState != GameState.GAMEPLAY) {

                        //Preload map tilesheet
                        tileSheet = getImageFromPath("tilesheets/" + main.room.getTileSet() + ".gif");
                        MediaTracker mt = new MediaTracker(this);
                        mt.addImage(tileSheet, 0);

                        //Preload background image
                        bgImage = getImageFromPath("bgs/" + main.room.getLevelName() + ".gif");
                        mt.addImage(bgImage, 1);

                        int i = 2;

                        //Load all images
                        for (String imagePath : main.room.getImageResources()) {
                            if (!objectImageMap.containsKey(imagePath)) {

                                objectImageMap.put(imagePath, getImageFromPath(imagePath));

                                mt.addImage(objectImageMap.get(imagePath), i);

                                i++;
                            }
                        }

                        try {
                            mt.waitForAll();
                        } catch (InterruptedException e) {
                        }

                        gameState = GameState.GAMEPLAY;
                    }

                    //Retrieve necessary objects

                    playerX = (int) main.room.getPlayer().getX();
                    playerY = (int) main.room.getPlayer().getY();

                    visibleBlocks = main.room.getBlockMap().getVisibleBlocks(playerX, playerY);
                    visibleEnvironment = main.room.getEnvironmentMap().getVisibleEnvironment(playerX, playerY);

                    visibleObjects = main.room.getVisibleObjects();

                    textLabels = main.room.getTextLabels();

                    //Decay tiles
                    if (main.getDecayState()) {
                        decayOffset = 5;
                    } else {
                        decayOffset = 0;
                    }

                    //Tells Main thread to begin fetching next frame
                    threadSync.consumed();

                    repaint();

                    //Only repaint after the previous frame has been painted and game is in a state of update - Reduces unnecessary calls
                    try {
                        wait();
                    } catch (InterruptedException ex) {
        				ex.printStackTrace();
                    }
                    break;

                case DIALOGUE:

                    //Retrieve Dialogue text

                    name = null;
                    name = main.room.getDialogueHandler().getCurrentDialogueItem().getName();

                    line1 = null;
                    line1 = main.room.getDialogueHandler().getLine(1);

                    line2 = null;
                    line2 = main.room.getDialogueHandler().getLine(2);

                    line3 = null;
                    line3 = main.room.getDialogueHandler().getLine(3);

                    //Tell Main thread to continue with game loop
                    threadSync.consumed();

                    repaint();

                    //Only repaint after the previous frame has been painted and game is in a state of update - Reduces unnecessary calls
                    try {
                        wait();
                    } catch (InterruptedException ex) {
        				ex.printStackTrace();
                    }

                default:

                    //If not in a state of update, synchronize gameState
                    if (gameState != main.getGameState()) {

                        gameState = main.getGameState();
                        paintOnce = true;

                    }

                    //Tells Main thread to begin fetching next frame
                    threadSync.consumed();

                    repaint();

                    //If not in a state of update, wait and keep checking gameState
                    try {
                        wait();
                    } catch (InterruptedException ex) {
        				ex.printStackTrace();
                    }
                    

                    break;
            }


        }

    }

    //Implements Double-Buffering
    public void update(Graphics g) {
        paint(g);
    }

    //Calls paint methods of appropriate object
    public void paint(Graphics g) {

        //Do not clear screen in case of dialogue - Paused game should remain as background during cutscenes or dialogue)
        if (gameState != GameState.DIALOGUE && gameState != GameState.PAUSE && gameState != GameState.GAMEOVER) {

            //Clear screen and draw Background
            dbg.setColor(getBackground());
            dbg.fillRect(0, 0, this.getSize().width, this.getSize().height);

        }

        //Check for which paint method to call
        switch (gameState) {
            case GAMEPLAY:

                //Draw Background Image
                int offsetX = playerX / 128;
                int offsetY = playerY / 128;
                dbg.drawImage(bgImage, 0, 0, 640, 320, offsetX, offsetY, 240 + offsetX, 120 + offsetY, this);

                //Draw blocks
                int[] tileLocation;

                int gridOffsetX = (playerX % 32) + 10;
                int gridOffsetY = (playerY % 32) + 16;

                //Loop through Y axis of visibleBlocks
                for (int i = 0; i < 12; i++) {
                    //Loop through X axis of visibleBlocks
                    for (int j = 0; j < 22; j++) {

                        //Draw Environment
                        if ((tileLocation = imageMap.get("e_" + String.valueOf(visibleEnvironment[i][j]))) != null) {
                            dbg.drawImage(tileSheet, j * 32 - gridOffsetX, i * 32 - gridOffsetY, j * 32 - gridOffsetX + 32, i * 32 - gridOffsetY + 32, tileLocation[0] * 16, (tileLocation[1] + decayOffset) * 16, tileLocation[0] * 16 + 16, (tileLocation[1] + decayOffset) * 16 + 16, this);
                        }
                        //Draw Block
                        if ((tileLocation = imageMap.get(String.valueOf(visibleBlocks[i][j]))) != null) {
                            dbg.drawImage(tileSheet, j * 32 - gridOffsetX, i * 32 - gridOffsetY, j * 32 - gridOffsetX + 32, i * 32 - gridOffsetY + 32, tileLocation[0] * 16, (tileLocation[1] + decayOffset) * 16, tileLocation[0] * 16 + 16, (tileLocation[1] + decayOffset) * 16 + 16, this);
                        }
                    }
                }

                //Draw game objects
                for (ObjectImage i : visibleObjects) {
                    dbg.drawImage(objectImageMap.get(i.imagePath), i.dstx1, i.dsty1, i.dstx2, i.dsty2, i.srcx1, i.srcy1, i.srcx2, i.srcy2, this);
                }

                int shadowOffset = 1;

                //Draw text labels
                for (TextLabel i : textLabels) {
                    if (i.isSmall()) {
                        dbg.setFont(fSmall);
                        shadowOffset = 1;
                    } else {
                        dbg.setFont(f);
                        shadowOffset = 2;
                    }

                    dbg.setColor(Color.BLACK);
                    dbg.drawString(i.getString(), i.getX() - playerX + 310, i.getY() - playerY + 144 + shadowOffset);
                    dbg.setColor(Color.WHITE);
                    dbg.drawString(i.getString(), i.getX() - playerX + 310, i.getY() - playerY + 144);
                }

                break;

            case LOADINGROOM:

                dbg.drawImage(loadingImage, 425, 230, 600, 300, 0, loadingClock / 16 * 70, 175, loadingClock / 16 * 70 + 70, this);
                loadingClock++;

                if (loadingClock == 96) {
                    loadingClock = 0;
                }

                break;

            case DIALOGUE:
                //Draw background box
                dbg.setColor(new Color(30, 30, 30));
                dbg.fillRect(0, 210, 640, 110);

                dbg.setFont(f);

                //Draw name
                dbg.setColor(Color.GRAY);
                dbg.drawString(name, 100, 235);

                //Draw text
                dbg.setColor(Color.WHITE);
                if (line1 != null) {
                    dbg.drawString(line1, 100, 255);
                }
                if (line2 != null) {
                    dbg.drawString(line2, 100, 275);
                }
                if (line3 != null) {
                    dbg.drawString(line3, 100, 295);
                }

                dbg.setFont(fSmall);
                //Draw "Press "X" to continue" instruction
                dbg.setColor(Color.BLACK);
                dbg.drawString("Press \"X\" to continue", 450, 313);
                dbg.setColor(Color.GRAY);
                dbg.drawString("Press \"X\" to continue", 450, 312);
                break;

            case PAUSE:
                if (paintOnce) {
                    dbg.setColor(new Color(0, 0, 0, 180));
                    dbg.fillRect(0, 0, 640, 320);

                    dbg.setFont(f);
                    dbg.setColor(Color.BLACK);
                    dbg.drawString("[ PAUSED ]", 280, 142);
                    dbg.setColor(Color.WHITE);
                    dbg.drawString("[ PAUSED ]", 280, 140);

                    dbg.setFont(fSmall);
                    dbg.setColor(Color.BLACK);
                    dbg.drawString("Current Save Code:", 283, 161);
                    dbg.setColor(Color.WHITE);
                    dbg.drawString("Current Save Code:", 283, 160);
                    
                    //Draw 9-digit save code:
                    int curChar;
                    
                    //KillCount
                    int killCount = main.getKillCount();
                    
                    //Digit 1:
                    curChar = killCount / 256;
                    dbg.drawImage(objectImageMap.get("levelcode.gif"), 182, 170, 218, 190, (3 - curChar) * 18, 0, (4 - curChar) * 18, 10, this);
                    killCount -= curChar * 256;
                    
                    //Digit 2:
                    curChar = killCount / 64;
                    dbg.drawImage(objectImageMap.get("levelcode.gif"), 212, 170, 248, 190, (3 - curChar) * 18, 0, (4 - curChar) * 18, 10, this);
                    killCount -= curChar * 64;
                    
                    //Digit 3:
                    curChar = killCount / 16;
                    dbg.drawImage(objectImageMap.get("levelcode.gif"), 242, 170, 278, 190, (3 - curChar) * 18, 0, (4 - curChar) * 18, 10, this);
                    killCount -= curChar * 16;
                    
                    //Digit 4:
                    curChar = killCount / 4;
                    dbg.drawImage(objectImageMap.get("levelcode.gif"), 272, 170, 308, 190, (3 - curChar) * 18, 0, (4 - curChar) * 18, 10, this);
                    killCount -= curChar * 4;
                    
                    //Digit 5:
                    curChar = killCount;
                    dbg.drawImage(objectImageMap.get("levelcode.gif"), 302, 170, 338, 190, (3 - curChar) * 18, 0, (4 - curChar) * 18, 10, this);
                    
                    String levelCode = SaveCodeHandler.getLevelCode(main.getRoom().getLevelName()+"."+main.getRoom().getRoomName());
                    
                    //Digit 6:
                    curChar = SaveCodeHandler.digitValue(levelCode.charAt(0));
                    dbg.drawImage(objectImageMap.get("levelcode.gif"), 332, 170, 368, 190, (3 - curChar) * 18, 0, (4 - curChar) * 18, 10, this);
                    
                    //Digit 7:
                    curChar = SaveCodeHandler.digitValue(levelCode.charAt(1));
                    dbg.drawImage(objectImageMap.get("levelcode.gif"), 362, 170, 398, 190, (3 - curChar) * 18, 0, (4 - curChar) * 18, 10, this);
                    
                    //Digit 8:
                    curChar = SaveCodeHandler.digitValue(levelCode.charAt(2));
                    dbg.drawImage(objectImageMap.get("levelcode.gif"), 392, 170, 428, 190, (3 - curChar) * 18, 0, (4 - curChar) * 18, 10, this);
                    
                    //Digit 9:
                    curChar = SaveCodeHandler.digitValue(levelCode.charAt(3));
                    dbg.drawImage(objectImageMap.get("levelcode.gif"), 422, 170, 458, 190, (3 - curChar) * 18, 0, (4 - curChar) * 18, 10, this);

                    paintOnce = false;
                }
                break;

            case GAMEOVER:
                if (paintOnce) {
                    dbg.setColor(new Color(0, 0, 0, 180));
                    dbg.fillRect(0, 0, 640, 320);

                    dbg.setFont(f);
                    dbg.setColor(Color.BLACK);
                    dbg.drawString("[ GAME OVER ]", 260, 162);
                    dbg.setColor(Color.WHITE);
                    dbg.drawString("[ GAME OVER ]", 260, 160);

                    dbg.setFont(fSmall);
                    dbg.setColor(Color.BLACK);
                    dbg.drawString("PRESS \"R\" TO RESTART", 275, 181);
                    dbg.setColor(Color.GRAY);
                    dbg.drawString("PRESS \"R\" TO RESTART", 275, 180);

                    paintOnce = false;
                }
                break;

            default:
                if (paintOnce) {
                    paintOnce = false;
                }
                break;
        }

        //Draw offscreen image
        g.drawImage(dbImage, 0, 0, this);

        //Syncronize Paint with Run - Ensures there is only one paint per repaint
        synchronized (this) {
            notifyAll();
        }

    }

    //Image Loading Script
    public Image getImageFromPath(String path) {

        //Retrieve image
        Image image = getImage(getCodeBase(), "../share/gfx/" + path);

        return image;
    }

    public void buildImageMap() {

        //BLOCKS
        imageMap.put("q", new int[]{1, 1});
        imageMap.put("w", new int[]{2, 1});
        imageMap.put("W", new int[]{3, 1});
        imageMap.put("e", new int[]{4, 1});
        imageMap.put("a", new int[]{1, 2});
        imageMap.put(".", new int[]{2, 2});
        imageMap.put(",", new int[]{3, 2});
        imageMap.put("d", new int[]{4, 2});
        imageMap.put("A", new int[]{1, 3});
        imageMap.put("-", new int[]{2, 3});
        imageMap.put("=", new int[]{3, 3});
        imageMap.put("D", new int[]{4, 3});
        imageMap.put("z", new int[]{1, 4});
        imageMap.put("x", new int[]{2, 4});
        imageMap.put("X", new int[]{3, 4});
        imageMap.put("c", new int[]{4, 4});

        imageMap.put("t", new int[]{1, 0});
        imageMap.put("y", new int[]{2, 0});
        imageMap.put("Y", new int[]{3, 0});
        imageMap.put("u", new int[]{4, 0});

        imageMap.put("r", new int[]{0, 1});
        imageMap.put("f", new int[]{0, 2});
        imageMap.put("F", new int[]{0, 3});
        imageMap.put("v", new int[]{0, 4});

        imageMap.put("s", new int[]{0, 0});

        //Platforms

        imageMap.put("O", new int[]{5, 4});
        imageMap.put("i", new int[]{6, 4});
        imageMap.put("o", new int[]{7, 4});
        imageMap.put("p", new int[]{8, 4});
        imageMap.put("_", new int[]{5, 3});

        //Ladders
        imageMap.put("k", new int[]{5, 0});
        imageMap.put("l", new int[]{5, 1});
        imageMap.put("L", new int[]{5, 2});

        imageMap.put("g", new int[]{6, 0});
        imageMap.put("h", new int[]{7, 0});
        imageMap.put("j", new int[]{8, 0});

        imageMap.put("G", new int[]{6, 1});
        imageMap.put("H", new int[]{7, 1});
        imageMap.put("J", new int[]{8, 1});
        imageMap.put("b", new int[]{6, 2});
        imageMap.put("n", new int[]{7, 2});
        imageMap.put("m", new int[]{8, 2});
        imageMap.put("B", new int[]{6, 3});
        imageMap.put("N", new int[]{7, 3});
        imageMap.put("M", new int[]{8, 3});

        //Spikes

        imageMap.put("^", new int[]{9, 0});
        imageMap.put("V", new int[]{9, 1});
        imageMap.put(">", new int[]{9, 2});
        imageMap.put("<", new int[]{9, 3});

        //ENVIRONMENT
        //Ground
        imageMap.put("e_q", new int[]{10, 0});
        imageMap.put("e_w", new int[]{11, 0});
        imageMap.put("e_e", new int[]{12, 0});
        imageMap.put("e_a", new int[]{10, 1});
        imageMap.put("e_s", new int[]{11, 1});
        imageMap.put("e_d", new int[]{12, 1});
        imageMap.put("e_z", new int[]{10, 2});
        imageMap.put("e_x", new int[]{11, 2});
        imageMap.put("e_c", new int[]{12, 2});

        //Water
        imageMap.put("e_r", new int[]{13, 0});
        imageMap.put("e_t", new int[]{14, 0});
        imageMap.put("e_y", new int[]{15, 0});
        imageMap.put("e_f", new int[]{13, 1});
        imageMap.put("e_g", new int[]{14, 1});
        imageMap.put("e_h", new int[]{15, 1});
        imageMap.put("e_v", new int[]{13, 2});
        imageMap.put("e_b", new int[]{14, 2});
        imageMap.put("e_n", new int[]{15, 2});

        //Underground Environment
        imageMap.put("e_U", new int[]{10, 3});
        imageMap.put("e_I", new int[]{11, 3});
        imageMap.put("e_O", new int[]{12, 3});
        imageMap.put("e_J", new int[]{10, 4});
        imageMap.put("e_K", new int[]{11, 4});
        imageMap.put("e_L", new int[]{12, 4});

        //Overworld Environment
        imageMap.put("e_u", new int[]{13, 3});
        imageMap.put("e_i", new int[]{14, 3});
        imageMap.put("e_o", new int[]{15, 3});
        imageMap.put("e_j", new int[]{13, 4});
        imageMap.put("e_k", new int[]{14, 4});
        imageMap.put("e_l", new int[]{15, 4});
    }
}
