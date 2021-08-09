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

package net.officefloor.frame.api.build;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;

/**
 * Builder of {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveBuilder<TS extends ExecutiveSource> {

	/**
	 * Specifies a property for the {@link ExecutiveSource}.
	 * 
	 * @param name  Name of property.
	 * @param value Value of property.
	 */
	void addProperty(String name, String value);

}
