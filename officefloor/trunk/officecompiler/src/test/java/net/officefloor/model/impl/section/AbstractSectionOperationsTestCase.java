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
package net.officefloor.model.impl.section;

import net.officefloor.model.impl.AbstractOperationsTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionOperations;

/**
 * Abstract functionality for testing the {@link SectionOperations}.
 * 
 * @author Daniel
 */
public abstract class AbstractSectionOperationsTestCase extends
		AbstractOperationsTestCase<SectionModel, SectionOperations> {

	/**
	 * Initiate.
	 */
	public AbstractSectionOperationsTestCase() {
	}

	/**
	 * Initiate.
	 * 
	 * @param isSpecificSetupFilePerTest
	 *            Flag if specific setup file to be used.
	 */
	public AbstractSectionOperationsTestCase(boolean isSpecificSetupFilePerTest) {
		super(isSpecificSetupFilePerTest);
	}

	/*
	 * ================== AbstractOperationsTestCase =========================
	 */

	@Override
	protected SectionModel retrieveModel(ConfigurationItem configurationItem)
			throws Exception {
		return new SectionRepositoryImpl(new ModelRepositoryImpl())
				.retrieveSection(configurationItem);
	}

	@Override
	protected SectionOperations createModelOperations(SectionModel model) {
		return new SectionOperationsImpl(model);
	}

	@Override
	protected String getModelFileExtension() {
		return ".section.xml";
	}

}