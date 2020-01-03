package net.officefloor.compile.spi.section;

/**
 * Output for a {@link SubSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SubSectionOutput extends SectionFlowSourceNode {

	/**
	 * Obtains the name of this {@link SubSectionOutput}.
	 * 
	 * @return Name of this {@link SubSectionOutput}.
	 */
	String getSubSectionOutputName();

}