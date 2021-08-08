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

package net.officefloor.compile.impl.properties;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;

/**
 * {@link SourceProperties} initialised from a {@link PropertyList}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyListSourceProperties extends SourcePropertiesImpl {

	/**
	 * Initiate with {@link Property} instances within the {@link PropertyList}.
	 * 
	 * @param properties
	 *            {@link PropertyList}.
	 */
	public PropertyListSourceProperties(PropertyList properties) {
		if (properties != null) {
			for (Property property : properties) {
				this.addProperty(property.getName(), property.getValue());
			}
		}
	}

}
