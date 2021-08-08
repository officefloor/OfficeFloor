/*-
 * #%L
 * OfficeFloor integration of WAR
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

package net.officefloor.webapp;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.supply.ServletSupplierSource;
import net.officefloor.woof.WoofContext;
import net.officefloor.woof.WoofExtensionService;
import net.officefloor.woof.WoofExtensionServiceFactory;

/**
 * {@link WoofExtensionService} to configure {@link ServletManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebAppWoofExtensionService implements WoofExtensionServiceFactory, WoofExtensionService {

	/*
	 * ================ WoofExtensionServiceFactory ===================
	 */

	@Override
	public WoofExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================== WoofExtensionService =========================
	 */

	@Override
	public void extend(WoofContext context) throws Exception {
		OfficeArchitect office = context.getOfficeArchitect();

		// Add the supplier to chain in servlet manager
		office.addSupplier("WEBAPP", WebAppSupplierSource.class.getName());
	}

	/**
	 * WebApp {@link SupplierSource}.
	 */
	public static class WebAppSupplierSource extends AbstractSupplierSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Chain in the servlet manager
			ServletManager servletManager = ServletSupplierSource.getServletManager();
			servletManager.chainInServletManager();
		}

		@Override
		public void terminate() {
			// Nothing to terminate
		}
	}

}
