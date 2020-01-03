package net.officefloor.web.security.build;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.web.spi.security.HttpSecurity;

/**
 * <p>
 * Abstract {@link HttpSecurable} implementation to be configured as
 * {@link HttpSecurableBuilder}.
 * <p>
 * This is useful to extend for configuration items requiring to be configured
 * HTTP secure.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpSecurable implements HttpSecurable, HttpSecurableBuilder {

	/**
	 * Name of the {@link HttpSecurity}. May be <code>null</code>.
	 */
	private String httpSecurityName = null;

	/**
	 * Any roles.
	 */
	private final List<String> anyRoles = new LinkedList<>();

	/**
	 * Required roles.
	 */
	private final List<String> requiredRoles = new LinkedList<>();

	/*
	 * =============== HttpSecurable ====================
	 */

	@Override
	public String getHttpSecurityName() {
		return this.httpSecurityName;
	}

	@Override
	public String[] getAnyRoles() {
		return this.anyRoles.toArray(new String[this.anyRoles.size()]);
	}

	@Override
	public String[] getRequiredRoles() {
		return this.requiredRoles.toArray(new String[this.requiredRoles.size()]);
	}

	/*
	 * ============= HttpSecurableBuilder ================
	 */

	@Override
	public void setHttpSecurityName(String httpSecurityName) {
		this.httpSecurityName = httpSecurityName;
	}

	@Override
	public void addRole(String anyRole) {
		this.anyRoles.add(anyRole);
	}

	@Override
	public void addRequiredRole(String requiredRole) {
		this.requiredRoles.add(requiredRole);
	}

}