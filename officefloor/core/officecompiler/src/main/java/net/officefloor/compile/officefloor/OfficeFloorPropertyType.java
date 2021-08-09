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

package net.officefloor.compile.officefloor;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * <code>Type definition</code> for a {@link Property} of the
 * {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorPropertyType {

	/**
	 * Obtains the name for the {@link Property}.
	 * 
	 * @return Name for the {@link Property}.
	 */
	String getName();

	/**
	 * Obtains the label to describe the {@link Property}.
	 * 
	 * @return Label to describe the {@link Property}.
	 */
	String getLabel();

	/**
	 * Obtains the default value for this {@link Property}.
	 * 
	 * @return Default value for this {@link Property}.
	 */
	String getDefaultValue();

}
