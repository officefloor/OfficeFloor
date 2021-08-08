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

import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;

/**
 * Annotates an interface to be a {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SectionInterface {

	/**
	 * {@link SectionSource} class for this section.
	 * 
	 * @return {@link SectionSource} class.
	 */
	Class<? extends SectionSource> source();

	/**
	 * Obtains the location of the section.
	 * 
	 * @return Location of the section.
	 */
	String location() default "";

	/**
	 * <p>
	 * Obtains the location of the section as a {@link Class}.
	 * <p>
	 * This simplifies configuring fully qualified class names as the section
	 * location.
	 * 
	 * @return Location as a {@link Class}.
	 */
	Class<?> locationClass() default Void.class;

	/**
	 * Obtains the {@link PropertyValue} instances for the section.
	 * 
	 * @return {@link PropertyValue} instances for the section.
	 */
	PropertyValue[] properties() default {};

	/**
	 * Obtains the {@link SectionOutputLink} instances for the {@link SectionOutput}
	 * instances.
	 * 
	 * @return {@link SectionOutputLink} instances for the {@link SectionOutput}
	 *         instances.
	 */
	SectionOutputLink[] outputs() default {};

}
