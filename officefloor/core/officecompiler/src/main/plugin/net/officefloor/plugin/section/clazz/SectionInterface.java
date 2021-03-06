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
