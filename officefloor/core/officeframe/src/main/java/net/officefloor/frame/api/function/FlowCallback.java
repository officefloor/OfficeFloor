package net.officefloor.frame.api.function;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;

/**
 * <p>
 * Callback for completion of a {@link Flow}.
 * <p>
 * The return state of the {@link Flow} is available from the
 * {@link ManagedObject} instances manipulated by the {@link Flow} (hence there
 * is no returned value to the {@link FlowCallback}).
 *
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface FlowCallback {

	/**
	 * {@link FlowCallback} that simply escalates the {@link Flow}
	 * {@link Escalation} to the invoking {@link ManagedFunctionContainer} (i.e.
	 * {@link ManagedFunction} invoking the {@link Flow}).
	 */
	public static FlowCallback ESCALATE = new FlowCallback() {
		@Override
		public void run(Throwable escalation) throws Throwable {
			if (escalation != null) {
				throw escalation;
			}
		}
	};

	/**
	 * Invoked on completion of the {@link Flow}.
	 * 
	 * @param escalation Possible {@link Throwable} from the {@link Flow}.
	 *                   <code>null</code> indicates all {@link Escalation}
	 *                   instances handled within {@link Flow}.
	 * @throws Throwable {@link Escalation} within the callback logic.
	 */
	void run(Throwable escalation) throws Throwable;

}