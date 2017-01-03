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
package net.officefloor.frame.impl.execute.escalation;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Invokes flow with an {@link EscalationHandler}.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class EscalationManagedObjectSource extends
		AbstractManagedObjectSource<None, EscalationManagedObjectSource.Flows> implements ManagedObject, FlowCallback {

	/**
	 * Instance.
	 */
	private static EscalationManagedObjectSource INSTANCE;

	/**
	 * Failure to be thrown from {@link EscalationHandler} of this
	 * {@link ManagedObjectSource}.
	 */
	private static Throwable escalationHandlerFailure;

	/**
	 * Resets for use.
	 * 
	 * @param escalationHandlerFailure
	 *            Failure to be thrown from {@link EscalationHandler} of this
	 *            {@link ManagedObjectSource}.
	 */
	public static void reset(Throwable escalationHandlerFailure) {
		EscalationManagedObjectSource.escalationHandlerFailure = escalationHandlerFailure;
	}

	/**
	 * Invokes processing.
	 * 
	 * @param argument
	 *            Argument passed by {@link ManagedObjectSource}.
	 */
	public static void invokeProcessing(String argument) {
		// Invoke processing
		INSTANCE.executeContext.invokeProcess(Flows.TASK_TO_ESCALATE, argument, INSTANCE, 0, INSTANCE);
	}

	/**
	 * Throws the escalation if handled by the {@link ManagedObjectSource}
	 * {@link EscalationHandler}.
	 * 
	 * @throws Throwable
	 *             Escalation.
	 */
	public static void throwPossibleEscalation() throws Throwable {
		if (INSTANCE.escalation != null) {
			throw INSTANCE.escalation;
		}
	}

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Flows> executeContext;

	/**
	 * Escalation.
	 */
	private Throwable escalation = null;

	/**
	 * Initiate and allow for invoking processes.
	 */
	public EscalationManagedObjectSource() {
		INSTANCE = this;
	}

	/*
	 * ================= AbstractManagedObjectSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		INSTANCE = this;
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
		context.setObjectClass(EscalationManagedObjectSource.class);
		context.addFlow(Flows.TASK_TO_ESCALATE, String.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
		this.executeContext = context;
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ================== ManagedObject ========================================
	 */

	@Override
	public Object getObject() throws Exception {
		return this;
	}

	/*
	 * ================= FlowCompletion ====================================
	 */

	@Override
	public void run(Throwable escalation) throws Throwable {
		this.escalation = escalation;

		// Determine if failure in handling escalation
		if (escalationHandlerFailure != null) {
			throw escalationHandlerFailure;
		}
	}

	/**
	 * Flows.
	 */
	public static enum Flows {
		TASK_TO_ESCALATE
	}

}