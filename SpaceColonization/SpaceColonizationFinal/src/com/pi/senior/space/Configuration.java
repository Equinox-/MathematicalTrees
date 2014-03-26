package com.pi.senior.space;

import com.pi.senior.math.Vector3D;
import com.pi.senior.space.gen.ConicalEnvelope;
import com.pi.senior.space.gen.EllipsoidEnvelope;
import com.pi.senior.space.gen.Envelope;
import com.pi.senior.space.gen.TextualEllipsoidEnvelope;

public class Configuration {
	public static Envelope createEnvelope() {
		if (envelopeType.toLowerCase().contains("con")) {
			ConicalEnvelope.PopulationSchema schema = ConicalEnvelope.PopulationSchema.FILL;
			try {
				schema = ConicalEnvelope.PopulationSchema
						.valueOf(populationSchema.toUpperCase());
			} catch (Exception e) {
			}
			return new ConicalEnvelope(new Vector3D(envelopeBaseX,
					envelopeBaseY, envelopeBaseZ), new Vector3D(envelopeSizeX,
					envelopeSizeY, envelopeSizeZ), schema).setRandomParameters(
					envelopeShellVariance, envelopeDistributionVariance);
		} else if (envelopeType.toLowerCase().contains("text")) {
			TextualEllipsoidEnvelope.PopulationSchema schema = TextualEllipsoidEnvelope.PopulationSchema.FILL;
			try {
				schema = TextualEllipsoidEnvelope.PopulationSchema
						.valueOf(populationSchema.toUpperCase());
			} catch (Exception e) {
			}
			return new TextualEllipsoidEnvelope(new Vector3D(envelopeBaseX,
					envelopeBaseY, envelopeBaseZ), new Vector3D(envelopeSizeX,
					envelopeSizeY, envelopeSizeZ), schema).setRandomParameters(
					envelopeShellVariance, envelopeDistributionVariance);
		} else {
			EllipsoidEnvelope.PopulationSchema schema = EllipsoidEnvelope.PopulationSchema.FILL;
			try {
				schema = EllipsoidEnvelope.PopulationSchema
						.valueOf(populationSchema.toUpperCase());
			} catch (Exception e) {
			}
			return new EllipsoidEnvelope(new Vector3D(envelopeBaseX,
					envelopeBaseY, envelopeBaseZ), new Vector3D(envelopeSizeX,
					envelopeSizeY, envelopeSizeZ), schema).setRandomParameters(
					envelopeShellVariance, envelopeDistributionVariance);
		}
	}

	public static float envelopeBaseX = 5, envelopeBaseY = 10,
			envelopeBaseZ = 0;
	public static float envelopeSizeX = 25, envelopeSizeY = 15,
			envelopeSizeZ = 25;
	public static float envelopeShellVariance = 0.725f;
	public static float envelopeDistributionVariance = 0.75f;
	public static String envelopeType = "ELLIPSOID";
	public static String populationSchema = "UMBRELLA";

	public static float ATTRACTOR_COUNT_DEGREDATION = 0.75f;
	public static float ATTRACTOR_KILLER_DEGREDATION = 0.25f;

	public static int ATTRACTOR_COUNT = 5000;
	public static float ATTRACTOR_KILL_RADIUS_SQUARED = 2f;
	public static float ATTRACTOR_ATTRACTION_RADIUS_SQUARED = 100;
	public static float INODE_LENGTH = 0.5f;
	public static float IDEAL_BRANCH_SLOPE = 0.5f;

	public static float RADI_PER_CROSS_SECTION = 0.035f * 3.0f;
	public static float TIP_CROSS_SECTION = 1f;
	public static float ACCUM_CROSS_SECTION = 0.1f;
	public static float NODE_CHILD_TOLERANCE = 0.1f;

	public static float TROPISM_WEIGHT = 10;
	public static float DIVERGENCE_WEIGHT = 10;

	public static float OUTSIDE_ENVELOPE_ATTRACTOR_TOLERANCE = 100;
}
