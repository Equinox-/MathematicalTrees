package com.pi.senior.space.renderer;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.pi.senior.math.Vector;

public class LeafVertexObject implements Renderable {
	private ByteBuffer indexBuffer;
	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer;
	private FloatBuffer normalBuffer;

	public LeafVertexObject(int slices, Vector start, Vector direction,
			Color color) {
		Vector up = new Vector(0, 1, 0);
		Vector crossed = Vector.crossProduct(direction, up).normalize();
		direction.normalize();

		vertexBuffer = BufferUtils.createFloatBuffer(slices * 3);
		normalBuffer = BufferUtils.createFloatBuffer(slices * 3);
		colorBuffer = BufferUtils.createFloatBuffer(slices * 2 * 4);
		indexBuffer = BufferUtils.createByteBuffer((slices + 1) * 2);

		for (int i = 0; i < slices; i++) {
			float angle = (float) (i * (Math.PI * 2.0 / slices));
			float x = ((float) (Math.cos(angle) * 2.0) + 2.0f) / 5f;
			float y = (float) Math.sin(angle) / 5f;
			Vector pos = start.clone().add(crossed.clone().multiply(y))
					.add(direction.clone().multiply(x));
			vertexBuffer.put(new float[] { pos.x, pos.y, pos.z });
			normalBuffer.put(new float[] { up.x, up.y, up.z });
			colorBuffer.put(new float[] { color.getRed() / 255f,
					color.getGreen() / 255f, color.getBlue() / 255f,
					color.getAlpha() / 255f });
			indexBuffer.put((byte) i);
		}

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
		GL11.glDrawElements(GL11.GL_POLYGON, indexBuffer);

		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
	}
}
