package acim.gui;
import java.awt.*;

import javax.swing.*;

public class ComputerManagerPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JPanel panelComputerList = new JPanel();	
	public JPanel getComputerListPanel() { return panelComputerList; }

	public ComputerManagerPanel() {
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
		panelComputerControl.add(btnMessage);
		
		JButton btnModifyInfo = new JButton("Modify Info");
		panelComputerControl.add(btnModifyInfo);

		JButton btnAllowAccess = new JButton("Allow Access");
		panelComputerControl.add(btnAllowAccess);

		JButton btnSendFile = new JButton("Send File");
		panelComputerControl.add(btnSendFile);

		JButton btnScreenCapture = new JButton("Screenshot");
		panelComputerControl.add(btnScreenCapture);
		
		JButton btnKickOut = new JButton("Kick Out");
		panelComputerControl.add(btnKickOut);
		
		JButton btnShutdown = new JButton("Shutdown");
		panelComputerControl.add(btnShutdown);

		JButton btnRestart = new JButton("Restart");
		panelComputerControl.add(btnRestart);
	}
}
