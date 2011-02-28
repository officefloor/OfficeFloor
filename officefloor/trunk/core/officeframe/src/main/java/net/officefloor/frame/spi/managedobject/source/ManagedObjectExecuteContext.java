/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.frame.spi.managedobject.source;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <p>
 * Context that the {@link ManagedObject} is to execute within.
 * <p>
 * In invoking processes the following should be taken into account:
 * <ol>
 * <li>The {@link Flow} (process) will be instigated in a new
 * {@link ProcessState} which for example will cause new {@link ManagedObject}
 * dependencies to be instantiated.</li>
 * <li>
 * The {@link ManagedObject} passed to the invocation will go through a full
 * life-cycle so be careful passing in an existing initialised
 * {@link ManagedObject}. For example the {@link AsynchronousListener} instance
 * will be overwritten which will likely cause dead-lock as the
 * {@link AsynchronousListener#notifyComplete()} will notify on the wrong
 * {@link ManagedObjectContainer}.</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecuteContext<F extends Enum<F>> {

	/**
	 * Instigates a {@link Flow}.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter to the first {@link Task} of the {@link Flow}.
	 * @param managedObject
	 *            {@link ManagedObject} for the {@link ProcessState} of the
	 *            {@link Flow}.
	 * @return {@link ProcessFuture}.
	 * 
	 * @see ManagedObjectExecuteContext
	 */
	ProcessFuture invokeProcess(F key, Object parameter,
			ManagedObject managedObject);

	/**
	 * Instigates a {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter that to the first {@link Task} of the {@link Flow}.
	 * @param managedObject
	 *            {@link ManagedObject} for the {@link ProcessState} of the
	 *            {@link Flow}.
	 * @return {@link ProcessFuture}
	 * 
	 * @see ManagedObjectExecuteContext
	 */
	ProcessFuture invokeProcess(int flowIndex, Object parameter,
			ManagedObject managedObject);

	/**
	 * <p>
	 * Instigates a {@link Flow} providing an {@link EscalationHandler} to
	 * handle {@link EscalationFlow} from the {@link Flow}.
	 * <p>
	 * An example of using this would be a HTTP server socket that sends status
	 * 500 on {@link EscalationFlow} from {@link Flow}.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter to the first {@link Task} of the {@link Flow}.
	 * @param managedObject
	 *            {@link ManagedObject} for the {@link ProcessState} of the
	 *            {@link Flow}.
	 * @param escalationHandler
	 *            {@link EscalationHandler}.
	 * @return {@link ProcessFuture}.
	 * 
	 * @see ManagedObjectExecuteContext
	 */
	ProcessFuture invokeProcess(F key, Object parameter,
			ManagedObject managedObject, EscalationHandler escalationHandler);

	/**
	 * <p>
	 * Instigates a {@link Flow} providing an {@link EscalationHandler} to
	 * handle {@link EscalationFlow} from the {@link Flow}.
	 * <p>
	 * An example of using this would be a HTTP server socket that sends status
	 * 500 on {@link EscalationFlow} from {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter to the first {@link Task} of the {@link Flow}.
	 * @param managedObject
	 *            {@link ManagedObject} for the {@link ProcessState} of the
	 *            {@link Flow}.
	 * @param escalationHandler
	 *            {@link EscalationHandler}.
	 * @return {@link ProcessFuture}.
	 * 
	 * @see ManagedObjectExecuteContext
	 */
	ProcessFuture invokeProcess(int flowIndex, Object parameter,
			ManagedObject managedObject, EscalationHandler escalationHandler);

}