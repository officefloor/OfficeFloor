package net.officefloor.plugin.section.clazz.parameter;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.section.clazz.AbstractClassSectionSource;

/**
 * Manufactures the {@link Flow} for {@link AbstractClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionParameterInterrogator {

	/**
	 * Determines if parameter.
	 * 
	 * @param context {@link ClassSectionParameterInterrogatorContext}.
	 * @return <code>true</code> if parameter.
	 * @throws Exception If fails to determine if parameter.
	 */
	boolean isParameter(ClassSectionParameterInterrogatorContext context) throws Exception;

}