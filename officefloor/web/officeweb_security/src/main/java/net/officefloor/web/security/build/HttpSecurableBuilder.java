package net.officefloor.web.security.build;

import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Interface for {@link HttpSecurer} to correspond with the
 * {@link HttpSecurable}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurableBuilder {

	/**
	 * Specifies the particular {@link HttpSecurity}.
	 * 
	 * @param httpSecurityName
	 *            Name of the {@link HttpSecurity} to use.
	 */
	void setHttpSecurityName(String httpSecurityName);

	/**
	 * Adds to listing of roles that must have at least one for access.
	 * 
	 * @param anyRole
	 *            Any role.
	 */
	void addRole(String anyRole);

	/**
	 * Adds to listing of required roles that must have all for access.
	 * 
	 * @param requiredRole
	 *            Required roles.
	 */
	void addRequiredRole(String requiredRole);

}