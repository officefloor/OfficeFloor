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

package net.officefloor.compile;

import net.officefloor.compile.spi.section.source.SectionSource;

/**
 * <p>
 * Service to plug-in an {@link SectionSource} {@link Class} alias by including
 * the extension {@link SectionSource} jar on the class path.
 * <p>
 * {@link OfficeFloorCompiler#addSectionSourceAlias(String, Class)} will be
 * invoked for each found {@link SectionSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionSourceService<S extends SectionSource> {

	/**
	 * Obtains the alias for the {@link SectionSource} {@link Class}.
	 * 
	 * @return Alias for the {@link SectionSource} {@link Class}.
	 */
	String getSectionSourceAlias();

	/**
	 * Obtains the {@link SectionSource} {@link Class}.
	 * 
	 * @return {@link SectionSource} {@link Class}.
	 */
	Class<S> getSectionSourceClass();

}
