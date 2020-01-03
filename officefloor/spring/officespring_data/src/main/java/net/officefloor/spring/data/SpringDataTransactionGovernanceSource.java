package net.officefloor.spring.data;

import org.springframework.transaction.PlatformTransactionManager;

import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.frame.api.build.None;

/**
 * Spring Data transaction {@link GovernanceSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringDataTransactionGovernanceSource extends AbstractGovernanceSource<PlatformTransactionManager, None> {

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<PlatformTransactionManager, None> context) throws Exception {
		context.setExtensionInterface(PlatformTransactionManager.class);
		context.setGovernanceFactory(() -> new SpringDataTransactionGovernance());
	}

}