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

package net.officefloor.compile.spi.section;

import net.officefloor.compile.internal.structure.FunctionNamespaceNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyConfigurable;

/**
 * {@link FunctionNamespaceNode} within the {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionFunctionNamespace extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link SectionFunctionNamespace}.
	 * 
	 * @return Name of this {@link SectionFunctionNamespace}.
	 */
	String getSectionFunctionNamespaceName();

	/**
	 * Adds a {@link SectionFunction}.
	 * 
	 * @param functionName
	 *            Name of the {@link SectionFunction}.
	 * @param functionTypeName
	 *            Name of the {@link ManagedFunctionType} on the
	 *            {@link FunctionNamespaceType}.
	 * @return {@link SectionFunction}.
	 */
	SectionFunction addSectionFunction(String functionName, String functionTypeName);

}
