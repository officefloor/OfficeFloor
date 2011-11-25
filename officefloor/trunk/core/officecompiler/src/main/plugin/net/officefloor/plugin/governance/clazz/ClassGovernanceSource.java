/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.governance.clazz;

import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.governance.Governance;

/**
 * {@link GovernanceSource} that uses a {@link Class} to reflectively provide
 * the functionality for {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassGovernanceSource extends
		AbstractGovernanceSource<Object, Indexed> {

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/*
	 * ================== GovernanceSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_NAME_PROPERTY_NAME, "Class");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Object, Indexed> context)
			throws Exception {
		// TODO implement AbstractGovernanceSource<I,Indexed>.loadMetaData
		throw new UnsupportedOperationException(
				"TODO implement AbstractGovernanceSource<I,Indexed>.loadMetaData");
	}

	@Override
	public Governance<Object, Indexed> createGovernance() throws Throwable {
		// TODO implement GovernanceSource<I,Indexed>.createGovernance
		throw new UnsupportedOperationException(
				"TODO implement GovernanceSource<I,Indexed>.createGovernance");
	}

}