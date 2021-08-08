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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpValueLocation;

/**
 * {@link ValueLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueLoaderImpl implements ValueLoader {

	/**
	 * Object to load values on.
	 */
	private final Object object;

	/**
	 * State for loading values.
	 */
	private final Map<PropertyKey, Object> state = new HashMap<PropertyKey, Object>();

	/**
	 * {@link StatelessValueLoader} to undertake loading the values.
	 */
	private final StatelessValueLoader delegate;

	/**
	 * Initiate.
	 * 
	 * @param object
	 *            Object to load values on.
	 * @param delegate
	 *            {@link StatelessValueLoader} to undertake loading the values.
	 */
	public ValueLoaderImpl(Object object, StatelessValueLoader delegate) {
		this.object = object;
		this.delegate = delegate;
	}

	/*
	 * =================== ValueLoader ==========================
	 */

	@Override
	public void loadValue(String name, String value, HttpValueLocation location) throws HttpException {
		// Load the value
		this.delegate.loadValue(this.object, name, 0, value, location, this.state);
	}

}
