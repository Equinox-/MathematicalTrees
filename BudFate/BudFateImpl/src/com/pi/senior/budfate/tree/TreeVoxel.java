package com.pi.senior.budfate.tree;

public class TreeVoxel {
	public boolean isOccluding;
	public PositionedMetamer livingHere;
	public float shadowLevel;

	public String toString() {
		return "TreeVoxel[livingHere=" + livingHere + ",shadowLevel="
				+ shadowLevel + "]";
	}
}
