package net.officefloor.compile.impl.structure;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.OptionalThreadLocalReceiver;
import net.officefloor.frame.api.thread.OptionalThreadLocal;

/**
 * Links the {@link OptionalThreadLocalReceiver} to the
 * {@link ManagedObjectSourceNode} for an {@link InputManagedObjectNode}.
 * 
 * @author Daniel Sagenschneider
 */
public class OptionalThreadLocalInputLinker {

	/**
	 * Listing of {@link OptionalThreadLocalReceiver} instances interested in the
	 * {@link OptionalThreadLocal}.
	 */
	private final List<OptionalThreadLocalReceiver> receivers = new LinkedList<>();

	/**
	 * {@link ManagedObjectSourceNode}.
	 */
	private ManagedObjectSourceNode managedObjectSource = null;

	/**
	 * Specifies the {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceNode {@link ManagedObjectSourceNode} for the
	 *                                {@link InputManagedObjectNode}.
	 */
	public void setManagedObjectSourceNode(ManagedObjectSourceNode managedObjectSourceNode) {
		this.managedObjectSource = managedObjectSourceNode;

		// Add null to trigger receiving the optional thread local
		this.addOptionalThreadLocalReceiver(null);
	}

	/**
	 * Adds an {@link OptionalThreadLocalReceiver}.
	 * 
	 * @param optionalThreadLocalReceiver {@link OptionalThreadLocalReceiver}.
	 */
	public void addOptionalThreadLocalReceiver(OptionalThreadLocalReceiver optionalThreadLocalReceiver) {

		// Add the optional thread local receiver
		if (optionalThreadLocalReceiver != null) {
			this.receivers.add(optionalThreadLocalReceiver);
		}

		// Ensure have managed object source node
		if (this.managedObjectSource == null) {
			return; // not yet available
		}

		// Load the optional thread local receivers
		for (OptionalThreadLocalReceiver receiver : this.receivers) {
			managedObjectSource.buildSupplierThreadLocal(receiver);
		}

		// All received (so clear to avoid adding again)
		this.receivers.clear();
	}

}