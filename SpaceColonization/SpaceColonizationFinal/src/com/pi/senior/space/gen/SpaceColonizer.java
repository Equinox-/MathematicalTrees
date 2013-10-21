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
import com.pi.senior.space.Configuration;
import com.pi.senior.space.renderer.CylinderVertexObject;
import com.pi.senior.space.tree.Node;
import com.pi.senior.space.tree.NodeIterator;

public class SpaceColonizer {
	private Node rootNode;
	private Envelope populationArea;
	private Random rand = new Random();

	private List<Vector> attractors;

	private int nodeCount = 1;

	private ArrayList<CylinderVertexObject> vertexObjects = new ArrayList<CylinderVertexObject>();

	public SpaceColonizer(Node root, Envelope area) {
		this.rootNode = root;
		this.populationArea = area;
	}

	public void generateAttractors() {
		long startTime = System.nanoTime();
		attractors = new ArrayList<Vector>(Configuration.ATTRACTOR_COUNT);
		for (int i = 0; i < Configuration.ATTRACTOR_COUNT; i++) {
			attractors.add(populationArea.nextRandom(rand));
		}
		System.out.println("Generated " + attractors.size() + " attractors in "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");

		killOffAttractors();
	}

	private void killOffAttractors() {
		long startTime = System.nanoTime();
		int startCount = attractors.size();
		// Kill off old attractors
		Iterator<Vector> attractionItr = attractors.iterator();
		while (attractionItr.hasNext()) {
			Iterator<Node> nodes = new NodeIterator(rootNode);
			Vector kill = attractionItr.next();
			while (nodes.hasNext()) {
				Node next = nodes.next();
				if (kill.distSquared(next.getPosition()) < Configuration.ATTRACTOR_KILL_RADIUS_SQUARED) {
					attractionItr.remove();
					break;
				}
			}
		}
		System.out.println("Killed " + (startCount - attractors.size())
				+ " attractors in "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");
	}

	private Map<Node, Vector> generateAttractionVectors() {
		long startTime = System.nanoTime();
		Map<Node, Vector> attractions = new HashMap<Node, Vector>(nodeCount);
		for (Vector v : attractors) {
			Iterator<Node> ndIterator = new NodeIterator(rootNode);

			Vector tropism = v.clone().subtract(rootNode.getPosition());
			// We want to assume perfectly flat branching structures
			tropism.y = Configuration.IDEAL_BRANCH_SLOPE;
			tropism.normalize();

			AttractionNode attracted = AttractionNode.computeNodeFor(
					ndIterator, v, Configuration.USE_BIAS_VECTORS, tropism,
					Configuration.ATTRACTOR_ATTRACTION_RADIUS_SQUARED);
			if (attracted != null) {
				Vector curr = attractions.get(attracted.getAttracted());
				if (curr == null) {
					curr = new Vector(0, 0, 0);
					attractions.put(attracted.getAttracted(), curr);
				}
				curr.add(attracted.getGrowthDirection());
			}
		}
		if (attractions.size() == 0 && attractors.size() > 0) {
			// If we don't get any attractions, get the closest one & ignore the
			// funny bizniz
			AttractionNode bestAttracted = null;
			for (Vector v : attractors) {
				Iterator<Node> ndIterator = new NodeIterator(rootNode);

				AttractionNode attracted = AttractionNode.computeNodeFor(
						ndIterator, v, false, null, Float.MAX_VALUE);
				if (attracted != null
						&& (bestAttracted == null || attracted
								.compareTo(bestAttracted) > 0)) {
					bestAttracted = attracted;
				}
			}
			if (bestAttracted != null) {
				attractions.put(bestAttracted.getAttracted(),
						bestAttracted.getGrowthDirection());
			}
		}
		System.out.println("Generated " + attractions.size()
				+ " attraction vectors in "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");
		return attractions;
	}

	public void evolve() {
		// First step is to inspect every attraction vector and find the closest
		// node.
		Map<Node, Vector> attractions = generateAttractionVectors();

		// Add the new nodes
		long startTime = System.nanoTime();
		Set<Entry<Node, Vector>> dirSet = attractions.entrySet();
		for (Entry<Node, Vector> dirSpec : dirSet) {
			dirSpec.getValue().normalize().multiply(Configuration.INODE_LENGTH);
			dirSpec.getKey().addChild(
					new Node(dirSpec.getKey().getPosition().clone()
							.add(dirSpec.getValue())));
			nodeCount++;
		}
		System.out.println("Added " + attractions.size() + " new nodes in "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");

		killOffAttractors();
	}

	public void updateModels() {
		long startTime = System.nanoTime();
		rootNode.updateCrossSection();
		System.out.println("Update " + nodeCount + " radi in "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");

		startTime = System.nanoTime();
		vertexObjects.clear();
		vertexObjects.ensureCapacity(nodeCount - 1);
		Iterator<Node> nodes = new NodeIterator(rootNode);
		while (nodes.hasNext()) {
			Node node = nodes.next();
			if (node.getParent() != null) {
				float initRadius = node.getParent().getRadius();
				if (node.getParent().getCrossSection() - node.getCrossSection() > Configuration.ACCUM_CROSS_SECTION * 10) {
					// Joining with a big branch...
					initRadius = node.getRadius();
				}
				vertexObjects.add(new CylinderVertexObject(initRadius, node
						.getRadius(), 10, node.getParent().getPosition(), node
						.getPosition()));
			}
		}
		System.out.println("Generated " + (nodeCount - 1) + " cylinders "
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

		// Now render the cylinders
		for (CylinderVertexObject obj : vertexObjects) {
			obj.render();
		}
	}
}
