/*-
 * #%L
 * JAX-RS with Spring Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.spring.jaxrs;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
