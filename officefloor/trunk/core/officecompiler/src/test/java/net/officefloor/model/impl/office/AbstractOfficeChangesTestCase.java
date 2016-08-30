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
package net.officefloor.model.impl.office;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.test.changes.AbstractChangesTestCase;

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

		/**
		 * Adds an {@link OfficeSectionInput}.
		 * 
		 * @param name
		 *            Name.
		 * @param parameterType
		 *            Parameter type.
		 */
		void addOfficeSectionInput(String name, Class<?> parameterType);

		/**
		 * Adds an {@link OfficeSectionOutput}.
		 * 
		 * @param name
		 *            Name.
		 * @param argumentType
		 *            Argument type.
		 * @param isEscalationOnly
		 *            Flag indicating if escalation only.
		 */
		void addOfficeSectionOutput(String name, Class<?> argumentType,
				boolean isEscalationOnly);

		/**
		 * Adds an {@link OfficeSectionObject}.
		 * 
		 * @param name
		 *            Name.
		 * @param objectType
		 *            Object type.
		 * @param qualifier
		 *            Type qualifier.
		 */
		void addOfficeSectionObject(String name, Class<?> objectType,
				String qualifier);
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
		private Map<String, OfficeSectionInput> inputs = new HashMap<String, OfficeSectionInput>();

		/**
		 * {@link OfficeSectionOutput} instances.
		 */
		private Map<String, OfficeSectionOutput> outputs = new HashMap<String, OfficeSectionOutput>();

		/**
		 * {@link OfficeSectionObject} instances.
		 */
		private Map<String, OfficeSectionObject> objects = new HashMap<String, OfficeSectionObject>();

		/*
		 * ===================== OfficeSectionContext =====================
		 */

		@Override
		public void addOfficeSectionInput(String name, Class<?> parameterType) {
			this.inputs.put(name, new OfficeSectionItem(name));
		}

		@Override
		public void addOfficeSectionOutput(String name, Class<?> argumentType,
				boolean isEscalationOnly) {
			this.outputs.put(name, new OfficeSectionItem(name));
		}

		@Override
		public void addOfficeSectionObject(String name, Class<?> objectType,
				String qualifier) {
			this.objects.put(name, new OfficeSectionItem(name));
		}

		/*
		 * ===================== OfficeSection ===========================
		 */

		@Override
		public String getOfficeSectionName() {
			return this.sectionName;
		}

		@Override
		public void addProperty(String name, String value) {
			fail("Should not require to set properties in testing");
		}

		@Override
		public OfficeSectionInput getOfficeSectionInput(String inputName) {
			return this.inputs.get(inputName);
		}

		@Override
		public OfficeSectionOutput getOfficeSectionOutput(String outputName) {
			return this.outputs.get(outputName);
		}

		@Override
		public OfficeSectionObject getOfficeSectionObject(String objectName) {
			return this.objects.get(objectName);
		}

		@Override
		public OfficeSectionManagedObjectSource getOfficeSectionManagedObjectSource(
				String managedObjectName) {
			fail("Currently not testing sub sections");
			return null;
		}

		@Override
		public void addGovernance(OfficeGovernance governance) {
			fail("Currently not testing sub sections");
		}

		@Override
		public OfficeSubSection getOfficeSubSection(String sectionName) {
			fail("Currently not testing sub sections");
			return null;
		}

		@Override
		public OfficeTask getOfficeTask(String taskName) {
			fail("Currently not testing sub sections");
			return null;
		}
	}

	/**
	 * Item from {@link OfficeSection}.
	 */
	private class OfficeSectionItem implements OfficeSectionInput,
			OfficeSectionOutput, OfficeSectionObject {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Initialise.
		 * 
		 * @param name
		 *            Name.
		 */
		public OfficeSectionItem(String name) {
			this.name = name;
		}

		/*
		 * ================ OfficeSectionInput ======================
		 */

		@Override
		public String getOfficeSectionInputName() {
			return this.name;
		}

		/*
		 * ================ OfficeSectionOutput ======================
		 */

		@Override
		public String getOfficeSectionOutputName() {
			return this.name;
		}

		/*
		 * ================ OfficeSectionObject ======================
		 */

		@Override
		public String getOfficeSectionObjectName() {
			return this.name;
		}
	}

}