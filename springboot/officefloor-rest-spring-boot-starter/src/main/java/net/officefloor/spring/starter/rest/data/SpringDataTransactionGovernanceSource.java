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

import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.frame.api.build.None;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.TransactionDefinition;

/**
 * Spring Data transaction {@link GovernanceSource}.
 *
 * @author Daniel Sagenschneider
 */
public class SpringDataTransactionGovernanceSource extends AbstractGovernanceSource<Repository<?, ?>, None> {

    private final TransactionDefinition transactionDefinition;

    private final ApplicationContext applicationContext;

    /**
     * Instantiate.
     *
     * @param transactionDefinition {@link TransactionDefinition}.
     * @param applicationContext    {@link ApplicationContext}.
     */
    public SpringDataTransactionGovernanceSource(TransactionDefinition transactionDefinition,
                                                 ApplicationContext applicationContext) {
        this.transactionDefinition = transactionDefinition;
        this.applicationContext = applicationContext;
    }

    /*
     * ==================== GovernanceSource ====================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
        // no specification
    }

    @Override
    protected void loadMetaData(MetaDataContext<Repository<?, ?>, None> context) throws Exception {
        Class<Repository<?, ?>> repositoryClass = (Class) Repository.class;
        context.setExtensionInterface(repositoryClass);
        context.setGovernanceFactory(() -> new SpringDataTransactionGovernance(this.transactionDefinition, this.applicationContext));
    }

}
