/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.api.source;

import java.util.Properties;

/**
 * Properties for the source.
 * 
 * @author Daniel Sagenschneider
 */
public interface SourceProperties {

	/**
	 * <p>
	 * Obtains the names of the available properties in the order they were
	 * defined. This allows for ability to provide variable number of properties
	 * identified by a naming convention and being able to maintain their order.
	 * <p>
	 * An example would be providing a listing of routing configurations, each
	 * entry named <code>route.[something]</code> and order indicating priority.
	 * 
	 * @return Names of the properties in the order defined.
	 */
	String[] getPropertyNames();

	/**
	 * Obtains a required property value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @return Value of the property.
	 * @throws UnknownPropertyError
	 *             If property was not configured. Let this propagate as
	 *             OfficeFloor will handle it.
	 */
	String getProperty(String name) throws UnknownPropertyError;

	/**
	 * Obtains the property value or subsequently the default value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param defaultValue
	 *            Default value if property not specified.
	 * @return Value of the property or the the default value if not specified.
	 */
	String getProperty(String name, String defaultValue);

	/**
	 * Properties to configure the source.
	 * 
	 * @return Properties specific for the source.
	 */
	Properties getProperties();

}
