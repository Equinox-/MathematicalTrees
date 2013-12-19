package com.pi.senior.space;

import com.pi.senior.math.Vector;
import com.pi.senior.space.gen.ConicalEnvelope;
import com.pi.senior.space.gen.EllipsoidEnvelope;
import com.pi.senior.space.gen.Envelope;

public class Configuration {
	public static Envelope createEnvelope() {
		if (envelopeType.toLowerCase().contains("con")) {
			ConicalEnvelope.PopulationSchema schema = ConicalEnvelope.PopulationSchema.FILL;
			try {
				schema = ConicalEnvelope.PopulationSchema
						.valueOf(populationSchema.toUpperCase());
			} catch (Exception e) {
			}
			return new ConicalEnvelope(new Vector(envelopeBaseX, envelopeBaseY,
					envelopeBaseZ), new Vector(envelopeSizeX, envelopeSizeY,
					envelopeSizeZ), schema).setRandomParameters(
					envelopeShellVariance, envelopeDistributionVariance);
		} else {
			EllipsoidEnvelope.PopulationSchema schema = EllipsoidEnvelope.PopulationSchema.FILL;
			try {
				schema = EllipsoidEnvelope.PopulationSchema
						.valueOf(populationSchema.toUpperCase());
			} catch (Exception e) {
			}
			return new EllipsoidEnvelope(new Vector(envelopeBaseX,
					envelopeBaseY, envelopeBaseZ), new Vector(envelopeSizeX,
					envelopeSizeY, envelopeSizeZ), schema).setRandomParameters(
					envelopeShellVariance, envelopeDistributionVariance);
		}
	}

	public static float envelopeBaseX = 0, envelopeBaseY = 0,
			envelopeBaseZ = 0;
	public static float envelopeSizeX = 25, envelopeSizeY = 10,
			envelopeSizeZ = 25;
	public static float envelopeShellVariance = 0.25f;
	public static float envelopeDistributionVariance = 0.5f;
	public static String envelopeType = "ELLIPSOID";
	public static String populationSchema = "UMBRELLA";

	public static float ATTRACTOR_COUNT_DEGREDATION = 0.5f;
	public static float ATTRACTOR_KILLER_DEGREDATION = 0.5f;

	public static int ATTRACTOR_COUNT = 5000;
	public static float ATTRACTOR_KILL_RADIUS_SQUARED = 2f;
	public static float ATTRACTOR_ATTRACTION_RADIUS_SQUARED = 100;
	public static float INODE_LENGTH = 0.5f;
	public static float IDEAL_BRANCH_SLOPE = 0.5f;

	public static float RADI_PER_CROSS_SECTION = 0.035f;
	public static float TIP_CROSS_SECTION = 1f;
	public static float ACCUM_CROSS_SECTION = 0.1f;
	public static float NODE_CHILD_TOLERANCE = 0.1f;

	public static float TROPISM_WEIGHT = 0f;
	public static float DIVERGENCE_WEIGHT = 0f;

	public static float OUTSIDE_ENVELOPE_ATTRACTOR_TOLERANCE = 100;
}
