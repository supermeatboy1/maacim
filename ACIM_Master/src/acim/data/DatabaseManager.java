package acim.data;

import java.text.*;
import java.io.IOException;
//import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import javax.swing.*;
import javax.swing.table.*;

public class DatabaseManager {
	private static final long TABLE_UPDATE_MILLISECONDS_LIMIT = 1000;
	private static String ROW_SEPARATOR = "\uE000";
	private static JTable tableAccounts = null;
	private static DefaultTableModel tableModel = null;
	private static long lastTableUpdateMillis = 0;
	private static NumberFormat decimalFormatter = new DecimalFormat("0.00");
	public static void setAccountTable(JTable table) {
		tableAccounts = table;
		tableModel = (DefaultTableModel) tableAccounts.getModel();
	}
	public static DefaultTableModel getAccountTableModel() { return tableModel; }
	private static Stream<String> getAccountContentsStream() {
		try {
			return Files.lines(Paths.get("Accounts.txt"));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "File read error: " + e.getLocalizedMessage(),
					e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}
	private static List<String> getAccountContentsList() {
		try {
			return Files.readAllLines(Paths.get("Accounts.txt"));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "File read error: " + e.getLocalizedMessage(),
					e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}
	private static void saveAccountListContents(List<String> contents) {
		try {
			Files.write(Paths.get("Accounts.txt"), contents);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "File write error: " + e.getLocalizedMessage(),
					e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private static void addRowToTableFromString(String row) {
		Account account = deserializeAccount(row);
		if (account == null)
			return;
		
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

				// Get the contents of the file as a Stream of Strings.
				Stream<String> dbContentStream = getAccountContentsStream();
				if (dbContentStream == null) {
					return;
				}
				dbContentStream.forEach(row -> addRowToTableFromString(row));
				if (selectedRow != -1) {
					try {
						tableAccounts.setRowSelectionInterval(selectedRow, selectedRow);
					} catch (Exception e) {
						// Ignore "row selection out of range" errors.
					}
				}
				dbContentStream.close();
			}
		});
		lastTableUpdateMillis = System.currentTimeMillis();
	}

	public static Account getAccountByUsername(String username) {
		// Get the contents of the file as a Stream of Strings.
		Stream<String> dbContentStream = getAccountContentsStream();
		if (dbContentStream == null) {
			return null;
		}
		String line = dbContentStream.filter(row -> row.startsWith(username + ROW_SEPARATOR))
									.findFirst()
									.orElse(null);
		if (line == null)
			return null;
		
		return deserializeAccount(line);
	}
	public static void removeAccount(Account account) {
		List<String> rowList = getAccountContentsList();
		if (rowList == null || rowList.isEmpty())
			return;
		
		int lineNumber = 0;
		for (String row : rowList) {
			if (account.getUsername().equals(row.split(ROW_SEPARATOR)[0]))
				break;
			lineNumber++;
		}
		rowList.remove(lineNumber);
		saveAccountListContents(rowList);
	}
	public static void createNewAccount(Account account) {
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
	public static void updateAccount(Account account) {
		List<String> rowList = getAccountContentsList();
		int lineNumber = 0;
		for (String row : rowList) {
			if (account.getUsername().equals(row.split(ROW_SEPARATOR)[0]))
				rowList.set(lineNumber, serializeAccount(account));
			lineNumber++;
		}
		saveAccountListContents(rowList);
	}
	private static Account deserializeAccount(String line) {
		if (line == null || line.strip().isEmpty())
			return null;
		String[] split = line.split(ROW_SEPARATOR);
		Account account = new Account(
			split[0], split[1], split[2], split[3], split[4], split[5], split[9]
		);
		account.setLastLogin(Long.parseLong(split[7]));
		account.setAvailableSeconds(Long.parseLong(split[6]));
		account.setTotalHours(Float.parseFloat(split[8]));
		return account;
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
}
