package com.pi.senior.budfate.tree;

import com.pi.senior.math.Vector;
import com.pi.senior.world.WorldProvider;

public class Metamer {
	// Hierarchy information
	private Vector endPosition;
	private Vector startPosition;

	private Metamer parent;

	// State and Type spec
	private MetamerState state;
	private final MetamerType type;

	// Extra State Information
	private long stateBegin;
	private long creationTime;

	public Metamer(Metamer parent, MetamerType type, Vector startPosition) {
		this(parent, type, startPosition, startPosition.clone());
	}

	public Metamer(Metamer parent, MetamerType type, Vector startPosition,
			Vector endPosition) {
		this.type = type;
		this.parent = parent;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.state = MetamerState.BUD_LIVE;
		this.creationTime = WorldProvider.currentTimeMillis();
	}

	public void setInodeSize(float f) {
		endPosition.subtract(startPosition).normalize().multiply(f)
				.add(startPosition);
	}

	public float getInodeSize() {
		return (float) endPosition.dist(startPosition);
	}

	public Vector getStartPosition() {
		return startPosition;
	}

	public Vector getEndPosition() {
		return endPosition;
	}

	public MetamerType getBudType() {
		return type;
	}

	public MetamerState getBudState() {
		return state;
	}

	public void changeBudState(MetamerState s) {
		this.stateBegin = WorldProvider.currentTimeMillis();
		this.state = s;
	}

	public long getLifetime() {
		return WorldProvider.currentTimeMillis() - creationTime;
	}

	public long getCurrentStateLifetime() {
		return WorldProvider.currentTimeMillis() - stateBegin;
	}
}
