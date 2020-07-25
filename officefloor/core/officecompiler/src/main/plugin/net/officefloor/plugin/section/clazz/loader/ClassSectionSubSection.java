package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.SubSection;

/**
 * {@link SubSection} with meta-data for {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionSubSection {

	/**
	 * {@link SubSection}.
	 */
	private final SubSection subSection;

	/**
	 * {@link SectionType}.
	 */
	private final SectionType sectionType;

	/**
	 * Instantiate.
	 * 
	 * @param subSection  {@link SubSection}.
	 * @param sectionType {@link SectionType}.
	 */
	public ClassSectionSubSection(SubSection subSection, SectionType sectionType) {
		this.subSection = subSection;
		this.sectionType = sectionType;
	}

	/**
	 * Obtains the {@link SubSection}.
	 * 
	 * @return {@link SubSection}.
	 */
	public SubSection getSubSection() {
		return subSection;
	}

	/**
	 * Obtains the {@link SectionType}.
	 * 
	 * @return {@link SectionType}.
	 */
	public SectionType getSectionType() {
		return sectionType;
	}

}