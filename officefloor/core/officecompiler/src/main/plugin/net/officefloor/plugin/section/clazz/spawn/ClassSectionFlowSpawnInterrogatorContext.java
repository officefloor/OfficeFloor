package net.officefloor.plugin.section.clazz.spawn;

import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.spi.section.source.SectionSourceContext;

/**
 * Context for the {@link ClassSectionFlowSpawnInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionFlowSpawnInterrogatorContext {

	/**
	 * Obtains the {@link ManagedFunctionFlowType}.
	 * 
	 * @return {@link ManagedFunctionFlowType}.
	 */
	ManagedFunctionFlowType<?> getManagedFunctionFlowType();

	/**
	 * Obtains the {@link SectionSourceContext}.
	 * 
	 * @return {@link SectionSourceContext}.
	 */
	SectionSourceContext getSourceContext();

}