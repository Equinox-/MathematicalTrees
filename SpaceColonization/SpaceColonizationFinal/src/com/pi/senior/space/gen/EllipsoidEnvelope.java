package com.pi.senior.space.gen;

import java.util.Random;

import com.pi.senior.math.Vector;

public class EllipsoidEnvelope implements Envelope {
	private Vector size;
	private Vector center;

	public EllipsoidEnvelope(Vector center, Vector size) {
		this.size = size;
		this.center = center;
	}

	public boolean contains(Vector v) {
		float dx = (v.x - center.x) / size.x;
		float dy = (v.y - center.y) / size.y;
		float dz = (v.z - center.z) / size.z;
		return (dx * dx) + (dy * dy) + (dz * dz) <= 1.0f;
	}

	@Override
	public Vector nextRandom(Random rand) {
		Vector v;
		do {
			float xRand = (rand.nextFloat() * 2f) - 1f;
			float yRand = (rand.nextFloat() * 2f) - 1f;
			float zRand = (rand.nextFloat() * 2f) - 1f;
			v = new Vector(center.x + (xRand * size.x), center.y
					+ (yRand * size.y), center.z + (zRand * size.z));
		} while (!contains(v));
		return v;
	}
}
