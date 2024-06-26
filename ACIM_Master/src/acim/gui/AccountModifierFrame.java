package acim.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import acim.data.*;

public class AccountModifierFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	private JTextField txtUsername, txtFirstName, txtLastName, txtEmail, txtPhoneNumber, txtNotes;
	private JLabel lblPassword, lblFirstName, lblLastName, lblEmail, lblPhoneNumber, lblNotes;
	private JPasswordField txtPassword;

	private JButton btnProceed, btnCancel;

	private void initializeStart(boolean isCreatingNewAccount) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		if (isCreatingNewAccount)
			setTitle("Create New Account");
		else
			setTitle("Modify Account Information");

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 2, 8, 8));

		JLabel lblUsername = new JLabel("Username");
		contentPane.add(lblUsername);

		txtUsername = new JTextField();
		txtUsername.setText("username");
		contentPane.add(txtUsername);
		txtUsername.setColumns(10);

		if (isCreatingNewAccount)
			lblPassword = new JLabel("Password");
		else
			lblPassword = new JLabel("New password (blank if unchanged)");
		contentPane.add(lblPassword);

		txtPassword = new JPasswordField();
		txtPassword.setText("");
		contentPane.add(txtPassword);
		txtPassword.setColumns(10);

		lblFirstName = new JLabel("First Name");
		contentPane.add(lblFirstName);

		txtFirstName = new JTextField();
		txtFirstName.setText("First Name");
		contentPane.add(txtFirstName);
		txtFirstName.setColumns(10);

		lblLastName = new JLabel("Last Name");
		contentPane.add(lblLastName);

		txtLastName = new JTextField();
		txtLastName.setText("Last Name");
		contentPane.add(txtLastName);
		txtLastName.setColumns(10);

		lblEmail = new JLabel("Email");
		contentPane.add(lblEmail);

		txtEmail = new JTextField();
		txtEmail.setText("Email");
		contentPane.add(txtEmail);
		txtEmail.setColumns(10);

		lblPhoneNumber = new JLabel("Phone Number");
		contentPane.add(lblPhoneNumber);

		txtPhoneNumber = new JTextField();
		txtPhoneNumber.setText("Phone Number");
		contentPane.add(txtPhoneNumber);
		txtPhoneNumber.setColumns(10);

		lblNotes = new JLabel("Notes");
		contentPane.add(lblNotes);

		txtNotes = new JTextField();
		txtNotes.setText("notes");
		contentPane.add(txtNotes);
		txtNotes.setColumns(10);
	}
	
	private void initializeEnd() {
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		contentPane.add(btnCancel);

		setSize(640, 480);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private AccountModifierFrame(JTable table) {
		initializeStart(true);

		btnProceed = new JButton("Create Account");
		btnProceed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Account newAccount = new Account(txtUsername.getText(), null, txtFirstName.getText(),
						txtLastName.getText(), txtEmail.getText(),
						txtPhoneNumber.getText(), txtNotes.getText());
				newAccount.setPassword(new String(txtPassword.getPassword()));
				DatabaseManager.createNewAccount(newAccount);

				DefaultTableModel model = (DefaultTableModel) table.getModel();
				model.addRow(new String[] {
						txtUsername.getText(),
						"\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022", // Dots to censor password
						txtFirstName.getText(),
						txtLastName.getText(),
						txtEmail.getText(),
						txtPhoneNumber.getText(),
						"0.0",
						newAccount.getLastLoginFormattedString(),
						"0.0",
						txtNotes.getText()
				});

				dispose();
			}
		});
		contentPane.add(btnProceed);
		initializeEnd();
	}

	private AccountModifierFrame(JTable table, Account modify, int selectedRow, int databaseLineNumber) {
		initializeStart(false);
		
		txtUsername.setText(modify.getUsername());
		txtFirstName.setText(modify.getFirstName());
		txtLastName.setText(modify.getLastName());
		txtEmail.setText(modify.getEmail());
		txtPhoneNumber.setText(modify.getPhoneNumber());
		txtNotes.setText(modify.getNotes());

		btnProceed = new JButton("Modify");
		btnProceed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				modify.setUsername(txtUsername.getText());
				String passStr = new String(txtPassword.getPassword());
				if (!passStr.isEmpty())
					modify.setPassword(passStr);
				modify.setFirstName(txtFirstName.getText());
				modify.setLastName(txtLastName.getText());
				modify.setEmail(txtEmail.getText());
				modify.setPhoneNumber(txtPhoneNumber.getText());
				modify.setNotes(txtNotes.getText());

				DefaultTableModel model = (DefaultTableModel) table.getModel();
				model.setValueAt(txtUsername.getText(), selectedRow, 0);
				model.setValueAt(txtFirstName.getText(), selectedRow, 2);
				model.setValueAt(txtLastName.getText(), selectedRow, 3);
				model.setValueAt(txtEmail.getText(), selectedRow, 4);
				model.setValueAt(txtPhoneNumber.getText(), selectedRow, 5);
				model.setValueAt(txtNotes.getText(), selectedRow, 9);
				
				DatabaseManager.updateDatabaseLine(databaseLineNumber, modify);
				
				dispose();
			}
		});
		contentPane.add(btnProceed);
		initializeEnd();
	}

	public static AccountModifierFrame newAccountFrame(JTable table) {
		AccountModifierFrame frame = new AccountModifierFrame(table);
		return frame;
	}

	public static AccountModifierFrame updateAccountFrame(JTable table, Account modify, int selectedRow, int databaseLineNumber) {
		AccountModifierFrame frame = new AccountModifierFrame(table, modify, selectedRow, databaseLineNumber);
		return frame;
	}
}
