package com.pi.senior.space.renderer;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import com.pi.senior.math.Vector;
import com.pi.senior.space.gen.EllipsoidEnvelope;
import com.pi.senior.space.gen.SpaceColonizer;
import com.pi.senior.space.tree.Node;

public class ViewerProgram {
	private double horizontalTan = Math.tan(Math.toRadians(25));

	private static final int TARGET_FPS = 60;
	private int width = 800, height = 600;
	private SpaceColonizer colonizer;

	private float pitch = 0;
	private float yaw = 0;
	private float off = 10;

	public ViewerProgram() {
		colonizer = new SpaceColonizer(new Node(new Vector(0, 0, 0)),
				new EllipsoidEnvelope(new Vector(0, 10, 0),
						new Vector(5, 10, 5)));
		colonizer.generateAttractors();
	}

	public void init() throws LWJGLException {
		Display.setDisplayMode(new DisplayMode(width, height));
		Display.setLocation((Display.getDesktopDisplayMode().getWidth() / 2)
				- (width / 2),
				(Display.getDesktopDisplayMode().getHeight() / 2)
						- (height / 2));
		Display.create();
	}

	private void render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		double aspect = ((double) height) / ((double) width);
		GL11.glFrustum(-horizontalTan, horizontalTan, aspect * -horizontalTan,
				aspect * horizontalTan, 1, 100000);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		GL11.glTranslatef(0, 0, -off);
		GL11.glRotatef(pitch, 1, 0, 0);
		GL11.glRotatef(yaw, 0, 1, 0);

		colonizer.render();
	}

	private void initGL() {

	}

	boolean triedEvolveLastLoop = false;

	public void update() {
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			yaw--;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			pitch--;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			yaw++;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			pitch++;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			off -= 1;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			off += 1;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
			if (!triedEvolveLastLoop) {
				colonizer.evolve();
			}
			triedEvolveLastLoop = true;
		} else {
			triedEvolveLastLoop = false;
		}
	}

	public void run() {
		while (!Display.isCloseRequested()) {
			render();
			Display.update();
			Display.sync(TARGET_FPS);
			update();
		}
	}

	public void dispose() {
		Display.destroy();
	}

	public static void main(String[] args) throws LWJGLException {
		ViewerProgram pgm = new ViewerProgram();
		pgm.init();
		pgm.run();
		pgm.dispose();
	}
}
