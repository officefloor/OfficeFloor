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

import net.officefloor.compile.spi.office.ManagedFunctionAugmentor;
import net.officefloor.compile.spi.section.SectionDependencyRequireNode;
import net.officefloor.compile.type.AnnotatedType;

/**
 * Context for the {@link ClassSectionObjectManufacturer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionObjectManufacturerContext extends ClassSectionObjectContext {

	/**
	 * Obtains the {@link AnnotatedType} of {@link SectionDependencyRequireNode}.
	 * 
	 * @return {@link AnnotatedType} of {@link SectionDependencyRequireNode}.
	 */
	AnnotatedType getAnnotatedType();

	/**
	 * Flags the dependency is being provided by a {@link ManagedFunctionAugmentor}.
	 */
	void flagAugmented();

}
