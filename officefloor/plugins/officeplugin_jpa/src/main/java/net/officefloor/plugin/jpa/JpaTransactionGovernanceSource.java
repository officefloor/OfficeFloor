/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.jpa;

import javax.persistence.EntityTransaction;

import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;

/**
 * {@link GovernanceSource} to provide JPA {@link EntityTransaction} management.
 * 
 * @author Daniel Sagenschneider
 */
public class JpaTransactionGovernanceSource extends AbstractGovernanceSource<EntityTransaction, None>
		implements GovernanceFactory<EntityTransaction, None> {

	/*
	 * ===================== GovernanceSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<EntityTransaction, None> context) throws Exception {
		// Provide the meta-data
		context.setExtensionInterface(EntityTransaction.class);
		context.setGovernanceFactory(this);
	}

	/*
	 * ==================== GovernanceFactory =========================
	 */

	@Override
	public Governance<EntityTransaction, None> createGovernance() throws Throwable {
		return new JpaTransactionGovernance();
	}

}