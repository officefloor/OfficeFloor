/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile;

import java.util.ServiceLoader;

import net.officefloor.compile.spi.section.source.SectionSource;

/**
 * <p>
 * {@link ServiceLoader} service to plug-in an {@link SectionSource}
 * {@link Class} alias by including the extension {@link SectionSource} jar on
 * the class path.
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
