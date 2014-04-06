package com.pi.senior.budfate.impl.light;

import com.pi.senior.budfate.tree.TreeVoxel;
import com.pi.senior.math.Vector3D;
import com.pi.senior.math.VoxelGrid;

public class LightGradientUtil {
	public static Vector3D getShadowGradient(VoxelGrid<TreeVoxel> grid,
			Vector3D vec) {
		TreeVoxel at = grid.getVoxel(vec.x, vec.y, vec.z);
		TreeVoxel[][] dirs = {
				{ grid.getVoxel(vec.x - grid.getGridSize(), vec.y, vec.z), at,
						grid.getVoxel(vec.x + grid.getGridSize(), vec.y, vec.z) },
				{ grid.getVoxel(vec.x, vec.y - grid.getGridSize(), vec.z), at,
						grid.getVoxel(vec.x, vec.y + grid.getGridSize(), vec.z) },
				{ grid.getVoxel(vec.x, vec.y, vec.z - grid.getGridSize()), at,
						grid.getVoxel(vec.x, vec.y, vec.z + grid.getGridSize()) } };
		float[] slope = new float[3];
		for (int i = 0; i < dirs.length; i++) {
			float slopes = 0;
			for (int j = 0; j <= 1; j++) {
				float nextLevel = dirs[i][j + 1] != null ? dirs[i][j + 1].shadowLevel
						: 0;
				float thisLevel = dirs[i][j] != null ? dirs[i][j].shadowLevel
						: 0;
				slopes += (nextLevel - thisLevel) / grid.getGridSize();
			}
			slope[i] = slopes / 2.0f;
		}
		return new Vector3D(slope[0], slope[1], slope[2]);
	}

	public static void applyShadow(VoxelGrid<TreeVoxel> grid,
			Vector3D position, float initialShadow, float degredation) {
		float depth = 0;
		while (true) {
			float intensity = (float) (initialShadow * Math.pow(degredation,
					depth * grid.getGridSize()));
			if (Math.abs(intensity) < 0.1
					|| position.z - (depth * grid.getGridSize()) < -2) {
				break;
			}
			for (float x = position.x - (depth * grid.getGridSize()); x <= position.x
					+ (depth * grid.getGridSize()); x += grid.getGridSize()) {
				for (float y = position.y - (depth * grid.getGridSize()); y <= position.y
						+ (depth * grid.getGridSize()); y += grid.getGridSize()) {
					TreeVoxel voxel = grid.getVoxel(x, y, position.z
							- (depth * grid.getGridSize()));
					if (voxel == null) {
						voxel = new TreeVoxel();
						grid.putVoxel(x, y,
								position.z - (depth * grid.getGridSize()),
								voxel);
					}
					voxel.shadowLevel += intensity;
				}
			}
			depth++;
		}
	}
}
