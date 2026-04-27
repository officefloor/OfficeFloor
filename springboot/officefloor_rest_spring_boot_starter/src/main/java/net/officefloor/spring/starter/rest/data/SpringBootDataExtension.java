package net.officefloor.spring.starter.rest.data;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.spring.starter.rest.OfficeFloorSpringBootExtension;
import net.officefloor.spring.starter.rest.OfficeFloorSpringBootExtensionContext;
import org.springframework.transaction.TransactionDefinition;

public class SpringBootDataExtension implements OfficeFloorSpringBootExtension {

    /*
     * ========================== OfficeFloorSpringBootExtension ================
     */

    @Override
    public void extendSpringBootSupport(OfficeFloorSpringBootExtensionContext context) throws Exception {

        // Obtain the governance names
        String transactionName = context.getOfficeSourceContext().getProperty("officefloor.transaction.governance.name", "transaction");
        String readOnlyName = context.getOfficeSourceContext().getProperty("officefloor.transaction.readonly.governance.name", "readonly-transaction");

        // Configure the transaction governance
        OfficeGovernance transaction = context.getOfficeArchitect()
                .addOfficeGovernance(transactionName, new SpringDataTransactionGovernanceSource(new ReadWriteTransactionDefinition(), context.getApplicationContext()));
        transaction.enableAutoWireExtensions();

        // Configure the read only transaction governance
        OfficeGovernance readOnlyTransaction = context.getOfficeArchitect()
                .addOfficeGovernance(readOnlyName, new SpringDataTransactionGovernanceSource(new ReadOnlyTransactionDefinition(), context.getApplicationContext()));
        readOnlyTransaction.enableAutoWireExtensions();

        // Make available for governance
        ComposeArchitect compose = context.getComposeArchitect();
        compose.addGovernance(transactionName, transaction);
        compose.addGovernance(readOnlyName, readOnlyTransaction);
    }

    private static class ReadWriteTransactionDefinition implements TransactionDefinition {
    }

    private static class ReadOnlyTransactionDefinition implements TransactionDefinition {
        @Override
        public boolean isReadOnly() {
            return true;
        }
    }

}
