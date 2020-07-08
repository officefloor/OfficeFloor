package net.officefloor.plugin.section.clazz.spawn;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.section.clazz.AbstractClassSectionSource;

/**
 * Manufactures the {@link Flow} for {@link AbstractClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionFlowSpawnInterrogator {

	/**
	 * Determines if spawn {@link Flow}.
	 * 
	 * @param context {@link ClassSectionFlowSpawnInterrogatorContext}.
	 * @return <code>true</code> if spawn {@link Flow}.
	 * @throws Exception If fails to determine if spawn.
	 */
	boolean isSpawnFlow(ClassSectionFlowSpawnInterrogatorContext context) throws Exception;

}