package net.officefloor.compile.spi.section;

import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.properties.PropertyConfigurable;

/**
 * {@link SubSection} of an {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SubSection extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link SubSection}.
	 * 
	 * @return Name of this {@link SubSection}.
	 */
	String getSubSectionName();

	/**
	 * Obtains the {@link SubSectionInput}.
	 * 
	 * @param inputName
	 *            Name of the {@link SubSectionInput} to obtain.
	 * @return {@link SubSectionInput}.
	 */
	SubSectionInput getSubSectionInput(String inputName);

	/**
	 * Obtains the {@link SubSectionOutput}.
	 * 
	 * @param outputName
	 *            Name of the {@link SubSectionOutput} to obtain.
	 * @return {@link SubSectionOutput}.
	 */
	SubSectionOutput getSubSectionOutput(String outputName);

	/**
	 * Obtains the {@link SubSectionObject}.
	 * 
	 * @param objectName
	 *            Name of the {@link SubSectionObject} to obtain.
	 * @return {@link SubSectionObject}.
	 */
	SubSectionObject getSubSectionObject(String objectName);

}