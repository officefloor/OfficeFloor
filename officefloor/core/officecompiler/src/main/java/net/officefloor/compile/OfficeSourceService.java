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

import net.officefloor.compile.spi.office.source.OfficeSource;

/**
 * <p>
 * {@link ServiceLoader} service to plug-in an {@link OfficeSource}
 * {@link Class} alias by including the extension {@link OfficeSource} jar on
 * the class path.
 * <p>
 * {@link OfficeFloorCompiler#addOfficeSourceAlias(String, Class)} will be
 * invoked for each found {@link OfficeSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSourceService<S extends OfficeSource> {

	/**
	 * Obtains the alias for the {@link OfficeSource} {@link Class}.
	 * 
	 * @return Alias for the {@link OfficeSource} {@link Class}.
	 */
	String getOfficeSourceAlias();

	/**
	 * Obtains the {@link OfficeSource} {@link Class}.
	 * 
	 * @return {@link OfficeSource} {@link Class}.
	 */
	Class<S> getOfficeSourceClass();

}
