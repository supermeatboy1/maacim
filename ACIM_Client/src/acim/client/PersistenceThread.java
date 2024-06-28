package acim.client;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class PersistenceThread extends Thread {
	private boolean persisting = false;
	private JFrame frame;
	
	public PersistenceThread(JFrame frame) {
		this.frame = frame;
		persisting = true;
	}
	
	public void run() {
		Robot robot;
		try {
			robot = new Robot();
			while (persisting) {
				robot.keyRelease(KeyEvent.VK_ALT);
				robot.keyRelease(KeyEvent.VK_CONTROL);
				//robot.keyRelease(KeyEvent.VK_WINDOWS);
				robot.keyRelease(KeyEvent.VK_TAB);
				//frame.requestFocusInWindow();
				Thread.sleep(80);
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
			return;
		}
	}
	
	public void stopPersisting() { persisting = false; }
}
