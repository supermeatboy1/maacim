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
		if (checkIfClientIsInPanel(client.getInetAddress().getHostAddress())) {
			client.close();
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
	public static void queueCommandToSelectedConnection(String command) {
		if (selectedClientConnection == null) {
			JOptionPane.showMessageDialog(null,
					"<html>No computer selected!"
					+ "<br>Please select a computer "
					+ "before performing an action.</html>",
					"No computer selected!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		queueCommandToSelectedConnectionDirect(command);
	}
	public static String getSelectedIpAddress() {
		if (selectedClientConnection == null) {
			JOptionPane.showMessageDialog(null, "<html>No computer selected!"
					+ "<br>Please select a computer "
					+ "before performing an action.</html>",
					"No computer selected!",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return selectedClientConnection.getIpAddress();
	}
	
	// Functions that modify the GUI.
	public static void setManagerPanel(JPanel panel) {
		managerPanel = panel;
	}
	public static boolean checkIfClientIsInPanel(String ipAddress) {
		try {
			for (Component c : managerPanel.getComponents()) {
				if (!(c instanceof ClientPanel))
					continue;
				
				ClientPanel panel = (ClientPanel) c;
				
				if (panel.getIpAddress().equals(ipAddress)) {
					return true;
				}
			}
		} catch (Exception e) {}
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
