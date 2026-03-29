/*-
 * #%L
 * JAX-RS with Spring Integration
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

package net.officefloor.spring.jaxrs;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.officefloor.plugin.clazz.Dependency;

/**
 * JAX-RS resource with dependency injection.
 * 
 * @author Daniel Sagenschneider
 */
@Path("/")
public class JaxRsResource {

	@GET
	@Path("/jaxrs")
	public String get() {
		return "JAX-RS";
	}

	private @Autowired SpringDependency springAutowired;

	@GET
	@Path("/jaxrs/autowired/spring")
	public String getSpringAutowired() {
		return "Autowired " + this.springAutowired.getMessage();
	}

	private @Autowired OfficeFloorDependency officeFloorAutowired;

	@GET
	@Path("/jaxrs/autowired/officefloor")
	public String getOfficeFloorAutowired() {
		return "Autowired " + this.officeFloorAutowired.getMessage();
	}

	private @Inject SpringDependency springInject;

	@GET
	@Path("/jaxrs/inject/spring")
	public String getSpringInject() {
		return "Inject " + this.springInject.getMessage();
	}

	private @Inject OfficeFloorDependency officeFloorInject;

	@GET
	@Path("/jaxrs/inject/officefloor")
	public String getOfficeFloorInject() {
		return "Inject " + this.officeFloorInject.getMessage();
	}

	private @Dependency OfficeFloorDependency officeFloorDependency;

	@GET
	@Path("/jaxrs/dependency")
	public String getOfficeFloorDependency() {
		return "Dependency " + this.officeFloorDependency.getMessage();
	}

}
