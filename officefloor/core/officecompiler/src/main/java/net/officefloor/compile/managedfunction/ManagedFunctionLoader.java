/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
	 * @param managedFunctionSourceClass Class of the {@link ManagedFunctionSource}.
	 * @param properties                 {@link PropertyList} containing the
	 *                                   properties to source the
	 *                                   {@link FunctionNamespaceType}.
	 * @return {@link FunctionNamespaceType} or <code>null</code> if issues, which
	 *         is reported to the {@link CompilerIssues}.
	 */
	<S extends ManagedFunctionSource> FunctionNamespaceType loadManagedFunctionType(Class<S> managedFunctionSourceClass,
			PropertyList properties);

	/**
	 * Loads and returns the {@link FunctionNamespaceType} from the
	 * {@link ManagedFunctionSource} class.
	 * 
	 * @param managedFunctionSource {@link ManagedFunctionSource} instance.
	 * @param properties            {@link PropertyList} containing the properties
	 *                              to source the {@link FunctionNamespaceType}.
	 * @return {@link FunctionNamespaceType} or <code>null</code> if issues, which
	 *         is reported to the {@link CompilerIssues}.
	 */
	FunctionNamespaceType loadManagedFunctionType(ManagedFunctionSource managedFunctionSource, PropertyList properties);

}
