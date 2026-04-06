package net.officefloor.spring.starter.rest.data;

import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.spring.starter.rest.OfficeFloorSpringBootExtension;
import net.officefloor.spring.starter.rest.OfficeFloorSpringBootExtensionContext;

public class SpringBootDataExtension implements OfficeFloorSpringBootExtension {

    /*
     * ========================== OfficeFloorSpringBootExtension ================
     */

    @Override
    public void extendSpringBootSupport(OfficeFloorSpringBootExtensionContext context) throws Exception {

        // Obtain the governance name
        String governanceName = context.getOfficeSourceContext().getProperty("officefloor.transaction.governance.name", "transaction");

        // Configure the transaction management governance
        OfficeGovernance governance = context.getOfficeArchitect()
                .addOfficeGovernance(governanceName, new SpringDataTransactionGovernanceSource(context.getApplicationContext()));
        governance.enableAutoWireExtensions();

        // Make available for governance
        context.getComposeArchitect().addGovernance(governanceName, governance);
    }
}
