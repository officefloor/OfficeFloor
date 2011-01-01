/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

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
	 * Obtains the {@link Property} instances.
	 * 
	 * @return {@link Property} instances.
	 */
	Property[] properties() default {};

	/**
	 * Obtains the {@link FlowLink} instances.
	 * 
	 * @return {@link FlowLink} instances.
	 */
	FlowLink[] flows() default {};

}