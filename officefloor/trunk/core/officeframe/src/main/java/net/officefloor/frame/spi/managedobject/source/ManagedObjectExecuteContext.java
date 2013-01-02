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
package net.officefloor.frame.spi.managedobject.source;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.JobSequence;
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
 * <li>The {@link JobSequence} (process) will be instigated in a new
 * {@link ProcessState} which for example will cause new {@link ManagedObject}
 * dependencies to be instantiated.</li>
 * <li>
 * The {@link ManagedObject} passed to the invocation will go through a full
 * life-cycle so be careful passing in an existing initialised
 * {@link ManagedObject}. For example the {@link AsynchronousListener} instance
 * will be overwritten which will likely cause live-lock as the
 * {@link AsynchronousListener#notifyComplete()} will notify on the wrong
 * {@link ManagedObjectContainer}.</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecuteContext<F extends Enum<F>> {

	/**
	 * Instigates a {@link JobSequence}.
	 * 
	 * @param key
	 *            Key identifying the {@link JobSequence} to instigate.
	 * @param parameter
	 *            Parameter to first {@link Task} of the {@link JobSequence}.
	 * @param managedObject
	 *            {@link ManagedObject} for the {@link ProcessState} of the
	 *            {@link JobSequence}.
	 * @param delay
	 *            Delay in milliseconds before the {@link JobSequence} is
	 *            invoked. A 0 or negative value invokes the {@link JobSequence}
	 *            immediately.
	 * @return {@link ProcessFuture}.
	 * 
	 * @see ManagedObjectExecuteContext
	 */
	ProcessFuture invokeProcess(F key, Object parameter,
			ManagedObject managedObject, long delay);

	/**
	 * Instigates a {@link JobSequence}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link JobSequence} to instigate.
	 * @param parameter
	 *            Parameter that to the first {@link Task} of the
	 *            {@link JobSequence}.
	 * @param managedObject
	 *            {@link ManagedObject} for the {@link ProcessState} of the
	 *            {@link JobSequence}.
	 * @param delay
	 *            Delay in milliseconds before the {@link JobSequence} is
	 *            invoked. A 0 or negative value invokes the {@link JobSequence}
	 *            immediately.
	 * @return {@link ProcessFuture}
	 * 
	 * @see ManagedObjectExecuteContext
	 */
	ProcessFuture invokeProcess(int flowIndex, Object parameter,
			ManagedObject managedObject, long delay);

	/**
	 * <p>
	 * Instigates a {@link JobSequence} providing an {@link EscalationHandler}
	 * to handle {@link EscalationFlow} from the {@link JobSequence}.
	 * <p>
	 * An example of using this would be a HTTP server socket that sends status
	 * 500 on {@link EscalationFlow} from {@link JobSequence}.
	 * 
	 * @param key
	 *            Key identifying the {@link JobSequence} to instigate.
	 * @param parameter
	 *            Parameter to the first {@link Task} of the {@link JobSequence}
	 *            .
	 * @param managedObject
	 *            {@link ManagedObject} for the {@link ProcessState} of the
	 *            {@link JobSequence}.
	 * @param delay
	 *            Delay in milliseconds before the {@link JobSequence} is
	 *            invoked. A 0 or negative value invokes the {@link JobSequence}
	 *            immediately.
	 * @param escalationHandler
	 *            {@link EscalationHandler}.
	 * @return {@link ProcessFuture}.
	 * 
	 * @see ManagedObjectExecuteContext
	 */
	ProcessFuture invokeProcess(F key, Object parameter,
			ManagedObject managedObject, long delay,
			EscalationHandler escalationHandler);

	/**
	 * <p>
	 * Instigates a {@link JobSequence} providing an {@link EscalationHandler}
	 * to handle {@link EscalationFlow} from the {@link JobSequence}.
	 * <p>
	 * An example of using this would be a HTTP server socket that sends status
	 * 500 on {@link EscalationFlow} from {@link JobSequence}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link JobSequence} to instigate.
	 * @param parameter
	 *            Parameter to first {@link Task} of the {@link JobSequence}.
	 * @param managedObject
	 *            {@link ManagedObject} for the {@link ProcessState} of the
	 *            {@link JobSequence}.
	 * @param delay
	 *            Delay in milliseconds before the {@link JobSequence} is
	 *            invoked. A 0 or negative value invokes the {@link JobSequence}
	 *            immediately.
	 * @param escalationHandler
	 *            {@link EscalationHandler}.
	 * @return {@link ProcessFuture}.
	 * 
	 * @see ManagedObjectExecuteContext
	 */
	ProcessFuture invokeProcess(int flowIndex, Object parameter,
			ManagedObject managedObject, long delay,
			EscalationHandler escalationHandler);

}