package com.pi.senior.space.renderer;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import com.pi.senior.math.Vector;
import com.pi.senior.space.AnaglyphConfigurator;
import com.pi.senior.space.Configuration;
import com.pi.senior.space.gen.SpaceColonizer;
import com.pi.senior.space.tree.Node;

public class ViewerProgram {
	private static final int TARGET_FPS = 60;
	private int width = 800, height = 600;
	private SpaceColonizer colonizer;

	private float pitch = 0;
	private float yaw = 0;
	private float off = 10;
	private StereoCamera cam;

	public ViewerProgram() {
		colonizer = new SpaceColonizer(new Node(new Vector(0, 0, 0)),
				Configuration.createEnvelope());
		colonizer.generateAttractors();
		AnaglyphConfigurator.show();
		cam = new StereoCamera(45, 1, 20000);
	}

	public void init() throws LWJGLException {
		Display.setTitle("Trees!");
		Display.setDisplayMode(new DisplayMode(width, height));
		Display.setLocation((Display.getDesktopDisplayMode().getWidth() / 2)
				- (width / 2),
				(Display.getDesktopDisplayMode().getHeight() / 2)
						- (height / 2));
		Display.setResizable(true);
		Display.create();
	}

	private void render() {
		float aspect = (float) Display.getWidth() / (float) Display.getHeight();
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		if (cam.isStereo()) {
			cam.applyLeftFrustum(aspect);
			GL11.glColorMask(true, false, false, false);
			renderInternal();

			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			cam.applyRightFrustum(aspect);
			GL11.glColorMask(false, false, true, false);
		} else {
			cam.applyRightFrustum(aspect);
		}
		renderInternal();

		GL11.glColorMask(true, true, true, true);
	}

	private void renderInternal() {
		GL11.glTranslatef(0, 0, -off);
		GL11.glRotatef(pitch, 1, 0, 0);
		GL11.glRotatef(yaw, 0, 1, 0);

		setLights();

		GL11.glTranslatef(0, -10, 0);
		colonizer.renderAttractors();
		if (triedEvolveLastLoop) {
			colonizer.renderLineConnectors();
		} else {
			colonizer.renderCylinderConnectors();
		}
	}

	private void initGL() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glEnable(GL11.GL_LIGHT1);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);

		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);

		GL11.glLightModel(
				GL11.GL_LIGHT_MODEL_AMBIENT,
				(FloatBuffer) BufferUtils.createFloatBuffer(4)
						.put(new float[] { .25f, .3f, .25f, 1f }).flip());
		GL11.glLightModeli(GL11.GL_LIGHT_MODEL_TWO_SIDE, GL11.GL_TRUE);
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
			pitch = Math.max(pitch, -45);
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
			Configuration.ATTRACTOR_COUNT *= Configuration.ATTRACTOR_COUNT_DEGREDATION;
			Configuration.ATTRACTOR_KILL_RADIUS_SQUARED *= Configuration.ATTRACTOR_KILLER_DEGREDATION;
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
		colonizer.shutdown();
		Display.destroy();
		AnaglyphConfigurator.kill();
	}

	public static void main(String[] args) throws LWJGLException {
		final ViewerProgram pgm = new ViewerProgram();
		new Thread(new Runnable() {
			public void run() {
				try {
					pgm.init();
					pgm.run();
					pgm.dispose();
				} catch (LWJGLException e) {
				}
			}
		}).start();
	}
}
