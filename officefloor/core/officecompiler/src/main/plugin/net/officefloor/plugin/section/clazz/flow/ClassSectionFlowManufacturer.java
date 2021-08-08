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

package net.officefloor.plugin.section.clazz.flow;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.loader.ClassSectionFlow;

/**
 * Manufactures the {@link Flow} for {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionFlowManufacturer {

	/**
	 * Creates the {@link ClassSectionFlow}.
	 * 
	 * @param context {@link ClassSectionFlowManufacturerContext}.
	 * @return {@link ClassSectionFlow} or <code>null</code> to indicate to use
	 *         another {@link ClassSectionFlowManufacturer}.
	 * @throws Exception If fails to create {@link ClassSectionFlow}.
	 */
	ClassSectionFlow createFlow(ClassSectionFlowManufacturerContext context) throws Exception;

}
