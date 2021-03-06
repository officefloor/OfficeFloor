/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Context for the {@link ExecutionExplorer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutionExplorerContext {

	/**
	 * Obtains the initial {@link ExecutionManagedFunction} for the
	 * {@link OfficeSectionInput}.
	 * 
	 * @return Initial {@link ExecutionManagedFunction} for the
	 *         {@link OfficeSectionInput}.
	 */
	ExecutionManagedFunction getInitialManagedFunction();

	/**
	 * <p>
	 * Obtains the {@link ExecutionManagedFunction} by {@link ManagedFunction}
	 * name.
	 * <p>
	 * This enables obtaining dynamically invoked {@link ManagedFunction}
	 * instances via execution.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return {@link ExecutionManagedFunction}.
	 */
	ExecutionManagedFunction getManagedFunction(String functionName);

}
