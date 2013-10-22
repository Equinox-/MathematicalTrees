package com.pi.senior.space.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

	private ThreadPoolExecutor threadPool;

	public SpaceColonizer(Node root, Envelope area) {
		this.rootNode = root;
		this.populationArea = area;
		this.threadPool = new ThreadPoolExecutor(Runtime.getRuntime()
				.availableProcessors() * 2, Runtime.getRuntime()
				.availableProcessors() * 4, 1000, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
	}

	public void generateAttractors() {
		long startTime = System.nanoTime();
		attractors = new ArrayList<Vector>(Configuration.ATTRACTOR_COUNT);
		for (int i = 0; i < Configuration.ATTRACTOR_COUNT; ++i) {
			attractors.add(populationArea.nextRandom(rand));
		}
		System.out.println("Generated " + attractors.size() + " attractors in "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");

		killOffAttractors(NodeIterator.createFactory(rootNode));
	}

	private void killOffAttractors(
			final Callable<? extends Iterator<Node>> iteratorFactory) {
		long startTime = System.nanoTime();
		int startCount = attractors.size();

		// Kill off old attractors
		Iterator<Vector> attractionItr = attractors.iterator();
		Stack<Future<Vector>> futures = new Stack<Future<Vector>>();
		while (attractionItr.hasNext()) {
			final Vector kill = attractionItr.next();
			Callable<Vector> runner = new Callable<Vector>() {
				public Vector call() {
					try {
						Iterator<Node> nodes = iteratorFactory.call();
						while (nodes.hasNext()) {
							Node next = nodes.next();
							if (kill.distSquared(next.getPosition()) < Configuration.ATTRACTOR_KILL_RADIUS_SQUARED) {
								return kill;
							}
						}
					} catch (Exception e) {
					}
					return null;
				}
			};
			futures.add(threadPool.submit(runner));
		}
		while (!futures.empty()) {
			try {
				Vector v = futures.pop().get(1000, TimeUnit.SECONDS);
				if (v != null) {
					attractors.remove(v);
				}
			} catch (Exception e) {
			}
		}
		System.out.println("Killed " + (startCount - attractors.size())
				+ " attractors in "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");
	}

	private Map<Node, Vector> generateAttractionVectors() {
		long startTime = System.nanoTime();
		final Map<Node, Vector> attractions = new HashMap<Node, Vector>(
				nodeCount);
		Stack<Future<?>> futures = new Stack<Future<?>>();
		for (final Vector v : attractors) {
			Runnable runner = new Runnable() {
				public void run() {
					Iterator<Node> ndIterator = new NodeIterator(rootNode);
					Vector tropism = v.clone().subtract(rootNode.getPosition());
					// We want to assume perfectly flat branching structures
					tropism.y = Configuration.IDEAL_BRANCH_SLOPE;
					tropism.normalize();

					AttractionNode attracted = AttractionNode.computeNodeFor(
							ndIterator, v, Configuration.USE_BIAS_VECTORS,
							tropism,
							Configuration.ATTRACTOR_ATTRACTION_RADIUS_SQUARED);
					synchronized (attractions) {
						if (attracted != null) {
							Vector curr = attractions.get(attracted
									.getAttracted());
							if (curr == null) {
								curr = new Vector(0, 0, 0);
								attractions.put(attracted.getAttracted(), curr);
							}
							curr.add(attracted.getGrowthDirection());
						}
					}
				}
			};
			if (threadPool != null) {
				futures.add(threadPool.submit(runner));
			} else {
				runner.run();
			}
		}
		while (!futures.empty()) {
			try {
				futures.pop().get(1000, TimeUnit.SECONDS);
			} catch (Exception e) {
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
		int nodesAdded = 0;
		final List<Node> newNodes = new ArrayList<Node>(dirSet.size());
		for (Entry<Node, Vector> dirSpec : dirSet) {
			dirSpec.getValue().normalize().multiply(Configuration.INODE_LENGTH);
			Node nd = new Node(dirSpec.getKey().getPosition().clone()
					.add(dirSpec.getValue()));
			if (dirSpec.getKey().addChild(nd)) {
				newNodes.add(nd);
				++nodeCount;
				++nodesAdded;
			}
		}
		System.out.println("Added " + nodesAdded + " new nodes in "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");

		killOffAttractors(new Callable<Iterator<Node>>() {
			public Iterator<Node> call() {
				return newNodes.iterator();
			}
		});
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
				Vector startDirection = null;
				if (node.getParent().getCrossSection() - node.getCrossSection() > Configuration.ACCUM_CROSS_SECTION * 10) {
					// Joining with a big branch...
					initRadius = node.getRadius();
				} else if (node.getParent().getParent() != null) {
					startDirection = node
							.getParent()
							.getPosition()
							.clone()
							.subtract(
									node.getParent().getParent().getPosition())
							.normalize();
				}
				if (startDirection == null) {
					startDirection = node.getPosition().clone()
							.subtract(node.getParent().getPosition())
							.normalize();
				}
				vertexObjects.add(new CylinderVertexObject(initRadius, node
						.getRadius(), 10, node.getParent().getPosition(),
						startDirection, node.getPosition()));
			}
		}
		System.out.println("Generated " + (nodeCount - 1) + " cylinders "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");
	}

	public void renderAttractors() {
		GL11.glDisable(GL11.GL_LIGHTING);
		// Render attractors
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glColor3f(1f, 0f, 0f);
		for (Vector v : attractors) {
			GL11.glVertex3f(v.x, v.y, v.z);
		}
		GL11.glEnd();
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	public void renderLineConnectors() {
		GL11.glDisable(GL11.GL_LIGHTING);
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
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	public void renderCylinderConnectors() {
		GL11.glEnable(GL11.GL_LIGHTING);
		// Now render the cylinders
		for (CylinderVertexObject obj : vertexObjects) {
			obj.render();
		}
	}

	public void shutdown() {
		threadPool.shutdown();
		System.out.println(threadPool.getActiveCount());
		try {
			threadPool.awaitTermination(10000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
