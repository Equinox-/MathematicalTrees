package com.pi.senior.space;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SpaceTechDemo2D extends JFrame implements MouseListener {
	private CharacterRenderer[] renderers;

	private LinkedBlockingQueue<Runnable> poolQueue = new LinkedBlockingQueue<Runnable>();
	private ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(3, 8,
			100L, TimeUnit.MILLISECONDS, poolQueue);
	private final String render;

	public static void main(String[] args) {
		if (args.length == 0) {
			args = new String[] { JOptionPane
					.showInputDialog("Enter your message.") };
			args[0] = args[0].replace("\\n", "\n");
		}
		new SpaceTechDemo2D(args[0]);//.dispose();
	}

	public SpaceTechDemo2D(String render) {
		super("Space Tech Demo 2D");
		this.render = render;
		setSize(1366, 600);
		setVisible(true);

		renderers = new CharacterRenderer[render.length()];
		Graphics g = getGraphics();
		g.setFont(getFont().deriveFont(200f));
		for (int i = 0; i < renderers.length; i++) {
			renderers[i] = new CharacterRenderer(Character.valueOf(render
					.charAt(i)), g);
			System.out.println("Created renderer for: " + render.charAt(i)
					+ " (" + (i + 1) + "/" + renderers.length + ")");
		}
		g.dispose();

		//update(1000);
		repaint();
		addMouseListener(this);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	public void update(final int maxSteps) {
		List<Future<?>> tasks = new ArrayList<Future<?>>(renderers.length);
		for (CharacterRenderer tmp : renderers) {
			final CharacterRenderer rr = tmp;
			tasks.add(poolExecutor.submit(new Runnable() {
				public void run() {
					for (int i = 0; i < maxSteps && !rr.isComplete(); i++) {
						rr.update();
					}
					rr.paint();
				}
			}));
		}
		for (Future<?> task : tasks) {
			try {
				task.get();
			} catch (Exception e) {
			}
		}
	}

	private void renderText(Graphics gg) {
		int left = CharacterRenderer.MARGINS;
		int yOff = 0;
		int maxRowHeight = 0;
		for (CharacterRenderer rr : renderers) {
			if (rr != null) {
				if (rr.getCharacter().charValue() == '\n') {
					yOff += maxRowHeight;
					maxRowHeight = 0;
					left = CharacterRenderer.MARGINS;
				} else {
					gg.drawImage(rr.getOutput(), left
							- CharacterRenderer.MARGINS
							- CharacterRenderer.ASSUME_PADDING, yOff, null);
					maxRowHeight = Math.max(maxRowHeight, rr.getOutput()
							.getHeight() - (CharacterRenderer.MARGINS * 2));
					left += rr.getOutput().getWidth()
							- (CharacterRenderer.MARGINS * 2)
							- (CharacterRenderer.ASSUME_PADDING * 2);
				}
			}
		}
	}

	public void paint(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		Graphics2D gg = ((Graphics2D) g);
		renderText(gg);
	}

	public void dispose() {
		super.dispose();
		// Write out!
		int width = 0;
		int height = 0;
		int tWidth = 0;
		int tHeight = 0;
		for (CharacterRenderer rr : renderers) {
			if (rr != null) {
				if (rr.getCharacter().charValue() == '\n') {
					width = Math.max(width, tWidth);
					tWidth = 0;
					height += tHeight;
					tHeight = 0;
				} else {
					tWidth += rr.getOutput().getWidth()
							- (CharacterRenderer.MARGINS * 2)
							- (CharacterRenderer.ASSUME_PADDING * 2);
					tHeight = Math.max(rr.getOutput().getHeight()
							- (CharacterRenderer.MARGINS * 2), tHeight);
				}
			}
		}
		width = Math.max(width, tWidth);
		height += tHeight;

		BufferedImage output = new BufferedImage(width
				+ (CharacterRenderer.MARGINS * 2), height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D ggA = (Graphics2D) output.getGraphics();
		ggA.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
		ggA.fillRect(0, 0, output.getWidth(), output.getHeight());
		ggA.dispose();

		Graphics g = output.getGraphics();
		Graphics2D gg = ((Graphics2D) g);
		renderText(gg);
		g.dispose();
		String qName = render.replace("\n", "").replace("/", "");
		String fname = (qName.length() > 10 ? qName.substring(0, 15) : qName)
				.concat(".png");
		try {
			ImageIO.write(output, "PNG", new File(fname));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Wrote output as " + fname);
		System.exit(0);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//for (int i = 0; i < 10; i++) {
			update(3);
			repaint();
		//}
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
