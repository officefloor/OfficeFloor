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
import java.net.HttpCookie;

import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.QualifierNameFactory;

/**
 * {@link Annotation} to indicate the value is loaded from a {@link HttpCookie}
 * parameter.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Qualifier(nameFactory = HttpCookieParameter.HttpCookieParameterNameFactory.class)
public @interface HttpCookieParameter {

	/**
	 * {@link QualifierNameFactory}.
	 */
	public static class HttpCookieParameterNameFactory implements QualifierNameFactory<HttpCookieParameter> {

		/**
		 * Obtains the qualifier from name.
		 * 
		 * @param name Name.
		 * @return Qualifier.
		 */
		public static String getQualifier(String name) {
			return HttpCookieParameter.class.getSimpleName() + "_" + name;
		}

		/*
		 * ================== QualifierNameFactory =======================
		 */

		@Override
		public String getQualifierName(HttpCookieParameter annotation) {
			return getQualifier(annotation.value());
		}
	}

	/**
	 * Name of parameter.
	 * 
	 * @return Name parameter.
	 */
	String value();

}
