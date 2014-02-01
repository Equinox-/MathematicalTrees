package com.pi.senior.budfate.tree;

import com.pi.senior.world.WorldProvider;

public class MetamerInfo {
	// State and Type spec
	private MetamerState state;
	private final MetamerType type;

	// Extra State Information
	private long stateBegin;
	private long creationTime;

	public MetamerInfo(MetamerType type) {
		this.type = type;
		this.state = MetamerState.BUD_LIVE;
		this.creationTime = WorldProvider.currentTimeMillis();
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
