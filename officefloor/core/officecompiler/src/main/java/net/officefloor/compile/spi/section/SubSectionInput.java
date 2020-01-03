package net.officefloor.compile.spi.section;

/**
 * Input into a {@link SubSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SubSectionInput extends SectionFlowSinkNode {

	/**
	 * Obtains the name of this {@link SubSectionInput}.
	 * 
	 * @return Name of this {@link SubSectionInput}.
	 */
	String getSubSectionInputName();

}