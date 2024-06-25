package acim.client;

import java.awt.*;
import java.io.*;
import java.net.*;

import javax.swing.*;

public class ServerConnectionManager {
	public static Socket askForTargetSocket() {
		Socket socket = null;
		
		while (socket == null) {
			JTextField txtIpAddress = new JTextField(5);
			JTextField txtPort = new JTextField(5);
			
			JPanel panel = new JPanel();
			
			// Make sure the elements added below are in one column.
			panel.setLayout(new GridLayout(0, 1));
			
			panel.add(new JLabel("It seems this is your first time connecting to a server."));
			panel.add(new JLabel("Enter server information: "));
			panel.add(Box.createHorizontalStrut(15));
			panel.add(new JLabel("Server IP Address / Hostname: "));
			panel.add(txtIpAddress);
			panel.add(Box.createHorizontalStrut(15));
			panel.add(new JLabel("Server Port: "));
			panel.add(txtPort);
			
			// Show dialog box.
			JOptionPane.showMessageDialog(null, panel, "Server Information", JOptionPane.OK_OPTION);
			
			try {
				socket = new Socket(txtIpAddress.getText(), Integer.parseInt(txtPort.getText()));
			} catch (NumberFormatException e) {
				if (txtIpAddress.getText().trim().isEmpty() ||
						txtPort.getText().trim().isEmpty())
					JOptionPane.showMessageDialog(null, "<html>You have provided empty input/s.<br>Please enter valid input only.<html>", e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
				else
					JOptionPane.showMessageDialog(null, "<html>Your input contains invalid characters.<br>Please enter valid input only.<html>", e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			} catch (UnknownHostException e) {
				JOptionPane.showMessageDialog(null, "Unknown host error: " + e.getLocalizedMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "I/O error: " + e.getLocalizedMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		}
		return socket;
	}
}
