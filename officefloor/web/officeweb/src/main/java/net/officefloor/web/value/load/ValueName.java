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

import net.officefloor.web.build.HttpValueLocation;

/**
 * Name of a value.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueName {

	/**
	 * Name of value.
	 */
	private final String name;

	/**
	 * {@link HttpValueLocation}.
	 */
	private final HttpValueLocation location;

	/**
	 * Instantiate.
	 * 
	 * @param name     Name of value.
	 * @param location {@link HttpValueLocation}.
	 */
	public ValueName(String name, HttpValueLocation location) {
		this.name = name;
		this.location = location;
	}

	/**
	 * Obtains the name of the value.
	 * 
	 * @return Name of the value.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the {@link HttpValueLocation} for the value.
	 * 
	 * @return {@link HttpValueLocation} for the value. <code>null</code> to
	 *         indicate any.
	 */
	public HttpValueLocation getLocation() {
		return this.location;
	}

}
