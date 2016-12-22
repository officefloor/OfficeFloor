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
package net.officefloor.compile.managedfunction;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceProperty;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceSpecification;
import net.officefloor.frame.api.execute.Work;

/**
 * Loads the {@link FunctionNamespaceType} from the
 * {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link ManagedFunctionSourceSpecification} for the {@link ManagedFunctionSource}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param <WS>
	 *            {@link ManagedFunctionSource} type.
	 * @param mangedFunctionSourceClass
	 *            Class of the {@link ManagedFunctionSource}.
	 * @return {@link PropertyList} of the {@link ManagedFunctionSourceProperty} instances
	 *         of the {@link ManagedFunctionSourceSpecification} or <code>null</code> if
	 *         issue, which is reported to the {@link CompilerIssues}.
	 */
	<W extends Work, WS extends ManagedFunctionSource<W>> PropertyList loadSpecification(
			Class<WS> mangedFunctionSourceClass);

	/**
	 * Loads and returns the {@link FunctionNamespaceType} from the
	 * {@link ManagedFunctionSource} class.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param <WS>
	 *            {@link ManagedFunctionSource} type.
	 * @param workSourceClass
	 *            Class of the {@link ManagedFunctionSource}.
	 * @param properties
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link FunctionNamespaceType}.
	 * @return {@link FunctionNamespaceType} or <code>null</code> if issues,
	 *         which is reported to the {@link CompilerIssues}.
	 */
	<W extends Work, WS extends ManagedFunctionSource<W>> FunctionNamespaceType<W> loadFunctionNamespaceType(
			Class<WS> workSourceClass, PropertyList properties);

}