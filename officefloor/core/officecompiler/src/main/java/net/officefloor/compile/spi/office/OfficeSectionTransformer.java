package net.officefloor.compile.spi.office;

/**
 * Enables transforming the {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionTransformer {

	/**
	 * Transforms the {@link OfficeSection}.
	 * 
	 * @param context
	 *            {@link OfficeSectionTransformerContext}.
	 */
	void transformOfficeSection(OfficeSectionTransformerContext context);

}