package acim.data;

import java.text.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

public class DatabaseManager {
	private static final long TABLE_UPDATE_MILLISECONDS_LIMIT = 1000;
	
	private static ArrayList<Account> accountList;
	private static String ROW_SEPARATOR = "\uE000";

	private static JTable tableAccounts = null;
	private static DefaultTableModel tableModel = null;
	
	private static long lastTableUpdateMillis = 0;
	private static NumberFormat decimalFormatter = new DecimalFormat("0.00");
	
	public static void loadAccountList() throws IOException {
		File file = new File("Accounts.txt");
		accountList = new ArrayList<Account>();
		
		if (!file.exists()) {
			file.createNewFile();
			return;
		}
		
		Scanner fileScan = new Scanner(file);
		System.out.println("Reading Accounts.txt");
		while (fileScan.hasNextLine()) {
			String line = fileScan.nextLine();
			if (line.strip().isEmpty())
				continue;
			String[] split = line.split(ROW_SEPARATOR);
			Account readAccount = new Account(
				split[0], split[1], split[2], split[3], split[4], split[5], split[9]
			);
			readAccount.setLastLogin(Long.parseLong(split[7]));
			readAccount.setAvailableSeconds(Long.parseLong(split[6]));
			readAccount.setTotalHours(Float.parseFloat(split[8]));
			accountList.add(readAccount);
		}
		fileScan.close();
	}
	
	public static void setAccountTable(JTable table) {
		tableAccounts = table;
		tableModel = (DefaultTableModel) tableAccounts.getModel();
	}
	
	public static DefaultTableModel getAccountTableModel() { return tableModel; }
	
	public static void updateAccountTable() {
		if (System.currentTimeMillis() - lastTableUpdateMillis < TABLE_UPDATE_MILLISECONDS_LIMIT)
			return;
		
		// To fix JTable flickering...
		// https://forums.oracle.com/ords/apexds/post/jtable-flickering-when-updated-4725
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Save the current selected row.
				int selectedRow = tableAccounts.getSelectedRow();
				
				while (tableModel.getRowCount() > 0)
					tableModel.removeRow(0);
				
				for (Account account : accountList) {
					tableModel.addRow(new String[] {
							account.getUsername(),
							"\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022", // Dots to censor password
							account.getFirstName(),
							account.getLastName(),
							account.getEmail(),
							account.getPhoneNumber(),
							"" + account.getAvailableSeconds(),
							account.getLastLoginFormattedString(),
							decimalFormatter.format(account.getTotalHours()),
							account.getNotes()
						});
				}
				
				if (selectedRow != -1) {
					try {
						tableAccounts.setRowSelectionInterval(selectedRow, selectedRow);
					} catch (Exception e) {
						// Ignore "row selection out of range" errors.
					}
				}
			}
		});
		
		lastTableUpdateMillis = System.currentTimeMillis();
	}

	public static Account getAccountByUsername(String username) {
		for (Account account : accountList) {
			if (account.getUsername().equals(username))
				return account;
		}
		return null;
	}
	public static void removeAccount(Account account) {
		accountList.remove(account);
	}
	
	private static String serializeAccount(Account account) {
		return account.getUsername() + ROW_SEPARATOR +
				account.getEncodedPassword() + ROW_SEPARATOR +
				account.getFirstName() + ROW_SEPARATOR +
				account.getLastName() + ROW_SEPARATOR +
				account.getEmail() + ROW_SEPARATOR +
				account.getPhoneNumber() + ROW_SEPARATOR +
				account.getAvailableSeconds() + ROW_SEPARATOR +
				account.getLastLogin() + ROW_SEPARATOR +
				account.getTotalHours() + ROW_SEPARATOR +
				account.getNotes();
	}
	
	public static void createNewAccount(Account account) {
		// Add account to account list stored in memory.
		accountList.add(account);
		// Add account to account list stored in the file.
		try {
			Files.write(Paths.get("Accounts.txt"), (serializeAccount(account) + "\r\n"
			).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "File write error: " + e.getLocalizedMessage(),
					e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public static int getLineNumberFromUsername(String username) {
		try {
			// Get the contents of the file as an ArrayList.
			ArrayList<String> content = new ArrayList<String>(Files.readAllLines(Paths.get("Accounts.txt")));
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i).split(ROW_SEPARATOR)[0].equals(username)) {
					return i;
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "File read error: " + e.getLocalizedMessage(),
					e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		return -1;
	}

	public static void updateDatabaseLine(int lineNumber, Account account) {
		try {
			// Get the contents of the file as an ArrayList.
			ArrayList<String> content = new ArrayList<String>(Files.readAllLines(Paths.get("Accounts.txt")));
			if (lineNumber != -1)
				content.set(lineNumber, serializeAccount(account));
			Files.write(Paths.get("Accounts.txt"), content);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "File read error: " + e.getLocalizedMessage(),
					e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public static void removeDatabaseLine(int lineNumber) {
		try {
			// Get the contents of the file as an ArrayList.
			ArrayList<String> content = new ArrayList<String>(Files.readAllLines(Paths.get("Accounts.txt")));
			content.remove(lineNumber);
			Files.write(Paths.get("Accounts.txt"), content);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "File read error: " + e.getLocalizedMessage(),
					e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
}
