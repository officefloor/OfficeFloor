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

package net.officefloor.plugin.section.clazz.flow.impl;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturer;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerContext;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerServiceFactory;
import net.officefloor.plugin.section.clazz.loader.ClassSectionFlow;

/**
 * {@link ClassSectionFlowManufacturer} for {@link Next}.
 * 
 * @author Daniel Sagenschneider
 */
public class NextClassSectionFlowManufacturer
		implements ClassSectionFlowManufacturer, ClassSectionFlowManufacturerServiceFactory {

	/*
	 * ============== ClassSectionFlowManufacturerServiceFactory ==============
	 */

	@Override
	public ClassSectionFlowManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ==================== ClassSectionFlowManufacturer ======================
	 */

	@Override
	public ClassSectionFlow createFlow(ClassSectionFlowManufacturerContext context) throws Exception {

		// Obtain the possible next
		AnnotatedType annotatedType = context.getAnnotatedType();
		Next next = annotatedType.getAnnotation(Next.class);
		if (next == null) {
			return null; // no next
		}

		// Obtain the function type (as must be managed function)
		ManagedFunctionType<?, ?> functionType = (ManagedFunctionType<?, ?>) annotatedType;

		// Obtain the argument type
		Class<?> returnType = functionType.getReturnType();
		Class<?> argumentType = ((returnType == null) || (void.class.equals(returnType))
				|| (Void.TYPE.equals(returnType))) ? null : returnType;

		// Obtain the next flow sink
		return context.getFlow(next.value(), argumentType != null ? argumentType.getName() : null);
	}

}
