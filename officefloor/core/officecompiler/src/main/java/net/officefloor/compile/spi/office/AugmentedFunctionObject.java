package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Augmented {@link FunctionObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AugmentedFunctionObject {

	/**
	 * Obtains the name of this {@link FunctionObject}.
	 * 
	 * @return Name of this {@link FunctionObject}.
	 */
	String getFunctionObjectName();

	/**
	 * Flags this {@link FunctionObject} as a parameter for the
	 * {@link ManagedFunction}.
	 */
	void flagAsParameter();

	/**
	 * Indicates if the {@link FunctionObject} is already linked.
	 * 
	 * @return <code>true</code> if already linked.
	 */
	boolean isLinked();

}