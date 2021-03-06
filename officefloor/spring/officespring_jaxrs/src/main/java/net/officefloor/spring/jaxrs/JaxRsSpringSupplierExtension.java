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

import org.springframework.boot.builder.SpringApplicationBuilder;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.spring.extension.SpringSupplierExtensionServiceFactory;

/**
 * JAX-RS {@link SpringSupplierExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsSpringSupplierExtension implements SpringSupplierExtension, SpringSupplierExtensionServiceFactory {

	/*
	 * ================== SpringSupplierExtensionServiceFactory ==================
	 */

	@Override
	public SpringSupplierExtension createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== SpringSupplierExtension ===========================
	 */

	@Override
	public void configureSpring(SpringApplicationBuilder builder) throws Exception {

		// Prefer filter to allow Spring Web MVC to still operate
		builder.properties("spring.jersey.type=FILTER");

		// If Servlet, must load with Tomcat setup for OfficeFloor dependencies
		builder.properties("spring.jersey.servlet.loadOnStartup=1");
	}

}
