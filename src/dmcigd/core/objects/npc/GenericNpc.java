package dmcigd.core.objects.npc;

import dmcigd.core.objects.interfaces.Region;
import dmcigd.core.room.DialogueHandler;

public class GenericNpc extends SimpleNpc implements Region {
    
	public GenericNpc (int x, int y, int width, int height, int frameLimit, float frameSpeed, String fileName, String name, String message, DialogueHandler dialogueHandler) {
		
		super(x, y, name, message, dialogueHandler);
		
		setWidth(width);
		setHeight(height-2);
		setImageWidth(width);
		setImageHeight(height);
		
		setSequence(0);
		setFrame(0);
		
		setFrameLimits(new int[] {frameLimit});
		setAnimationLoops(new boolean[] {true});
		setFrameSpeed(frameSpeed);
		
		setImagePath("objects/"+fileName);
		
	}
	
	public void step() {
		animate();
	}
}
