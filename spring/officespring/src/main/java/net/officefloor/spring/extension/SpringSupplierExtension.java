/*-
 * #%L
 * Spring Integration
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

package net.officefloor.spring.extension;

import org.springframework.boot.builder.SpringApplicationBuilder;

import net.officefloor.spring.SpringSupplierSource;

/**
 * Extension to {@link SpringSupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SpringSupplierExtension {

	/**
	 * <p>
	 * Invoked before Spring is loaded.
	 * <p>
	 * This allows initial setup to be undertaken. It also allows capturing
	 * information on the current {@link Thread} as Spring loads.
	 * 
	 * @param context {@link BeforeSpringLoadSupplierExtensionContext}.
	 * @throws Exception If fails to setup.
	 */
	default void beforeSpringLoad(BeforeSpringLoadSupplierExtensionContext context) throws Exception {
		// does nothing by default
	}

	/**
	 * Configures Spring via {@link SpringApplicationBuilder}.
	 * 
	 * @param builder {@link SpringApplicationBuilder}.
	 * @throws Exception If fails to configure.
	 */
	default void configureSpring(SpringApplicationBuilder builder) throws Exception {
		// does nothing by default
	}

	/**
	 * <p>
	 * Invoked after Spring is loaded.
	 * <p>
	 * Allows processing captured information.
	 * 
	 * @param context {@link AfterSpringLoadSupplierExtensionContext}.
	 * @throws Exception If fails to complete extension configuration.
	 */
	default void afterSpringLoad(AfterSpringLoadSupplierExtensionContext context) throws Exception {
		// does nothing by default
	}

	/**
	 * Invoked for each registered Spring bean to further decorate integration.
	 * 
	 * @param context {@link SpringBeanDecoratorContext}.
	 * @throws Exception If fails to decorate the Spring Bean.
	 */
	default void decorateSpringBean(SpringBeanDecoratorContext context) throws Exception {
		// does nothing by default
	}

}
