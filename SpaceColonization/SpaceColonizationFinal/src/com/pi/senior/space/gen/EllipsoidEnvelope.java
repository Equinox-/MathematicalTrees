package com.pi.senior.space.gen;

import java.util.Random;

import com.pi.senior.math.Vector3D;

public class EllipsoidEnvelope implements Envelope {
	public enum PopulationSchema {
		FILL, SHELL, UMBRELLA;
	}

	private Vector3D size;
	private Vector3D center;
	private PopulationSchema populationSchema;

	private float randomShellVariance = 0.25f;
	private float randomDistributionVariance = 0.5f;

	public EllipsoidEnvelope setRandomParameters(float shellVariance,
			float distVariance) {
		this.randomShellVariance = shellVariance;
		this.randomDistributionVariance = distVariance;
		return this;
	}

	public EllipsoidEnvelope(Vector3D center, Vector3D size,
			PopulationSchema populationSchema) {
		this.size = size;
		this.center = center;
		this.populationSchema = populationSchema;
	}

	@Override
	public boolean contains(Vector3D v, Random rand) {
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
	public Vector3D nextRandom(Random rand) {
		Vector3D v;
		do {
			float xRand = (rand.nextFloat() * 2f) - 1f;
			float yRand = (rand.nextFloat() * 2f) - 1f;
			float zRand = (rand.nextFloat() * 2f) - 1f;
			v = new Vector3D(center.x
					+ (xRand * (size.x + randomShellVariance)), center.y
					+ (yRand * (size.y + randomShellVariance)), center.z
					+ (zRand * (size.z + randomShellVariance)));
		} while (!contains(v, rand));
		return v;
	}
}
