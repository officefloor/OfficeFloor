/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
