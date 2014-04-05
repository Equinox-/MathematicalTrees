package com.pi.budfate.demo;

import static org.lwjgl.opengl.GL11.GL_AMBIENT;
import static org.lwjgl.opengl.GL11.GL_COLOR_MATERIAL;
import static org.lwjgl.opengl.GL11.GL_CONSTANT_ATTENUATION;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LINEAR_ATTENUATION;
import static org.lwjgl.opengl.GL11.GL_POSITION;
import static org.lwjgl.opengl.GL11.GL_QUADRATIC_ATTENUATION;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLightf;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import com.pi.senior.budfate.tree.PositionedMetamer;
import com.pi.senior.budfate.tree.Tree;

public class TreeDisplay {
	private static double horizontalTan = Math.tan(Math.toRadians(25));
	static FloatBuffer l0_position = (FloatBuffer) BufferUtils
			.createFloatBuffer(4).put(new float[] { 0.0f, 0.0f, 1.0f, 0.0f })
			.rewind();
	static FloatBuffer l0_ambient = (FloatBuffer) BufferUtils
			.createFloatBuffer(4).put(new float[] { 0.2f, 0.2f, 0.2f, 1.0f })
			.rewind();

	// Camera Parameters
	float pitch = 0;
	float yaw = 0;
	float off = 10;

	Tree tree;

	public TreeDisplay() throws LWJGLException, IOException {
		tree = new Tree();
		tree.calculate();

		Display.setDisplayMode(new DisplayMode(768, 768));
		Display.setLocation((1920 / 2) - (768 / 2), 1080 + (1080 / 2)
				- (768 / 2));
		Display.create();

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		double aspect = 1f;
		GL11.glFrustum(-horizontalTan, horizontalTan, aspect * -horizontalTan,
				aspect * horizontalTan, 1, 100000);

		// glEnable(GL11.GL_LIGHTING);
		glEnable(GL_COLOR_MATERIAL);
		glEnable(GL_DEPTH_TEST);
		glLightf(GL_LIGHT0, GL_CONSTANT_ATTENUATION, 1.0f);
		glLightf(GL_LIGHT0, GL_LINEAR_ATTENUATION, 0.05f);
		glLightf(GL_LIGHT0, GL_QUADRATIC_ATTENUATION, 0.01f);
		glEnable(GL_LIGHT0);

		while (!Display.isCloseRequested()) {
			render();
		}
	}

	float availableNutrition = 5;

	public void render() {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glLight(GL_LIGHT0, GL_AMBIENT, l0_ambient);
		GL11.glLight(GL_LIGHT0, GL_POSITION, l0_position);

		GL11.glTranslatef(0, 0, -off);
		GL11.glRotatef(pitch, 1, 0, 0);
		GL11.glRotatef(yaw, 0, 1, 0);
		GL11.glRotatef(-90, 1, 0, 0);

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		GL11.glColor3f(1f, 0f, 0f);
		GL11.glBegin(GL11.GL_LINES);
		drawMetamer(tree.getRootMetamer());
		GL11.glEnd();

		Display.update();
		Display.sync(60);
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			yaw++;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			pitch++;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			yaw--;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			pitch--;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			off -= 1;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			off += 1;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			tree.evolve();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
	}

	public void drawMetamer(PositionedMetamer b) {
		GL11.glVertex3f(b.getNodeStart().x, b.getNodeStart().y,
				b.getNodeStart().z);
		GL11.glVertex3f(b.getNodeEnd().x, b.getNodeEnd().y, b.getNodeEnd().z);
		for (PositionedMetamer s : b.getChildren()) {
			drawMetamer(s);
		}
	}

	public static void main(String[] args) throws LWJGLException, IOException {
		new TreeDisplay();
	}
}
