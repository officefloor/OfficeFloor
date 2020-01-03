package net.officefloor.compile.spi.section;

/**
 * Object for a {@link SubSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SubSectionObject extends SectionDependencyRequireNode {

	/**
	 * Obtains the name of this {@link SubSectionObject}.
	 * 
	 * @return Name of this {@link SubSectionObject}.
	 */
	String getSubSectionObjectName();

}