package com.pi.senior.math;

public class VoxelGrid<T> {
	private float gridSize;
	private Object[][][] dataArray;
	private Vector3D minimumPoint;

	public VoxelGrid(float gridSize, int initialSize) {
		this.gridSize = gridSize;
		this.minimumPoint = new Vector3D(0, 0, 0);
		dataArray = new Object[1][1][1];
		ensureLocation(initialSize, initialSize, initialSize);
	}

	@SuppressWarnings("unchecked")
	public T getVoxel(float x, float y, float z) {
		int vX = (int) Math.floor((x - minimumPoint.x) / gridSize);
		int vY = (int) Math.floor((y - minimumPoint.y) / gridSize);
		int vZ = (int) Math.floor((z - minimumPoint.z) / gridSize);
		if (vX >= 0 && vY >= 0 && vZ >= 0 && vX < dataArray.length
				&& vY < dataArray[vX].length && vZ < dataArray[vX][vY].length) {
			return (T) dataArray[vX][vY][vZ];
		}
		return null;
	}

	public void putVoxel(float x, float y, float z, T obj) {
		ensureLocation(x, y, z);
		int vX = (int) Math.floor((x - minimumPoint.x) / gridSize);
		int vY = (int) Math.floor((y - minimumPoint.y) / gridSize);
		int vZ = (int) Math.floor((z - minimumPoint.z) / gridSize);
		dataArray[vX][vY][vZ] = obj;
	}

	private void ensureLocation(float x, float y, float z) {
		int vOX = (int) Math.floor((x - minimumPoint.x) / gridSize);
		int vOY = (int) Math.floor((y - minimumPoint.y) / gridSize);
		int vOZ = (int) Math.floor((z - minimumPoint.z) / gridSize);
		if (vOX >= 0 && vOY >= 0 && vOZ >= 0 && vOX < dataArray.length
				&& vOY < dataArray[vOX].length
				&& vOZ < dataArray[vOX][vOY].length) {
			return;
		}

		if (minimumPoint.x > x || minimumPoint.y > y || minimumPoint.z > z) {
			// Scale down. First recalculate minPoint
			Vector3D oldMin = minimumPoint;
			Object[][][] oldArray = dataArray;

			int vX = (int) Math.floor(x / gridSize);
			int vY = (int) Math.floor(y / gridSize);
			int vZ = (int) Math.floor(z / gridSize);

			int growX = Math.max(0, 1 + (int) Math.ceil(oldMin.x / gridSize)
					- vX);
			int growY = Math.max(0, 1 + (int) Math.ceil(oldMin.y / gridSize)
					- vY);
			int growZ = Math.max(0, 1 + (int) Math.ceil(oldMin.z / gridSize)
					- vZ);

			dataArray = new Object[oldArray.length + growX][oldArray[0].length
					+ growY][oldArray[0][0].length + growZ];

			for (int vx = 0; vx < oldArray.length; vx++) {
				for (int vy = 0; vy < oldArray[vx].length; vy++) {
					System.arraycopy(oldArray[vx][vy], 0,
							dataArray[vx + growX][vy + growY], growZ,
							oldArray[vx][vy].length);
				}
			}

			minimumPoint = new Vector3D(vX, vY, vZ);
		} else {
			// Scale up
			int xSize = Math.max(dataArray.length,
					(int) Math.ceil((x - minimumPoint.x) / gridSize) + 1);
			int ySize = Math.max(dataArray[0].length,
					(int) Math.ceil((y - minimumPoint.y) / gridSize) + 1);
			int zSize = Math.max(dataArray[0][0].length,
					(int) Math.ceil((z - minimumPoint.z) / gridSize) + 1);
			Object[][][] oldArray = dataArray;
			dataArray = new Object[xSize][ySize][zSize];
			for (int vx = 0; vx < oldArray.length; vx++) {
				for (int vy = 0; vy < oldArray[vx].length; vy++) {
					System.arraycopy(oldArray[vx][vy], 0, dataArray[vx][vy], 0,
							oldArray[vx][vy].length);
				}
			}
		}
	}
}
