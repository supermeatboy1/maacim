package acim.net;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import acim.gui.*;

public class ClientManager {
	private static ArrayList<ClientThread> clientThreads;
	private static JPanel managerPanel;
	
	public static void initialize() {
		clientThreads = new ArrayList<ClientThread>();
	}
	public static void forceCloseEverything() throws IOException {
		for (ClientThread thread : clientThreads) {
			thread.closeConnection();
		}
	}
	public static void addClient(Socket client) throws IOException {
		// Reject duplicate client connections...
		if (checkIfClientIsInPanel(client.getInetAddress().getHostAddress())) {
			client.close();
		}

		ClientThread thread = new ClientThread(client);
		clientThreads.add(thread);
		thread.start();
		
		addClientToPanel(client);
	}
	public static void removeClientThread(ClientThread thread) {
		if (clientThreads.contains(thread)) {
			clientThreads.remove(thread);
		}
		removeClientFromPanel(thread.getClientSocket().getInetAddress().getHostAddress());
	}
	
	// Functions that modify the GUI.
	public static void setManagerPanel(JPanel panel) {
		managerPanel = panel;
	}
	public static boolean checkIfClientIsInPanel(String ipAddress) {
		for (Component c : managerPanel.getComponents()) {
			if (!(c instanceof ClientPanel))
				continue;
			
			ClientPanel panel = (ClientPanel) c;
			
			if (panel.getIpAddress().equals(ipAddress)) {
				return true;
			}
		}
		return false;
	}
	public static void addClientToPanel(Socket client) {
		addClientToPanel(client, ClientPanel.Status.ACTIVE);
	}
	public static void addClientToPanel(Socket client, ClientPanel.Status status) {
		if (!checkIfClientIsInPanel(client.getInetAddress().getHostAddress())) {
			ClientPanel panel = ClientPanel.createPanel(client);
			panel.setStatus(status);
			panel.updateText();
			managerPanel.add(panel);

			// https://stackoverflow.com/a/43267593
			managerPanel.revalidate();
			managerPanel.repaint();
		}
		
	}
	public static void removeClientFromPanel(String ipAddress) {
		for (Component c : managerPanel.getComponents()) {
			if (!(c instanceof ClientPanel))
				continue;
			
			ClientPanel panel = (ClientPanel) c;
			
			if (panel.getIpAddress().equals(ipAddress)) {
				managerPanel.remove(panel);
				
				// https://stackoverflow.com/a/43267593
				managerPanel.revalidate();
				managerPanel.repaint();
			}
		}
	}
}
