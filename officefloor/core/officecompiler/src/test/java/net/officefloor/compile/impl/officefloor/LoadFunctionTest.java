package net.officefloor.compile.impl.officefloor;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;

/**
 * Tests loading a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadFunctionTest extends AbstractOfficeFloorTestCase {

	/**
	 * Ensure can load a simple {@link ManagedFunction}.
	 */
	public void testLoadSimpleFunction() {

		ManagedFunctionFactory<?, ?> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Record loading the function
		this.record_initiateOfficeFloorBuilder();
		this.record_officefloor_addTeam("OFFICE_FLOOR_TEAM");
		OfficeBuilder officeBuilder = this.record_officefloor_addOffice("OFFICE");
		officeBuilder.registerTeam("OFFICE_TEAM", "OFFICE_FLOOR_TEAM");
		ManagedFunctionBuilder<?, ?> functionBuilder = this.record_office_addFunction("SECTION.FUNCTION",
				functionFactory);
		functionBuilder.setResponsibleTeam("OFFICE_TEAM");

		// Loads the OfficeFloor with a simple function
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) throws Exception {
				OfficeFloorDeployer deployer = context.getDeployer();

				// Add an office with a team
				DeployedOffice office = context.addOffice("OFFICE", new OfficeMaker() {
					@Override
					public void make(OfficeMakerContext context) {
						OfficeArchitect architect = context.getArchitect();

						// Add the section with a team
						OfficeSection section = context.addSection("SECTION", new SectionMaker() {
							@Override
							public void make(SectionMakerContext context) {
								context.addFunction("NAMESPACE", "FUNCTION", functionFactory, null);
							}
						});

						// Link in team responsible for function
						architect.link(section.getOfficeSectionFunction("FUNCTION").getResponsibleTeam(),
								architect.addOfficeTeam("OFFICE_TEAM"));
					}
				});

				// Specify team responsible for function
				deployer.link(office.getDeployedOfficeTeam("OFFICE_TEAM"), context.addTeam("OFFICE_FLOOR_TEAM", null));
			}
		});
	}

}