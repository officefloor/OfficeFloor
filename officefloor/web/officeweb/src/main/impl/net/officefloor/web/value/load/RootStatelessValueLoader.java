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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpValueLocation;

/**
 * Root {@link StatelessValueLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class RootStatelessValueLoader implements StatelessValueLoader {

	/**
	 * {@link StatelessValueLoader} instances by {@link PropertyKey}.
	 */
	private Map<PropertyKey, StatelessValueLoader> valueLoaders = new HashMap<PropertyKey, StatelessValueLoader>();

	/**
	 * {@link PropertyKeyFactory}.
	 */
	private final PropertyKeyFactory propertyKeyFactory;

	/**
	 * Initiate.
	 * 
	 * @param valueLoaders       {@link StatelessValueLoader} instances by
	 *                           {@link PropertyKey}.
	 * @param propertyKeyFactory {@link PropertyKeyFactory}.
	 */
	public RootStatelessValueLoader(Map<PropertyKey, StatelessValueLoader> valueLoaders,
			PropertyKeyFactory propertyKeyFactory) {
		this.valueLoaders = valueLoaders;
		this.propertyKeyFactory = propertyKeyFactory;
	}

	/*
	 * ================== StatelessValueLoader ==========================
	 */

	@Override
	public void loadValue(Object object, String name, int nameIndex, String value, HttpValueLocation location,
			Map<PropertyKey, Object> state) throws HttpException {

		// Parse out the property name (start at name index)
		int index = -1;
		SEPARATOR_FOUND: for (int i = nameIndex; i < name.length(); i++) {
			char character = name.charAt(i);
			switch (character) {
			case '.':
			case '{':
				index = i;
				break SEPARATOR_FOUND;
			default:
				// not separator so continue to next character
			}
		}

		// Split out the property name from the name
		String propertyName;
		if (index < 0) {
			// Entire name
			propertyName = name.substring(nameIndex);
			nameIndex = name.length(); // end of name
		} else {
			// Not entire name
			propertyName = name.substring(nameIndex, index);
			nameIndex = index + 1; // ignore separator ('.' character)
		}

		// Create the property key
		PropertyKey propertyKey = this.propertyKeyFactory.createPropertyKey(propertyName);

		// Obtain the value loader for the property name
		StatelessValueLoader valueLoader = this.valueLoaders.get(propertyKey);
		if (valueLoader == null) {
			return; // no value loader for property
		}

		// Load the value
		valueLoader.loadValue(object, name, nameIndex, value, location, state);
	}

	@Override
	public void visitValueNames(Consumer<ValueName> visitor, String namePrefix,
			List<StatelessValueLoader> visitedLoaders) {
		this.valueLoaders.forEach((propertyKey, valueLoader) -> {

			// Determine if recursive
			List<StatelessValueLoader> visited = (visitedLoaders != null ? visitedLoaders : new ArrayList<>());
			if (visited.contains(RootStatelessValueLoader.this)) {
				// Recursive
				visitor.accept(new ValueName(namePrefix + "...", null));
				return;
			}

			// Not recursive, so setup to track recursive
			List<StatelessValueLoader> childVisited = new ArrayList<>(visited.size() + 1);
			childVisited.addAll(visited);
			childVisited.add(RootStatelessValueLoader.this);

			// Visit children
			String childNamePrefix = (namePrefix == null ? ""
					: (namePrefix.length() == 0 ? namePrefix : namePrefix + ".")) + propertyKey.getPropertyName();
			valueLoader.visitValueNames(visitor, childNamePrefix, childVisited);
		});
	}

}
