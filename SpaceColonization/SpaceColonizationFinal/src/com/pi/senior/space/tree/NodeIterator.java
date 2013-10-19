package com.pi.senior.space.tree;

import java.util.Iterator;

public class NodeIterator implements Iterator<Node> {
	private Node root;
	private Node current;
	private boolean killedFirst = false;

	public NodeIterator(Node root) {
		this.root = root;
		this.current = root;
		// Go to the top
		while (current.getChildren().size() > 0) {
			current = current.getChildren().get(0);
		}
	}

	@Override
	public boolean hasNext() {
		return current != null && current.getParent() != null
				&& current != root;
	}

	@Override
	public Node next() {
		if (!killedFirst) {
			killedFirst = true;
			return current;
		}
		int nextIDX = current.getParent().getChildren().indexOf(current) + 1;
		if (nextIDX >= current.getParent().getChildren().size()) {
			current = current.getParent();
		} else {
			current = current.getParent().getChildren().get(nextIDX);
			while (current.getChildren().size() > 0) {
				current = current.getChildren().get(0);
			}
		}
		return current;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"Theoretically supported, but not yet.");
	}
}
