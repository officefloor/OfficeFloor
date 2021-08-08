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

package net.officefloor.configuration;

import java.io.InputStream;
import java.io.Reader;

/**
 * Item of configuration within a {@link ConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConfigurationItem {

	/**
	 * Obtains the {@link Reader} to the configuration that this represents.
	 * 
	 * @return {@link Reader} to the configuration.
	 * @throws ConfigurationError
	 *             Let this propagate to let OfficeFloor handle failure in
	 *             loading {@link ConfigurationItem}.
	 */
	Reader getReader() throws ConfigurationError;

	/**
	 * Obtains {@link InputStream} to the configuration that this represents.
	 * 
	 * @return {@link InputStream} to the configuration.
	 * @throws ConfigurationError
	 *             Let this propagate to let OfficeFloor handle failure in
	 *             loading {@link ConfigurationItem}.
	 */
	InputStream getInputStream() throws ConfigurationError;

}
