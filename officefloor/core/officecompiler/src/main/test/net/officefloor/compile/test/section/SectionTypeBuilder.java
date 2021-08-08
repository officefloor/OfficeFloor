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

package net.officefloor.compile.test.section;

import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.frame.api.escalate.Escalation;

/**
 * Facade builder for the {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionTypeBuilder {

	/**
	 * Adds an {@link SectionInputType}.
	 * 
	 * @param name          Name.
	 * @param parameterType Parameter type.
	 */
	void addSectionInput(String name, Class<?> parameterType);

	/**
	 * Adds an {@link SectionOutputType}.
	 * 
	 * @param name             Name.
	 * @param argumentType     Argument type.
	 * @param isEscalationOnly Flag indicating if escalation only.
	 */
	void addSectionOutput(String name, Class<?> argumentType, boolean isEscalationOnly);

	/**
	 * Adds a non-{@link Escalation} {@link SectionOutputType}.
	 * 
	 * @param name         Name.
	 * @param argumentType Argument type.
	 */
	void addSectionOutput(String name, Class<?> argumentType);

	/**
	 * Adds an {@link Escalation} {@link SectionOutputType}.
	 * 
	 * @param escalationType {@link Escalation} type.
	 */
	void addSectionEscalation(Class<?> escalationType);

	/**
	 * Adds an {@link SectionObjectType}.
	 * 
	 * @param name            Name.
	 * @param objectType      Object type.
	 * @param typeQualifier   Type qualifier.
	 * @param annotationTypes Expected annotation types.
	 */
	void addSectionObject(String name, Class<?> objectType, String typeQualifier, Class<?>... annotationTypes);

	/**
	 * Obtains the underlying {@link SectionDesigner}.
	 * 
	 * @return Underlying {@link SectionDesigner}.
	 */
	SectionDesigner getSectionDesigner();

}
