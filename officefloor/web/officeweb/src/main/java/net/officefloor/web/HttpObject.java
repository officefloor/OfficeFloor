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

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.build.HttpObjectParser;

/**
 * {@link Annotation} on the {@link Class} of the parameter (or the parameter
 * itself) to indicate it is parsed out of the {@link HttpRequest} via an
 * {@link HttpObjectParser}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpObject {

	/**
	 * <p>
	 * Leaving an empty array will provide all <code>Content-Type</code> parsing
	 * available for the {@link Object}.
	 * <p>
	 * This is provided to restrict <code>Content-Type</code> parsing.
	 * 
	 * @return Restricted list of <code>Content-Type</code> parsing available
	 *         for the {@link Object}.
	 */
	String[] acceptedContentTypes() default {};

}
