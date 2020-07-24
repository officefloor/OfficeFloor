package net.officefloor.plugin.section.clazz.parameter;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.frame.api.source.SourceContext;

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
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}