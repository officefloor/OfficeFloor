/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
