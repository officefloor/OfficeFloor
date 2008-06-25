/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.administratorsource;

import junit.framework.TestCase;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource;

/**
 * Test {@link AdministratorSource}.
 * 
 * @author Daniel
 */
public class TestAdministratorSource extends
		AbstractAdministratorSource<Object, MockDutyKeys> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource#loadSpecification(net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty("test property");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource#loadMetaData(net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource.MetaDataContext)
	 */
	@Override
	protected void loadMetaData(MetaDataContext<Object, MockDutyKeys> context)
			throws Exception {
		context.setDutyKeys(MockDutyKeys.class);
		context.setDutyFlows(MockDutyKeys.KEY_ONE, MockDutyFlowKeys.class);
		context.setExtensionInterface(Object.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.AdministratorSource#createAdministrator()
	 */
	@Override
	public Administrator<Object, MockDutyKeys> createAdministrator() {
		TestCase.fail("Should not be creating administrator");
		return null;
	}

}
