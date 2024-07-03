package acim.net;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import acim.gui.*;

public class ClientManager {
	private static ArrayList<ClientConnection> clientConnections;
	private static ClientConnection selectedClientConnection;
	private static JPanel managerPanel;
	
	public static void initialize() {
		clientConnections = new ArrayList<ClientConnection>();
	}
	public static void forceCloseEverything() throws IOException {
		for (ClientConnection conn : clientConnections) {
			conn.closeConnection();
		}
	}
	public static void addClient(Socket client) throws IOException {
		// Reject duplicate client connections...
		if (getPanelFromIpAddress(client.getInetAddress().getHostAddress()) != null) {
			client.close();
			return;
		}

		ClientConnection conn = new ClientConnection(client);
		clientConnections.add(conn);
		conn.startThreads();
		
		addClientToPanel(client);
	}
	public static void removeClientConnection(ClientConnection connection) {
		if (clientConnections.contains(connection)) {
			clientConnections.remove(connection);
		}
		removeClientFromPanel(connection.getIpAddress());
		
		// Deselect the current client connection just in case it's the same one we're removing.
		if (connection.equals(selectedClientConnection)) {
			selectedClientConnection = null;
		}
	}
	public static void setSelectedClientConnection(ClientPanel panel) {
		for (ClientConnection connection : clientConnections) {
			if (connection.getIpAddress().equals(panel.getIpAddress())) {
				selectedClientConnection = connection;
				return;
			}
		}
	}
	public static void queueCommandToSelectedConnectionDirect(String command) {
		selectedClientConnection.queueCommand(command);
	}
	public static boolean checkForSelectedConnection() {
		if (selectedClientConnection == null) {
			JOptionPane.showMessageDialog(null,
					"<html>No computer selected!"
					+ "<br>Please select a computer "
					+ "before performing an action.</html>",
					"No computer selected!", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	public static void queueCommandToSelectedConnection(String command) {
		if (!checkForSelectedConnection()) {
			return;
		}
		queueCommandToSelectedConnectionDirect(command);
	}
	public static String getSelectedIpAddress() {
		if (!checkForSelectedConnection()) {
			return null;
		}
		return selectedClientConnection.getIpAddress();
	}
	public static ClientConnection getConnectionFromUsername(String username) {
		for (ClientConnection connection : clientConnections) {
			if (connection.getCurrentUser() != null &&
					connection.getCurrentUser().equals(username)) {
				return connection;
			}
		}
		return null;
	}
	
	// ********************************************************************************************************
	
	// Functions that modify the GUI.
	public static void setManagerPanel(JPanel panel) {
		managerPanel = panel;
	}
	public static ClientPanel getPanelFromUser(String username) {
		for (Component c : managerPanel.getComponents()) {
			if (!(c instanceof ClientPanel))
				continue;
			ClientPanel panel = (ClientPanel) c;
			if (panel.getCurrentUser() != null && panel.getCurrentUser().equals(username))
				return panel;
		}
		return null;
	}
	public static ClientPanel getPanelFromIpAddress(String ipAddress) {
		for (Component c : managerPanel.getComponents()) {
			if (!(c instanceof ClientPanel))
				continue;
			ClientPanel panel = (ClientPanel) c;
			if (panel.getIpAddress() != null && panel.getIpAddress().equals(ipAddress))
				return panel;
		}
		return null;
	}
	public static void addClientToPanel(Socket client) {
		addClientToPanel(client, ClientPanel.Status.ACTIVE);
	}
	public static void addClientToPanel(Socket client, ClientPanel.Status status) {
		if (getPanelFromIpAddress(client.getInetAddress().getHostAddress()) == null) {
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
	public static void setClientPanelCurrentName(String ipAddress, String name) {
		for (Component c : managerPanel.getComponents()) {
			if (!(c instanceof ClientPanel))
				continue;
			
			ClientPanel panel = (ClientPanel) c;
			if (panel.getIpAddress().equals(ipAddress)) {
				panel.setCurrentName(name);
				panel.updateText();
				return;
			}
		}
	}
	public static void setClientPanelCurrentUser(String ipAddress, String user) {
		for (Component c : managerPanel.getComponents()) {
			if (!(c instanceof ClientPanel))
				continue;
			
			ClientPanel panel = (ClientPanel) c;
			if (panel.getIpAddress().equals(ipAddress)) {
				panel.setCurrentUser(user);
				panel.updateText();
				return;
			}
		}
	}
	public static void setClientPanelStatus(String ipAddress, ClientPanel.Status status) {
		for (Component c : managerPanel.getComponents()) {
			if (!(c instanceof ClientPanel))
				continue;
			
			ClientPanel panel = (ClientPanel) c;
			if (panel.getIpAddress().equals(ipAddress)) {
				panel.setStatus(status);
				panel.updateText();
				return;
			}
		}
	}
	public static void resetCurrentSelectedClient() {
		selectedClientConnection = null;
		
		for (Component c : managerPanel.getComponents()) {
			if (!(c instanceof ClientPanel))
				continue;
			
			ClientPanel panel = (ClientPanel) c;
			panel.resetColors();
		}
	}
}
