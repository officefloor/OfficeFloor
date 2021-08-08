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

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ValueLoaderFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueLoaderFactoryImpl<T> implements ValueLoaderFactory<T> {

	/**
	 * Delegate {@link StatelessValueLoader} to load values.
	 */
	private final StatelessValueLoader delegate;

	/**
	 * Initiate.
	 * 
	 * @param delegate Delegate {@link StatelessValueLoader} to load values.
	 */
	public ValueLoaderFactoryImpl(StatelessValueLoader delegate) {
		this.delegate = delegate;
	}

	/*
	 * =================== ValueLoaderFactory ============================
	 */

	@Override
	public ValueLoader createValueLoader(T object) throws Exception {
		// Create and return the new value loader
		return new ValueLoaderImpl(object, this.delegate);
	}

	@Override
	public ValueName[] getValueNames() {

		// Load the values
		List<ValueName> valueNames = new ArrayList<>();
		this.delegate.visitValueNames((name) -> valueNames.add(name), null, null);

		// Return the value names
		return valueNames.toArray(new ValueName[valueNames.size()]);
	}

}
