package com.pi.senior.space;

import java.awt.Color;

import com.pi.senior.math.Vector3D;

public class Leaf extends Vector3D {
	private float rotation;

	public Leaf(Vector3D pos, Vector3D emanate) {
		super(pos.x, pos.y, pos.z);
		rotation = (float) Math.atan2(pos.x - emanate.x, pos.y - emanate.y);
	}

	public float getRotation() {
		return rotation;
	}
}
