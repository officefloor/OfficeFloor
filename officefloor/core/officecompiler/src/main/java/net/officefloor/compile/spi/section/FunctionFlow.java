package net.officefloor.compile.spi.section;

import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link Flow} from the {@link SectionFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionFlow {

	/**
	 * Obtains the name of this {@link FunctionFlow}.
	 * 
	 * @return Name of this {@link FunctionFlow}.
	 */
	String getFunctionFlowName();

}