package net.officefloor.spring.starter.rest.data;

import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.frame.api.build.None;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.Repository;

/**
 * Spring Data transaction {@link GovernanceSource}.
 *
 * @author Daniel Sagenschneider
 */
public class SpringDataTransactionGovernanceSource extends AbstractGovernanceSource<Repository<?, ?>, None> {

    private final ApplicationContext applicationContext;

    /**
     * Instantiate.
     *
     * @param applicationContext {@link ApplicationContext}.
     */
    public SpringDataTransactionGovernanceSource(ApplicationContext applicationContext) {
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
        context.setGovernanceFactory(() -> new SpringDataTransactionGovernance(this.applicationContext));
    }

}
