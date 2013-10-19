package com.pi.senior.space.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.pi.senior.math.Vector;

public class Node {
	private List<Node> children = new ArrayList<Node>();
	private Node parent;
	private Vector position;

	public Node(Vector position) {
		this.position = position;
	}

	public void addChild(Node child) {
		if (child.parent == this) {
			return;
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

		child.parent = this;
		children.add(child);
	}

	public Vector getPosition() {
		return position;
	}

	public Node getParent() {
		return parent;
	}

	public List<Node> getChildren() {
		return Collections.unmodifiableList(children);
	}

	@Override
	public String toString() {
		return "Node[" + position + ", children: " + children.size() + "]";
	}
}
