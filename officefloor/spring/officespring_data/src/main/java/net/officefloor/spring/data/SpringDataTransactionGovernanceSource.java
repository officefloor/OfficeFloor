/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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