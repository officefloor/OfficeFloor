package net.officefloor.plugin.section.clazz;

import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;

/**
 * {@link FlowLink} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowLinkAnnotation {

	/**
	 * Name of the {@link ManagedObjectFlow}.
	 */
	private final String name;

	/**
	 * Name of the method to link the {@link FlowLink}.
	 */
	private final String method;

	/**
	 * Instantiate.
	 * 
	 * @param name   Name of the {@link ManagedObjectFlow}.
	 * @param method Name of the method to link the {@link FlowLink}.
	 */
	public FlowLinkAnnotation(String name, String method) {
		this.name = name;
		this.method = method;
	}

	/**
	 * Instantiate.
	 * 
	 * @param flowLink {@link FlowLink}.
	 */
	public FlowLinkAnnotation(FlowLink flowLink) {
		this(flowLink.name(), flowLink.method());
	}

	/**
	 * Obtains the name of the {@link ManagedObjectFlow}.
	 * 
	 * @return Name of the {@link ManagedObjectFlow}.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the name of the method to link the {@link FlowLink}.
	 * 
	 * @return Name of the method to link the {@link FlowLink}.
	 */
	public String getMethod() {
		return this.method;
	}

}