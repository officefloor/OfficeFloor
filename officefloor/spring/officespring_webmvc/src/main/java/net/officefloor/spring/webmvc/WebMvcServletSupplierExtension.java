/*-
 * #%L
 * Spring Web MVC Integration
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

package net.officefloor.spring.webmvc;

import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.servlet.supply.extension.BeforeCompleteServletSupplierExtensionContext;
import net.officefloor.servlet.supply.extension.ServletSupplierExtension;
import net.officefloor.servlet.supply.extension.ServletSupplierExtensionServiceFactory;
import net.officefloor.spring.SpringSupplierSource;

/**
 * Web MVC {@link ServletSupplierExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebMvcServletSupplierExtension
		implements ServletSupplierExtensionServiceFactory, ServletSupplierExtension {

	/*
	 * ================== ServletSupplierExtensionServiceFactory ==================
	 */

	@Override
	public ServletSupplierExtension createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ========================== ServletSupplierExtension ========================
	 */

	@Override
	public void beforeCompletion(BeforeCompleteServletSupplierExtensionContext context) throws Exception {

		// Force start Spring (which in turn should force start the Servlet container)
		AvailableType[] availableTypes = context.getAvailableTypes();
		SpringSupplierSource.forceStartSpring(availableTypes);
	}

}
