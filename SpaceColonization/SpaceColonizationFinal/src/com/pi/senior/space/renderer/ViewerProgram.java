package com.pi.senior.space.renderer;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
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
				new EllipsoidEnvelope(new Vector(0, 15, 0), new Vector(25, 10,
						25), EllipsoidEnvelope.PopulationSchema.UMBRELLA));
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

		setLights();

		GL11.glTranslatef(0, -10, 0);
		colonizer.render();
	}

	private void initGL() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glEnable(GL11.GL_LIGHT1);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);

		GL11.glLightModel(
				GL11.GL_LIGHT_MODEL_AMBIENT,
				(FloatBuffer) BufferUtils.createFloatBuffer(4)
						.put(new float[] { .25f, .3f, .25f, 1f }).flip());
	}

	private void setLights() {
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, (FloatBuffer) BufferUtils
				.createFloatBuffer(4).put(new float[] { 1f, 1f, 1f, 1f })
				.flip());
		GL11.glLight(
				GL11.GL_LIGHT0,
				GL11.GL_POSITION,
				(FloatBuffer) BufferUtils.createFloatBuffer(4)
						.put(new float[] { 1f, 1f, 1f, 1f }).flip());

		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, (FloatBuffer) BufferUtils
				.createFloatBuffer(4).put(new float[] { 1f, 1f, 1f, 1f })
				.flip());
		GL11.glLight(
				GL11.GL_LIGHT1,
				GL11.GL_POSITION,
				(FloatBuffer) BufferUtils.createFloatBuffer(4)
						.put(new float[] { -1f, 1f, -1f, 1f }).flip());
	}

	boolean triedEvolveLastLoop = false;
	boolean triedSubdivideLastLoop = false;

	public void update() {
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			yaw++;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			pitch++;
			pitch = Math.min(pitch, 90);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			yaw--;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			pitch--;
			pitch = Math.max(pitch, 0);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			off -= 1;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			off += 1;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
			colonizer.evolve();
			triedEvolveLastLoop = true;
		} else {
			if (triedEvolveLastLoop) {
				colonizer.updateModels();
			}
			triedEvolveLastLoop = false;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_TAB)) {
			SpaceColonizer.ATTRACTOR_COUNT /= 2;
			SpaceColonizer.ATTRACTOR_KILL_RADIUS_SQUARED /= 2;
			colonizer.generateAttractors();
			triedSubdivideLastLoop = true;
		} else {
			triedSubdivideLastLoop = false;
		}
	}

	public void run() {
		initGL();
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
