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
package net.officefloor.frame.integrate.escalation;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Invokes flow with an {@link EscalationHandler}.
 * 
 * @author Daniel
 */
public class EscalationManagedObjectSource extends
		AbstractManagedObjectSource<None, EscalationManagedObjectSource.Flows>
		implements ManagedObject, EscalationHandler {

	/**
	 * Instance.
	 */
	private static EscalationManagedObjectSource INSTANCE;

	/**
	 * Invokes processing.
	 * 
	 * @param argument
	 *            Argument passed by {@link ManagedObjectSource}.
	 */
	public static void invokeProcessing(String argument) {
		// Invoke processing
		INSTANCE.executeContext.invokeProcess(Flows.TASK_TO_ESCALATE, argument,
				INSTANCE, INSTANCE);
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
	protected void loadMetaData(MetaDataContext<None, Flows> context)
			throws Exception {
		context.setObjectClass(EscalationManagedObjectSource.class);
		context.addFlow(Flows.TASK_TO_ESCALATE, String.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context)
			throws Exception {
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
	 * ================= EscalationHandler ====================================
	 */

	@Override
	public void handleEscalation(Throwable escalation) throws Throwable {
		this.escalation = escalation;
	}

	/**
	 * Flows.
	 */
	public static enum Flows {
		TASK_TO_ESCALATE
	}

}