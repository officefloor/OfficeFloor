/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.frame.api.build.None;

/**
 * {@link GovernanceSource} to provide JPA {@link EntityTransaction} management.
 * 
 * @author Daniel Sagenschneider
 */
public class JpaTransactionGovernanceSource extends
		AbstractGovernanceSource<EntityManager, None> {

	/*
	 * ===================== GovernanceSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// TODO implement
		// AbstractGovernanceSource<EntityManager,None>.loadSpecification
		throw new UnsupportedOperationException(
				"TODO implement AbstractGovernanceSource<EntityManager,None>.loadSpecification");
	}

	@Override
	protected void loadMetaData(MetaDataContext<EntityManager, None> context)
			throws Exception {
		// TODO implement
		// AbstractGovernanceSource<EntityManager,None>.loadMetaData
		throw new UnsupportedOperationException(
				"TODO implement AbstractGovernanceSource<EntityManager,None>.loadMetaData");
	}

}