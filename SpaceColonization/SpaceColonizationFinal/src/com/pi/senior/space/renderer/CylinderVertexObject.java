package com.pi.senior.space.renderer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.pi.senior.math.Vector;

public class CylinderVertexObject {
	private ByteBuffer indexBuffer;
	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer;
	private FloatBuffer normalBuffer;

	public CylinderVertexObject(float radStart, float radEnd, int slices,
			Vector start, Vector end) {
		Vector change = end.clone().subtract(start).normalize();
		Vector r = Vector.crossProduct(change,
				new Vector(0.577350269f, -0.577350269f, 0.577350269f))
				.normalize();
		Vector s = Vector.crossProduct(change, r).normalize();

		vertexBuffer = BufferUtils.createFloatBuffer(slices * 2 * 3);
		normalBuffer = BufferUtils.createFloatBuffer(slices * 2 * 3);
		colorBuffer = BufferUtils.createFloatBuffer(slices * 2 * 4);
		indexBuffer = BufferUtils.createByteBuffer((slices + 1) * 2);

		byte index = 0;
		for (int i = 0; i < slices; i++) {
			double theta = Math.PI * 2f * ((double) i) / ((double) slices);
			float cos = (float) Math.cos(theta);
			float sin = (float) Math.sin(theta);

			float oX = (cos * r.x) + (sin * s.x);
			float oY = (cos * r.y) + (sin * s.y);
			float oZ = (cos * r.z) + (sin * s.z);

			colorBuffer.put(new float[] { 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f });

			vertexBuffer.put(start.x + (oX * radStart))
					.put(start.y + (oY * radStart))
					.put(start.z + (oZ * radStart));
			normalBuffer.put(oX).put(oY).put(oZ);

			indexBuffer.put(index);
			index++;

			vertexBuffer.put(end.x + (oX * radEnd)).put(end.y + (oY * radEnd))
					.put(end.z + (oZ * radEnd));
			normalBuffer.put(oX).put(oY).put(oZ);

			indexBuffer.put(index);
			index++;
		}

		indexBuffer.put((byte) 0).put((byte) 1);

		normalBuffer.flip();
		vertexBuffer.flip();
		colorBuffer.flip();
		indexBuffer.flip();
	}

	public void render() {
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

		GL11.glVertexPointer(3, 3 << 2, vertexBuffer);
		GL11.glNormalPointer(3 << 2, normalBuffer);
		GL11.glColorPointer(4, 4 << 2, colorBuffer);
		GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, indexBuffer);

		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
	}
}
