package net.officefloor.plugin.section.clazz;

import net.officefloor.compile.spi.section.SubSection;

/**
 * Name of {@link SubSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionNameAnnotation {

	/**
	 * Name of the {@link SubSection}.
	 */
	private final String name;

	/**
	 * Instantiate.
	 * 
	 * @param name Name of the {@link SubSection}.
	 */
	public SectionNameAnnotation(String name) {
		this.name = name;
	}

	/**
	 * Obtains the name of the {@link SubSection}.
	 * 
	 * @return Name of the {@link SubSection}.
	 */
	public String getName() {
		return this.name;
	}

}