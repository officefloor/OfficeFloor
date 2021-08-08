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

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.source.SourceProperties;

/**
 * Configuration of an {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveConfiguration<XS extends ExecutiveSource> {

	/**
	 * Obtains the {@link ExecutiveSource} instance to use.
	 * 
	 * @return {@link ExecutiveSource} instance to use. This may be
	 *         <code>null</code> and therefore the
	 *         {@link #getExecutiveSourceClass()} should be used to obtain the
	 *         {@link ExecutiveSource}.
	 */
	XS getExecutiveSource();

	/**
	 * Obtains the {@link Class} of the {@link ExecutiveSource}.
	 * 
	 * @return {@link Class} of the {@link ExecutiveSource}.
	 */
	Class<XS> getExecutiveSourceClass();

	/**
	 * Obtains the {@link SourceProperties} for initialising the {@link ExecutiveSource}.
	 * 
	 * @return {@link SourceProperties} for initialising the {@link ExecutiveSource}.
	 */
	SourceProperties getProperties();

}
