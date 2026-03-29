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

import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.QualifierNameFactory;

/**
 * Annotates an {@link ObjectResponse} parameter to provide additional HTTP
 * response configuration.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Qualifier(nameFactory = HttpResponse.HttpResponseQualifierNameFactory.class)
public @interface HttpResponse {

	/**
	 * {@link QualifierNameFactory} to obtain qualified name for
	 * {@link HttpResponse}.
	 */
	public static class HttpResponseQualifierNameFactory implements QualifierNameFactory<HttpResponse> {
		@Override
		public String getQualifierName(HttpResponse annotation) {
			return String.valueOf(annotation.status());
		}
	}

	/**
	 * Specifies status to use for {@link ObjectResponse}.
	 * 
	 * @return Status to use for {@link ObjectResponse}.
	 */
	int status();

}
