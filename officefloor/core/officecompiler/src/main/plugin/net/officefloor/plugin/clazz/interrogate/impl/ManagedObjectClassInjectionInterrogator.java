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

package net.officefloor.plugin.clazz.interrogate.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.interrogate.ClassInjectionInterrogator;
import net.officefloor.plugin.clazz.interrogate.ClassInjectionInterrogatorContext;
import net.officefloor.plugin.clazz.interrogate.ClassInjectionInterrogatorServiceFactory;
import net.officefloor.plugin.section.clazz.ManagedObject;

/**
 * {@link ManagedObject} {@link ClassInjectionInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectClassInjectionInterrogator
		implements ClassInjectionInterrogator, ClassInjectionInterrogatorServiceFactory {

	/*
	 * ================ ClassInjectionInterrogatorServiceFactory ==========
	 */

	@Override
	public ClassInjectionInterrogator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ==================== ClassInjectionInterrogator ====================
	 */

	@Override
	public void interrogate(ClassInjectionInterrogatorContext context) throws Exception {

		// Load if flow interface field
		// (Constructors/Methods identified via other means)
		AnnotatedElement element = context.getAnnotatedElement();
		if (element instanceof Field) {
			Field field = (Field) element;

			// Determine if field managed object
			if (field.isAnnotationPresent(ManagedObject.class)) {

				// Register the injection of dependency
				context.registerInjectionPoint(field);
			}
		}
	}

}
