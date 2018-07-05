/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.web.state.HttpApplicationState;

/**
 * Annotated on the class of the parameter to indicate it should be bound to the
 * application state. This allows for in-line configuration of application
 * objects.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpApplicationStateful {

	/**
	 * Allows specifying the name to bind the object into the
	 * {@link HttpApplicationState}.
	 * 
	 * @return Name to bind the object into the {@link HttpApplicationState}.
	 *         The blank default value indicates for the {@link ManagedObject}
	 *         to assign its own unique value.
	 */
	String bind() default "";

}