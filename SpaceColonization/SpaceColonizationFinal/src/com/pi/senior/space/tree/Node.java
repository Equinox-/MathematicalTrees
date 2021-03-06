package com.pi.senior.space.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.pi.senior.math.Vector3D;
import com.pi.senior.space.Configuration;
import com.pi.senior.space.util.WorldProvider;

public class Node {
	private List<Node> children = new ArrayList<Node>();
	private Node parent;
	private Vector3D position;
	private Vector3D direction; // Cached for better performance
	private float crossSection = Configuration.TIP_CROSS_SECTION;
	private BudState budState = BudState.NEW_BRANCH;
	private long budStateBegin;

	private WorldProvider worldProvider;

	public Node(Vector3D position, WorldProvider worldProvider) {
		this.position = position;
		this.direction = new Vector3D(0, 1, 0);
		this.worldProvider = worldProvider;
		this.budStateBegin = worldProvider.currentTimeMillis();
	}

	public boolean addChild(Node child) {
		if (child.parent == this) {
			return false;
		}
		if (child.parent != null) {
			throw new IllegalArgumentException(
					"This child already has a parent!");
		}

		// Safety check. This should NEVER occur.
		Node tmpParent = this.parent;
		while (tmpParent != null) {
			if (tmpParent == child) {
				throw new IllegalArgumentException(
						"A node can't be both the parent and the child of one node.");
			}
			tmpParent = tmpParent.getParent();
		}

		// Does this parent already have a node near there?
		for (Node n : children) {
			if (n.getPosition().dist(child.getPosition()) < Configuration.NODE_CHILD_TOLERANCE) {
				// Assume they are the same
				return false;
			}
		}

		child.parent = this;
		child.direction = child.position.clone().subtract(position).normalize();
		children.add(child);
		return true;
	}

	public Vector3D getPosition() {
		return position;
	}

	public Node getParent() {
		return parent;
	}

	public Vector3D getDirection() {
		return direction;
	}

	public List<Node> getChildren() {
		return Collections.unmodifiableList(children);
	}

	@Override
	public int hashCode() {
		return position.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}

	@Override
	public String toString() {
		return "Node[" + position + ", children: " + children.size() + "]";
	}

	public void updateCrossSection() {
		if (children.size() == 0) {
			crossSection = Configuration.TIP_CROSS_SECTION;
		} else {
			crossSection = Configuration.ACCUM_CROSS_SECTION;
			for (Node n : children) {
				n.updateCrossSection();
				crossSection += n.crossSection;
			}
		}
	}

	public float getRadius() {
		return (float) Math.pow(crossSection, 0.3f)
				* Configuration.RADI_PER_CROSS_SECTION;
	}

	public float getCrossSection() {
		return crossSection;
	}

	public long getStateLifetime() {
		return worldProvider.currentTimeMillis() - budStateBegin;
	}

	public void setBudState(BudState state) {
		budStateBegin = worldProvider.currentTimeMillis();
		budState = state;
	}

	public BudState getBudState() {
		if (budState == BudState.BUD && getStateLifetime() > 1000) {
			setBudState(BudState.LEAF);
		}

		if (budState == BudState.LEAF && getStateLifetime() > 1000) {
			setBudState(BudState.NEW_BRANCH);
		}

		if (budState == BudState.NEW_BRANCH && getStateLifetime() > 30000) {
			//setBudState(BudState.OLD_BRANCH);
		}
		return budState;
	}
}
