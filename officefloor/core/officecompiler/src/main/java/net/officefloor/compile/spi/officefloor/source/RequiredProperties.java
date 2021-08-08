/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.spi.officefloor.source;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Enables the {@link OfficeFloorSource} to specify any required
 * {@link Property} instances necessary for loading the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RequiredProperties {

	/**
	 * Adds a required {@link Property}.
	 * 
	 * @param name
	 *            Name of the {@link Property} which is also used as its label.
	 */
	void addRequiredProperty(String name);

	/**
	 * Adds a required {@link Property}.
	 * 
	 * @param name
	 *            Name of the {@link Property}.
	 * @param label
	 *            Descriptive label for the {@link Property}.
	 */
	void addRequiredProperty(String name, String label);

}
