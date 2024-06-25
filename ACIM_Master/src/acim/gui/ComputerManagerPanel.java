package acim.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import acim.net.ClientManager;

public class ComputerManagerPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JPanel panelComputerList = new JPanel();	
	public JPanel getComputerListPanel() { return panelComputerList; }

	public ComputerManagerPanel() {
		JPanel thisPanel = this;
		
		setLayout(new GridLayout(0, 1, 0, 0));

		JSplitPane splitPaneComputerManager = new JSplitPane();
		splitPaneComputerManager.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPaneComputerManager.setResizeWeight(0.85);
		add(splitPaneComputerManager);

		JScrollPane scrollPaneComputers = new JScrollPane();
		scrollPaneComputers.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneComputers.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		splitPaneComputerManager.setLeftComponent(scrollPaneComputers);
		
		panelComputerList = new JPanel();
		scrollPaneComputers.setViewportView(panelComputerList);
		panelComputerList.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
		panelComputerList.setLayout(new GridLayout(2, 2, 16, 16));
		panelComputerList.add(ClientPanel.createLocalPanel());
		
		JPanel panelComputerControl = new JPanel();
		splitPaneComputerManager.setRightComponent(panelComputerControl);
		panelComputerControl.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
		panelComputerControl.setLayout(new GridLayout(2, 2, 16, 16));

		JButton btnMessage = new JButton("Message");
		btnMessage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String ipAddress = ClientManager.getSelectedIpAddress();
				if (ipAddress == null)
					return;
				
				String msg = JOptionPane.showInputDialog(thisPanel, "Enter a message for " + ipAddress + ".");
				ClientManager.queueCommandToSelectedConnection("message " + msg);
				System.out.println("Message: " + msg);
			}
		});
		panelComputerControl.add(btnMessage);

		JButton btnSendFile = new JButton("Send File");
		btnSendFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		panelComputerControl.add(btnSendFile);

		JButton btnScreenCapture = new JButton("Screenshot");
		btnScreenCapture.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		panelComputerControl.add(btnScreenCapture);
		
		JButton btnKickOut = new JButton("Kick Out");
		btnKickOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		panelComputerControl.add(btnKickOut);
		
		JButton btnShutdown = new JButton("Shutdown");
		btnShutdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ClientManager.queueCommandToSelectedConnection("shutdown ");
			}
		});
		panelComputerControl.add(btnShutdown);

		JButton btnRestart = new JButton("Restart");
		btnRestart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ClientManager.queueCommandToSelectedConnection("restart ");
			}
		});
		panelComputerControl.add(btnRestart);
	}
}
