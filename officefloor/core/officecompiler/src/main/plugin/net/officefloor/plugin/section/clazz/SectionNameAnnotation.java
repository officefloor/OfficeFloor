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

package net.officefloor.plugin.section.clazz;

import net.officefloor.compile.spi.section.SubSection;

/**
 * Name of {@link SubSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionNameAnnotation {

	/**
	 * Name of the {@link SubSection}.
	 */
	private final String name;

	/**
	 * Instantiate.
	 * 
	 * @param name Name of the {@link SubSection}.
	 */
	public SectionNameAnnotation(String name) {
		this.name = name;
	}

	/**
	 * Obtains the name of the {@link SubSection}.
	 * 
	 * @return Name of the {@link SubSection}.
	 */
	public String getName() {
		return this.name;
	}

}
