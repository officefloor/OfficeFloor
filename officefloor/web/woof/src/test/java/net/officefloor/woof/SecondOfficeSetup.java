package net.officefloor.woof;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerConfigurationService;
import net.officefloor.compile.OfficeFloorCompilerConfigurationServiceFactory;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.TestSource;

/**
 * Sets up alternate {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class SecondOfficeSetup extends AbstractOfficeFloorSource
		implements OfficeFloorCompilerConfigurationService, OfficeFloorCompilerConfigurationServiceFactory {

	/**
	 * Indicates whether to configure second {@link Office}.
	 */
	public static boolean isConfigureSecond = false;

	/*
	 * ============== OfficeFloorCompilerConfigurationServiceFactory ==============
	 */

	@Override
	public OfficeFloorCompilerConfigurationService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================= OfficeFloorCompilerConfigurationService ==================
	 */

	@Override
	public void configureOfficeFloorCompiler(OfficeFloorCompiler compiler) throws Exception {
		if (isConfigureSecond) {
			compiler.setOfficeFloorSource(this);
		}
	}

	/*
	 * ============================ OfficeFloorSource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	public void specifyConfigurationProperties(RequiredProperties requiredProperties, OfficeFloorSourceContext context)
			throws Exception {
		// no configuration properties
	}

	@Override
	public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {
		deployer.addDeployedOffice("second", new SecondOfficeSource(), null);
	}

	/**
	 * {@link OfficeSource} to create an empty {@link Office}.
	 */
	@TestSource
	private static class SecondOfficeSource extends AbstractOfficeSource {

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