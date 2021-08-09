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

package net.officefloor.plugin.section.clazz.parameter.impl;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.ParameterAnnotation;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogator;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogatorContext;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogatorServiceFactory;

/**
 * {@link ClassSectionParameterInterrogator} for the
 * {@link ParameterAnnotation}.
 * 
 * @author Daniel Sagenschneider
 */
public class ParameterAnnotationClassSectionParameterInterrogator
		implements ClassSectionParameterInterrogator, ClassSectionParameterInterrogatorServiceFactory {

	/*
	 * ============= ClassSectionParameterInterrogatorServiceFactory =============
	 */

	@Override
	public ClassSectionParameterInterrogator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================== ClassSectionParameterInterrogator=======================
	 */

	@Override
	public boolean isParameter(ClassSectionParameterInterrogatorContext context) throws Exception {

		// Determine if have parameter annotation
		ParameterAnnotation parameter = context.getManagedFunctionObjectType().getAnnotation(ParameterAnnotation.class);
		return parameter != null;
	}

}
