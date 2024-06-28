package acim.client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.JProgressBar;
import javax.swing.JTextPane;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class UsageMonitorFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel lblTimeRemaining;
	private static Color defaultLabelTextColor;

	private UsageMonitorFrame() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(240, 96);
		setLocation(0, 24);
		setUndecorated(true);
		setAlwaysOnTop(true);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		lblTimeRemaining = new JLabel("");
		defaultLabelTextColor = lblTimeRemaining.getForeground();
		lblTimeRemaining.setHorizontalAlignment(SwingConstants.CENTER);
		lblTimeRemaining.setFont(new Font("Dialog", Font.BOLD, 16));
		contentPane.add(lblTimeRemaining, BorderLayout.CENTER);
	}
	
	private static UsageMonitorFrame frame;
	private static UsageUpdateThread thread;
	public static void showFrame() {
		if (frame == null)
			frame = new UsageMonitorFrame();
		frame.setVisible(true);
		frame.lblTimeRemaining.setForeground(defaultLabelTextColor);
	}
	
	public static void hideFrame() {
		if (frame == null)
			return;
		frame.setVisible(false);
		frame.lblTimeRemaining.setForeground(defaultLabelTextColor);
		interruptUpdateThread();
	}
	
	public static void createUpdateThread(long seconds) {
		interruptUpdateThread();
		
		thread = new UsageUpdateThread(seconds);
		thread.start();
	}
	
	public static void interruptUpdateThread() {
		if (thread != null && !thread.isInterrupted())
			thread.interrupt();
	}
	
	public static void updateRemainingSeconds(long newSeconds) {
		if (thread != null) {
			thread.setSeconds(newSeconds);
		}
	}
	
	private static class UsageUpdateThread extends Thread {
		private long seconds, startMillis, endMillis, secondsRemaining;
		private UsageUpdateThread(long beginSeconds) {
			setSeconds(beginSeconds);
		}
		private void setSeconds(long newSeconds) {
			seconds = newSeconds;
			startMillis = System.currentTimeMillis();
			secondsRemaining = seconds - 3;
			endMillis = startMillis + (seconds * 1000);
		}
		@Override
		public void run() {
			try {
				while (System.currentTimeMillis() <= endMillis) {
					sleep(1000);

					long minutesTime = Math.floorDiv(secondsRemaining, 60);
					long hoursTime = Math.floorDiv(minutesTime, 60);
					minutesTime %= 60;
					long secondsTime = secondsRemaining % 60;
					
					if (secondsRemaining <= 60) {
						float normalTextBrightness = secondsRemaining / 60.0f;
						normalTextBrightness *= normalTextBrightness;
						float redBrightness = 1.0f - normalTextBrightness;
						Color color = new Color(
							(int) Math.max((redBrightness * 255) +
									(normalTextBrightness * defaultLabelTextColor.getRed()), 255),
							(int) (normalTextBrightness * defaultLabelTextColor.getGreen()),
							(int) (normalTextBrightness * defaultLabelTextColor.getBlue())
						);
						frame.lblTimeRemaining.setForeground(color);
					} else {
						frame.lblTimeRemaining.setForeground(defaultLabelTextColor);
					}
					
					String hold = "<html>Time remaining is<br>";
					if (hoursTime > 0)
						hold += hoursTime + " hours,<br>";
					
					hold += minutesTime + " minutes and<br>" + secondsTime + " seconds.</html>";

					frame.lblTimeRemaining.setText(hold);
					if (secondsRemaining > 0)
						secondsRemaining--;
					
					frame.setLocation(0, 24);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
