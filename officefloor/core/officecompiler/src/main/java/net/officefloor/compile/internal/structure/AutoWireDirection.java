package net.officefloor.compile.internal.structure;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * Direction of {@link AutoWire}.
 * 
 * @author Daniel Sagenschneider
 */
public enum AutoWireDirection {

	/**
	 * <p>
	 * Flags that the source requires to use the target. Hence, target must be child
	 * of source.
	 * <p>
	 * This is typically used in {@link ManagedObject} auto-wirings to provide
	 * dependent {@link ManagedObject}.
	 */
	SOURCE_REQUIRES_TARGET,

	/**
	 * <p>
	 * Flags that the target categories the source. Hence, source must be child of
	 * target.
	 * <p>
	 * This is typically used in {@link Team} auto-wirings to assign
	 * {@link ManagedFunction} to {@link Team}.
	 */
	TARGET_CATEGORISES_SOURCE
}