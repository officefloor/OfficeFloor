package net.officefloor.plugin.section.clazz.flow;

import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link Flow} link for {@link SubSectionOutput}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionSubSectionOutputLink {

	/**
	 * Obtains the {@link SubSectionOutput} name.
	 * 
	 * @return {@link SubSectionOutput} name.
	 */
	String getSubSectionOutputName();

	/**
	 * Obtains the link name.
	 * 
	 * @return Link name.
	 */
	String getLinkName();

}