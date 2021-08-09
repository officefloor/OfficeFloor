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

package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Annotates a {@link Field} for the {@link ClassSectionSource} for
 * configuration of a {@link SectionManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManagedObject {

	/**
	 * Obtains the implementing {@link ManagedObjectSource}.
	 * 
	 * @return Implementing {@link ManagedObjectSource}.
	 */
	Class<? extends ManagedObjectSource<?, ?>> source();

	/**
	 * Obtains the {@link PropertyValue} instances.
	 * 
	 * @return {@link PropertyValue} instances.
	 */
	PropertyValue[] properties() default {};

	/**
	 * Obtains the {@link TypeQualifier} instances.
	 * 
	 * @return {@link TypeQualifier} instances.
	 */
	TypeQualifier[] qualifiers() default {};

	/**
	 * Obtains the {@link SectionOutputLink} instances.
	 * 
	 * @return {@link SectionOutputLink} instances.
	 */
	SectionOutputLink[] flows() default {};

}
