package com.pi.senior.space.gen;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

import com.pi.senior.math.Vector3D;
import com.pi.senior.space.Configuration;
import com.pi.senior.space.renderer.CylinderVertexObject;
import com.pi.senior.space.renderer.LeafVertexObject;
import com.pi.senior.space.renderer.Renderable;
import com.pi.senior.space.tree.BudState;
import com.pi.senior.space.tree.Node;
import com.pi.senior.space.tree.NodeIterator;
import com.pi.senior.space.util.Filter;
import com.pi.senior.space.util.FilteredIterator;
import com.pi.senior.space.util.WorldProvider;

public class SpaceColonizer implements WorldProvider {
	private Node rootNode;
	private Envelope populationArea;
	private Random rand = new Random();

	private List<Vector3D> attractors;
	private int nodeCount = 1;

	private ArrayList<Renderable> vertexObjects = new ArrayList<Renderable>();
	private ThreadPoolExecutor threadPool;

	private long currentTime = 0;

	public SpaceColonizer(Vector3D root, Envelope area) {
		this.rootNode = new Node(root, this);
		rootNode.setBudState(BudState.NEW_BRANCH);

		this.populationArea = area;
		this.threadPool = new ThreadPoolExecutor(Runtime.getRuntime()
				.availableProcessors() * 2, Runtime.getRuntime()
				.availableProcessors() * 4, 1000, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
	}

	public void generateAttractors() {
		long startTime = System.nanoTime();
		attractors = new ArrayList<Vector3D>(Configuration.ATTRACTOR_COUNT);
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
		Iterator<Vector3D> attractionItr = attractors.iterator();
		Stack<Future<Vector3D>> futures = new Stack<Future<Vector3D>>();
		while (attractionItr.hasNext()) {
			final Vector3D kill = attractionItr.next();
			Callable<Vector3D> runner = new Callable<Vector3D>() {
				public Vector3D call() {
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
				Vector3D v = futures.pop().get(1000, TimeUnit.SECONDS);
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

	private Map<Node, Vector3D> generateAttractionVectors() {
		long startTime = System.nanoTime();
		final Map<Node, Vector3D> attractions = new HashMap<Node, Vector3D>(
				nodeCount);
		Stack<Future<?>> futures = new Stack<Future<?>>();
		for (final Vector3D v : attractors) {
			Runnable runner = new Runnable() {
				public void run() {
					Iterator<Node> ndIterator = new FilteredIterator<Node>(
							new NodeIterator(rootNode), new Filter<Node>() {
								@Override
								public boolean accept(Node t) {
									return t.getBudState() == BudState.NEW_BRANCH;
								}
							});
					Vector3D tropism = v.clone().subtract(
							rootNode.getPosition());
					// We want to assume perfectly flat branching structures
					tropism.y = Configuration.IDEAL_BRANCH_SLOPE;

					AttractionNode attracted = AttractionNode
							.computeBestNodeFor(
									ndIterator,
									v,
									tropism,
									Configuration.ATTRACTOR_ATTRACTION_RADIUS_SQUARED,
									Configuration.DIVERGENCE_WEIGHT,
									Configuration.TROPISM_WEIGHT);
					synchronized (attractions) {
						if (attracted != null) {
							Vector3D curr = attractions.get(attracted
									.getAttracted());
							if (curr == null) {
								curr = new Vector3D(0, 0, 0);
								attractions.put(attracted.getAttracted(), curr);
							}
							curr.add(attracted.getGrowthDirection().multiply(
									1f / (float) Math.sqrt(attracted
											.getWeightedDistance())));
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
			// If we don't get any attractions, average all the ones within a
			// certain distance of each other
			AttractionNode bestAttracted = null;
			for (Vector3D v : attractors) {
				Iterator<Node> ndIterator = new FilteredIterator<Node>(
						new NodeIterator(rootNode), new Filter<Node>() {
							@Override
							public boolean accept(Node t) {
								return t.getBudState() == BudState.NEW_BRANCH;
							}
						});

				AttractionNode attracted = AttractionNode.computeBestNodeFor(
						ndIterator, v, null, Float.MAX_VALUE, 0, 0);
				if (attracted != null
						&& (bestAttracted == null || attracted
								.compareTo(bestAttracted) > 0)) {
					bestAttracted = attracted;
				}
			}
			// Now that we have the best ones average the nodes close to that
			// distance away
			if (bestAttracted != null) {
				Vector3D accum = new Vector3D(0, 0, 0);
				for (Vector3D v : attractors) {
					AttractionNode attractionInformation = AttractionNode
							.computeAttractionOf(bestAttracted.getAttracted(),
									v, null, 0, 0);
					if (Math.abs(attractionInformation.getWeightedDistance()
							- bestAttracted.getWeightedDistance()) < Configuration.OUTSIDE_ENVELOPE_ATTRACTOR_TOLERANCE) {
						accum.add(attractionInformation
								.getGrowthDirection()
								.multiply(
										1f / (float) Math
												.sqrt(attractionInformation
														.getWeightedDistance())));
					}
				}
				attractions
						.put(bestAttracted.getAttracted(), accum.normalize());
			}
		}
		System.out.println("Generated " + attractions.size()
				+ " attraction vectors in "
				+ ((System.nanoTime() - startTime) / 1000000.0) + " ms");
		return attractions;
	}

	public void evolve() {
		currentTime += 1000;

		// First step is to inspect every attraction vector and find the closest
		// node.
		Map<Node, Vector3D> attractions = generateAttractionVectors();

		// Add the new nodes
		long startTime = System.nanoTime();
		Set<Entry<Node, Vector3D>> dirSet = attractions.entrySet();
		int nodesAdded = 0;
		final List<Node> newNodes = new ArrayList<Node>(dirSet.size());
		for (Entry<Node, Vector3D> dirSpec : dirSet) {
			dirSpec.getValue().normalize().multiply(Configuration.INODE_LENGTH);
			Node nd = new Node(dirSpec.getKey().getPosition().clone()
					.add(dirSpec.getValue()), this);
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
				Vector3D startDirection = null;
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
				Color color = Color.white;
				switch (node.getBudState()) {
				case BUD:
					color = Color.blue;
					break;
				case LEAF:
					color = new Color(0f, 1f, 0f, 0.5f);
					break;
				case NEW_BRANCH:
					color = new Color(139, 69, 19);
					break;
				case OLD_BRANCH:
					color = new Color(100, 49, 0);
					break;
				default:
					color = Color.white;
				}
				if (node.getBudState() == BudState.LEAF) {
					vertexObjects.add(new LeafVertexObject(10, node.getParent()
							.getPosition(), startDirection, color, 1f));
				} else {
					Vector3D leafDIR = Vector3D
							.crossProduct(
									startDirection,
									node.getPosition()
											.clone()
											.subtract(
													node.getDirection()
															.clone()
															.normalize()
															.add(new Vector3D(
																	(float) Math
																			.random() * 3f - 1.5f,
																	(float) Math
																			.random() * 3f - 1.5f,
																	(float) Math
																			.random() * 3f - 1.5f)))
											.normalize());
					if (Math.random() < .5f) {
						leafDIR.x *= -1;
						leafDIR.z *= -1;
					}
					if (Math.random() < .75 && node.getCrossSection() < 10) {
						vertexObjects.add(new LeafVertexObject(10,
								node.getPosition()
										.clone()
										.add(leafDIR.clone().multiply(
												node.getRadius())), leafDIR,
								new Color(0f, 1f, 0f, 0.5f), .5f));
					}
					if (Math.random() < .5 && node.getCrossSection() < 5) {
						vertexObjects.add(new LeafVertexObject(10, node
								.getPosition(), Vector3D.crossProduct(leafDIR,
								new Vector3D(1f, 1f, 1f)).normalize(),
								new Color(0f, 1f, 0f, 0.5f), .4f));
					}
					vertexObjects.add(new CylinderVertexObject(initRadius, node
							.getRadius(), 10, node.getParent().getPosition(),
							startDirection, node.getPosition(), color));
				}
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
		for (Vector3D v : attractors) {
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
				Color color = Color.white;
				switch (n.getBudState()) {
				case BUD:
					color = Color.blue;
					break;
				case LEAF:
					color = Color.green;
					break;
				case NEW_BRANCH:
					color = new Color(139, 69, 19);
					break;
				case OLD_BRANCH:
					color = new Color(80, 40, 0);
					break;
				default:
					color = Color.white;
				}
				GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f,
						color.getBlue() / 255f, color.getAlpha() / 255f);
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
		for (Renderable obj : vertexObjects) {
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

	public void saveSTL(File file) {
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			int i = 0;
			int len = 0;
			for (Renderable obj : vertexObjects) {
				if (obj instanceof CylinderVertexObject) {
					String stl = ((CylinderVertexObject) obj).getSTL("chunk-"
							+ i);
					output.write(stl);
					len += stl.length();
					i++;
					output.newLine();
				}
			}
			System.out.println("STL Length: " + len);
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public long currentTimeMillis() {
		return currentTime;
	}
}
