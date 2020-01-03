package net.officefloor.compile.spi.office.source;

import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.frame.api.manage.Office;

/**
 * Sources the {@link OfficeType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSource {

	/**
	 * <p>
	 * Obtains the {@link OfficeSourceSpecification} for this
	 * {@link OfficeSource}.
	 * <p>
	 * This enables the {@link OfficeSourceContext} to be populated with the
	 * necessary details as per this {@link OfficeSourceSpecification} in
	 * loading the {@link OfficeType}.
	 * 
	 * @return {@link OfficeSourceSpecification}.
	 */
	OfficeSourceSpecification getSpecification();

	/**
	 * Sources the {@link OfficeType} by constructing it via the input
	 * {@link OfficeArchitect}.
	 * 
	 * @param officeArchitect
	 *            {@link OfficeArchitect} to structure the {@link Office}.
	 * @param context
	 *            {@link OfficeSourceContext} to source details to structure the
	 *            {@link Office}.
	 * @throws Exception
	 *             If fails to construct the {@link Office}.
	 */
	void sourceOffice(OfficeArchitect officeArchitect,
			OfficeSourceContext context) throws Exception;

}