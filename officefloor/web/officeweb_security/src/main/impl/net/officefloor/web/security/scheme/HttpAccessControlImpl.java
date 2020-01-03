package net.officefloor.web.security.scheme;

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;

import net.officefloor.web.security.HttpAccessControl;

/**
 * {@link HttpAccessControl} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAccessControlImpl implements HttpAccessControl, Serializable {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Authentication scheme.
	 */
	private final String authenticationScheme;

	/**
	 * {@link Principal}.
	 */
	private final Principal principal;

	/**
	 * Roles for the user.
	 */
	private final Set<String> roles;

	/**
	 * Initiate.
	 * 
	 * @param authenticationScheme Authentication scheme.
	 * @param principal            {@link Principal}.
	 * @param roles                Roles for the user.
	 */
	public HttpAccessControlImpl(String authenticationScheme, Principal principal, Set<String> roles) {
		this.authenticationScheme = authenticationScheme;
		this.principal = principal;
		this.roles = roles;
	}

	/**
	 * Initiate with simple {@link Principal}.
	 * 
	 * @param authenticationScheme Authentication scheme.
	 * @param principalName        {@link Principal} name.
	 * @param roles                Roles for the user.
	 */
	public HttpAccessControlImpl(String authenticationScheme, final String principalName, Set<String> roles) {
		this.authenticationScheme = authenticationScheme;
		this.roles = roles;

		// Create simple principal
		this.principal = new Principal() {
			@Override
			public String getName() {
				return principalName;
			}
		};
	}

	/*
	 * ======================== HttpSecurity =========================
	 */

	@Override
	public String getAuthenticationScheme() {
		return this.authenticationScheme;
	}

	@Override
	public Principal getPrincipal() {
		return this.principal;
	}

	@Override
	public boolean inRole(String role) {
		return this.roles.contains(role);
	}

}