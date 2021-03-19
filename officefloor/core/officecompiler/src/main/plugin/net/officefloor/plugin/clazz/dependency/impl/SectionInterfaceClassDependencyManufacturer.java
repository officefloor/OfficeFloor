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

package net.officefloor.plugin.clazz.dependency.impl;

import java.lang.annotation.Annotation;

import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext.ClassFlow;
import net.officefloor.plugin.clazz.flow.ClassFlowContext;
import net.officefloor.plugin.section.clazz.SectionInterface;
import net.officefloor.plugin.section.clazz.SectionNameAnnotation;

/**
 * {@link ClassDependencyManufacturer} for {@link SectionInterface}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionInterfaceClassDependencyManufacturer extends AbstractFlowClassDependencyManufacturer {

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return SectionInterface.class;
	}

	@Override
	protected int buildFlow(ClassFlow classFlow, ClassFlowContext flowContext) {

		// Add the section name
		Class<?> flowInterface = flowContext.getFlowInterfaceType();
		classFlow.addAnnotation(new SectionNameAnnotation(flowInterface.getSimpleName()));

		// Build the flow
		return classFlow.build().getIndex();
	}

}
