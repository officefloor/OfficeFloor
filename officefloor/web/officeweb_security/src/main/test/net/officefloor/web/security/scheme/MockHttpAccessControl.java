package net.officefloor.web.security.scheme;

import java.security.Principal;

import net.officefloor.web.security.HttpAccessControl;

/**
 * Mock {@link HttpAccessControl}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpAccessControl implements HttpAccessControl {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link MockAccessControl}.
	 */
	private final MockAccessControl accessControl;

	/**
	 * Instantiate.
	 * 
	 * @param accessControl {@link MockAccessControl}.
	 */
	public MockHttpAccessControl(MockAccessControl accessControl) {
		this.accessControl = accessControl;
	}

	/*
	 * ================= HttpAccessControl ==============
	 */

	@Override
	public String getAuthenticationScheme() {
		return this.accessControl.getAuthenticationScheme();
	}

	@Override
	public Principal getPrincipal() {
		return () -> this.accessControl.getUserName();
	}

	@Override
	public boolean inRole(String role) {
		return this.accessControl.getRoles().contains(role);
	}

}