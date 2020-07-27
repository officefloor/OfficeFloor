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

package net.officefloor.plugin.section.clazz.object.impl;

import net.officefloor.compile.spi.section.SectionDependencyObjectNode;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturer;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturerContext;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturerServiceFactory;
import net.officefloor.plugin.variable.VariableAnnotation;
import net.officefloor.plugin.variable.VariableManagedObjectSource;

/**
 * {@link ClassSectionObjectManufacturer} for
 * {@link VariableManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableSectionClassObjectManufacturer
		implements ClassSectionObjectManufacturer, ClassSectionObjectManufacturerServiceFactory {

	/*
	 * ============== SectionClassObjectManufacturerServiceFactory ==============
	 */

	@Override
	public ClassSectionObjectManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ===================== SectionClassObjectManufacturer =====================
	 */

	@Override
	public SectionDependencyObjectNode createObject(ClassSectionObjectManufacturerContext context) throws Exception {

		// Determine if variable
		VariableAnnotation variable = context.getAnnotatedType().getAnnotation(VariableAnnotation.class);
		if (variable != null) {
			// Will augment the variable
			context.flagAugmented();
		}

		// Nothing to link
		return null;
	}

}
