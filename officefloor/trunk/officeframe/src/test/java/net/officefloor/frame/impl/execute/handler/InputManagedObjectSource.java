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
package net.officefloor.frame.impl.execute.handler;

import java.util.Map;

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.impl.AbstractMockManagedObjectSource;
import net.officefloor.frame.impl.PassByReference;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Invokes handlers to input data to Office.
 * 
 * @author Daniel
 */
public class InputManagedObjectSource<D extends Enum<D>> extends
		AbstractMockManagedObjectSource<D, Handlers> {

	/**
	 * Instance.
	 */
	private static InputManagedObjectSource<?> INSTANCE;

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
	 * Store the instance.
	 */
	protected void init() throws Exception {
		INSTANCE = this;
	}

	/**
	 * Initiate handlers.
	 * 
	 * @param handlerKeys
	 *            Handler.
	 * @param handlers
	 *            Handlers.
	 */
	@Override
	protected void initHandlers(PassByReference<Class<Handlers>> handlerKeys,
			Map<Handlers, Class<?>> handlers) {
		handlerKeys.setValue(Handlers.class);
		handlers.put(Handlers.INPUT, MockHandler.class);
	}

	/**
	 * Inputs the parameter.
	 * 
	 * @param parameter
	 *            Parameter.
	 * @param managedObject
	 *            {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public void inputParameter(Object parameter, ManagedObject managedObject) {
		// Obtain the handler
		Handler<?> handler = this.getExecuteContext()
				.getHandler(Handlers.INPUT);
		MockHandler mockHandler = (MockHandler) handler;

		// Input the parameter
		mockHandler.handle(parameter, managedObject);
	}

}
