/*-
 * #%L
 * Web Template
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
