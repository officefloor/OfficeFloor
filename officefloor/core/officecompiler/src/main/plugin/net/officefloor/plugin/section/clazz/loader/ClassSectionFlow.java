package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.spi.section.SectionFlowSinkNode;

/**
 * {@link SectionFlowSinkNode} with meta-data for {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionFlow {

	/**
	 * {@link SectionFlowSinkNode}.
	 */
	private final SectionFlowSinkNode flowSink;

	/**
	 * Argument type for the {@link SectionFlowSinkNode}. May be <code>null</code>
	 * for no argument.
	 */
	private final Class<?> argumentType;

	/**
	 * Instantiate.
	 * 
	 * @param flowSink     {@link SectionFlowSinkNode}.
	 * @param argumentType Argument type for the {@link SectionFlowSinkNode}. May be
	 *                     <code>null</code> for no argument.
	 */
	public ClassSectionFlow(SectionFlowSinkNode flowSink, Class<?> argumentType) {
		this.flowSink = flowSink;
		this.argumentType = argumentType;
	}

	/**
	 * Obtains the {@link SectionFlowSinkNode}.
	 * 
	 * @return {@link SectionFlowSinkNode}.
	 */
	public SectionFlowSinkNode getFlowSink() {
		return flowSink;
	}

	/**
	 * Obtains the argument type.
	 * 
	 * @return Argument type for the {@link SectionFlowSinkNode}. May be
	 *         <code>null</code> for no argument.
	 */
	public Class<?> getArgumentType() {
		return argumentType;
	}

}