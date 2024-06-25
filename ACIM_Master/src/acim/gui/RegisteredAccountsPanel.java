package acim.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;

import acim.data.*;

public class RegisteredAccountsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JTable tableAccount;
	private DefaultTableModel tableModel;
	
	public RegisteredAccountsPanel() {
		setLayout(new GridLayout(0, 1, 0, 0));
		
		JSplitPane splitPaneAccountManager = new JSplitPane();
		splitPaneAccountManager.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPaneAccountManager.setResizeWeight(0.85);
		add(splitPaneAccountManager);
		
		String column[] = {"Username", "Password", "First Name", "Last Name", "Email", "Phone Number", "Balance", "Last Login", "Total Hours", "Notes"};
		String data[][] = {};
		tableAccount = new JTable();
		tableAccount.setModel(new DefaultTableModel(data, column) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		// Adjust some column widths...
		TableColumnModel columnModel = tableAccount.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(40);
		columnModel.getColumn(1).setPreferredWidth(40);
		columnModel.getColumn(4).setPreferredWidth(40);
		columnModel.getColumn(6).setPreferredWidth(40);
		columnModel.getColumn(7).setPreferredWidth(140);
		columnModel.getColumn(8).setPreferredWidth(40);
		tableAccount.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		DatabaseManager.setAccountTable(tableAccount);
		DatabaseManager.updateAccountTable();
		tableModel = (DefaultTableModel) tableAccount.getModel();

		JScrollPane scrollPaneTableAccount = new JScrollPane();
		scrollPaneTableAccount.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneTableAccount.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPaneTableAccount.setViewportView(tableAccount);
		
		splitPaneAccountManager.setLeftComponent(scrollPaneTableAccount);
		
		JPanel panelAccountActions = new JPanel();
		splitPaneAccountManager.setRightComponent(panelAccountActions);

		panelAccountActions.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
		panelAccountActions.setLayout(new GridLayout(2, 2, 16, 16));
		
		JButton btnUpdateInformation = new JButton("Update Information");
		btnUpdateInformation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Return if there is no selected row.
				if (tableAccount.getSelectedRow() == -1)
					return;
				
				Account modifyingAccount = DatabaseManager.getAccountByUsername(
						(String) tableAccount.getValueAt(tableAccount.getSelectedRow(), 0));
				int databaseLineNumber = DatabaseManager.getLineNumberFromUsername(modifyingAccount.getUsername());
				AccountModifierFrame.updateAccountFrame(tableAccount, modifyingAccount, tableAccount.getSelectedRow(), databaseLineNumber);

				DatabaseManager.DISPLAY_ALL_ACCOUNTS();
			}
		});
		panelAccountActions.add(btnUpdateInformation);

		JButton btnViewInformation = new JButton("View Information");
		btnViewInformation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Return if there is no selected row.
				if (tableAccount.getSelectedRow() == -1)
					return;
				String username = (String) tableAccount.getValueAt(tableAccount.getSelectedRow(), 0);
				
				Account account = DatabaseManager.getAccountByUsername(username);
				JOptionPane.showMessageDialog(null, account.getDialogString());
			}
		});
		panelAccountActions.add(btnViewInformation);
		
		JButton btnSearch = new JButton("Search");
		panelAccountActions.add(btnSearch);
		
		JButton btnNewAccount = new JButton("New Account");
		btnNewAccount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AccountModifierFrame.newAccountFrame(tableAccount);
				
				DatabaseManager.DISPLAY_ALL_ACCOUNTS();
			}
		});
		panelAccountActions.add(btnNewAccount);
		
		JButton btnPayForHours = new JButton("Pay For Hours");
		panelAccountActions.add(btnPayForHours);
		
		JButton btnDeleteAccount = new JButton("Delete Account");
		btnDeleteAccount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this account?") != 0)
					return;
				
				// Return if there is no selected row.
				if (tableAccount.getSelectedRow() == -1)
					return;
				String username = (String) tableAccount.getValueAt(tableAccount.getSelectedRow(), 0);
				
				Account modifyingAccount = DatabaseManager.getAccountByUsername(username);
				int databaseLineNumber = DatabaseManager.getLineNumberFromUsername(modifyingAccount.getUsername());
				DatabaseManager.removeDatabaseLine(databaseLineNumber);
				DatabaseManager.removeAccount(modifyingAccount);
				tableModel.removeRow(tableAccount.getSelectedRow());

				DatabaseManager.DISPLAY_ALL_ACCOUNTS();
			}
		});
		panelAccountActions.add(btnDeleteAccount);
	}
}
