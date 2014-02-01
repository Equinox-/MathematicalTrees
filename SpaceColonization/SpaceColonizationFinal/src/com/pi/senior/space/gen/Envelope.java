package com.pi.senior.space.gen;

import java.util.Random;

import com.pi.senior.math.Vector3D;

public interface Envelope {
	public boolean contains(Vector3D v, Random rand);

	public Vector3D nextRandom(Random rand);
}
