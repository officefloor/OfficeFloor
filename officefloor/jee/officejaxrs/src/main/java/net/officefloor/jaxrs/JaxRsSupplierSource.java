/*-
 * #%L
 * JAX-RS
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

package net.officefloor.jaxrs;

import java.util.Properties;

import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.supply.ServletSupplierSource;

/**
 * JAX-RS {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsSupplierSource extends AbstractSupplierSource {

	/**
	 * ==================== SupplierSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void supply(SupplierSourceContext context) throws Exception {

		// Obtain the servlet manager
		ServletManager servletManager = ServletSupplierSource.getServletManager();

		// Add the JAX-RS filter (applicable only if chaining)
		final String FILTER_NAME = "JAXRS";
		servletManager.addFilter(FILTER_NAME, ServletContainer.class, (filter) -> {

			// Configure initialisation properties
			Properties properties = context.getProperties();
			for (String name : properties.stringPropertyNames()) {
				String value = properties.getProperty(name);
				filter.addInitParameter(name, value);
			}

			// Flag to forward on unhandled requests
			filter.addInitParameter(ServletProperties.FILTER_FORWARD_ON_404, Boolean.TRUE.toString());
		});

		// Map in the filter (applicable only if chaining)
		FilterMap filterMap = new FilterMap();
		filterMap.setFilterName(FILTER_NAME);
		filterMap.addURLPattern("/*");
		servletManager.getContext().addFilterMap(filterMap);
	}

	@Override
	public void terminate() {
		// Nothing to terminate
	}

}
