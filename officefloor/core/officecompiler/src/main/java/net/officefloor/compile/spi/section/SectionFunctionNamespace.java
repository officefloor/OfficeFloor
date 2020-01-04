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
