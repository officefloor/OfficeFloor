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

package net.officefloor.frame.spi.managedobject.source;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Context that the {@link ManagedObject} is to execute within.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecuteContext<F extends Enum<F>> {

	/**
	 * <p>
	 * Instigates a {@link Flow}.
	 * <p>
	 * The {@link Flow} will be instigated in a new {@link ProcessState}.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter to the first {@link Task} of the {@link Flow}.
	 * @param managedObject
	 *            {@link ManagedObject} for the {@link ProcessState} of the
	 *            {@link Flow}.
	 */
	void invokeProcess(F key, Object parameter, ManagedObject managedObject);

	/**
	 * <p>
	 * Instigates a {@link Flow}.
	 * <p>
	 * The {@link Flow} will be instigated in a new {@link ProcessState}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter that to the first {@link Task} of the {@link Flow}.
	 * @param managedObject
	 *            {@link ManagedObject} for the {@link ProcessState} of the
	 *            {@link Flow}.
	 */
	void invokeProcess(int flowIndex, Object parameter,
			ManagedObject managedObject);

	/**
	 * <p>
	 * Instigates a {@link Flow} providing an {@link EscalationHandler} to
	 * handle {@link EscalationFlow} from the {@link Flow}.
	 * <p>
	 * An example of using this would be a HTTP server socket that sends status
	 * 500 on {@link EscalationFlow} from {@link Flow}.
	 * <p>
	 * The {@link Flow} will be instigated in a new {@link ProcessState}.
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
	 */
	void invokeProcess(F key, Object parameter, ManagedObject managedObject,
			EscalationHandler escalationHandler);

	/**
	 * <p>
	 * Instigates a {@link Flow} providing an {@link EscalationHandler} to
	 * handle {@link EscalationFlow} from the {@link Flow}.
	 * <p>
	 * An example of using this would be a HTTP server socket that sends status
	 * 500 on {@link EscalationFlow} from {@link Flow}.
	 * <p>
	 * The {@link Flow} will be instigated in a new {@link ProcessState}.
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
	 */
	void invokeProcess(int flowIndex, Object parameter,
			ManagedObject managedObject, EscalationHandler escalationHandler);

}