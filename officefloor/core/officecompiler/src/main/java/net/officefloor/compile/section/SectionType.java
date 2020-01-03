package net.officefloor.compile.section;

import net.officefloor.frame.api.manage.Office;

/**
 * <code>Type definition</code> of a section of the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionType {

	/**
	 * Obtains the {@link SectionInputType} definitions for the inputs into the
	 * {@link SectionType}.
	 * 
	 * @return {@link SectionInputType} definitions for the inputs into the
	 *         {@link SectionType}.
	 */
	SectionInputType[] getSectionInputTypes();

	/**
	 * Obtains the {@link SectionOutputType} definitions for the outputs from
	 * the {@link SectionType}.
	 * 
	 * @return {@link SectionOutputType} definitions for the outputs from the
	 *         {@link SectionType}.
	 */
	SectionOutputType[] getSectionOutputTypes();

	/**
	 * Obtains the {@link SectionObjectType} definitions for the {@link Object}
	 * dependencies required by the {@link SectionType}.
	 * 
	 * @return {@link SectionObjectType} definitions for the {@link Object}
	 *         dependencies required by the {@link SectionType}.
	 */
	SectionObjectType[] getSectionObjectTypes();

}