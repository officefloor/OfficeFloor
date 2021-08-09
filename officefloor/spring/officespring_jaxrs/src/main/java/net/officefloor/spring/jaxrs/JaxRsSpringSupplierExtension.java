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
