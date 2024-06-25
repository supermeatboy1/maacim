package acim.client;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

public class LockFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public LockFrame() {
		super();

		JPanel contentPane = new JPanel();
		Dimension size = new Dimension(640, 480); // Toolkit.getDefaultToolkit().getScreenSize();

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setMinimumSize(size);
		contentPane.setMaximumSize(size);
		contentPane.setPreferredSize(size);

		//setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// https://stackoverflow.com/a/20913713
		// To center the internal panel:
		contentPane.setLayout(new GridBagLayout());
		
		getContentPane().add(contentPane, BorderLayout.CENTER);
		JPanel internal = new LoginPanel();
		contentPane.add(internal);
		pack();
		
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
