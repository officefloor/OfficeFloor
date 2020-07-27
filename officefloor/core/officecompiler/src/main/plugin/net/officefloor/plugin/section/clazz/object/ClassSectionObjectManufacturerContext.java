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
