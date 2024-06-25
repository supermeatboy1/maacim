package acim.net;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

/**
 * This code deals with individual clients that are connected to the server.
 */
public class ClientConnection {
	private Socket client;

	private Scanner scan;
	private PrintWriter writer;
	
	private String ipAddress;
	private int port;
	
	private InputThread inThread;
	private OutputThread outThread;
	
	private Queue<String> commandQueue;

	private ClientConnection() {}

	public ClientConnection(Socket client) throws IOException {
		super();
		this.client = client;

		scan = new Scanner(client.getInputStream());
		writer = new PrintWriter(client.getOutputStream(), true);
		
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
					String input = scan.nextLine();

					// Reflect input back to the client.
					writer.println("> " + input + "\r");
					System.out.println("[ IN " + ipAddress + ":" + port + " ]: " + input);

					if (input.equals("quit") || input.equals("exit")) {
						close();
						break;
					} else {
						// Display the message through a dialog box.
						JOptionPane.showMessageDialog(null, input,
								"Client (" + ipAddress + ":" + port + ") has a message for you!",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			} catch (NoSuchElementException e) {
				// This exception is thrown when the connection is abruptly cut.
				close();
			}
		}
	}

	private class OutputThread extends Thread {
		@Override
		public void run() {
			while (client.isConnected()) {
				// Wait until an command is queued.
				while (commandQueue.isEmpty()) {
					try {
						sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				// Empty all queued commands
				while (!commandQueue.isEmpty()) {
					String command = commandQueue.poll();
					System.out.println("[ OUT " + ipAddress + ":" + port + " ]: " + command);
					writer.println(command + "\r");
				}
			}
		}
	}
}
