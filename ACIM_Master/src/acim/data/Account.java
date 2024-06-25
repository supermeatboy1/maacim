package acim.data;
import java.time.*;
import java.util.*;
import java.text.*;

public class Account {
	private String username;
	private String passwordHash;
	private String firstName, lastName, email, phoneNumber, notes;
	
	private long lastLogin; // Unix timestamp
	private float totalHours;
	private float balance;
	
	private Account() {}
	
	public Account(String username, String password,
					String firstName, String lastName, String email,
					String phoneNumber, String notes) {
		updateLastLoginToNow();
		totalHours = balance = 0.0f;
		
		this.username = username;
		setPassword(password);
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.notes = notes;
	}
	
	public String getLastLoginFormattedString() {
		Date date = new Date(lastLogin * 1000);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		return sdf.format(date);
	}
	// TODO: Do not store passwords like THIS!
	public void setPassword(String password) { this.passwordHash = password; }

	
	public void updateLastLoginToNow() {
		lastLogin = Instant.now(Clock.systemUTC()).getEpochSecond();
	}
	
	public String getUsername() { return username; }
	public String getPasswordHash() { return passwordHash; }
	public String getFirstName() { return firstName; }
	public String getLastName() { return lastName; }
	public String getEmail() { return email; }
	public String getPhoneNumber() { return phoneNumber; }
	public String getNotes() { return notes; }
	public long getLastLogin() { return lastLogin; }
	public float getTotalHours() { return totalHours; }
	public float getBalance() { return balance; }

	public void setUsername(String username) { this.username = username; }
	public void setFirstName(String firstName) { this.firstName = firstName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
	public void setEmail(String email) { this.email = email; }
	public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
	public void setNotes(String notes) { this.notes = notes; }
	public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }
	public void setTotalHours(float totalHours) { this.totalHours = totalHours; }
	public void setBalance(float balance) { this.balance = balance; }

	@Override
	public String toString() {
		return "Account [username=" + username + ", passwordHash=" + passwordHash + ", firstName="
				+ firstName + ", lastName=" + lastName + ", email=" + email + ", phoneNumber=" + phoneNumber
				+ ", notes=" + notes + ", lastLogin=" + lastLogin + ", totalHours=" + totalHours + ", balance="
				+ balance + "]";
	}
	
	public String getDialogString() {
		return "<html><h1>Account Details: </h1><br>" +
				"<b>Username:</b> " + username + "<br>" +
				"<b>First Name:</b> " + firstName + "<br>" +
				"<b>Last Name:</b> " + lastName + "<br>" +
				"<b>Email:</b> " + email + "<br>" +
				"<b>Phone Number:</b> " + phoneNumber + "<br><br>" +
				"<b>Balance:</b> " + balance + "<br><br>" +
				"<b>Last Login:</b> " + getLastLoginFormattedString() + "<br>" +
				"<b>Total Hours:</b> " + totalHours + "<br>" +
				"<b>Notes:</b> " + notes + "<br></html>";
	}
	
	/**********************************************************************************************/
}
