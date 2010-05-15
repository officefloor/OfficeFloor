package net.officefloor.example.weborchestration.login;

/**
 * Customer details.
 * 
 * @author daniel
 */
public class CustomerDetails {

	/**
	 * Name.
	 */
	private String name;

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
	public CustomerDetails() {
	}

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param email
	 *            Email.
	 * @param error
	 *            Error.
	 */
	public CustomerDetails(String name, String email, String error) {
		this.email = email;
		this.error = error;
	}

	/**
	 * Obtains the name.
	 * 
	 * @return Name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * HTTP parameter for name.
	 * 
	 * @param name
	 *            Name.
	 */
	public void setName(String name) {
		this.name = name;
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