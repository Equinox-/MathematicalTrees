package com.pi.senior.budfate.impl.light;

import com.pi.senior.budfate.api.BudEvolutionScheme;
import com.pi.senior.budfate.tree.MetamerType;
import com.pi.senior.budfate.tree.PositionedMetamer;
import com.pi.senior.budfate.tree.TreeVoxel;
import com.pi.senior.math.Vector3D;
import com.pi.senior.math.VoxelGrid;
import com.pi.senior.world.WorldProvider;

public class LightGradientEvolution implements BudEvolutionScheme {
	private VoxelGrid<TreeVoxel> voxelGrid;
	private Vector3D UP_VECTOR = new Vector3D(0, 0, 1);

	private float metamerLength = .25f;

	public LightGradientEvolution(VoxelGrid<TreeVoxel> voxelGrid) {
		this.voxelGrid = voxelGrid;
	}

	@Override
	public PositionedMetamer getNextMetamer(PositionedMetamer base) {
		Vector3D lightGradient = LightGradientUtil
				.getShadowGradient(voxelGrid, base.getNodeEnd()).normalize()
				.multiply(-1);
		boolean foundTerminal = false;
		int lateral = 0;
		for (PositionedMetamer mm : base.getChildren()) {
			if (mm.getStateInfo().getBudType() == MetamerType.TERMINAL) {
				foundTerminal = true;
			} else {
				lateral++;
			}
		}
		Vector3D parentDir = base.getDirection().normalize();
		if (!foundTerminal) {
			if (!parentDir.equals(UP_VECTOR)) {
				parentDir = Vector3D.slerp(parentDir, UP_VECTOR, 0.1f);
			}
			return new PositionedMetamer(MetamerType.TERMINAL, base, base
					.getNodeEnd().clone(), base.getNodeEnd().clone()
					.add(parentDir.multiply(metamerLength)), false);
		} else if (lateral == 0) {
			Vector3D dir = base.getLocalToWorld().inverse()
					.multiply(base.getNodeEnd().clone().add(lightGradient))
					.subtract(new Vector3D(0, 0, base.getLength()));
			if (Math.acos(Vector3D.dotProduct(dir.clone().normalize(), base
					.getDirection().normalize())) < Math.PI / 5.0) {
				float randDir = (float) (WorldProvider.nextRandom() * Math.PI * 2.0);
				dir = Vector3D.slerp(
						dir,
						new Vector3D((float) Math.cos(randDir), (float) Math
								.sin(randDir), 0f), 0.25f);
			} else {
				dir = Vector3D.slerp(base.getDirection(), dir, 0.75f);
			}
			return new PositionedMetamer(MetamerType.LATERAL, base,
					new Vector3D(0, 0, 0), dir.multiply(metamerLength), true);
		}
		return null;
	}

	@Override
	public float getMetamerCost(PositionedMetamer base, PositionedMetamer child) {
		float divergence = (float) Math
				.abs(Math.acos(Vector3D.dotProduct(base.getDirection()
						.normalize(), child.getDirection().normalize())));
		float upwards = (float) Math.abs(Math.acos(Vector3D.dotProduct(
				new Vector3D(0, 0, 1), child.getDirection().normalize())));
		float minSeparation = (float) (Math.PI);
		for (PositionedMetamer s : base.getChildren()) {
			float angle = (float) Math.abs(Math.acos(Vector3D.dotProduct(s
					.getDirection().normalize(), child.getDirection()
					.normalize())));
			if (s == child) {
				return Float.MAX_VALUE; // Already exists
			}
			if (angle < minSeparation) {
				minSeparation = angle;
			}
		}
		return (float) (divergence + (upwards * 5)
				+ (base.getChildren().size() / minSeparation) + (base
				.getDepth() * base.getDepth() / 2.0));
	}

}
