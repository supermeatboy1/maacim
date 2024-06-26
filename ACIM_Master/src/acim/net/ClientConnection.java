package acim.net;

import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.table.*;

import acim.data.*;
import acim.gui.*;

/**
 * This code deals with individual clients that are connected to the server.
 */
public class ClientConnection {
	private Socket client;

	private BufferedReader reader;
	private BufferedWriter writer;
	
	private String ipAddress;
	private int port;
	
	private InputThread inThread;
	private OutputThread outThread;
	private UsageMonitoringThread usageThread = null;
	
	private Queue<String> commandQueue;
	private String currentUser;

	private ClientConnection() {}

	public ClientConnection(Socket client) throws IOException {
		super();
		this.client = client;

		reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
		
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
	public void updateUsageSeconds() {
		if (usageThread != null)
			usageThread.updateSeconds();
	}
	public void kickout() {
		if (usageThread != null)
			usageThread.kickout();
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
	public void setCurrentUser(String newUser) {
		currentUser = newUser;
	}
	public String getCurrentUser() { return currentUser; }

	public void closeConnection() throws IOException {
		client.close();
	}

	private void close() {
		try {
			closeConnection();
			ClientManager.removeClientConnection(this);
			System.out.println("Client disconnected: " + client.getInetAddress().getHostAddress() + ":" + client.getPort());
			
			// Make sure to interrupt the usage thread.
			// 
			if (usageThread != null && !usageThread.isInterrupted())
				usageThread.interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class InputThread extends Thread {
		@Override
		public void run() {
			try {
				writer.write("Welcome to the server!\r\n");
				while (client.isConnected() && !client.isClosed()) {
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
							String line = reader.readLine();
							
							if (line.startsWith("chunk length ")) {
								int chunk_length = Integer.parseInt(line.replaceFirst("chunk length ", ""));
								String chunk_line = "";
								while (chunk_line.isBlank() || chunk_line.equals("null"))
									chunk_line = reader.readLine();
								byte[] chunk = decoder.decode(chunk_line);
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
								new PictureViewerFrame(screenshot, "[Screenshot] " + date_time_str).setVisible(true);
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
						} else if (account.getAvailableSeconds() == 0) {
							queueCommand("login fail Account balance is empty.");
						} else if (ClientManager.getConnectionFromUsername(clientUsername) != null) {
							queueCommand("login fail This username is currently in use.<br>Please try again later.");
						} else {
							account.updateLastLoginToNow();
							
							queueCommand("allow access " + account.getAvailableSeconds());
							ClientManager.setClientPanelCurrentUser(ipAddress, clientUsername);
							ClientManager.setClientPanelStatus(ipAddress, ClientPanel.Status.IN_USE);
							currentUser = clientUsername;
							
							usageThread = new UsageMonitoringThread(account);
							usageThread.start();
						}
					}
				}
			} catch (Exception e) {
				System.out.println("Exception occured in InputThread (" + ipAddress + "): ");
				e.printStackTrace();
				close();
			}
		}
	}

	private class OutputThread extends Thread {
		@Override
		public void run() {
			try {
				while (client.isConnected() && !client.isClosed()) {
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
						if (command == null)
							continue;
						
						if (command.equals("kickout") && usageThread != null) {
							usageThread.interrupt();
						}
						writer.write(command + "\r\n");
						writer.flush();
					}
				}
			} catch (Exception e) {
				System.out.println("Exception occured in OutputThread (" + ipAddress + "): ");
				e.printStackTrace();
			}
		}
	}

	private class UsageMonitoringThread extends Thread {
		private Account account;
		private long startMillis = 0;
		private long endMillis = 0;
		private UsageMonitoringThread(Account account) {
			this.account = account;
			updateSeconds();
		}
		private void updateSeconds() {
			startMillis = System.currentTimeMillis();
			endMillis = startMillis + (account.getAvailableSeconds() * 1000);
		}
		private void kickout() {
			queueCommand("kickout");
			ClientManager.setClientPanelCurrentUser(ipAddress, "");
			ClientManager.setClientPanelStatus(ipAddress, ClientPanel.Status.ACTIVE);
		}
		private void deductSecond() {
			// Deduct the available seconds for an account based on the time elapsed.
			//long secondsDeduction = ((System.currentTimeMillis() - startMillis) / 1000);
			
			account.setAvailableSeconds(
					account.getAvailableSeconds() - 1//secondsDeduction
				);
			
			// Prevent negative seconds balance.
			if (account.getAvailableSeconds() < 0)
				account.setAvailableSeconds(0);

			DatabaseManager.updateDatabaseLine(
					DatabaseManager.getLineNumberFromUsername(account.getUsername()),
					account);
			DatabaseManager.updateAccountTable();
		}
		
		@Override
		public void run() {
			boolean interrupted = false;
			try {
				while (System.currentTimeMillis() <= endMillis) {
					Thread.sleep(1000); 
					deductSecond();
				}
			} catch (InterruptedException e) {
				interrupted = true;
				
				//deduct();
			}
			
			kickout();
			
			if (!interrupted)
				account.setAvailableSeconds(0);
			
			DatabaseManager.updateDatabaseLine(
					DatabaseManager.getLineNumberFromUsername(account.getUsername()),
					account);
			DatabaseManager.updateAccountTable();
			
			currentUser = null;
			usageThread = null;
		}
	}
}
