/*-
 * #%L
 * OfficeFloor Flyway
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

package net.officefloor.flyway;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

/**
 * Configures the {@link Flyway}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FlywayConfigurer {

	/**
	 * Configures {@link Flyway}.
	 * 
	 * @param configuration {@link FluentConfiguration} for {@link Flyway}.
	 * @throws Exception If fails to configure.
	 */
	void configure(FluentConfiguration configuration) throws Exception;

}
