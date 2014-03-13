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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import com.pi.senior.budfate.api.BudEvolutionScheme;
import com.pi.senior.budfate.impl.SimplexBudEvolution;
import com.pi.senior.budfate.tree.PositionedMetamer;
import com.pi.senior.budfate.tree.Tree;
import com.pi.senior.util.NodeIterator;

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
	BudEvolutionScheme budEvolution;

	public TreeDisplay() throws LWJGLException, IOException {
		budEvolution = new SimplexBudEvolution();
		tree = new Tree();
		tree.calculate();

		Display.setDisplayMode(new DisplayMode(768, 768));
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
			availableNutrition += 5;
			System.out.println("EVOLVING");
			List<PositionedMetamer> evolution = new ArrayList<PositionedMetamer>();
			NodeIterator itr = new NodeIterator(tree.getRootMetamer());
			while (itr.hasNext()) {
				evolution.add(itr.next());
			}
			List<Entry<Float, PositionedMetamer>> possibleSteps = new ArrayList<Entry<Float, PositionedMetamer>>();
			for (PositionedMetamer mm : evolution) {
				PositionedMetamer mChild = budEvolution.getNextMetamer(mm);
				if (mChild != null) {
					float cost = budEvolution.getMetamerCost(mm, mChild);
					possibleSteps
							.add(new AbstractMap.SimpleEntry<Float, PositionedMetamer>(
									Float.valueOf(cost), mChild));
				}
			}
			Collections.sort(possibleSteps,
					new Comparator<Entry<Float, PositionedMetamer>>() {

						@Override
						public int compare(
								Entry<Float, PositionedMetamer> arg0,
								Entry<Float, PositionedMetamer> arg1) {
							return Float.compare(arg0.getKey(), arg1.getKey());
						}
					});
			for (int i = 0; i < possibleSteps.size() && availableNutrition > 0; i++) {
				if (availableNutrition > possibleSteps.get(i).getKey()) {
					possibleSteps.get(i).getValue().getParent()
							.addChild(possibleSteps.get(i).getValue());
					availableNutrition -= possibleSteps.get(i).getKey();
				}
			}

			tree.getRootMetamer().calculateRecursive();
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
