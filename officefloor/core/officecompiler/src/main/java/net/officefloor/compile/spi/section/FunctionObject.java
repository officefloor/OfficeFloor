package net.officefloor.compile.spi.section;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link Object} required by the {@link SectionFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionObject extends SectionDependencyRequireNode {

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

}