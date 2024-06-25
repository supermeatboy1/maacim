package acim.client;
import javax.swing.*;

import java.awt.*;

public class LoginPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField txtUsername;
	private JPasswordField pwdPassword;
	private JButton btnSubmit;

	public LoginPanel() {
		setLayout(new GridLayout(0, 2, 8, 8));

		add(new JLabel("A.C.I.M. Login"));
		add(new JLabel(" "));
		
		JLabel lblUsername = new JLabel("Username");
		add(lblUsername);
		
		txtUsername = new JTextField();
		txtUsername.setText("username");
		add(txtUsername);
		txtUsername.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password");
		add(lblPassword);
		
		pwdPassword = new JPasswordField();
		add(pwdPassword);
		
		add(new JLabel(" "));
		add(new JLabel(" "));
		add(new JLabel(" "));
		
		btnSubmit = new JButton("Submit");
		add(btnSubmit);

	}
}
