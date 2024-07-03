package acim.gui;

import java.awt.*;
import java.net.*;

import javax.swing.*;
import javax.swing.border.*;

import acim.net.*;

import javax.imageio.*;
import java.awt.event.*;

public class ClientPanel extends JPanel {
	public enum Status {
		ACTIVE,
		IN_USE,
	};
	public static final Color HIGHLIGHTED_COLOR = new Color(138, 206, 0);
	
	private static final long serialVersionUID = 1L;
	private static ImageIcon iconClientPanelActive, iconClientPanelInUse;
	
	private String ipAddress;
	private int port;
	private String nickname;
	private boolean isLocalClientPanel;
	private Status status;

	private JLabel lblText;
	private Color defaultBackgroundColor;
	private Color defaultTextColor;
	private String currentUser = "", currentName = "";

	public ClientPanel(String ipAddress, int port, String nickname) {
		ClientPanel thisPanel = this;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ClientManager.resetCurrentSelectedClient();

				if (!isLocalClientPanel) {
					setBackground(HIGHLIGHTED_COLOR);
					lblText.setForeground(Color.DARK_GRAY);
					setBorder(new BevelBorder(BevelBorder.LOWERED));
					
					ClientManager.setSelectedClientConnection(thisPanel);
				}
			}
		});
		
		setLayout(new BorderLayout(0, 0));
		try {
			if (iconClientPanelActive == null)
				iconClientPanelActive = new ImageIcon(ImageIO.read(
						getClass().getClassLoader().getResourceAsStream("computer_active.png")
					));
			if (iconClientPanelInUse == null)
				iconClientPanelInUse = new ImageIcon(ImageIO.read(
						getClass().getClassLoader().getResourceAsStream("computer_in_use.png")
					));
		} catch (Exception e) {
			e.printStackTrace();
		}

		lblText = new JLabel(iconClientPanelActive);
		add(lblText, BorderLayout.CENTER);

		this.ipAddress = ipAddress;
		this.port = port;
		this.nickname = nickname;
		this.status = Status.ACTIVE;

		updateText();

		defaultBackgroundColor = getBackground();
		defaultTextColor = lblText.getForeground();
	}

	public void updateText() {
		if (isLocalClientPanel)
			lblText.setText("<html><b>(This computer)<br>" + nickname + "</b><br>IP Address: " + ipAddress + "<br>Port: " + 6969 + "<br>ACTIVE</html>");
		else {
			String hold;
			if (status == Status.ACTIVE) {
				hold = "<html><b>" + nickname + "</b><br>IP Address: " + ipAddress +
						"<br>Port: " + port + "<br>" + status + "</html>";
			} else {
				hold = "<html><b>" + nickname + "</b><br>IP Address: " + ipAddress +
						"<br>Port: " + port + "<br>" + status + "<br>Logged In: " +
						currentName + " - \"" + currentUser + "\"" + "</html>";
			}
			lblText.setText(hold);
		}

		switch (status) {
		case IN_USE:
			lblText.setIcon(iconClientPanelInUse);
			break;
		default:
			lblText.setIcon(iconClientPanelActive);
			break;
		}
	}
	public void resetColors() {
		setBackground(defaultBackgroundColor);
		lblText.setForeground(defaultTextColor);
		setBorder(new EmptyBorder(0, 0, 0, 0));
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public void setCurrentUser(String user) {
		currentUser = user;
	}
	public void setCurrentName(String name) {
		currentName = name;
	}

	public String getIpAddress() { return ipAddress; }
	public int getPort() { return port; }
	public String getNickname() { return nickname; }
	public boolean isLocalClientPanel() { return isLocalClientPanel; }
	public Status getStatus() { return status; }
	public String getCurrentUser() { return currentUser; }

	public static ClientPanel createLocalPanel() {
		InetAddress ip;
		try {
			ip = InetAddress.getLocalHost();
			String hostname = ip.getHostName();
			ClientPanel self = new ClientPanel(ip.getHostAddress(), -1, hostname);
			self.isLocalClientPanel = true;
			self.status = Status.IN_USE;
			self.updateText();
			return self;
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "Unknown host: " + e.getLocalizedMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	
	public static ClientPanel createPanel(Socket socket) {
		return new ClientPanel(
				socket.getInetAddress().getHostAddress(),
				socket.getPort(),
				socket.getInetAddress().getCanonicalHostName()
		);
	}
}
