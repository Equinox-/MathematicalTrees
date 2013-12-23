package com.pi.senior.space;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.pi.senior.math.Vector;

public class CharacterRenderer {
	public static final int MARGINS = 25;
	public static final int ASSUME_PADDING = 10;
	private static final float FLOATING_POINT_MATH = 0.5f;
	private static final float NODE_SIZE = 1f;
	private static final int LEAF_SIZE = 5;
	private static final float NODE_VIEWPORT = 30;
	private static final float ATTRACTOR_TOLERANCE = 1.25f;//0.35f;// 1.25f;
	private static final float ENTRY_ATTRACTOR_TOLERANCE = 25f;
	private static final float ATTRACTOR_DENSITY = .03f;//.1f;// .03f;// Per pixel
	private static final float ATTRACTORS_ANOTHER_CHUNK = 5;

	private List<Vector> nodes = new ArrayList<Vector>();
	private List<Vector> attractionVectors = new ArrayList<Vector>();

	private List<Leaf> leaves = new ArrayList<Leaf>();

	private Character c;
	private BufferedImage mask;
	private BufferedImage dest;

	private Rectangle2D characterBounds;

	private ThreadPoolExecutor threadPool;

	public Character getCharacter() {
		return c;
	}

	public CharacterRenderer(Character c, Graphics g) {
		this.c = c;
		Rectangle2D cBounds = g.getFontMetrics().getStringBounds(
				Character.toString(c), g);
		if (cBounds.getWidth() <= 0.0 || cBounds.getHeight() <= 0.0) {
			return;
		}
		mask = new BufferedImage((int) Math.ceil(cBounds.getWidth()),
				(int) Math.ceil(cBounds.getHeight()),
				BufferedImage.TYPE_INT_ARGB);
		dest = new BufferedImage((int) Math.ceil(cBounds.getWidth())
				+ (MARGINS * 2), (int) Math.ceil(cBounds.getHeight())
				+ (MARGINS * 2), BufferedImage.TYPE_INT_ARGB);
		Graphics g2 = mask.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, mask.getWidth(), mask.getHeight());
		g2.setFont(g.getFont());
		g2.setColor(Color.BLACK);
		characterBounds = g2.getFontMetrics().getStringBounds(c.toString(), g2);

		g2.drawString(c.toString(), 0, (int) -characterBounds.getY());

		// Images are expensive
		this.threadPool = new ThreadPoolExecutor(Runtime.getRuntime()
				.availableProcessors() * 2, Runtime.getRuntime()
				.availableProcessors() * 4, 1000, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());

		genAttractors();
		genCharacterStarters();
		System.out.println("Generated " + attractionVectors.size()
				+ " attractors with " + nodeHeads + " heads");
	}

	public CharacterRenderer(String s, Graphics g, float spacing) {
		this.c = s.charAt(0);
		Rectangle2D cBounds = new Rectangle2D.Float(0, 0, 0, 0);
		for (int i = 0; i < s.length(); i++) {
			Rectangle2D tmp = g.getFontMetrics().getStringBounds(
					Character.toString(s.charAt(i)), g);
			cBounds.setRect(0, 0,
					cBounds.getWidth() + tmp.getWidth() - (i>0?spacing:0),
					Math.max(tmp.getHeight(), cBounds.getHeight()));
		}
		if (cBounds.getWidth() <= 0.0 || cBounds.getHeight() <= 0.0) {
			return;
		}
		System.out.println(cBounds);
		mask = new BufferedImage((int) Math.ceil(cBounds.getWidth()),
				(int) Math.ceil(cBounds.getHeight()),
				BufferedImage.TYPE_INT_ARGB);
		dest = new BufferedImage((int) Math.ceil(cBounds.getWidth())
				+ (MARGINS * 2), (int) Math.ceil(cBounds.getHeight())
				+ (MARGINS * 2), BufferedImage.TYPE_INT_ARGB);
		Graphics g2 = mask.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, mask.getWidth(), mask.getHeight());
		g2.setFont(g.getFont());
		g2.setColor(Color.BLACK);
		// characterBounds = g2.getFontMetrics().getStringBounds(c.toString(),
		// g2);

		characterBounds = new Rectangle2D.Float(0, 0, 0, 0);
		for (int i = 0; i < s.length(); i++) {
			Rectangle2D tmp = g2.getFontMetrics().getStringBounds(
					Character.toString(s.charAt(i)), g);
			g2.drawString(Character.toString(s.charAt(i)),
					(int) (characterBounds.getWidth() - (i>0?spacing:0)),
					(int) -tmp.getY());
			characterBounds.setRect(0, 0,
					characterBounds.getWidth() + tmp.getWidth() - spacing,
					Math.max(tmp.getHeight(), characterBounds.getHeight()));
		}

		// Images are expensive
		this.threadPool = new ThreadPoolExecutor(Runtime.getRuntime()
				.availableProcessors() * 2, Runtime.getRuntime()
				.availableProcessors() * 4, 1000, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());

		genAttractors();
		genCharacterStarters();
		System.out.println("Generated " + attractionVectors.size()
				+ " attractors with " + nodeHeads + " heads");
	}

	public CharacterRenderer(BufferedImage img) {
		this.c = '\0';
		mask = img;
		dest = new BufferedImage(img.getWidth() + MARGINS * 2, img.getHeight()
				+ MARGINS * 2, BufferedImage.TYPE_INT_ARGB);
		characterBounds = new Rectangle2D.Float(0, 0, img.getWidth(),
				img.getHeight());

		// Images are expensive
		this.threadPool = new ThreadPoolExecutor(Runtime.getRuntime()
				.availableProcessors() * 2, Runtime.getRuntime()
				.availableProcessors() * 4, 1000, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());

		genAttractors();
		genImageStarters();
		System.out.println("Generated " + attractionVectors.size()
				+ " attractors with " + nodeHeads + " heads");
	}

	public BufferedImage getOutput() {
		return dest;
	}

	private void genAttractors() {
		attractionVectors.clear();
		float count = 0;
		for (int x = 0; x < characterBounds.getWidth(); x++) {
			for (int y = 0; y < characterBounds.getHeight(); y++) {
				if (isInEnvelope(x, y)) {
					count++;
				}
			}
		}
		count *= ATTRACTOR_DENSITY;
		for (int i = 0; i < count;) {
			float x = (float) ((Math.random() * characterBounds.getWidth()));
			float y = (float) ((Math.random() * characterBounds.getHeight()));
			if (isInEnvelope(x, y)) {
				attractionVectors.add(new Vector(x, y));
				i++;
			}
		}
	}

	private void genImageStarters() {
		for (int x = 0; x < mask.getWidth(); x += mask.getWidth() / 4) {
			nodes.add(new Vector(x, 0, 0));
			nodeHeads++;
		}
		Iterator<Vector> vItr = attractionVectors.iterator();
		while (vItr.hasNext()) {
			Vector vTest = vItr.next();
			for (Vector v2 : nodes) {
				if (v2.dist(vTest) < ATTRACTOR_TOLERANCE) {
					vItr.remove();
					break;
				}
			}
		}
	}

	private void genCharacterStarters() {
		nodeHeads = 0;
		double bestY = Double.MIN_VALUE;
		Vector bestV = null;
		major: for (Vector v : attractionVectors) {
			if (v.y > bestY) {
				for (Vector v2 : nodes) {
					if (v2.dist(v) < ENTRY_ATTRACTOR_TOLERANCE) {
						continue major;
					}
				}
				bestY = v.y;
				bestV = v;
			}
		}
		if (bestV != null) {
			nodes.add(bestV);
			nodeHeads++;

			Iterator<Vector> vItr = attractionVectors.iterator();
			while (vItr.hasNext()) {
				Vector vTest = vItr.next();
				for (Vector v : nodes) {
					if (v.dist(vTest) < ATTRACTOR_TOLERANCE) {
						vItr.remove();
						break;
					}
				}
			}
		} else {
			return;
		}

		for (int i = 0; i < 10; i++) {
			Vector vv = null;
			major: for (Vector v : attractionVectors) {
				if (v.y > bestY - (ATTRACTOR_TOLERANCE / 2)) {
					for (Vector v2 : nodes) {
						if (v2.dist(v) < ENTRY_ATTRACTOR_TOLERANCE) {
							continue major;
						}
					}
					vv = v;
					break;
				}
			}
			if (vv == null) {
				break;
			}
			nodes.add(vv);
			nodeHeads++;

			Iterator<Vector> vItr = attractionVectors.iterator();
			while (vItr.hasNext()) {
				Vector vTest = vItr.next();
				for (Vector v2 : nodes) {
					if (v2.dist(vTest) < ATTRACTOR_TOLERANCE) {
						vItr.remove();
						break;
					}
				}
			}
		}
	}

	private int nodeHeads = 0;

	public void update() {
		if (characterBounds.getWidth() <= 0.0
				|| characterBounds.getHeight() <= 0.0) {
			return;
		}
		final HashMap<Vector, Vector> movementNodes = new HashMap<Vector, Vector>(
				Math.max(nodes.size() - (nodeHeads * 3), 10));
		Stack<Future<?>> futures = new Stack<Future<?>>();
		for (final Vector v : attractionVectors) {
			Runnable runner = new Runnable() {
				public void run() {
					double bestDist = Double.MAX_VALUE;
					Vector bestVector = null;
					for (int i = nodes.size() - 1; i >= Math.max(nodes.size()
							- nodeHeads * 3, 0); i--) {
						Vector vTest = nodes.get(i);
						double testDist = v.dist(vTest);
						if (testDist < bestDist) {
							Vector movePlace = vTest.clone().add(
									v.clone().subtract(vTest).normalize());
							if (isInEnvelope(movePlace.x, movePlace.y)) {
								bestVector = vTest;
								bestDist = testDist;
							}
						}
					}
					if (bestVector != null && bestDist < NODE_VIEWPORT) {
						synchronized (movementNodes) {
							Vector move = movementNodes.get(bestVector);
							if (move == null) {
								move = v.clone().subtract(bestVector);
								movementNodes.put(bestVector, move);
							} else {
								movementNodes.put(bestVector, move.add(v
										.clone().subtract(bestVector)));
							}
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

		int oldHeads = nodeHeads;
		nodeHeads = 0;
		for (Entry<Vector, Vector> entry : movementNodes.entrySet()) {
			Vector nVector = entry.getKey().clone()
					.add(entry.getValue().normalize().multiply(NODE_SIZE));
			// Check for intersections...
			boolean intersects = false;

			for (int i = nodes.size() - 1; i >= Math.max(nodes.size()
					- oldHeads * 3, 0); i--) {
				Vector vTest = nodes.get(i);
				if (nVector.dist(vTest) < NODE_SIZE - FLOATING_POINT_MATH) {
					intersects = true;
					break;
				}
			}

			if (!intersects) {
				nodeHeads++;
				leaves.remove(entry.getKey());
				leaves.add(new Leaf(nVector, entry.getKey()));
				nodes.add(nVector);
			}
		}

		Stack<Future<Vector>> killFuture = new Stack<Future<Vector>>();
		for (final Vector vTest : attractionVectors) {
			Callable<Vector> runner = new Callable<Vector>() {
				@Override
				public Vector call() throws Exception {
					for (int i = nodes.size() - 1; i >= Math.max(0,
							nodes.size() - nodeHeads * 2); i--) {
						Vector v = nodes.get(i);
						if (v.dist(vTest) < ATTRACTOR_TOLERANCE) {
							return vTest;
						}
					}
					return null;
				}
			};
			if (threadPool != null) {
				killFuture.add(threadPool.submit(runner));
			} else {
				Vector v;
				try {
					v = runner.call();
					if (v != null) {
						attractionVectors.remove(v);
					}
				} catch (Exception e) {
				}
			}
		}
		while (!killFuture.empty()) {
			try {
				Vector v = killFuture.pop().get(1000, TimeUnit.SECONDS);
				if (v != null) {
					attractionVectors.remove(v);
				}
			} catch (Exception e) {
			}
		}

		if (c != '\0' && nodeHeads == 0
				&& attractionVectors.size() > ATTRACTORS_ANOTHER_CHUNK) {
			genCharacterStarters();
		}
		System.out.println("Update " + (c == '\0' ? "Image" : c.toString())
				+ "\tAttractors: " + attractionVectors.size() + ";\tNodes: "
				+ nodes.size() + ";\tHeads: " + nodeHeads);
	}

	private void prepareImage() {
		Graphics2D g = (Graphics2D) dest.getGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
		g.fillRect(0, 0, dest.getWidth(), dest.getHeight());
		g.dispose();
	}

	public void paint() {
		if (characterBounds.getWidth() <= 0.0
				|| characterBounds.getHeight() <= 0.0) {
			return;
		}
		prepareImage();
		Graphics g = dest.getGraphics();
		g.translate(MARGINS, MARGINS);

		// Attractors
		g.setColor(Color.BLUE);
		for (Vector v : attractionVectors) {
			g.fillOval((int) (v.x - 3), (int) (v.y - 3), 6, 6);
		}

		// Nodes
		g.setColor(new Color(128, 101, 23));
		for (Vector v : nodes) {
			g.drawOval((int) (v.x - (NODE_SIZE / 2)),
					(int) (v.y - (NODE_SIZE / 2)), (int) NODE_SIZE,
					(int) NODE_SIZE);
		}
	}

	public void cleanPaint() {
		if (characterBounds.getWidth() <= 0.0
				|| characterBounds.getHeight() <= 0.0) {
			return;
		}
		prepareImage();
		Graphics g = dest.getGraphics();
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.translate(MARGINS, MARGINS);

		// Nodes
		g.setColor(new Color(128, 101, 23, c == '\0' ? 100 : 255));
		for (Vector v : nodes) {
			g.drawOval((int) (v.x - (NODE_SIZE / 2)),
					(int) (v.y - (NODE_SIZE / 2)), (int) NODE_SIZE,
					(int) NODE_SIZE);
		}

		// Leaves
		g.setColor(new Color(52, 114, 53, 100));
		for (Leaf v : leaves) {

			Graphics2D inst = (Graphics2D) g.create();
			if (c == '\0') {
				Color color;
				if (isInEnvelope((int) v.x, (int) v.y)) {
					color = new Color(mask.getRGB((int) v.x, (int) v.y));
					color = new Color(color.getRed(), color.getGreen(),
							color.getBlue(), color.getAlpha() / 2);
				} else {
					color = new Color(0, 0, 0, 0);
				}
				inst.setColor(color);
			}
			inst.translate(v.x, v.y);
			inst.rotate(Math.PI / -2.0 + v.getRotation());
			inst.fillOval((int) (-(LEAF_SIZE / 2)), (int) (-(LEAF_SIZE / 2)),
					LEAF_SIZE, LEAF_SIZE * 2);
			inst.dispose();

		}
	}

	public boolean isComplete() {
		return attractionVectors.size() < ATTRACTORS_ANOTHER_CHUNK
				&& nodeHeads == 0;
	}

	public boolean isSortaComplete() {
		return attractionVectors.size() < 200 && nodeHeads < 125;
	}

	private boolean isInEnvelope(float x, float y) {
		if (x >= 0 && y >= 0 && x < mask.getWidth() && y < mask.getHeight()) {
			if (c == '\0')
				return true;
			Color c = new Color(mask.getRGB((int) x, (int) y));
			return c.getRed() == 0;
		} else {
			return false;
		}
	}
}
