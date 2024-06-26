package acim.client;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

public class ConnectionThread extends Thread {
	public static final int MAXIMUM_RECONNECTION_TRIES = 30;
	
	private Socket socket;
	private InetSocketAddress addr;
	private BufferedReader reader;
	private PrintWriter writer;

	private boolean running = false;
	private int connectionTries = 0;
	
	private Queue<String> commandQueue;
	private LockFrame lockFrame;

	public ConnectionThread(Socket s, LockFrame lockFrame) throws IOException {
		socket = s;
		addr = (InetSocketAddress) s.getRemoteSocketAddress();

		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintWriter(socket.getOutputStream(), true);

		commandQueue = new LinkedList<String>();
		lockFrame.setCommandQueue(commandQueue);
		this.lockFrame = lockFrame;
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
			connectionTries = 0;
			
			try {
				new InputThread().start();
				new OutputThread().start();
				
				// Wait until the connection between the server is lost.
				while (socket.isConnected() && !socket.isClosed()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
				}
				
				reader.close();
				writer.close();
				closeSocket();
			}catch (Exception e) {
				e.printStackTrace();
				closeSocket();
			}
			
			// Try reconnecting with the server...
			try {
				boolean reconnectionSuccess = false;
				while (!reconnectionSuccess) {
					if (connectionTries >= MAXIMUM_RECONNECTION_TRIES) {
						JOptionPane.showMessageDialog(null,
								"<html>Cannot reconnect to server " + addr.toString() + " after <i>"
									+ MAXIMUM_RECONNECTION_TRIES + "</i> tries. Closing...<html>",
									"Reconnection failed.",
									JOptionPane.ERROR_MESSAGE);
						System.exit(-1);
					}
					
					connectionTries++;
					System.out.println("Trying to reconnect with server... (" + connectionTries + ") " + addr.toString());
					sleep(1000);
				
					try {
						socket = new Socket(addr.getAddress().getHostAddress(), addr.getPort());

						reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						writer = new PrintWriter(socket.getOutputStream(), true);
						
						reconnectionSuccess = true;
						
						System.out.println("Reconnection successful.");
					} catch (IOException e) {}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class InputThread extends Thread {
		@Override
		public void run() {
			try {
				while (socket.isConnected()) {
					Thread.sleep(1);
					String input = reader.readLine();
					if (input == null) {
						System.out.println("Input from server is null");
						closeSocket();
						return;
					}

					if (input.startsWith("login fail ")) {
						String failMsg = input.replaceFirst("login fail ", "");
						JOptionPane.showMessageDialog(null,
								"<html>Failed to login: <br>" + failMsg + "<html>",
									"Login failed.",
									JOptionPane.ERROR_MESSAGE);
					} else if (input.equals("kickout")) {
						lockFrame.setVisible(true);
					} else if (input.equals("allow access")) {
						JOptionPane.showMessageDialog(null,
								"<html>Login successful!<html>",
									"Success!",
									JOptionPane.INFORMATION_MESSAGE);
						lockFrame.setVisible(false);
					} else if (input.startsWith("message ")) {
						String msg = input.replaceFirst("message ", "");
						JOptionPane.showMessageDialog(null, "<html>Message from server:<br>" + msg + "</html>");
					} else if (input.equals("shutdown")) {
						try {
							SystemCloser.shutdown(false);
						} catch (Exception e) {
							e.printStackTrace();
							commandQueue.add("message " + e.getClass().getSimpleName() + ": Cannot shutdown the target computer.<br>" + e.getLocalizedMessage());
						}
					} else if (input.equals("restart")) {
						try {
							SystemCloser.shutdown(true);
						} catch (Exception e) {
							e.printStackTrace();
							commandQueue.add("message " + e.getClass().getSimpleName() + ": Cannot restart the target computer.<br>" + e.getLocalizedMessage());
						}
					} else if (input.equals("start sending file")) {
						String filename = reader.readLine().replaceFirst("filename ", "");
						File file = new File(filename);
						file.createNewFile();
						FileOutputStream fos = new FileOutputStream(file);
						Base64.Decoder decoder = Base64.getUrlDecoder();
						boolean complete = false;
						
						try {
							while (!complete) {
								Thread.sleep(1);
								String line = reader.readLine();
								if (line.startsWith("cancel sending file")) {
									complete = true;
									fos.flush();
									fos.close();
									file.delete();
									break;
								} else if (line.startsWith("end sending file")) {
									complete = true;
									fos.flush();
									fos.close();
									JOptionPane.showMessageDialog(null, "Received file: " + filename);
									break;
								} else if (line.startsWith("chunk length ")) {
									int chunk_length = Integer.parseInt(line.replaceFirst("chunk length ", ""));
									byte[] chunk = decoder.decode(reader.readLine());
									Thread.sleep(1);
									fos.write(chunk, 0, chunk_length);
								}
							}
						} catch (NullPointerException e) {
							fos.flush();
							fos.close();
						}
					} else if (input.equals("request screenshot")) {
						commandQueue.add("start receive screenshot");
						ByteArrayInputStream bais = new ByteArrayInputStream(ScreenCapture.getScreencapBytes());
						byte[] buffer = new byte[512];
						long read_bytes = 0;
						Base64.Encoder encoder = Base64.getUrlEncoder();
						while ((read_bytes = bais.read(buffer)) > 0) {
							String encoded_string = encoder.encodeToString(buffer);
							commandQueue.add("chunk length " + read_bytes);
							commandQueue.add(encoded_string);
						}
						commandQueue.add("stop receive screenshot");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				closeSocket();
			}
		}
	}

	private class OutputThread extends Thread {
		@Override
		public void run() {
			while (socket.isConnected()) {
				// Wait until a command is queued.
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// Empty all queued commands
				while (!commandQueue.isEmpty()) {
					String command = commandQueue.poll();
					writer.println(command + "\r");
				}
				writer.flush();
			}
		}
	}
}
