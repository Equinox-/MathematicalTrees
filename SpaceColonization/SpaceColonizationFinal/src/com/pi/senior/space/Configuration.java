package com.pi.senior.space;

import com.pi.senior.math.Vector;
import com.pi.senior.space.gen.EllipsoidEnvelope;
import com.pi.senior.space.gen.Envelope;

public class Configuration {
	public static Envelope ENVELOPE = new EllipsoidEnvelope(
			new Vector(0, 15, 0), new Vector(25, 10, 25),
			EllipsoidEnvelope.PopulationSchema.UMBRELLA);

	public static float ATTRACTOR_COUNT_DEGREDATION = 0.5f;
	public static float ATTRACTOR_KILLER_DEGREDATION = 0.5f;

	public static int ATTRACTOR_COUNT = 5000;
	public static float ATTRACTOR_KILL_RADIUS_SQUARED = 2f;
	public static float ATTRACTOR_ATTRACTION_RADIUS_SQUARED = 100;
	public static float INODE_LENGTH = 0.5f;
	public static boolean USE_BIAS_VECTORS = false;
	public static float IDEAL_BRANCH_SLOPE = 0.5f;

	public static float RADI_PER_CROSS_SECTION = 0.035f;
	public static float TIP_CROSS_SECTION = 1f;
	public static float ACCUM_CROSS_SECTION = 0.1f;
	public static float NODE_CHILD_TOLERANCE = 0.1f;
}
