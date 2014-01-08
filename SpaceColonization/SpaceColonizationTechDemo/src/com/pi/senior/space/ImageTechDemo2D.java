package com.pi.senior.space;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ImageTechDemo2D extends JFrame implements MouseListener {
	private CharacterRenderer imageRenderer;

	public static void main(String[] args) throws IOException {
		new ImageTechDemo2D(null);
	}

	public ImageTechDemo2D(File image) throws IOException {
		super("Image Tech Demo 2D");
		setSize(1366, 600);
		setVisible(true);
		Graphics g2 = getGraphics();
		FontChooser chooser = new FontChooser(this);
		chooser.show();
		Font f = chooser.getSelectedFont();//new Font(JOptionPane.showInputDialog(this, ), Font.PLAIN, 200);
		g2.setFont(f.deriveFont(500f));
		imageRenderer = new CharacterRenderer(JOptionPane.showInputDialog(this,
				"What do you want to say?"), g2, -0.075f, 125f);// ImageIO.read(image));

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
		ImageIO.write(imageRenderer.getOutput(), "PNG", new File("majorOutput.png"));
		try {
			Desktop.getDesktop().open(new File("majorOutput.png"));
		} catch (Exception e) {
		}
		dispose();
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
