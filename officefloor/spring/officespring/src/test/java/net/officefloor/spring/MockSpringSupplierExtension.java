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

package net.officefloor.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.thread.ThreadSynchroniser;
import net.officefloor.notscan.ExtensionBean;
import net.officefloor.spring.extension.AfterSpringLoadSupplierExtensionContext;
import net.officefloor.spring.extension.BeforeSpringLoadSupplierExtensionContext;
import net.officefloor.spring.extension.SpringBeanDecoratorContext;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.spring.extension.SpringSupplierExtensionServiceFactory;

/**
 * Mock {@link SpringSupplierExtension} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockSpringSupplierExtension implements SpringSupplierExtension, SpringSupplierExtensionServiceFactory {

	/**
	 * Indicates if active.
	 */
	public static boolean isActive = false;

	/**
	 * {@link ThreadLocal}.
	 */
	public static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

	/**
	 * {@link ComplexBean} loading from {@link ConfigurableApplicationContext}.
	 */
	public static ComplexBean springBean = null;

	/**
	 * {@link OfficeFloorInterfaceDependency}.
	 */
	public static OfficeFloorInterfaceDependency officeFloorManagedObject = null;
	
	/**
	 * {@link AvailableType} instances.
	 */
	public static AvailableType[] availableTypes = null;

	/**
	 * Decorated Spring Bean types by their name.
	 */
	public static final Map<String, Class<?>> decoratedBeanTypes = new HashMap<>();

	/*
	 * ============ SpringSupplierExtensionServiceFactory ============
	 */

	@Override
	public MockSpringSupplierExtension createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================== SpringSupplierExtension ====================
	 */

	@Override
	public void beforeSpringLoad(BeforeSpringLoadSupplierExtensionContext context) throws Exception {

		// Determine if active
		if (!isActive) {
			return;
		}

		// Obtain managed object
		officeFloorManagedObject = context.getManagedObject(null, OfficeFloorInterfaceDependency.class);
		
		// Obtain the available types
		availableTypes = context.getAvailableTypes();
	}

	@Override
	public void configureSpring(SpringApplicationBuilder builder) throws Exception {

		// Determine if active
		if (!isActive) {
			return;
		}

		// Include extra bean
		builder.sources(ExtensionBean.class);
	}

	@Override
	public void afterSpringLoad(AfterSpringLoadSupplierExtensionContext context) throws Exception {

		// Determine if active
		if (!isActive) {
			return;
		}

		// Capture the spring bean
		springBean = context.getSpringContext().getBean(ComplexBean.class);

		// Add the thread synchroniser
		context.addThreadSynchroniser(() -> new ThreadSynchroniser() {

			private String value;

			@Override
			public void suspendThread() {
				this.value = threadLocal.get();
				threadLocal.set(null);
			}

			@Override
			public void resumeThread() {
				threadLocal.set(this.value);
			}
		});
	}

	@Override
	public void decorateSpringBean(SpringBeanDecoratorContext context) throws Exception {

		// Determine if active
		if (!isActive) {
			return;
		}

		// Register the bean
		decoratedBeanTypes.put(context.getBeanName(), context.getBeanType());

		// Add additional dependency
		if ("simpleBean".equals(context.getBeanName())) {
			context.addDependency(null, LoadBean.class);
		}
	}

}
