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

package net.officefloor.web.value.retrieve;


/**
 * <p>
 * Root object type.
 * <p>
 * Provides methods for testing.
 * 
 * @author Daniel Sagenschneider
 */
public interface RootObject {

	/**
	 * Obtains String value for simple property name.
	 * 
	 * @return String value as per testing.
	 */
	String getValue();

	/**
	 * Obtains an object for <code>property.text</code> property names.
	 * 
	 * @return {@link PropertyObject} as per testing.
	 */
	PropertyObject getProperty();

}
