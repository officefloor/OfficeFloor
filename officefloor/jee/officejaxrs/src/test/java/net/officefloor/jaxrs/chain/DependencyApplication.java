/*-
 * #%L
 * JAX-RS
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

package net.officefloor.jaxrs.chain;

import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import net.officefloor.plugin.clazz.Dependency;

/**
 * {@link Application} with {@link Dependency}.
 * 
 * @author Daniel Sagenschneider
 */
@ApplicationPath("/")
public class DependencyApplication extends ResourceConfig {

	/**
	 * Instantiate.
	 */
	public DependencyApplication() {
		this.register(DependencyResource.class);
	}
}
