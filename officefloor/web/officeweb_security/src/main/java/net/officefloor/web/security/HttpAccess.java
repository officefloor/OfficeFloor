/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
	String withQualifier() default "";

	/**
	 * Provides means to allow access if have role in any one of the configured
	 * roles.
	 * 
	 * @return Multiple roles that must support at least one for access to the
	 *         {@link ManagedFunction} {@link Method}.
	 */
	String[] ifRole() default {};

	/**
	 * Provides means to all access if have <strong>ALL</strong> roles.
	 * 
	 * @return All roles that must be supported for access to the
	 *         {@link ManagedFunction} {@link Method}.
	 */
	String[] ifAllRoles() default {};

}