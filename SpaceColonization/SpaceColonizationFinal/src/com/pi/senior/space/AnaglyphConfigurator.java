package com.pi.senior.space;

import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class AnaglyphConfigurator {
	private JFrame frame;
	private static AnaglyphConfigurator window;

	private Map<JComponent, Field> fieldMap = new HashMap<JComponent, Field>();

	public static void show() {
		window = new AnaglyphConfigurator();
		window.frame.setVisible(true);
	}

	public static void kill() {
		if (window != null && window.frame != null) {
			window.frame.dispose();
		}
	}

	/**
	 * Create the application.
	 */
	public AnaglyphConfigurator() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Anaglyph Config");
		frame.setBounds(100, 100, 250, 100);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(6, 1, 0, 0));

		// Dynamically generate contents
		try {
			Field[] fields = AnaglyphConfiguration.class.getFields();
			for (Field f : fields) {
				Class<?> type = f.getType();
				if (type.getSimpleName().equals("boolean")) {
					frame.getContentPane().add(new JLabel(f.getName()));
					JRadioButton btn = new JRadioButton();
					btn.setSelected(f.getBoolean(null));
					fieldMap.put(btn, f);
					frame.getContentPane().add(btn);
				} else if (type.getSimpleName().equals("float")) {
					frame.getContentPane().add(new JLabel(f.getName()));
					JTextField btn = new JTextField();
					btn.setText(String.valueOf(f.getFloat(null)));
					fieldMap.put(btn, f);
					frame.getContentPane().add(btn);
				} else if (type.getSimpleName().equals("int")) {
					frame.getContentPane().add(new JLabel(f.getName()));
					JTextField btn = new JTextField();
					btn.setText(String.valueOf(f.getInt(null)));
					fieldMap.put(btn, f);
					frame.getContentPane().add(btn);
				} else if (type.getSimpleName().equals("String")) {
					frame.getContentPane().add(new JLabel(f.getName()));
					JTextField btn = new JTextField();
					btn.setText(String.valueOf(f.get(null)));
					fieldMap.put(btn, f);
					frame.getContentPane().add(btn);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		JButton submit = new JButton("Set");
		submit.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				store();
			}
		});
		frame.getContentPane().add(submit);
		frame.pack();
	}

	public void store() {
		for (Entry<JComponent, Field> et : fieldMap.entrySet()) {
			try {
				if (et.getValue().getType().getSimpleName().equals("float")) {
					et.getValue()
							.setFloat(
									null,
									Float.valueOf(((JTextField) et.getKey())
											.getText()));
				} else if (et.getValue().getType().getSimpleName()
						.equals("int")) {
					et.getValue().setInt(
							null,
							Integer.valueOf(((JTextField) et.getKey())
									.getText()));
				} else if (et.getValue().getType().getSimpleName()
						.equals("String")) {
					et.getValue().set(null,
							((JTextField) et.getKey()).getText());
				} else if (et.getValue().getType().getSimpleName()
						.equals("boolean")) {
					et.getValue().setBoolean(null,
							((JRadioButton) et.getKey()).isSelected());
				}
			} catch (Exception e) {
			}
		}
	}
}
