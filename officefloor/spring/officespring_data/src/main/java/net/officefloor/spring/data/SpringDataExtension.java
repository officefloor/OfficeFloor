/*-
 * #%L
 * Spring Data Integration
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

package net.officefloor.spring.data;

import org.springframework.data.repository.Repository;
import org.springframework.transaction.PlatformTransactionManager;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.spring.extension.SpringBeanDecoratorContext;
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
	public void afterSpringLoad(SpringSupplierExtensionContext context) throws Exception {
		context.addThreadSynchroniser(() -> new SpringDataThreadSynchroniser());
	}

	@Override
	public void decorateSpringBean(SpringBeanDecoratorContext context) throws Exception {

		// Include transaction manager on all repositories for governance
		if (Repository.class.isAssignableFrom(context.getBeanType())) {
			context.addDependency(null, PlatformTransactionManager.class);
		}
	}

}
