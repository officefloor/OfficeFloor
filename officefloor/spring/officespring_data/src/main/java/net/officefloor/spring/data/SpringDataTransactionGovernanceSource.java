/*-
 * #%L
 * Spring Data Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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
