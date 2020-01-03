package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Registry of the {@link ManagedObjectNode} within a particular context (for
 * example {@link SectionNode}).
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectRegistry {

	/**
	 * <p>
	 * Obtains the {@link ManagedObjectNode} from the registry.
	 * <p>
	 * The returned {@link ManagedObjectNode} may or may not be initialised.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObjectNode}.
	 * @return {@link ManagedObjectNode} from the registry.
	 */
	ManagedObjectNode getManagedObjectNode(String managedObjectName);

	/**
	 * <p>
	 * Adds an initialised {@link ManagedObjectNode} to the registry.
	 * <p>
	 * Should an {@link ManagedObjectNode} already be added by the name, then an
	 * issue is reported to the {@link CompilerIssue}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObjectNode}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope}.
	 * @param managedObjectSourceNode
	 *            {@link ManagedObjectSourceNode} for the
	 *            {@link ManagedObjectNode}.
	 * @return Initialised {@link ManagedObjectNode} by the name.
	 */
	ManagedObjectNode addManagedObjectNode(String managedObjectName,
			ManagedObjectScope managedObjectScope,
			ManagedObjectSourceNode managedObjectSourceNode);

}