package com.pi.senior.budfate.tree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.pi.senior.math.Vector3D;

public class Tree {
	private PositionedMetamer rootMetamer;
	private Map<Integer, PositionedMetamer> metamers = new HashMap<Integer, PositionedMetamer>();

	public Tree() {
		rootMetamer = new PositionedMetamer(MetamerType.TERMINAL, new Vector3D(
				0, 0, 0), new Vector3D(0, 0, 1));
	}

	public void calculate() {
		rootMetamer.calculateRecursive();
	}

	public Iterator<Entry<Integer, PositionedMetamer>> getPositionedMetamers() {
		return metamers.entrySet().iterator();
	}

	public PositionedMetamer getRootMetamer() {
		return rootMetamer;
	}

	public PositionedMetamer getMetamer(int i) {
		return metamers.get(i);
	}
}
