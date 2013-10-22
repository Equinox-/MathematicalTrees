package com.pi.senior.space.gen;

import java.util.Iterator;

import com.pi.senior.math.Vector;
import com.pi.senior.space.tree.Node;

public class AttractionNode implements Comparable<AttractionNode> {
	private Node attracted;
	private double distance;
	private double rawDistance;
	private Vector growthDirection;

	public AttractionNode(Node attracted, Vector growthDirection,
			double distance, double rawDistance) {
		this.attracted = attracted;
		this.growthDirection = growthDirection;
		this.rawDistance = rawDistance;
		this.distance = distance;
	}

	public Node getAttracted() {
		return attracted;
	}

	public double getWeightedDistance() {
		return distance;
	}

	public double getRawDistance() {
		return rawDistance;
	}

	public Vector getGrowthDirection() {
		return growthDirection;
	}

	public static AttractionNode computeAttractionOf(Node nd, Vector attractor,
			Vector idealDirection, float divergenceWeight, float tropismWeight) {
		double distRaw = nd.getPosition().distSquared(attractor);
		double dist2 = distRaw;
		Vector testDirection = attractor.clone().subtract(nd.getPosition())
				.normalize();
		if (divergenceWeight > 0) {
			// Compare the current branch direction with the direction
			// this branch will cause.
			if (nd.getDirection() != null) {
				double angleOfChange = Math.abs(Math.acos(Vector.dotProduct(
						testDirection, nd.getDirection())));
				dist2 += angleOfChange * divergenceWeight;
			}
		}
		if (tropismWeight > 0) {
			// A second bias is to compare to the root of the tree. In
			// general branches should extend away from the root of the
			// tree on the XZ plane. AKA tropism
			double angleOfChange = Math.abs(Math.acos(Vector.dotProduct(
					idealDirection, testDirection)));
			dist2 += angleOfChange * tropismWeight;
		}
		return new AttractionNode(nd, testDirection, dist2, distRaw);
	}

	public static AttractionNode computeBestNodeFor(Iterator<Node> ndIterator,
			Vector attractor, Vector idealDirection,
			float maxAttractionDistance, float divergenceWeight,
			float tropismWeight) {
		AttractionNode bestNode = null;
		while (ndIterator.hasNext()) {
			AttractionNode testNode = computeAttractionOf(ndIterator.next(),
					attractor, idealDirection, divergenceWeight, tropismWeight);
			if (testNode.getRawDistance() < maxAttractionDistance
					&& (bestNode == null || bestNode.compareTo(testNode) < 0)) {
				bestNode = testNode;
			}
		}
		return bestNode;
	}

	@Override
	public int compareTo(AttractionNode n) {
		return -Double.compare(getWeightedDistance(), n.getWeightedDistance());
	}
}
