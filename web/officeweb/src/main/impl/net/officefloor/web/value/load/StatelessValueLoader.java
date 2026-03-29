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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpValueLocation;

/**
 * Loads a value onto the Object graph.
 * 
 * @author Daniel Sagenschneider
 */
public interface StatelessValueLoader {

	/**
	 * Loads the value onto the object graph.
	 * 
	 * @param object    Root object of the graph to have the value loaded.
	 * @param name      Full property name.
	 * @param nameIndex Index into property name to identify particular property
	 *                  name for next stringed property to load.
	 * @param value     Property value.
	 * @param location  {@link HttpValueLocation}.
	 * @param state     State of loading values to the Object graph.
	 * @throws HttpException If fails to load the value.
	 */
	void loadValue(Object object, String name, int nameIndex, String value, HttpValueLocation location,
			Map<PropertyKey, Object> state) throws HttpException;

	/**
	 * Traverses the {@link ValueName} instances.
	 * 
	 * @param visitor         Visits the {@link ValueName} instances.
	 * @param namePrefix      Prefix for name of {@link ValueName}.
	 * @param visistedLoaders Tracks already visited {@link StatelessValueLoader}
	 *                        instances. This avoids infinite loops in recursive
	 *                        {@link ValueName} instances.
	 */
	void visitValueNames(Consumer<ValueName> visitor, String namePrefix, List<StatelessValueLoader> visistedLoaders);

}
