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
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * Spring Resource.
 * 
 * @author Daniel Sagenschneider
 */
@Component
@Path("/")
public class SpringResource {

	@GET
	@Path("/spring")
	public String get() {
		return "SPRING";
	}

	private @Autowired SpringDependency springAutowired;

	@GET
	@Path("/spring/autowired/spring")
	public String getSpringAutowired() {
		return "Autowired " + this.springAutowired.getMessage();
	}

	private @Autowired OfficeFloorDependency officeFloorAutowired;

	@GET
	@Path("/spring/autowired/officefloor")
	public String getOfficeFloorAutowired() {
		return "Autowired " + this.officeFloorAutowired.getMessage();
	}

	private @Inject SpringDependency springInject;

	@GET
	@Path("/spring/inject/spring")
	public String getSpringInject() {
		return "Inject " + this.springInject.getMessage();
	}

	private @Inject OfficeFloorDependency officeFloorInject;

	@GET
	@Path("/spring/inject/officefloor")
	public String getOfficeFloorInject() {
		return "Inject " + this.officeFloorInject.getMessage();
	}

}
