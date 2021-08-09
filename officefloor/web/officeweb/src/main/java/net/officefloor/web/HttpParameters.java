/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.state.HttpRequestObjectManagedObjectSource;
import net.officefloor.web.state.HttpRequestState;

/**
 * <p>
 * Annotated on the class of the parameter to indicate it should be a
 * {@link HttpRequestObjectManagedObjectSource} that will load the
 * {@link HttpRequest} parameters onto the object.
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
	String bind() default "";

}
