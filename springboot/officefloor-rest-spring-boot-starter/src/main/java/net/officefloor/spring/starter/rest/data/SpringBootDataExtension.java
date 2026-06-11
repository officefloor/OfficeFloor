/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.spring.starter.rest.data;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.spring.starter.rest.OfficeFloorSpringBootExtension;
import net.officefloor.spring.starter.rest.OfficeFloorSpringBootExtensionContext;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.TransactionDefinition;

/** {@link OfficeFloorSpringBootExtension} for Spring Boot Data. */
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

        // Configure the read only transaction governance
        OfficeGovernance readOnlyTransaction = context.getOfficeArchitect()
                .addOfficeGovernance(readOnlyName, new SpringDataTransactionGovernanceSource(new ReadOnlyTransactionDefinition(), context.getApplicationContext()));

        // Only auto-wire governance extensions when Spring Data Repository beans are present.
        if (!context.getApplicationContext().getBeansOfType(Repository.class).isEmpty()) {
            transaction.enableAutoWireExtensions();
            readOnlyTransaction.enableAutoWireExtensions();
        }

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
