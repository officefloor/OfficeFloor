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

package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Documented;

import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link SectionOutputLink} of a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
public @interface SectionOutputLink {

	/**
	 * Obtains the name of the {@link ManagedObjectFlow}.
	 * 
	 * @return Name of the {@link ManagedObjectFlow}.
	 */
	String name();

	/**
	 * Obtains the name of the {@link SectionFlowSinkNode} to link the
	 * {@link SectionOutputLink}.
	 * 
	 * @return Name of the {@link SectionFlowSinkNode} to link the
	 *         {@link SectionOutputLink}.
	 */
	String link();

}
