package com.pi.senior.space.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.pi.senior.math.Vector;
import com.pi.senior.space.tree.Node;
import com.pi.senior.space.tree.NodeIterator;

public class SpaceColonizer {
	private static final int ATTRACTOR_COUNT = 1000;
	private static final float ATTRACTOR_KILL_RADIUS_SQUARED = 0.25f;
	private static final float ATTRACTOR_ATTRACTION_RADIUS_SQUARED = 10;
	private static final float INODE_LENGTH = 0.5f;

	private Node rootNode;
	private Envelope populationArea;
	private Random rand = new Random();

	private List<Vector> attractors;

	private int nodeCount = 1;

	public SpaceColonizer(Node root, Envelope area) {
		this.rootNode = root;
		this.populationArea = area;
	}

	public void generateAttractors() {
		long startTime = System.nanoTime();
		attractors = new ArrayList<Vector>(ATTRACTOR_COUNT);
		for (int i = 0; i < ATTRACTOR_COUNT; i++) {
			attractors.add(populationArea.nextRandom(rand));
		}
		System.out.println("Generated " + attractors.size() + " attractors in "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");
	}

	public void evolve() {
		long startTime = System.nanoTime();
		// First step is to inspect every attraction vector and find the closest
		// node.
		Map<Node, Vector> attractions = new HashMap<Node, Vector>(nodeCount);
		for (Vector v : attractors) {
			Iterator<Node> ndIterator = new NodeIterator(rootNode);
			double bestDist = Double.MAX_VALUE;
			Node bestNd = null;
			Vector bestDirection = null;
			while (ndIterator.hasNext()) {
				Node nd = ndIterator.next();
				double dist2 = nd.getPosition().distSquared(v);
				if (dist2 < ATTRACTOR_ATTRACTION_RADIUS_SQUARED
						&& dist2 < bestDist) {

					Vector testDirection = v.clone().subtract(nd.getPosition())
							.normalize();
					if (nd.getDirection() != null) {
						double angleOfChange = Math.abs(Math.acos(Vector
								.dotProduct(testDirection, nd.getDirection())));
						dist2 *= (angleOfChange);
					} else {
						dist2 *= Math.PI / 2f;
					}
					if (dist2 < bestDist) {
						bestDirection = testDirection;
						bestNd = nd;
						bestDist = dist2;
					}
				}
			}
			if (bestNd != null) {
				Vector curr = attractions.get(bestNd);
				if (curr == null) {
					curr = new Vector(0, 0, 0);
					attractions.put(bestNd, curr);
				}
				curr.add(bestDirection);
			}
		}
		System.out.println("Generated " + attractions.size()
				+ " attraction vectors in "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");

		startTime = System.nanoTime();
		// Add the new nodes
		Set<Entry<Node, Vector>> dirSet = attractions.entrySet();
		for (Entry<Node, Vector> dirSpec : dirSet) {
			dirSpec.getValue().normalize().multiply(INODE_LENGTH);
			dirSpec.getKey().addChild(
					new Node(dirSpec.getKey().getPosition().clone()
							.add(dirSpec.getValue())));
			nodeCount++;
		}
		System.out.println("Added " + attractions.size() + " new nodes in "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");

		startTime = System.nanoTime();
		int startCount = attractors.size();
		// Kill off old attractors
		Iterator<Vector> attractionItr = attractors.iterator();
		while (attractionItr.hasNext()) {
			Iterator<Node> nodes = new NodeIterator(rootNode);
			Vector kill = attractionItr.next();
			while (nodes.hasNext()) {
				Node next = nodes.next();
				if (kill.distSquared(next.getPosition()) < ATTRACTOR_KILL_RADIUS_SQUARED) {
					attractionItr.remove();
					break;
				}
			}
		}
		System.out.println("Killed " + (startCount - attractors.size())
				+ " attractors in "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");
	}

	public void render() {
		// Render attractors
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glColor3f(1f, 0f, 0f);
		for (Vector v : attractors) {
			GL11.glVertex3f(v.x, v.y, v.z);
		}
		GL11.glEnd();

		// Now render the connections
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(1f, 1f, 1f);
		Iterator<Node> nodes = new NodeIterator(rootNode);
		while (nodes.hasNext()) {
			Node n = nodes.next();
			if (n.getParent() != null) {
				GL11.glVertex3f(n.getPosition().x, n.getPosition().y,
						n.getPosition().z);
				GL11.glVertex3f(n.getParent().getPosition().x, n.getParent()
						.getPosition().y, n.getParent().getPosition().z);
			}
		}
		GL11.glEnd();
	}
}
