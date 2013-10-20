package com.pi.senior.space.gen;

import java.util.Random;

import com.pi.senior.math.Vector;

public interface Envelope {
	public boolean contains(Vector v, Random rand);

	public Vector nextRandom(Random rand);
}
