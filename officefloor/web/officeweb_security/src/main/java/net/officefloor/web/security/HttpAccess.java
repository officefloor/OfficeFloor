/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * <p>
 * {@link Annotation} on a {@link ManagedFunction} {@link Method} to control
 * access.
 * <p>
 * Should no meta-data be provided on this {@link Annotation}, then access is
 * granted if authenticated to any role.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface HttpAccess {

	/**
	 * Provides qualifier of which {@link HttpSecurity} to use for access
	 * control.
	 * 
	 * @return Qualifier of which {@link HttpSecurity} to use for access
	 *         control. Leaving blank allows any configured
	 *         {@link HttpSecurity}.
	 */
	String withHttpSecurity() default "";

	/**
	 * Provides means to allow access if have role in any one of the configured
	 * roles.
	 * 
	 * @return Multiple roles that must support at least one for access to the
	 *         {@link ManagedFunction} {@link Method}.
	 */
	String[] ifRole() default {};

	/**
	 * Provides means to allow access if have <strong>ALL</strong> roles.
	 * 
	 * @return All roles that must be supported for access to the
	 *         {@link ManagedFunction} {@link Method}.
	 */
	String[] ifAllRoles() default {};

}
