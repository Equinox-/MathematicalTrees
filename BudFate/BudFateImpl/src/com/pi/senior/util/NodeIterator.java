package com.pi.senior.util;

import java.util.Iterator;
import java.util.concurrent.Callable;

import com.pi.senior.budfate.tree.PositionedMetamer;

public class NodeIterator implements Iterator<PositionedMetamer> {
	private PositionedMetamer root;
	private PositionedMetamer current;
	private boolean killedFirst = false;

	public static Callable<NodeIterator> createFactory(final PositionedMetamer root) {
		return new Callable<NodeIterator>() {
			public NodeIterator call() {
				return new NodeIterator(root);
			}
		};
	}

	public NodeIterator(PositionedMetamer root) {
		this.root = root;
		this.current = root;
		// Go to the top
		while (current.getChildren().size() > 0) {
			current = current.getChildren().get(0);
		}
	}

	@Override
	public boolean hasNext() {
		return current != null
				&& (!killedFirst || (current.getParent() != null && current != root));
	}

	@Override
	public PositionedMetamer next() {
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
