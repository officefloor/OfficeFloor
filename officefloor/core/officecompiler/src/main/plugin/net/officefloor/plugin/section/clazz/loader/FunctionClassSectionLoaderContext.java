package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFunction;

/**
 * Context for the {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionClassSectionLoaderContext extends ClassSectionLoaderContext {

	/**
	 * Obtains the {@link SectionFunction}.
	 * 
	 * @return {@link SectionFunction}.
	 */
	SectionFunction getSectionFunction();

	/**
	 * Obtains the {@link ManagedFunctionType}.
	 * 
	 * @return {@link ManagedFunctionType}.
	 */
	ManagedFunctionType<?, ?> getManagedFunctionType();

	/**
	 * Obtains the parameter type.
	 * 
	 * @return Parameter type.
	 */
	Class<?> getParameterType();

	/**
	 * Flags the next {@link SectionFlowSinkNode} linked.
	 */
	void flagNextLinked();

	/**
	 * Flags the {@link FunctionObject} linked.
	 * 
	 * @param objectIndex Index of the {@link FunctionObject}.
	 */
	void flagFunctionObjectLinked(int objectIndex);

	/**
	 * Flags the {@link FunctionFlow} linked.
	 * 
	 * @param flowIndex Index of the {@link FunctionFlow}.
	 */
	void flagFunctionFlowLinked(int flowIndex);

}