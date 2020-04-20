/*-
 * #%L
 * Spring Integration
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
