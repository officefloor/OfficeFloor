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

package net.officefloor.plugin.section.clazz.spawn;

import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.spi.section.source.SectionSourceContext;

/**
 * Context for the {@link ClassSectionFlowSpawnInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionFlowSpawnInterrogatorContext {

	/**
	 * Obtains the {@link ManagedFunctionFlowType}.
	 * 
	 * @return {@link ManagedFunctionFlowType}.
	 */
	ManagedFunctionFlowType<?> getManagedFunctionFlowType();

	/**
	 * Obtains the {@link SectionSourceContext}.
	 * 
	 * @return {@link SectionSourceContext}.
	 */
	SectionSourceContext getSourceContext();

}
