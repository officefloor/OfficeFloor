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

package net.officefloor.plugin.section.clazz.parameter.impl;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogator;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogatorContext;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogatorServiceFactory;

/**
 * {@link ClassSectionParameterInterrogator} for the {@link Parameter}.
 * 
 * @author Daniel Sagenschneider
 */
public class ParameterClassSectionParameterInterrogator
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
		Parameter parameter = context.getManagedFunctionObjectType().getAnnotation(Parameter.class);
		return parameter != null;
	}

}
