package net.officefloor.plugin.clazz;

import net.officefloor.frame.internal.structure.Flow;

/**
 * Registry of {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassFlowRegistry {

	/**
	 * Registers the {@link Flow}.
	 * 
	 * @param label             Label for the {@link Flow}.
	 * @param flowParameterType {@link Class} for the parameter to the {@link Flow}.
	 *                          May be <code>null</code>.
	 * @return Index of the {@link Flow}.
	 */
	int registerFlow(String label, Class<?> flowParameterType);

}