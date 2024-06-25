package acim.net;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

/**
 * This code deals with individual clients that are connected to the server.
 */
public class ClientThread extends Thread {
	private Stack<String> outputActions;
	private Socket client;
	
	private Scanner scan;
	private PrintWriter writer;
	
	private ClientThread() {}
	
	public ClientThread(Socket client) throws IOException {
		this.client = client;
		
		scan = new Scanner(client.getInputStream());
		writer = new PrintWriter(client.getOutputStream(), true);
	}
	
	public Socket getClientSocket() { return client; }
	
	public void closeConnection() throws IOException {
		client.close();
	}
	
	private void close() {
		try {
			closeConnection();
			ClientManager.removeClientThread(this);
			System.out.println("Client disconnected: " + client.getInetAddress().getHostAddress() + ":" + client.getPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			String ipAddress = client.getInetAddress().getHostAddress();
			int port = client.getPort();
			
			writer.println("Welcome to the server!\r");
			
			while (client.isConnected()) {
				String input = scan.nextLine();
				
				// Reflect input back to the client.
				writer.println("> " + input + "\r");
				System.out.println("[" + ipAddress + ":" + port + "]: " + input);
				
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
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
