/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.web.security.build;

import java.security.Principal;

import net.officefloor.web.security.build.office.HttpOfficeSecurer;
import net.officefloor.web.security.build.section.HttpFlowSecurer;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Configures the {@link HttpOfficeSecurer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurerBuilder {

	/**
	 * {@link Principal} requires only one of these roles.
	 * 
	 * @param role
	 *            Role.
	 */
	void addRole(String role);

	/**
	 * {@link Principal} must have all these roles.
	 * 
	 * @param role
	 *            Role.
	 */
	void addRequiredRole(String role);

	/**
	 * Optionally allows specifying which {@link HttpSecurity}.
	 * 
	 * @param securityName
	 *            Name of the {@link HttpSecurity}.
	 */
	void setQualifier(String securityName);

	/**
	 * Registers {@link HttpOfficeSecurer}.
	 * 
	 * @param securer
	 *            {@link HttpOfficeSecurer}.
	 * @return {@link HttpSecurerBuilder} to configure the
	 *         {@link HttpOfficeSecurer}.
	 */
	void secure(HttpOfficeSecurer securer);

	/**
	 * Creates the {@link HttpFlowSecurer}.
	 * 
	 * @return {@link HttpFlowSecurer}.
	 */
	HttpFlowSecurer createFlowSecurer();

}