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
			Vector start, Vector startDirection, Vector end) {
		Vector change = end.clone().subtract(start).normalize();
		Vector rEnd = Vector.crossProduct(change,
				new Vector(0.577350269f, -0.577350269f, 0.577350269f))
				.normalize();
		Vector sEnd = Vector.crossProduct(change, rEnd).normalize();

		Vector rStart = Vector.crossProduct(startDirection,
				new Vector(0.577350269f, -0.577350269f, 0.577350269f))
				.normalize();
		Vector sStart = Vector.crossProduct(startDirection, rStart).normalize();

		vertexBuffer = BufferUtils.createFloatBuffer(slices * 2 * 3);
		normalBuffer = BufferUtils.createFloatBuffer(slices * 2 * 3);
		colorBuffer = BufferUtils.createFloatBuffer(slices * 2 * 4);
		indexBuffer = BufferUtils.createByteBuffer((slices + 1) * 2);

		byte index = 0;
		for (int i = 0; i < slices; i++) {
			double theta = Math.PI * 2f * ((double) i) / ((double) slices);
			float cos = (float) Math.cos(theta);
			float sin = (float) Math.sin(theta);

			float oSX = (cos * rStart.x) + (sin * sStart.x);
			float oSY = (cos * rStart.y) + (sin * sStart.y);
			float oSZ = (cos * rStart.z) + (sin * sStart.z);

			float oEX = (cos * rEnd.x) + (sin * sEnd.x);
			float oEY = (cos * rEnd.y) + (sin * sEnd.y);
			float oEZ = (cos * rEnd.z) + (sin * sEnd.z);

			colorBuffer.put(new float[] { 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f });

			vertexBuffer.put(start.x + (oSX * radStart))
					.put(start.y + (oSY * radStart))
					.put(start.z + (oSZ * radStart));
			normalBuffer.put(oSX).put(oSY).put(oSZ);

			indexBuffer.put(index);
			index++;

			vertexBuffer.put(end.x + (oEX * radEnd))
					.put(end.y + (oEY * radEnd)).put(end.z + (oEZ * radEnd));
			normalBuffer.put(oEX).put(oEY).put(oEZ);

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

	public String getSTL(String solidName) {
		StringBuilder builder = new StringBuilder();
		builder.append("solid " + solidName + "\n");
		for (int i = 0; i < indexBuffer.limit() - 2; i++) {
			builder.append("facet normal ");
			float x = 0, y = 0, z = 0;

			for (int iq = i; iq < i + 3; iq++) {
				int idx = indexBuffer.get(iq) * 3;
				x += normalBuffer.get(idx);
				y += normalBuffer.get(idx + 1);
				z += normalBuffer.get(idx + 2);
			}
			builder.append(x / 3f);
			builder.append(' ');
			builder.append(y / 3f);
			builder.append(' ');
			builder.append(z / 3f);
			builder.append('\n');

			// Triangle loop
			builder.append("outer loop\n");
			for (int iq = i; iq < i + 3; iq++) {
				int idx = indexBuffer.get(iq) * 3;
				builder.append("vertex ");
				for (int iqx = idx; iqx < idx + 3; iqx++) {
					if (iqx != idx) {
						builder.append(' ');
					}
					builder.append(vertexBuffer.get(iqx));
				}
				builder.append('\n');
			}
			builder.append("endloop\nendfacet\n");
		}

		builder.append("endsolid  " + solidName + "\n");
		return builder.toString();
	}
}
