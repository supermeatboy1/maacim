package acim.data;
import java.time.*;
import java.util.*;
import java.text.*;

public class Account {
	private String username;
	private String encodedPassword;
	private String firstName, lastName, email, phoneNumber, notes;
	
	private long lastLogin; // Unix timestamp
	private float totalHours;
	private long availableMinutes;
	
	private Account() {}
	
	public Account(String username, String encodedPassword,
					String firstName, String lastName, String email,
					String phoneNumber, String notes) {
		updateLastLoginToNow();
		totalHours = 0.0f;
		availableMinutes = 0;
		
		this.username = username;
		this.encodedPassword = encodedPassword;
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
	public void setPassword(String password) {
		Base64.Encoder encoder = Base64.getUrlEncoder();
		this.encodedPassword = encoder.encodeToString(password.getBytes());
	}
	
	public void updateLastLoginToNow() {
		lastLogin = Instant.now(Clock.systemUTC()).getEpochSecond();
	}
	
	public String getUsername() { return username; }
	public String getEncodedPassword() { return encodedPassword; }
	public String getFirstName() { return firstName; }
	public String getLastName() { return lastName; }
	public String getEmail() { return email; }
	public String getPhoneNumber() { return phoneNumber; }
	public String getNotes() { return notes; }
	public long getLastLogin() { return lastLogin; }
	public float getTotalHours() { return totalHours; }
	public long getAvailableMinutes() { return availableMinutes; }

	public void setUsername(String username) { this.username = username; }
	public void setFirstName(String firstName) { this.firstName = firstName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
	public void setEmail(String email) { this.email = email; }
	public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
	public void setNotes(String notes) { this.notes = notes; }
	public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }
	public void setTotalHours(float totalHours) { this.totalHours = totalHours; }
	public void setAvailableMinutes(long availableMinutes) { this.availableMinutes = availableMinutes; }

	@Override
	public String toString() {
		return "Account [username=" + username + ", encodedPassword=" + encodedPassword + ", firstName="
				+ firstName + ", lastName=" + lastName + ", email=" + email + ", phoneNumber=" + phoneNumber
				+ ", notes=" + notes + ", lastLogin=" + lastLogin + ", totalHours=" + totalHours + ", availableMinutes="
				+ availableMinutes + "]";
	}
	
	public String getDialogString() {
		return "<html><h1>Account Details: </h1><br>" +
				"<b>Username:</b> " + username + "<br>" +
				"<b>First Name:</b> " + firstName + "<br>" +
				"<b>Last Name:</b> " + lastName + "<br>" +
				"<b>Email:</b> " + email + "<br>" +
				"<b>Phone Number:</b> " + phoneNumber + "<br><br>" +
				"<b>Available Minutes:</b> " + availableMinutes + "<br><br>" +
				"<b>Last Login:</b> " + getLastLoginFormattedString() + "<br>" +
				"<b>Total Hours:</b> " + totalHours + "<br>" +
				"<b>Notes:</b> " + notes + "<br></html>";
	}
	
	/**********************************************************************************************/
}
