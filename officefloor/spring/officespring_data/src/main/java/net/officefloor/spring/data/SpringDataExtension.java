/*-
 * #%L
 * Spring Data Integration
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

package net.officefloor.spring.data;

import org.springframework.data.repository.Repository;
import org.springframework.transaction.PlatformTransactionManager;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.spring.extension.AfterSpringLoadSupplierExtensionContext;
import net.officefloor.spring.extension.SpringBeanDecoratorContext;
import net.officefloor.spring.extension.SpringSupplierExtension;
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
	public void afterSpringLoad(AfterSpringLoadSupplierExtensionContext context) throws Exception {
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
