/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.frame.util;

import java.util.Properties;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectSourceContextImpl;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Loads {@link ManagedObjectSource} for stand-alone use.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceStandAlone {

	/**
	 * Name of the {@link ManagedObjectSource} being loaded.
	 */
	public static final String STAND_ALONE_MANAGED_OBJECT_SOURCE_NAME = "managed.object.source";

	/**
	 * Name of the {@link Office} managing the {@link ManagedObjectSource} being
	 * loaded.
	 */
	public static final String STAND_ALONE_MANAGING_OFFICE_NAME = "office";

	/**
	 * {@link Properties}.
	 */
	private final Properties properties = new Properties();

	/**
	 * Adds a property for the {@link ManagedObjectSource}.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param value
	 *            Value for the property.
	 */
	public void addProperty(String name, String value) {
		this.properties.setProperty(name, value);
	}

	/**
	 * Instantiates and initialises the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectSourceClass
	 *            Class of the {@link ManagedObjectSource}.
	 * @return Initialised {@link ManagedObjectSource}.
	 * @throws Exception
	 *             If fails to initialise {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> MS initManagedObjectSource(
			Class<MS> managedObjectSourceClass) throws Exception {

		// Create a new instance of the managed object source
		MS moSource = managedObjectSourceClass.newInstance();

		// Create necessary builders
		OfficeFloorBuilder officeFloorBuilder = OfficeFrame
				.createOfficeFloorBuilder();
		ManagingOfficeBuilder<F> managingOfficeBuilder = officeFloorBuilder
				.addManagedObject(STAND_ALONE_MANAGED_OBJECT_SOURCE_NAME,
						managedObjectSourceClass).setManagingOffice(
						"STAND ALONE");
		OfficeBuilder officeBuilder = officeFloorBuilder
				.addOffice(STAND_ALONE_MANAGING_OFFICE_NAME);

		// Initialise the managed object source
		ManagedObjectSourceContextImpl sourceContext = new ManagedObjectSourceContextImpl(
				STAND_ALONE_MANAGED_OBJECT_SOURCE_NAME, this.properties,
				managedObjectSourceClass.getClassLoader(),
				managingOfficeBuilder, officeBuilder);
		moSource.init(sourceContext);

		// Return the initialised managed object source
		return moSource;
	}

	/**
	 * Starts the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @throws Exception
	 *             If fails to start the {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> void startManagedObjectSource(
			MS managedObjectSource) throws Exception {
		// Start the managed object source
		managedObjectSource.start(new LoadExecuteContext());
	}

	/**
	 * Loads (init and start) the {@link ManagedObjectSource}.
	 * 
	 * @return Loaded {@link ManagedObjectSource}.
	 * @throws Exception
	 *             If fails to init and start the {@link ManagedObjectSource}.
	 */
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> MS loadManagedObjectSource(
			Class<MS> managedObjectSourceClass) throws Exception {

		// Initialise the managed object source
		MS moSource = this.initManagedObjectSource(managedObjectSourceClass);

		// Start the managed object source
		this.startManagedObjectSource(moSource);

		// Return the loaded managed object source
		return moSource;
	}

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private class LoadExecuteContext<F extends Enum<F>> implements
			ManagedObjectExecuteContext<F> {

		/*
		 * ================ ManagedObjectExecuteContext =====================
		 */

		@Override
		public void invokeProcess(F key, Object parameter,
				ManagedObject managedObject) {
			throw new UnsupportedOperationException(
					ManagedObjectSourceStandAlone.class.getSimpleName()
							+ " does not support invoking processes");
		}

		@Override
		public void invokeProcess(int flowIndex, Object parameter,
				ManagedObject managedObject) {
			throw new UnsupportedOperationException(
					ManagedObjectSourceStandAlone.class.getSimpleName()
							+ " does not support invoking processes");
		}

		@Override
		public void invokeProcess(F key, Object parameter,
				ManagedObject managedObject, EscalationHandler escalationHandler) {
			throw new UnsupportedOperationException(
					ManagedObjectSourceStandAlone.class.getSimpleName()
							+ " does not support invoking processes");
		}

		@Override
		public void invokeProcess(int flowIndex, Object parameter,
				ManagedObject managedObject, EscalationHandler escalationHandler) {
			throw new UnsupportedOperationException(
					ManagedObjectSourceStandAlone.class.getSimpleName()
							+ " does not support invoking processes");
		}
	}

}