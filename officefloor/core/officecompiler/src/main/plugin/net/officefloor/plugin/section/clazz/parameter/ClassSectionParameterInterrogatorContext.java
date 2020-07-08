package net.officefloor.plugin.section.clazz.parameter;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.spi.section.source.SectionSourceContext;

/**
 * Context for the {@link ClassSectionParameterInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionParameterInterrogatorContext {

	/**
	 * Obtains the {@link ManagedFunctionObjectType}.
	 * 
	 * @return {@link ManagedFunctionObjectType}.
	 */
	ManagedFunctionObjectType<?> getManagedFunctionObjectType();

	/**
	 * Obtains the {@link SectionSourceContext}.
	 * 
	 * @return {@link SectionSourceContext}.
	 */
	SectionSourceContext getSourceContext();

}