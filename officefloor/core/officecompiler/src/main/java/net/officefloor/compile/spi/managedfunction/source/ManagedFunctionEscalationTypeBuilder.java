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

package net.officefloor.compile.spi.managedfunction.source;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * Provides means for the {@link ManagedFunctionSource} to provide a
 * <code>type definition</code> of a possible {@link EscalationFlow} by the
 * {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionEscalationTypeBuilder {

	/**
	 * <p>
	 * Provides means to specify a display label for the {@link EscalationFlow}.
	 * <p>
	 * This need not be set as is only an aid to better identify the
	 * {@link EscalationFlow}. If not set it will use the <code>Simple</code> name
	 * of the {@link EscalationFlow} {@link Class}.
	 * 
	 * @param label Display label for the {@link EscalationFlow}.
	 * @return <code>this</code>.
	 */
	ManagedFunctionEscalationTypeBuilder setLabel(String label);

}
