package net.officefloor.compile.impl;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.source.PrivateSource;

/**
 * <p>
 * {@link OfficeFloorSource} to create a single empty {@link Office}.
 * <p>
 * It is expected that functionality will be loaded via
 * {@link OfficeExtensionService} instances.
 *
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class ApplicationOfficeFloorSource extends AbstractOfficeFloorSource {

	/**
	 * Name of the default {@link Office}.
	 */
	public static final String OFFICE_NAME = "OFFICE";

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no properties required
	}

	@Override
	public void specifyConfigurationProperties(RequiredProperties requiredProperties, OfficeFloorSourceContext context)
			throws Exception {
		// no additional properties required
	}

	@Override
	public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {
		deployer.addDeployedOffice(OFFICE_NAME, new ApplicationOfficeSource(), null);
	}

	/**
	 * {@link OfficeSource} to create an empty {@link Office}.
	 */
	@PrivateSource
	private static class ApplicationOfficeSource extends AbstractOfficeSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no properties required
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {
			// empty office
		}
	}

}