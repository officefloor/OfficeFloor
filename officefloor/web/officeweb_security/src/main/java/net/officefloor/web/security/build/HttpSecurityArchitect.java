/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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

import java.io.Serializable;

import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Builds the {@link HttpSecurityBuilder} instances for the
 * {@link WebArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityArchitect {

	/**
	 * Adds a {@link HttpSecurity}.
	 *
	 * @param securityName
	 *            Name of the {@link HttpSecurityBuilder}. This name is use to
	 *            qualify dependency injection, should this particular
	 *            {@link HttpSecurityBuilder} be required.
	 * @param httpSecuritySourceClass
	 *            {@link HttpSecuritySource} {@link Class}.
	 * @return {@link HttpSecurityBuilder}.
	 */
	<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityBuilder addHttpSecurity(
			String securityName, Class<? extends HttpSecuritySource<A, AC, C, O, F>> httpSecuritySourceClass);

	/**
	 * Adds a {@link HttpSecurity}.
	 * 
	 * @param securityName
	 *            Name of the {@link HttpSecurityBuilder}. This name is use to
	 *            qualify dependency injection, should this particular
	 *            {@link HttpSecurityBuilder} be required.
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 * @return {@link HttpSecurityBuilder}.
	 */
	<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityBuilder addHttpSecurity(
			String securityName, HttpSecuritySource<A, AC, C, O, F> httpSecuritySource);

	/**
	 * Creates a {@link HttpSecurerBuilder}.
	 * 
	 * @return {@link HttpSecurerBuilder}.
	 */
	HttpSecurerBuilder createHttpSecurer();

	/**
	 * Informs the {@link WebArchitect} of the necessary security. This is to be
	 * invoked once all security is configured.
	 */
	void informWebArchitect();

}