/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.spring.data;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.spring.extension.SpringSupplierExtensionContext;
import net.officefloor.spring.extension.SpringSupplierExtensionServiceFactory;

/**
 * Spring data {@link SpringSupplierExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringDataExtension implements SpringSupplierExtensionServiceFactory, SpringSupplierExtension {

	/*
	 * ================= SpringSupplierExtensionServiceFactory =================
	 */

	@Override
	public SpringSupplierExtension createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== SpringSupplierExtension ===========================
	 */

	@Override
	public void beforeSpringLoad(SpringSupplierExtensionContext context) throws Exception {
	}

	@Override
	public void afterSpringLoad(SpringSupplierExtensionContext context) throws Exception {
		context.addThreadSynchroniser(() -> new SpringDataThreadSynchroniser());
	}

}