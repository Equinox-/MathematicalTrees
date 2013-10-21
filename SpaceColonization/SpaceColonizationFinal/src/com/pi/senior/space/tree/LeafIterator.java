package com.pi.senior.space.tree;

import java.util.Iterator;

public class LeafIterator implements Iterator<Node> {
	private Node root;
	private Node next;

	public LeafIterator(Node root) {
		this.root = root;
		this.next = root;
		// Go to the top
		while (next.getChildren().size() > 0) {
			next = next.getChildren().get(0);
		}
	}

	private void computeNext() {
		if (next == null || next.getParent() == null || next == root) {
			next = null;
			return;
		}
		int nextIDX = next.getParent().getChildren().indexOf(next) + 1;
		if (nextIDX >= next.getParent().getChildren().size()) {
			// Navigate down to next available leaf.
			next = next.getParent();
			computeNext();
		} else {
			next = next.getParent().getChildren().get(nextIDX);
			while (next.getChildren().size() > 0) {
				next = next.getChildren().get(0);
			}
		}
	}

	@Override
	public boolean hasNext() {
		return next != null && next.getChildren().size() == 0;
	}

	@Override
	public Node next() {
		Node tmp = next;
		computeNext();
		return tmp;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"Theoretically supported, but not yet.");
	}
}
