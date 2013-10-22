package com.pi.senior.space.renderer;

import org.lwjgl.opengl.GL11;

import com.pi.senior.space.AnaglyphConfiguration;

public class StereoCamera {
	public StereoCamera(float FOV, float nearClippingDistance,
			float farClippingDistance) {
		tanFOV = (float) Math.tan(FOV * Math.PI / 180.0f / 2f);
		mNearClippingDistance = nearClippingDistance;
		mFarClippingDistance = farClippingDistance;
	}

	void applyLeftFrustum(float aspect) {
		float top, bottom, left, right;

		top = mNearClippingDistance * tanFOV;
		bottom = -top;

		float a = aspect * tanFOV
				* AnaglyphConfiguration.STEREO_ANAGLYPH_CONVERGENCE;

		float b = a - AnaglyphConfiguration.STEREO_ANAGLYPH_SEPARATION / 2;
		float c = a + AnaglyphConfiguration.STEREO_ANAGLYPH_SEPARATION / 2;

		left = -b * mNearClippingDistance
				/ AnaglyphConfiguration.STEREO_ANAGLYPH_CONVERGENCE;
		right = c * mNearClippingDistance
				/ AnaglyphConfiguration.STEREO_ANAGLYPH_CONVERGENCE;

		// Set the Projection Matrix
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glFrustum(left, right, bottom, top, mNearClippingDistance,
				mFarClippingDistance);

		// Displace the world to right
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(AnaglyphConfiguration.STEREO_ANAGLYPH_SEPARATION / 2,
				0.0f, 0.0f);
	}

	void applyRightFrustum(float aspect) {
		float top, bottom, left, right;

		top = mNearClippingDistance * tanFOV;
		bottom = -top;

		float a = aspect * tanFOV
				* AnaglyphConfiguration.STEREO_ANAGLYPH_CONVERGENCE;

		float b = a - AnaglyphConfiguration.STEREO_ANAGLYPH_SEPARATION / 2;
		float c = a + AnaglyphConfiguration.STEREO_ANAGLYPH_SEPARATION / 2;

		left = -c * mNearClippingDistance
				/ AnaglyphConfiguration.STEREO_ANAGLYPH_CONVERGENCE;
		right = b * mNearClippingDistance
				/ AnaglyphConfiguration.STEREO_ANAGLYPH_CONVERGENCE;

		// Set the Projection Matrix
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glFrustum(left, right, bottom, top, mNearClippingDistance,
				mFarClippingDistance);

		// Displace the world to left
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(
				-AnaglyphConfiguration.STEREO_ANAGLYPH_SEPARATION / 2, 0.0f,
				0.0f);
	}

	public boolean isStereo() {
		return AnaglyphConfiguration.STEREO_ANAGLYPH_SEPARATION > 0.0;
	}

	private float tanFOV;
	private float mNearClippingDistance;
	private float mFarClippingDistance;
};