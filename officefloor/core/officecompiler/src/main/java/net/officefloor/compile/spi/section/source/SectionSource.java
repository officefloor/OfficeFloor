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

package net.officefloor.compile.spi.section.source;

import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionDesigner;

/**
 * Sources the {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionSource {

	/**
	 * <p>
	 * Obtains the {@link SectionSourceSpecification} for this
	 * {@link SectionSource}.
	 * <p>
	 * This enables the {@link SectionSourceContext} to be populated with the
	 * necessary details as per this {@link SectionSourceSpecification} in
	 * loading the {@link SectionType}.
	 * 
	 * @return {@link SectionSourceSpecification}.
	 */
	SectionSourceSpecification getSpecification();

	/**
	 * Sources the {@link OfficeSection} by constructing it via the input
	 * {@link SectionDesigner}.
	 * 
	 * @param designer
	 *            {@link SectionDesigner} to construct the structure of the
	 *            {@link OfficeSection}.
	 * @param context
	 *            {@link SectionSourceContext} to source details to construct
	 *            the {@link OfficeSection}.
	 * @throws Exception
	 *             If fails to construct the {@link OfficeSection}.
	 */
	void sourceSection(SectionDesigner designer, SectionSourceContext context)
			throws Exception;

}
