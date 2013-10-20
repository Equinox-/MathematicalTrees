package com.pi.senior.space.gen;

import java.util.Random;

import com.pi.senior.math.Vector;

public class EllipsoidEnvelope implements Envelope {
	public enum PopulationSchema {
		FILL, SHELL, UMBRELLA;
	}

	private Vector size;
	private Vector center;
	private PopulationSchema populationSchema;

	private float randomShellVariance = 0.25f;
	private float randomDistributionVariance = 0.5f;

	public EllipsoidEnvelope(Vector center, Vector size,
			PopulationSchema populationSchema) {
		this.size = size;
		this.center = center;
		this.populationSchema = populationSchema;
	}

	@Override
	public boolean contains(Vector v, Random rand) {
		float dx = (v.x - center.x) / size.x;
		float dy = (v.y - center.y) / size.y;
		float dz = (v.z - center.z) / size.z;
		float distSquared = (dx * dx) + (dy * dy) + (dz * dz)
				+ ((rand.nextFloat() - 0.5f) * randomShellVariance);
		switch (populationSchema) {
		case SHELL:
			return distSquared <= 1.0f
					&& distSquared >= (0.9f - Math.abs(rand.nextGaussian()
							* randomDistributionVariance));
		case UMBRELLA:
			return dy > 0
					&& distSquared <= 1.0f
					&& distSquared >= (0.9f - Math.abs(rand.nextGaussian()
							* randomDistributionVariance));
		case FILL:
		default:
			return distSquared <= 1.0f;
		}
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
		} while (!contains(v, rand));
		return v;
	}
}
