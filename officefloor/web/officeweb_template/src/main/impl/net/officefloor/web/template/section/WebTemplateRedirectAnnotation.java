/*-
 * #%L
 * Web Template
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

package net.officefloor.web.template.section;

import net.officefloor.web.HttpInputPath;
import net.officefloor.web.template.build.WebTemplate;

/**
 * Annotation identifying a redirect to the {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateRedirectAnnotation {

	/**
	 * Type provided to the redirect to source values to construct the
	 * {@link HttpInputPath}.
	 */
	private final Class<?> valuesType;

	/**
	 * Instantiate.
	 * 
	 * @param valuesType
	 *            Type provided to the redirect to source values to construct
	 *            the {@link HttpInputPath}. May be <code>null</code> if no
	 *            values are required.
	 */
	public WebTemplateRedirectAnnotation(Class<?> valuesType) {
		this.valuesType = valuesType;
	}

	/**
	 * Obtains the type provided to the redirect to source values to construct
	 * the {@link HttpInputPath}.
	 * 
	 * @return Type provided to the redirect to source values to construct the
	 *         {@link HttpInputPath}. May be <code>null</code>.
	 */
	public Class<?> getValuesType() {
		return this.valuesType;
	}

}
