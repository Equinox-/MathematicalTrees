package com.pi.senior.space;

import java.awt.Color;

import com.pi.senior.math.Vector;

public class Leaf extends Vector {
	private float rotation;

	public Leaf(Vector pos, Vector emanate) {
		super(pos.x, pos.y, pos.z);
		rotation = (float) Math.atan2(pos.x - emanate.x, pos.y - emanate.y);
	}

	public float getRotation() {
		return rotation;
	}
}
