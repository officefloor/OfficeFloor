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

package net.officefloor.web.value.load;

/**
 * Factory for a {@link PropertyKey}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyKeyFactory {

	/**
	 * Indicates if case insensitive match.
	 */
	private final boolean isCaseInsensitive;

	/**
	 * Initiate.
	 * 
	 * @param isCaseInsensitive
	 *            Indicates if case insensitive match.
	 */
	public PropertyKeyFactory(boolean isCaseInsensitive) {
		this.isCaseInsensitive = isCaseInsensitive;
	}

	/**
	 * Creates the {@link PropertyKey}.
	 * 
	 * @param propertyName
	 *            Property name.
	 * @return {@link PropertyKey}.
	 */
	public PropertyKey createPropertyKey(String propertyName) {
		return new PropertyKey(propertyName, this.isCaseInsensitive);
	}

}
