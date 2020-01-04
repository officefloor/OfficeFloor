/*-
 * #%L
 * Web Template
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

package net.officefloor.web.template.type;

import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.frame.api.manage.Office;

/**
 * <code>Type definition</code> of an output for a {@link WebTemplateType}.
 *
 * @author Daniel Sagenschneider
 */
public interface WebTemplateOutputType {

	/**
	 * Obtains the name of this {@link WebTemplateOutputType}.
	 * 
	 * @return Name of this {@link WebTemplateOutputType}.
	 */
	String getWebTemplateOutputName();

	/**
	 * <p>
	 * Obtains the fully qualified {@link Class} name of the argument type for this
	 * {@link WebTemplateOutputType}.
	 * <p>
	 * The name is returned rather than the actual {@link Class} to enable the
	 * {@link WebTemplateType} to be obtained should the {@link Class} not be
	 * available to the {@link ClassLoader}.
	 * 
	 * @return Fully qualified {@link Class} name of the argument type.
	 */
	String getArgumentType();

	/**
	 * <p>
	 * Indicates if this {@link WebTemplateOutputType} is used only to handle
	 * {@link ManagedFunctionEscalationType} instances.
	 * <p>
	 * A {@link ManagedFunctionFlowType} must be connected to an input, however a
	 * {@link ManagedFunctionEscalationType} may be generically handled by the
	 * {@link Office}.
	 * 
	 * @return <code>true</code> if this {@link WebTemplateOutputType} is
	 *         {@link ManagedFunctionEscalationType} instances only.
	 */
	boolean isEscalationOnly();

	/**
	 * Obtains the annotations for the {@link WebTemplateOutputType}.
	 * 
	 * @return Annotations for the {@link WebTemplateOutputType}.
	 */
	Object[] getAnnotations();

}
