/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.application;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * <p>
 * Annotated on the class of the parameters to the {@link HttpTemplate} logic
 * class to indicate it should be a {@link HttpRequestObjectManagedObjectSource}
 * that will load the {@link HttpRequest} parameters onto the object.
 * <p>
 * This simplifies means to specifying
 * {@link HttpRequestObjectManagedObjectSource} instances by in-lining it with
 * the code.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpParameters {

	/**
	 * Allows specifying the name to bind the object into the
	 * {@link HttpRequestState}.
	 * 
	 * @return Name to bind the object into the {@link HttpRequestState}. The
	 *         blank default value indicates for the {@link ManagedObject} to
	 *         assign its own unique value.
	 */
	String value() default "";

}