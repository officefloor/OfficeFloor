/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.integrate.managedobject.flow;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Invokes handlers to input data to Office.
 * 
 * @author Daniel
 */
public class InputManagedObjectSource<D extends Enum<D>> extends
		AbstractManagedObjectSource<D, InputManagedObjectSource.Flows> {

	/**
	 * Keys of {@link Flow} instances instigated.
	 */
	public enum Flows {
		INPUT
	}

	/**
	 * Instance.
	 */
	private static InputManagedObjectSource<?> INSTANCE;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Flows> executeContext;

	/**
	 * Inputs a parameter into the Office.
	 * 
	 * @param parameter
	 *            Parameter to input into the Office.
	 */
	public static void input(Object parameter, ManagedObject managedObject) {
		// Input the parameter
		INSTANCE.inputParameter(parameter, managedObject);
	}

	/**
	 * Inputs the parameter.
	 * 
	 * @param parameter
	 *            Parameter.
	 * @param managedObject
	 *            {@link ManagedObject}.
	 */
	public void inputParameter(Object parameter, ManagedObject managedObject) {
		// Execute the flow
		this.executeContext
				.invokeProcess(Flows.INPUT, parameter, managedObject);
	}

	/*
	 * ==================== AbstractManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		INSTANCE = this;
	}

	@Override
	protected void loadMetaData(MetaDataContext<D, Flows> context)
			throws Exception {
		context.setObjectClass(this.getClass());
		context.addFlow(Flows.INPUT, Object.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context)
			throws Exception {
		this.executeContext = context;
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return null;
	}

}