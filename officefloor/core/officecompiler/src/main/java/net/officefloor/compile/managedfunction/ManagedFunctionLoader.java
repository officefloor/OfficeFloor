/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.managedfunction;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceProperty;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceSpecification;

/**
 * Loads the {@link FunctionNamespaceType} from the
 * {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link ManagedFunctionSourceSpecification} for the
	 * {@link ManagedFunctionSource}.
	 * 
	 * @param <S>                       {@link ManagedFunctionSource} type.
	 * @param mangedFunctionSourceClass Class of the {@link ManagedFunctionSource}.
	 * @return {@link PropertyList} of the {@link ManagedFunctionSourceProperty}
	 *         instances of the {@link ManagedFunctionSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<S extends ManagedFunctionSource> PropertyList loadSpecification(Class<S> mangedFunctionSourceClass);

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link ManagedFunctionSourceSpecification} for the
	 * {@link ManagedFunctionSource}.
	 * 
	 * @param managedFunctionSource {@link ManagedFunctionSource} instance.
	 * @return {@link PropertyList} of the {@link ManagedFunctionSourceProperty}
	 *         instances of the {@link ManagedFunctionSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	PropertyList loadSpecification(ManagedFunctionSource managedFunctionSource);

	/**
	 * Loads and returns the {@link FunctionNamespaceType} from the
	 * {@link ManagedFunctionSource} class.
	 * 
	 * @param <S>                        {@link ManagedFunctionSource} type.
	 * @param managedFunctionSourceName  Name of the {@link ManagedFunctionSource}.
	 * @param managedFunctionSourceClass Class of the {@link ManagedFunctionSource}.
	 * @param properties                 {@link PropertyList} containing the
	 *                                   properties to source the
	 *                                   {@link FunctionNamespaceType}.
	 * @return {@link FunctionNamespaceType} or <code>null</code> if issues, which
	 *         is reported to the {@link CompilerIssues}.
	 */
	<S extends ManagedFunctionSource> FunctionNamespaceType loadManagedFunctionType(String managedFunctionSourceName,
			Class<S> managedFunctionSourceClass, PropertyList properties);

	/**
	 * Loads and returns the {@link FunctionNamespaceType} from the
	 * {@link ManagedFunctionSource} class.
	 * 
	 * @param managedFunctionSourceName Name of the {@link ManagedFunctionSource}.
	 * @param managedFunctionSource     {@link ManagedFunctionSource} instance.
	 * @param properties                {@link PropertyList} containing the
	 *                                  properties to source the
	 *                                  {@link FunctionNamespaceType}.
	 * @return {@link FunctionNamespaceType} or <code>null</code> if issues, which
	 *         is reported to the {@link CompilerIssues}.
	 */
	FunctionNamespaceType loadManagedFunctionType(String managedFunctionSourceName,
			ManagedFunctionSource managedFunctionSource, PropertyList properties);

}