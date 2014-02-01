package com.pi.senior.space.gen;

import java.util.Random;

import com.pi.senior.math.Vector3D;

public class ConicalEnvelope implements Envelope {
	public enum PopulationSchema {
		FILL, UMBRELLA;
	}

	private Vector3D bottom;
	private Vector3D size;
	private PopulationSchema populationSchema;
	
	private float randomShellVariance = 0.25f;
	private float randomDistributionVariance = 0.5f;

	public ConicalEnvelope(Vector3D bottom, Vector3D size,
			PopulationSchema populationSchema) {
		this.size = size;
		this.bottom = bottom;
		this.populationSchema = populationSchema;
	}
	
	public ConicalEnvelope setRandomParameters(float shellVariance, float distVariance) {
		this.randomShellVariance = shellVariance;
		this.randomDistributionVariance = distVariance;
		return this;
	}

	@Override
	public boolean contains(Vector3D v, Random rand) {
		float dy = (v.y - bottom.y) / size.y;
		float dx = (v.x - bottom.x) / (size.x * (1 - dy));
		float dz = (v.z - bottom.z) / (size.z * (1 - dy));
		float distSquared = (dx * dx) + (dz * dz)
				+ ((rand.nextFloat() - 0.5f) * randomShellVariance);
		switch (populationSchema) {
		case UMBRELLA:
			return distSquared <= 1.0f
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
			float yRand = rand.nextFloat();
			float zRand = (rand.nextFloat() * 2f) - 1f;
			v = new Vector3D(bottom.x + (xRand * (1 - yRand) * size.x), bottom.y
					+ (yRand * size.y), bottom.z
					+ (zRand * (1 - yRand) * size.z));
		} while (!contains(v, rand));
		return v;
	}
}
