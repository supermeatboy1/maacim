package acim.client;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.JOptionPane;

public class ConnectionThread extends Thread {
	private Socket socket;
	private SocketAddress addr;
	private Scanner scan;
	private PrintWriter writer;

	private boolean running = false;

	public ConnectionThread(Socket s) {
		socket = s;
		addr = s.getLocalSocketAddress();
	}
	
	public void closeSocket() {
		try {
			if (!socket.isClosed())
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		running = true;

		while (running) {
			try {
				scan = new Scanner(socket.getInputStream());
				writer = new PrintWriter(socket.getOutputStream(), true);
				
				new InputThread().start();
				new OutputThread().start();
				
				// Wait until the connection between the server is lost.
				while (socket.isConnected()) {
					Thread.sleep(1000);
				}
				
				scan.close();
				writer.close();
				closeSocket();
			} catch (IOException e) {
				e.printStackTrace();
				closeSocket();
			} catch (NoSuchElementException e) {
				closeSocket();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Try reconnecting with the server...
			try {
				socket = new Socket();
				while (!socket.isConnected()) {
					sleep(500);

					socket.connect(addr);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class InputThread extends Thread {
		@Override
		public void run() {
			while (socket.isConnected()) {
				String str = scan.nextLine();
				System.out.println("RECEIVED FROM SERVER: " + str);
				if (str.startsWith("message ")) {
					String msg = str.replaceFirst("message ", "");
					JOptionPane.showMessageDialog(null, "<html>Message from server:<br>" + msg + "</html>");
				} else if (str.startsWith("shutdown")) {
					try {
						SystemCloser.shutdown();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private class OutputThread extends Thread {
		@Override
		public void run() {
			while (socket.isConnected()) {
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
