package acim.net;

import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

import acim.data.*;
import acim.gui.ClientPanel;

/**
 * This code deals with individual clients that are connected to the server.
 */
public class ClientConnection {
	private Socket client;

	private BufferedReader reader;
	private PrintWriter writer;
	
	private String ipAddress;
	private int port;
	
	private InputThread inThread;
	private OutputThread outThread;
	private UsageMonitoringThread usageThread = null;
	
	private Queue<String> commandQueue;

	private ClientConnection() {}

	public ClientConnection(Socket client) throws IOException {
		super();
		this.client = client;

		reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		writer = new PrintWriter(client.getOutputStream());
		
		ipAddress = client.getInetAddress().getHostAddress();
		port = client.getPort();
		
		inThread = new InputThread();
		outThread = new OutputThread();
		
		commandQueue = new LinkedList<String>();
	}
	public void startThreads() {
		inThread.start();
		outThread.start();
	}
	public void queueCommand(String command) {
		commandQueue.add(command);
	}
	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof ClientConnection))
			return false;

		ClientConnection otherConn = (ClientConnection) other;
		return otherConn.ipAddress.equals(ipAddress);
	}
	
	public String getIpAddress() { return ipAddress; }

	public void closeConnection() throws IOException {
		client.close();
	}

	private void close() {
		try {
			closeConnection();
			ClientManager.removeClientConnection(this);
			System.out.println("Client disconnected: " + client.getInetAddress().getHostAddress() + ":" + client.getPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class InputThread extends Thread {
		@Override
		public void run() {
			writer.println("Welcome to the server!\r");
			
			try {
				while (client.isConnected()) {
					Thread.sleep(1);
					String input = reader.readLine();
					if (input == null) {
						close();
						return;
					}

					if (input.equals("quit") || input.equals("exit")) {
						close();
						break;
					} else if (input.equals("start receive screenshot")) {
						SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy, HH-mm-ss");
						String date_time_str = sdf.format(new Date());
						
						boolean complete = false;
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						Base64.Decoder decoder = Base64.getUrlDecoder();
						
						while (!complete) {
							Thread.sleep(1);
							String line = reader.readLine();
							if (line.startsWith("chunk length ")) {
								int chunk_length = Integer.parseInt(line.replaceFirst("chunk length ", ""));
								byte[] chunk = decoder.decode(reader.readLine());
								Thread.sleep(1);
								baos.write(chunk, 0, chunk_length);
							} else if (line.startsWith("stop receive screenshot")) {
								complete = true;
								break;
							}
						}
						baos.flush();
						baos.close();
						
						BufferedImage screenshot = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
						
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								JFrame screenshotFrame = new JFrame("[Screenshot] " + date_time_str);
								JPanel panel = new JPanel();
								panel.add(new JLabel(new ImageIcon(screenshot)));

								screenshotFrame.setLocationRelativeTo(null);
								screenshotFrame.add(panel);
								screenshotFrame.pack();
								screenshotFrame.setResizable(false);
								screenshotFrame.setVisible(true);
								screenshotFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							}
						});
					} else if (input.startsWith("message ")) {
						// Display the message through a dialog box.
						JOptionPane.showMessageDialog(null, "<html>" + input.replaceFirst("message ", "") + "</html>",
								"Client (" + ipAddress + ":" + port + ") has a message for you!",
								JOptionPane.INFORMATION_MESSAGE);
					} else if (input.startsWith("login ")) {
						String[] stringArray = input.split(" ");
						String clientUsername = stringArray[1];
						String clientEncodedPassword = stringArray[2];

						Account account = DatabaseManager.getAccountByUsername(clientUsername);
						if (account == null) {
							queueCommand("login fail No account exists with that username.");
						} else if (!account.getEncodedPassword().equals(clientEncodedPassword)) {
							queueCommand("login fail Invalid password.");
						} else if (account.getAvailableMinutes() == 0.0f) {
							queueCommand("login fail Account balance is empty.");
						} else {	
							queueCommand("allow access");
							ClientManager.setClientPanelStatus(ipAddress, ClientPanel.Status.IN_USE);
							
							usageThread = new UsageMonitoringThread(1, account);
							usageThread.start();
						}
					}
				}
			} catch (Exception e) {
				close();
			}
		}
	}

	private class OutputThread extends Thread {
		@Override
		public void run() {
			while (client.isConnected()) {
				// Wait until a command is queued.
				while (commandQueue.isEmpty()) {
					try {
						sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				// Empty all queued commands.
				while (!commandQueue.isEmpty()) {
					String command = commandQueue.poll();
					if (command.equals("kickout") && usageThread != null) {
						usageThread.interrupt();
					}
					writer.println(command + "\r");
				}
				writer.flush();
			}
		}
	}

	private class UsageMonitoringThread extends Thread {
		private Account account;
		private UsageMonitoringThread(long usageDuration, Account account) {
			this.account = account;
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {}
			
			queueCommand("kickout");
			ClientManager.setClientPanelStatus(ipAddress, ClientPanel.Status.ACTIVE);
			account.setAvailableMinutes(0);
			DatabaseManager.updateDatabaseLine(
					DatabaseManager.getLineNumberFromUsername(account.getUsername()),
					account);
			DatabaseManager.updateAccountTable();
			
			usageThread = null;
		}
	}
}
