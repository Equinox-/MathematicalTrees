package com.pi.senior.space.gen;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import com.pi.senior.math.Vector3D;

public class TextualEllipsoidEnvelope implements Envelope {
	public enum PopulationSchema {
		FILL, UMBRELLA;
	}

	private Vector3D bottom;
	private Vector3D size;
	private PopulationSchema populationSchema;

	private float randomShellVariance = 0.25f;
	private float randomDistributionVariance = 0.5f;

	private BufferedImage maskX;
	private BufferedImage maskY;
	private BufferedImage maskZ;

	private static void centeredString(BufferedImage img, String val,
			boolean rotate, int pad) {
		Graphics2D g = img.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		g.setColor(Color.WHITE);
		g.rotate(rotate ? Math.PI / 2 : 0, img.getWidth() / 2,
				img.getHeight() / 2);
		if (val.length() == 0) {
			return;
		}
		g.setFont(new Font("Aniron", Font.BOLD, (int) ((rotate ? img.getWidth() - pad
				* 2 : img.getHeight() - pad * 2) / val.length())));
		Rectangle2D rect = g.getFontMetrics().getStringBounds(val, g);
		g.drawString(
				val,
				(int) Math.round(((rotate ? img.getHeight() : img.getWidth()) / 2.0)
						- ((rotate ? rect.getHeight() : rect.getWidth()) / 2.0)),
				(int) Math.round(((rotate ? img.getWidth() : img.getHeight()) / 2.0)
						+ ((rotate ? rect.getWidth() : rect.getHeight()) / 3.5)));
		g.dispose();
	}

	public TextualEllipsoidEnvelope(Vector3D bottom, Vector3D size,
			PopulationSchema populationSchema) {
		this.size = size;
		this.bottom = bottom;
		this.populationSchema = populationSchema;

		this.maskX = new BufferedImage((int) Math.ceil(25 * size.y),
				(int) Math.ceil(25 * size.z), BufferedImage.TYPE_BYTE_BINARY);
		centeredString(maskX, "Maybe", maskX.getHeight() > maskX.getWidth(), 50);
		try {
			ImageIO.write(maskX, "PNG", new File("/tmp/maskX.png"));
		} catch (IOException e) {
		}

		this.maskY = new BufferedImage((int) Math.ceil(25 * size.x),
				(int) Math.ceil(25 * size.z), BufferedImage.TYPE_BYTE_BINARY);
		centeredString(maskY, "Hey", maskY.getHeight() > maskY.getWidth(), 50);
		try {
			ImageIO.write(maskY, "PNG", new File("/tmp/maskY.png"));
		} catch (IOException e) {
		}

		this.maskZ = new BufferedImage((int) Math.ceil(25 * size.x),
				(int) Math.ceil(25 * size.y), BufferedImage.TYPE_BYTE_BINARY);
		centeredString(maskZ, "Much Yes", maskZ.getHeight() > maskZ.getWidth(), 50);
		try {
			ImageIO.write(maskZ, "PNG", new File("/tmp/maskZ.png"));
		} catch (IOException e) {
		}
	}

	public TextualEllipsoidEnvelope setRandomParameters(float shellVariance,
			float distVariance) {
		this.randomShellVariance = shellVariance;
		this.randomDistributionVariance = distVariance;
		return this;
	}

	@Override
	public boolean contains(Vector3D v, Random rand) {
		int imgX = (int) Math.floor(Math.abs(v.x - bottom.x + size.x) * 12.5);
		int imgY = (int) Math
				.floor(Math.abs(v.y
						- bottom.y
						+ (populationSchema == PopulationSchema.UMBRELLA ? 0
								: size.y))
						* (populationSchema == PopulationSchema.UMBRELLA ? 25.0
								: 12.5));
		int imgZ = (int) Math.floor(Math.abs(v.z - bottom.z + size.z) * 12.5);

		if (imgY >= maskX.getWidth() || imgX >= maskY.getWidth()
				|| imgZ >= maskX.getHeight()) {
			return false;
		}
		Color a = new Color(maskX.getRGB(imgY, imgZ));
		Color b = new Color(maskY.getRGB(imgX, imgZ));
		Color c = new Color(maskZ.getRGB(imgX, imgY));
		if (a.getRed() != 0 || b.getRed() != 0 || c.getRed() != 0) {
			return false;
		}

		float dy = (v.y - bottom.y) / size.y;
		float dx = (v.x - bottom.x) / (size.x * (1 - dy));
		float dz = (v.z - bottom.z) / (size.z * (1 - dy));
		float distSquared = (dx * dx) + (dz * dz)
				+ ((rand.nextFloat() - 0.5f) * randomShellVariance);
		switch (populationSchema) {
		case UMBRELLA:
			return distSquared <= 1.0f
					&& distSquared >= (0.9f - Math.abs(rand.nextGaussian()
							* randomDistributionVariance));
		case FILL:
		default:
			return distSquared <= 1.0f;
		}
	}

	@Override
	public Vector3D nextRandom(Random rand) {
		Vector3D v;
		do {
			float xRand = (rand.nextFloat() * 2f) - 1f;
			float yRand = rand.nextFloat();
			float zRand = (rand.nextFloat() * 2f) - 1f;
			v = new Vector3D(bottom.x + (xRand * (1 - yRand) * size.x),
					bottom.y + (yRand * size.y), bottom.z
							+ (zRand * (1 - yRand) * size.z));
		} while (!contains(v, rand));
		return v;
	}
}
