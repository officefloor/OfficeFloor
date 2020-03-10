/*-
 * #%L
 * OfficeCompiler
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
