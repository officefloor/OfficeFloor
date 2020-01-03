package net.officefloor.compile.spi.section.source;

import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionDesigner;

/**
 * Sources the {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionSource {

	/**
	 * <p>
	 * Obtains the {@link SectionSourceSpecification} for this
	 * {@link SectionSource}.
	 * <p>
	 * This enables the {@link SectionSourceContext} to be populated with the
	 * necessary details as per this {@link SectionSourceSpecification} in
	 * loading the {@link SectionType}.
	 * 
	 * @return {@link SectionSourceSpecification}.
	 */
	SectionSourceSpecification getSpecification();

	/**
	 * Sources the {@link OfficeSection} by constructing it via the input
	 * {@link SectionDesigner}.
	 * 
	 * @param designer
	 *            {@link SectionDesigner} to construct the structure of the
	 *            {@link OfficeSection}.
	 * @param context
	 *            {@link SectionSourceContext} to source details to construct
	 *            the {@link OfficeSection}.
	 * @throws Exception
	 *             If fails to construct the {@link OfficeSection}.
	 */
	void sourceSection(SectionDesigner designer, SectionSourceContext context)
			throws Exception;

}