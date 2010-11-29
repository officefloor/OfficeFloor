package net.officefloor.example.weborchestration.login;

/**
 * Login credentials.
 * 
 * @author daniel
 */
public class LoginCredentials {

	/**
	 * Email.
	 */
	private String email;

	/**
	 * Password.
	 */
	private String password;

	/**
	 * Error.
	 */
	private String error;

	/**
	 * Default constructor.
	 */
	public LoginCredentials() {
	}

	/**
	 * Initiate.
	 * 
	 * @param email
	 *            Email.
	 * @param error
	 *            Error.
	 */
	public LoginCredentials(String email, String error) {
		this.email = email;
		this.error = error;
	}

	/**
	 * Obtains the email.
	 * 
	 * @return Email.
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 * HTTP parameter for email.
	 * 
	 * @param email
	 *            Email.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Obtains the password.
	 * 
	 * @return Password.
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * HTTP parameter for password.
	 * 
	 * @param password
	 *            Password.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Obtains the error in login.
	 * 
	 * @return Error in login.
	 */
	public String getError() {
		return this.error;
	}

}