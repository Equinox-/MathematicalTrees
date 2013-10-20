package com.pi.senior.space.gen;

import java.util.Iterator;

import com.pi.senior.math.Vector;
import com.pi.senior.space.tree.Node;

public class AttractionNode implements Comparable<AttractionNode> {
	private Node attracted;
	private double distance;
	private Vector growthDirection;

	public AttractionNode(Node attracted, Vector growthDirection,
			double distance) {
		this.attracted = attracted;
		this.growthDirection = growthDirection;
		this.distance = distance;
	}

	public Node getAttracted() {
		return attracted;
	}

	public double getWeightedDistance() {
		return distance;
	}

	public Vector getGrowthDirection() {
		return growthDirection;
	}

	public static AttractionNode computeNodeFor(Iterator<Node> ndIterator,
			Vector attractor, boolean useBiasVectors, Vector idealDirection,
			float maxAttractionDistance) {
		double bestDist = Double.MAX_VALUE;
		Node bestNd = null;
		Vector bestDirection = null;
		while (ndIterator.hasNext()) {
			Node nd = ndIterator.next();
			double dist2 = nd.getPosition().distSquared(attractor);
			if (dist2 < maxAttractionDistance && dist2 < bestDist) {
				Vector testDirection = attractor.clone()
						.subtract(nd.getPosition()).normalize();
				if (useBiasVectors) {
					// Compare the current branch direction with the direction
					// this branch will cause.
					if (nd.getDirection() != null) {
						double angleOfChange = Math.abs(Math.acos(Vector
								.dotProduct(testDirection, nd.getDirection())));
						dist2 *= (angleOfChange);
					} else {
						dist2 *= Math.PI / 2f;
					}

					// A second bias is to compare to the root of the tree. In
					// general branches should extend away from the root of the
					// tree on the XZ plane. AKA tropism
					double angleOfChange = Math.abs(Math.acos(Vector
							.dotProduct(idealDirection, testDirection)));
					dist2 *= (angleOfChange);
				}

				if (dist2 < bestDist) {
					bestDirection = testDirection;
					bestNd = nd;
					bestDist = dist2;
				}
			}
		}
		if (bestNd != null) {
			return new AttractionNode(bestNd, bestDirection, bestDist);
		}
		return null;
	}

	@Override
	public int compareTo(AttractionNode n) {
		return -Double.compare(getWeightedDistance(), n.getWeightedDistance());
	}
}
