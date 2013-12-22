package com.pi.senior.space;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class ImageTechDemo2D extends JFrame implements MouseListener {
	private CharacterRenderer imageRenderer;

	public static void main(String[] args) throws IOException {
		new ImageTechDemo2D(new File(
				"/tmp/1476512_778385558844520_1092994521_n.jpg"));
	}

	public ImageTechDemo2D(File image) throws IOException {
		super("Image Tech Demo 2D");
		setSize(1366, 600);
		setVisible(true);

		imageRenderer = new CharacterRenderer(ImageIO.read(image));

		// update(1000);
		repaint();
		addMouseListener(this);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		while (!imageRenderer.isSortaComplete()) {
			update(5);
			
			Graphics g = getContentPane().getGraphics();
			g.clearRect(0, 0, getWidth(), getHeight());
			Graphics2D gg = ((Graphics2D) g);
			renderText(gg);
		}
		imageRenderer.cleanPaint();
		ImageIO.write(imageRenderer.getOutput(), "PNG",
				new File(image.getAbsolutePath() + "-out.png"));
	}

	public void update(final int maxSteps) {
		for (int i = 0; i < maxSteps && !imageRenderer.isSortaComplete(); i++) {
			imageRenderer.update();
		}
		imageRenderer.cleanPaint();
	}

	private void renderText(Graphics gg) {
		if (imageRenderer != null && imageRenderer.getOutput() != null) {
			float scale = imageRenderer.getOutput().getHeight()
					/ (float) getHeight();
			gg.drawImage(imageRenderer.getOutput(), 0, 0, (int) (imageRenderer
					.getOutput().getWidth() / scale), getHeight(), null);
		}
	}

	public void paint(Graphics g) {
	}

	public void dispose() {
		super.dispose();
		imageRenderer.cleanPaint();
		try {
			ImageIO.write(imageRenderer.getOutput(), "PNG", new File(
					"output.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// for (int i = 0; i < 10; i++) {
		// }
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

}
