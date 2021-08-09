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

package net.officefloor.plugin.section.clazz.object;

import net.officefloor.compile.spi.section.SectionDependencyObjectNode;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Manufactures the {@link Object} for {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionObjectManufacturer {

	/**
	 * Creates the {@link SectionDependencyObjectNode}.
	 * 
	 * @param context {@link ClassSectionObjectManufacturerContext}.
	 * @return {@link SectionDependencyObjectNode} or <code>null</code> to indicate
	 *         to use another {@link ClassSectionObjectManufacturer}.
	 * @throws Exception If fails to create {@link SectionDependencyObjectNode}.
	 */
	SectionDependencyObjectNode createObject(ClassSectionObjectManufacturerContext context) throws Exception;

}
