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
