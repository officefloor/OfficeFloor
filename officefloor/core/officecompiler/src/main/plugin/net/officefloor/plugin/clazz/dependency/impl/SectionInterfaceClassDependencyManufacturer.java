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
