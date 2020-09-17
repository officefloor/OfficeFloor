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

package net.officefloor.test;

import net.officefloor.compile.state.autowire.AutoWireStateManager;

/**
 * Context for the {@link TestDependencyService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TestDependencyServiceContext {

	/**
	 * Obtains the qualifier of required dependency.
	 * 
	 * @return Qualifier of required dependency. May be <code>null</code>.
	 */
	String getQualifier();

	/**
	 * Obtains the type of required dependency.
	 * 
	 * @return Type of required dependency. May be <code>null</code>.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the {@link AutoWireStateManager}.
	 * 
	 * @return {@link AutoWireStateManager}.
	 */
	AutoWireStateManager getStateManager();

	/**
	 * Obtains the load timeout for the {@link AutoWireStateManager}.
	 * 
	 * @return Load timeout for the {@link AutoWireStateManager}.
	 */
	long getLoadTimeout();

}
