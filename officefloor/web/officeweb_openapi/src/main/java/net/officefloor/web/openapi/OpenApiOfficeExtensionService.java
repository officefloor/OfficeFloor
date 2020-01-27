package net.officefloor.web.openapi;

import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.web.build.WebArchitect;

/**
 * {@link OfficeExtensionService} to configure OpenAPI specification.
 * 
 * @author Daniel Sagenschneider
 */
public class OpenApiOfficeExtensionService implements OfficeExtensionService {

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		OfficeSectionInput input = officeArchitect.getOfficeSection(WebArchitect.HANDLER_SECTION_NAME)
				.getOfficeSectionInput(WebArchitect.HANDLER_INPUT_NAME);
		input.addExecutionExplorer((explorer) -> {

			ExecutionManagedFunction initial = explorer.getInitialManagedFunction();
			System.out.println("INTIIAL: " + initial.getManagedFunctionName());

			for (ManagedFunctionFlowType<?> childFlowType : initial.getManagedFunctionType().getFlowTypes()) {
				ExecutionManagedFunction child = initial.getManagedFunction(childFlowType);
				System.out.println("  CHILD: " + childFlowType.getFlowName() + " -> " + child.getManagedFunctionName());
				for (ManagedFunctionObjectType<?> objectType : child.getManagedFunctionType().getObjectTypes()) {
					System.out.println("    " + objectType.getObjectType().getName());
				}
				
				for (ManagedFunctionFlowType<?> grandChildFlowType : child.getManagedFunctionType().getFlowTypes()) {
					ExecutionManagedFunction grandChild = child.getManagedFunction(grandChildFlowType);
					System.out.println("    GRAND CHILD: " + grandChild.getManagedFunctionName());
				}
			}
		});
	}

}