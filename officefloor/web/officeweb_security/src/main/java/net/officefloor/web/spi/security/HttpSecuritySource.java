/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.web.spi.security;

import net.officefloor.server.http.HttpException;

/**
 * <p>
 * Source for obtaining {@link HttpSecurity}.
 * <p>
 * As security is specific to applications, both the security object and
 * credentials are specified by the application.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySource<A, AC, C, O extends Enum<O>, F extends Enum<F>> {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	HttpSecuritySourceSpecification getSpecification();

	/**
	 * Initialises the {@link HttpSecuritySource}.
	 * 
	 * @param context
	 *            {@link HttpSecuritySourceContext} to use in initialising.
	 * @return Meta-data to describe this.
	 * @throws Exception
	 *             Should the {@link HttpSecuritySource} fail to configure
	 *             itself from the input properties.
	 */
	HttpSecuritySourceMetaData<A, AC, C, O, F> init(HttpSecuritySourceContext context) throws Exception;

	/**
	 * Sources the {@link HttpSecurity}.
	 * 
	 * @param context
	 *            {@link HttpSecurity}.
	 * @return {@link HttpSecurity}.
	 * @throws HttpException
	 *             If fails to source the {@link HttpSecurity}.
	 */
	HttpSecurity<A, AC, C, O, F> sourceHttpSecurity(HttpSecurityContext context) throws HttpException;

}