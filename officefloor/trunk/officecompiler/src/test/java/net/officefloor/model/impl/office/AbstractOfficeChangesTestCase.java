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
package net.officefloor.model.impl.office;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.model.impl.AbstractChangesTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Abstract functionality for testing the {@link OfficeChanges}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeChangesTestCase extends
		AbstractChangesTestCase<OfficeModel, OfficeChanges> {

	/**
	 * Initiate.
	 */
	public AbstractOfficeChangesTestCase() {
	}

	/**
	 * Initiate.
	 *
	 * @param isSpecificSetupFilePerTest
	 *            Flag if specific setup file to be used.
	 */
	public AbstractOfficeChangesTestCase(boolean isSpecificSetupFilePerTest) {
		super(isSpecificSetupFilePerTest);
	}

	/*
	 * ================== AbstractChangesTestCase ==============================
	 */

	@Override
	protected OfficeModel retrieveModel(ConfigurationItem configurationItem)
			throws Exception {
		return new OfficeRepositoryImpl(new ModelRepositoryImpl())
				.retrieveOffice(configurationItem);
	}

	@Override
	protected OfficeChanges createModelOperations(OfficeModel model) {
		return new OfficeChangesImpl(model);
	}

	@Override
	protected String getModelFileExtension() {
		return ".office.xml";
	}

	/**
	 * Constructs an {@link OfficeSection} for testing.
	 *
	 * @param constructor
	 *            {@link OfficeSectionConstructor}.
	 * @return {@link OfficeSection}.
	 */
	protected OfficeSection constructOfficeSection(
			OfficeSectionConstructor constructor) {

		// Construct and return the office section
		OfficeSectionContextImpl context = new OfficeSectionContextImpl();
		constructor.construct(context);
		return context;
	}

	/**
	 * Constructor of an {@link OfficeSection}.
	 */
	protected interface OfficeSectionConstructor {

		/**
		 * Constructs the {@link OfficeSection}.
		 *
		 * @param context
		 *            {@link OfficeSection}.
		 */
		void construct(OfficeSectionContext context);
	}

	/**
	 * Context to construct the {@link OfficeSection}.
	 */
	protected interface OfficeSectionContext {

	}

	/**
	 * {@link OfficeSectionContext} implementation.
	 */
	private class OfficeSectionContextImpl implements OfficeSectionContext,
			OfficeSection {

		/**
		 * {@link OfficeSection} name.
		 */
		private String sectionName;

		/**
		 * {@link OfficeSectionInput} instances.
		 */
		private List<OfficeSectionInput> inputs = new LinkedList<OfficeSectionInput>();

		/**
		 * {@link OfficeSectionOutput} instances.
		 */
		private List<OfficeSectionOutput> outputs = new LinkedList<OfficeSectionOutput>();

		/**
		 * {@link OfficeSectionObject} instances.
		 */
		private List<OfficeSectionObject> objects = new LinkedList<OfficeSectionObject>();

		/*
		 * ===================== OfficeSection ===========================
		 */

		@Override
		public String getOfficeSectionName() {
			return this.sectionName;
		}

		@Override
		public OfficeSectionInput[] getOfficeSectionInputs() {
			return this.inputs.toArray(new OfficeSectionInput[0]);
		}

		@Override
		public OfficeSectionOutput[] getOfficeSectionOutputs() {
			return this.outputs.toArray(new OfficeSectionOutput[0]);
		}

		@Override
		public OfficeSectionObject[] getOfficeSectionObjects() {
			return this.objects.toArray(new OfficeSectionObject[0]);
		}

		@Override
		public OfficeSectionManagedObjectSource[] getOfficeSectionManagedObjectSources() {
			fail("Currently not testing sub sections");
			return null;
		}

		@Override
		public OfficeSubSection[] getOfficeSubSections() {
			fail("Currently not testing sub sections");
			return null;
		}

		@Override
		public OfficeTask[] getOfficeTasks() {
			fail("Currently not testing sub sections");
			return null;
		}
	}

}