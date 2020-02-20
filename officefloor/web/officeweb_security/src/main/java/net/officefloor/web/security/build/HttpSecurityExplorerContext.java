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

import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.web.security.type.HttpSecurityFlowType;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Context for the {@link HttpSecurityExplorer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityExplorerContext {

	/**
	 * Obtains the name of the {@link HttpSecurity}.
	 * 
	 * @return Name of the {@link HttpSecurity}.
	 */
	String getHttpSecurityName();

	/**
	 * Obtains the {@link HttpSecuritySource}.
	 * 
	 * @return {@link HttpSecuritySource}.
	 */
	HttpSecuritySource<?, ?, ?, ?, ?> getHttpSecuritySource();

	/**
	 * Obtains the {@link HttpSecurityType}.
	 * 
	 * @return {@link HttpSecurityType}.
	 */
	HttpSecurityType<?, ?, ?, ?, ?> getHttpSecurityType();

	/**
	 * Obtains the {@link ExecutionManagedFunction} for the
	 * {@link HttpSecurityFlowType}.
	 * 
	 * @param flowType {@link HttpSecurityFlowType}.
	 * @return {@link ExecutionManagedFunction} for the
	 *         {@link HttpSecurityFlowType}.
	 */
	ExecutionManagedFunction getManagedFunction(HttpSecurityFlowType<?> flowType);

}
