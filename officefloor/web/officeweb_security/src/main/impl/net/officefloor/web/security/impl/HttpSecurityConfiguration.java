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

package net.officefloor.web.security.impl;

import java.io.Serializable;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Configuration for {@link HttpSecuritySectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityConfiguration<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link HttpSecurity}.
	 * 
	 * @return Name of the {@link HttpSecurity}.
	 */
	String getHttpSecurityName();

	/**
	 * Obtains the {@link HttpSecurity}.
	 * 
	 * @return {@link HttpSecurity}.
	 */
	HttpSecurity<A, AC, C, O, F> getHttpSecurity();

	/**
	 * Obtains the {@link Flow} key {@link Enum} {@link Class}.
	 * 
	 * @return {@link Flow} key {@link Enum} {@link Class}.
	 */
	Class<F> getFlowKeyClass();

	/**
	 * Obtains the {@link HttpSecurityType}.
	 * 
	 * @return {@link HttpSecurityType}.
	 */
	HttpSecurityType<A, AC, C, O, F> getHttpSecurityType();

}
