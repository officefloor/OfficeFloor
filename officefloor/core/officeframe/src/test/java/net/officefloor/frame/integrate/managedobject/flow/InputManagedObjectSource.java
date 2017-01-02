/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.integrate.managedobject.flow;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Invokes flows to input data to Office.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class InputManagedObjectSource extends AbstractManagedObjectSource<None, InputManagedObjectSource.Flows> {

	/**
	 * Keys of {@link Flow} instances instigated.
	 */
	public enum Flows {
		INPUT
	}

	/**
	 * Instance.
	 */
	private static InputManagedObjectSource INSTANCE;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Flows> executeContext;

	/**
	 * Inputs a parameter into the Office.
	 * 
	 * @param parameter
	 *            Parameter to input into the Office.
	 * @param managedObject
	 *            {@link ManagedObject}.
	 * @param delay
	 *            Delay to invoke process.
	 */
	public static void input(Object parameter, ManagedObject managedObject, long delay) {
		// Input the parameter
		INSTANCE.executeContext.invokeProcess(Flows.INPUT, parameter, managedObject, delay, null);
	}

	/**
	 * Initialise and make available to invoke a process.
	 */
	public InputManagedObjectSource() {
		INSTANCE = this;
	}

	/*
	 * ==================== AbstractManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		INSTANCE = this;
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
		context.setObjectClass(this.getClass());
		context.addFlow(Flows.INPUT, Object.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
		this.executeContext = context;
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return null;
	}

}