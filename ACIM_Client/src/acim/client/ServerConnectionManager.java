package acim.client;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import javax.swing.*;

public class ServerConnectionManager {
	public static Socket askForTargetSocket() {
		String ipAddress = null;
		int port = 0;

		// Load connection information.
		File file = new File("Server.txt");
		try {
			if (file.exists()) {
				Scanner scan;
				scan = new Scanner(file);

				ipAddress = scan.nextLine();
				port = Integer.parseInt(scan.nextLine());

				scan.close();
			}
		} catch (FileNotFoundException e) {}

		Socket socket = null;

		while (socket == null) {
			JTextField txtIpAddress = new JTextField(5);
			JTextField txtPort = new JTextField(5);
			
			try {
				txtIpAddress.setText(ipAddress);
				txtPort.setText("" + port);

				JPanel panel = new JPanel();

				// Make sure the elements added below are in one column.
				panel.setLayout(new GridLayout(0, 1));

				if (!file.exists())
					panel.add(new JLabel("It seems this is your first time connecting to a server."));
				panel.add(new JLabel("Please enter server information: "));
				panel.add(Box.createHorizontalStrut(15));
				panel.add(new JLabel("Server IP Address / Hostname: "));
				panel.add(txtIpAddress);
				panel.add(Box.createHorizontalStrut(15));
				panel.add(new JLabel("Server Port: "));
				panel.add(txtPort);

				// Show dialog box.
				if (JOptionPane.showConfirmDialog(null, panel, "Server Information", JOptionPane.OK_CANCEL_OPTION) !=
						JOptionPane.OK_OPTION) {
					System.exit(0);
				}

				ipAddress = txtIpAddress.getText();
				port = Integer.parseInt(txtPort.getText());

				socket = new Socket(ipAddress, port);
				
				// Save/replace connection information.
				if (file.exists())
					file.delete();
				
				Files.write(Paths.get("Server.txt"), (ipAddress + "\r\n" + port + "\r\n"
						).getBytes(), StandardOpenOption.CREATE_NEW);
			}  catch (NumberFormatException e) {
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
