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