package acim.client;

import java.net.*;

public class ConnectionThread extends Thread {
	private Socket socket;
	
	public ConnectionThread(Socket socket) {
		super();
		this.socket = socket;
	}
}
