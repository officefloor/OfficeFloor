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
package net.officefloor.model.impl.officefloor;

import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.model.impl.AbstractChangesTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Abstract functionality for testing the {@link OfficeFloorChanges}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeFloorChangesTestCase extends
		AbstractChangesTestCase<OfficeFloorModel, OfficeFloorChanges> {

	/**
	 * Initiate.
	 */
	public AbstractOfficeFloorChangesTestCase() {
	}

	/**
	 * Initiate.
	 *
	 * @param isSpecificSetupFilePerTest
	 *            Flag if specific setup file to be used.
	 */
	public AbstractOfficeFloorChangesTestCase(boolean isSpecificSetupFilePerTest) {
		super(isSpecificSetupFilePerTest);
	}

	/*
	 * =================== AbstractChangesTestCase ===========================
	 */

	@Override
	protected OfficeFloorModel retrieveModel(ConfigurationItem configurationItem)
			throws Exception {
		return new OfficeFloorRepositoryImpl(new ModelRepositoryImpl())
				.retrieveOfficeFloor(configurationItem);
	}

	@Override
	protected OfficeFloorChanges createModelOperations(OfficeFloorModel model) {
		return new OfficeFloorChangesImpl(model);
	}

	@Override
	protected String getModelFileExtension() {
		return ".officefloor.xml";
	}

	/**
	 * Constructs the {@link OfficeType}.
	 *
	 * @param constructor
	 *            {@link OfficeTypeConstructor}.
	 * @return {@link OfficeType}.
	 */
	protected OfficeType constructOfficeType(OfficeTypeConstructor constructor) {
		OfficeTypeContextImpl context = new OfficeTypeContextImpl();
		constructor.construct(context);
		return context;
	}

	/**
	 * Constructs the {@link OfficeType}.
	 */
	protected interface OfficeTypeConstructor {

		/**
		 * Constructs the {@link OfficeType}.
		 *
		 * @param context
		 *            {@link OfficeTypeContext}.
		 */
		void construct(OfficeTypeContext context);
	}

	/**
	 * Context for the {@link OfficeTypeConstructor}.
	 */
	protected interface OfficeTypeContext {

	}

	/**
	 * {@link OfficeTypeContext} implementation.
	 */
	private class OfficeTypeContextImpl implements OfficeTypeContext,
			OfficeType {

		/*
		 * ===================== OfficeType ================================
		 */

		@Override
		public OfficeInputType[] getOfficeInputTypes() {
			// TODO Implement OfficeType.getOfficeInputTypes
			throw new UnsupportedOperationException(
					"OfficeType.getOfficeInputTypes");
		}

		@Override
		public OfficeManagedObjectType[] getOfficeManagedObjectTypes() {
			// TODO Implement OfficeType.getOfficeManagedObjectTypes
			throw new UnsupportedOperationException(
					"OfficeType.getOfficeManagedObjectTypes");
		}

		@Override
		public OfficeTeamType[] getOfficeTeamTypes() {
			// TODO Implement OfficeType.getOfficeTeamTypes
			throw new UnsupportedOperationException(
					"OfficeType.getOfficeTeamTypes");
		}
	}

}