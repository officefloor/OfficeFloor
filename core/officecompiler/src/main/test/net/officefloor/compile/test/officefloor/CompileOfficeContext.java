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

package net.officefloor.compile.test.officefloor;

import java.util.function.Consumer;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.variable.Var;

/**
 * Context for compiling the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileOfficeContext {

	/**
	 * Obtains the {@link OfficeArchitect}.
	 * 
	 * @return {@link OfficeArchitect}.
	 */
	OfficeArchitect getOfficeArchitect();

	/**
	 * Obtains the {@link OfficeSourceContext}.
	 * 
	 * @return {@link OfficeSourceContext}.
	 */
	OfficeSourceContext getOfficeSourceContext();

	/**
	 * Adds an {@link OfficeManagedObject} for {@link ClassManagedObjectSource}.
	 * 
	 * @param managedObjectName  Name of the {@link OfficeManagedObject}.
	 * @param managedObjectClass {@link Class} for the
	 *                           {@link ClassManagedObjectSource}.
	 * @param scope              {@link ManagedObjectScope}.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addManagedObject(String managedObjectName, Class<?> managedObjectClass,
			ManagedObjectScope scope);

	/**
	 * Adds an {@link OfficeSection} for {@link ClassSectionSource}.
	 * 
	 * @param sectionName  Name of the {@link OfficeSection}.
	 * @param sectionClass {@link Class} for the {@link ClassSectionSource}.
	 * @return {@link OfficeSection}.
	 */
	OfficeSection addSection(String sectionName, Class<?> sectionClass);

	/**
	 * Listens to a variable.
	 * 
	 * @param            <T> Variable type.
	 * @param qualifier  Qualifier for variable. May be <code>null</code>.
	 * @param type       Type for variable.
	 * @param compileVar Typical {@link CompileVar} to handle value. May, however,
	 *                   be any {@link Consumer} for the created {@link Var}.
	 */
	<T> void variable(String qualifier, Class<T> type, Consumer<Var<T>> compileVar);

	/**
	 * Obtains the {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSection}.
	 */
	OfficeSection getOfficeSection();

	/**
	 * Overrides the default {@link OfficeSection}.
	 * 
	 * @param sectionSourceClass {@link SectionSource} {@link Class}.
	 * @param sectionLocation    Location of the {@link OfficeSection}.
	 * @return Overridden {@link OfficeSection}.
	 */
	OfficeSection overrideSection(Class<? extends SectionSource> sectionSourceClass, String sectionLocation);

}
