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

package net.officefloor.web.state;

import java.io.Serializable;

import net.officefloor.web.build.HttpValueLocation;

/**
 * HTTP argument.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpArgument implements Serializable {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Argument name.
	 */
	public final String name;

	/**
	 * Argument value.
	 */
	public final String value;

	/**
	 * Location that this {@link HttpArgument} was sourced.
	 */
	public final HttpValueLocation location;

	/**
	 * Next {@link HttpArgument}.
	 */
	public HttpArgument next = null;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Argument name.
	 * @param value
	 *            Argument value.
	 * @param location
	 *            {@link HttpValueLocation}.
	 */
	public HttpArgument(String name, String value, HttpValueLocation location) {
		this.name = name;
		this.value = value;
		this.location = location;
	}

}
