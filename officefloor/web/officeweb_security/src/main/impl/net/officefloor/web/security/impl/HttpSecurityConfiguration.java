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
package net.officefloor.web.security.impl;

import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpAccessControlFactory;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * {@link HttpSecurity} configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityConfiguration<A, AC, C, O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Obtains the {@link HttpSecurity}.
	 * 
	 * @return {@link HttpSecurity}.
	 */
	HttpSecurity<A, AC, C, O, F> getHttpSecurity();

	/**
	 * Obtains the {@link HttpAccessControlFactory}.
	 * 
	 * @return {@link HttpAccessControlFactory}.
	 */
	HttpAccessControlFactory<AC> getAccessControlFactory();

	/**
	 * Obtains the {@link HttpSecurityType}.
	 * 
	 * @return {@link HttpSecurityType}.
	 */
	HttpSecurityType<A, AC, C, O, F> getHttpSecurityType();

}