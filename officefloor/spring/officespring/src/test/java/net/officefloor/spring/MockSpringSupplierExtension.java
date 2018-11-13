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
package net.officefloor.spring;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.thread.ThreadSynchroniser;
import net.officefloor.spring.extension.SpringBeanDecoratorContext;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.spring.extension.SpringSupplierExtensionContext;
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
	 * {@link OfficeFloorManagedObject}.
	 */
	public static OfficeFloorManagedObject officeFloorManagedObject = null;

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
	public void beforeSpringLoad(SpringSupplierExtensionContext context) throws Exception {

		// Determine if active
		if (!isActive) {
			return;
		}

		// Obtain managed object
		officeFloorManagedObject = context.getManagedObject(null, OfficeFloorManagedObject.class);
	}

	@Override
	public void afterSpringLoad(SpringSupplierExtensionContext context) throws Exception {

		// Determine if active
		if (!isActive) {
			return;
		}

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