/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.integrate.office;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionServiceFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link OfficeExtensionService} to override {@link Property} instances for the
 * {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeOverridePropertiesExtensionService
		implements OfficeFloorExtensionService, OfficeFloorExtensionServiceFactory {

	/**
	 * Properties.
	 */
	private static ThreadLocal<String[]> properties = new ThreadLocal<>();

	/**
	 * Operation.
	 */
	public static interface Operation<R, T extends Throwable> {

		/**
		 * Runs the operation.
		 * 
		 * @return Return.
		 * @throws T Possible {@link Throwable}.
		 */
		R run() throws T;
	}

	/**
	 * Run with properties.
	 * 
	 * @param <R>                Return type.
	 * @param <T>                Possible {@link Throwable}.
	 * @param operation          {@link Operation}.
	 * @param propertyNameValues {@link Property} name/value pairs.
	 * @return {@link Operation} return.
	 * @throws T Possible failure.
	 */
	public static <R, T extends Throwable> R runWithProperties(Operation<R, T> operation, String... propertyNameValues)
			throws T {
		try {
			properties.set(propertyNameValues);

			// Run operation
			return operation.run();

		} finally {
			properties.remove();
		}
	}

	/**
	 * Operation (void).
	 */
	public static interface OperationVoid<T extends Throwable> {

		/**
		 * Runs the operation.
		 * 
		 * @throws T Possible {@link Throwable}.
		 */
		void run() throws T;
	}

	/**
	 * Run with properties (void return).
	 * 
	 * @param <T>                Possible {@link Throwable}.
	 * @param operation          {@link Operation}.
	 * @param propertyNameValues {@link Property} name/value pairs.
	 * @throws T Possible failure.
	 */
	public static <T extends Throwable> void runWithProperties(OperationVoid<T> operation, String... propertyNameValues)
			throws T {
		runWithProperties(() -> {
			operation.run();
			return null;
		}, propertyNameValues);
	}

	/*
	 * =================== OfficeFloorExtensionServiceFactory ===================
	 */

	@Override
	public OfficeFloorExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== OfficeFloorExtensionService ======================
	 */

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {

		// Obtain the properties
		String[] propertyNameValues = properties.get();
		if (propertyNameValues == null) {
			return;
		}

		// Load the properties
		for (DeployedOffice office : officeFloorDeployer.getDeployedOffices()) {
			for (int i = 0; i < propertyNameValues.length; i += 2) {
				String name = propertyNameValues[i];
				String value = propertyNameValues[i + 1];
				office.addOverrideProperty(name, value);
			}
		}
	}

}
