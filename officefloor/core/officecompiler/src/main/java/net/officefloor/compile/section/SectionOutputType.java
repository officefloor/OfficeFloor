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

package net.officefloor.compile.section;

import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.api.manage.Office;

/**
 * <code>Type definition</code> of an output for a {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionOutputType extends AnnotatedType {

	/**
	 * Obtains the name of this {@link SectionOutputType}.
	 * 
	 * @return Name of this {@link SectionOutputType}.
	 */
	String getSectionOutputName();

	/**
	 * <p>
	 * Obtains the fully qualified {@link Class} name of the argument type for this
	 * {@link SectionOutputType}.
	 * <p>
	 * The name is returned rather than the actual {@link Class} to enable the
	 * {@link SectionType} to be obtained should the {@link Class} not be available
	 * to the {@link ClassLoader}.
	 * 
	 * @return Fully qualified {@link Class} name of the argument type.
	 */
	String getArgumentType();

	/**
	 * <p>
	 * Indicates if this {@link SectionOutputType} is used only to handle
	 * {@link ManagedFunctionEscalationType} instances.
	 * <p>
	 * A {@link ManagedFunctionFlowType} must be connected to an
	 * {@link SectionInputType}, however a {@link ManagedFunctionEscalationType} may
	 * be generically handled by the {@link Office}.
	 * 
	 * @return <code>true</code> if this {@link SectionOutputType} is
	 *         {@link ManagedFunctionEscalationType} instances only.
	 */
	boolean isEscalationOnly();

}
