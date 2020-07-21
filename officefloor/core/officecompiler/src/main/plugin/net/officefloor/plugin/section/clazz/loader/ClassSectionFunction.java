package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFunction;

/**
 * {@link SectionFunction} with meta-data for {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionFunction extends ClassSectionFlow {

	/**
	 * {@link ManagedFunctionType}.
	 */
	private final ManagedFunctionType<?, ?> managedFunctionType;

	/**
	 * Instantiate.
	 * 
	 * @param function            {@link SectionFunction}.
	 * @param argumentType        Argument type for the {@link SectionFlowSinkNode}.
	 *                            May be <code>null</code> for no argument.
	 * @param managedFunctionType {@link ManagedFunctionType}.
	 */
	public ClassSectionFunction(SectionFunction function, ManagedFunctionType<?, ?> managedFunctionType,
			Class<?> argumentType) {
		super(function, argumentType);
		this.managedFunctionType = managedFunctionType;
	}

	/**
	 * Obtains the {@link SectionFunction}.
	 * 
	 * @return {@link SectionFunction}.
	 */
	public SectionFunction getFunction() {
		return this.getFlowSink();
	}

	/**
	 * Obtains the {@link ManagedFunctionType}.
	 * 
	 * @return {@link ManagedFunctionType}.
	 */
	public ManagedFunctionType<?, ?> getManagedFunctionType() {
		return this.managedFunctionType;
	}

	/*
	 * ==================== ClassSectionFlow ======================
	 */

	@Override
	public SectionFunction getFlowSink() {
		return (SectionFunction) super.getFlowSink();
	}

}