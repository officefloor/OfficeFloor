package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Decoration of {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionDecoration {

	/**
	 * Indicates if include {@link ManagedFunctionType}.
	 * 
	 * @param functionType  {@link ManagedFunctionType}.
	 * @param loaderContext {@link ClassSectionLoaderContext}.
	 * @return <code>true</code> to include the {@link ManagedFunctionType}.
	 */
	public boolean isIncludeFunction(ManagedFunctionType<?, ?> functionType, ClassSectionLoaderContext loaderContext) {
		return true;
	}

	/**
	 * Obtains the {@link ManagedFunction} name from the
	 * {@link ManagedFunctionType}.
	 * 
	 * @param functionType  {@link ManagedFunctionType}.
	 * @param loaderContext {@link ClassSectionLoaderContext}.
	 * @return {@link ManagedFunction} name.
	 */
	public String getFunctionName(ManagedFunctionType<?, ?> functionType, ClassSectionLoaderContext loaderContext) {
		return functionType.getFunctionName();
	}

	/**
	 * Decorates the {@link SectionFunction}.
	 * 
	 * @param functionContext {@link FunctionClassSectionLoaderContext}.
	 */
	public void decorateSectionFunction(FunctionClassSectionLoaderContext functionContext) {
		// No decoration by default
	}

}