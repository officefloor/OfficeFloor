package net.officefloor.web.security.store;

import java.util.Set;

import net.officefloor.web.security.store.CredentialEntry;

/**
 * {@link PasswordFile} {@link CredentialEntry}.
 * 
 * @author Daniel Sagenschneider
 */
public class PasswordEntry implements CredentialEntry {

	/**
	 * User Id.
	 */
	private final String userId;

	/**
	 * Credentials.
	 */
	private final byte[] credentials;

	/**
	 * Roles.
	 */
	private final Set<String> roles;

	/**
	 * Initiate.
	 * 
	 * @param userId
	 *            User Id.
	 * @param credentials
	 *            Credentials.
	 * @param roles
	 *            Roles.
	 */
	public PasswordEntry(String userId, byte[] credentials, Set<String> roles) {
		this.userId = userId;
		this.credentials = credentials;
		this.roles = roles;
	}

	/**
	 * Obtains the User Id.
	 * 
	 * @return User Id.
	 */
	public String getUserId() {
		return this.userId;
	}

	/*
	 * ======================= CredentialEntry ======================
	 */

	@Override
	public byte[] retrieveCredentials() {
		return this.credentials;
	}

	@Override
	public Set<String> retrieveRoles() {
		return this.roles;
	}

}