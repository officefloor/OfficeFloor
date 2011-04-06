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
package net.officefloor.model.woof;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.test.changes.AbstractChangesTestCase;
import junit.framework.TestCase;

/**
 * Abstract {@link WoofChanges} {@link TestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWoofChangesTestCase extends
		AbstractChangesTestCase<WoofModel, WoofChanges> {

	/**
	 * Initiate.
	 */
	public AbstractWoofChangesTestCase() {
	}

	/**
	 * Initiate.
	 * 
	 * @param isSpecificSetupFilePerTest
	 *            Flags if there is a specific setup file per test.
	 */
	public AbstractWoofChangesTestCase(boolean isSpecificSetupFilePerTest) {
		super(isSpecificSetupFilePerTest);
	}

	/*
	 * =================== AbstractOperationsTestCase ========================
	 */

	@Override
	protected String getModelFileExtension() {
		return ".woof.xml";
	}

	@Override
	protected WoofModel retrieveModel(ConfigurationItem configurationItem)
			throws Exception {
		return new WoofRepositoryImpl(new ModelRepositoryImpl())
				.retrieveWoOF(configurationItem);
	}

	@Override
	protected WoofChanges createModelOperations(WoofModel model) {
		return new WoofChangesImpl(model);
	}

	/**
	 * Constructs an {@link OfficeSection} for testing.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param constructor
	 *            {@link OfficeSectionConstructor}.
	 * @return {@link OfficeSection}.
	 */
	protected OfficeSection constructOfficeSection(String sectionName,
			OfficeSectionConstructor constructor) {

		// Construct and return the office section
		OfficeSectionContextImpl context = new OfficeSectionContextImpl(
				sectionName);
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
		 */
		void addOfficeSectionObject(String name, Class<?> objectType);
	}

	/**
	 * {@link OfficeSectionContext} implementation.
	 */
	private class OfficeSectionContextImpl implements OfficeSectionContext,
			OfficeSection {

		/**
		 * {@link OfficeSection} name.
		 */
		private final String sectionName;

		/**
		 * {@link OfficeSectionInput} instances.
		 */
		private final List<OfficeSectionInput> inputs = new LinkedList<OfficeSectionInput>();

		/**
		 * {@link OfficeSectionOutput} instances.
		 */
		private final List<OfficeSectionOutput> outputs = new LinkedList<OfficeSectionOutput>();

		/**
		 * {@link OfficeSectionObject} instances.
		 */
		private final List<OfficeSectionObject> objects = new LinkedList<OfficeSectionObject>();

		/**
		 * Initiate.
		 * 
		 * @param sectionName
		 *            Section name
		 */
		public OfficeSectionContextImpl(String sectionName) {
			this.sectionName = sectionName;
		}

		/*
		 * ===================== OfficeSectionContext =====================
		 */

		@Override
		public void addOfficeSectionInput(String name, Class<?> parameterType) {
			this.inputs.add(new OfficeSectionItem(name,
					(parameterType == null ? null : parameterType.getName())));
		}

		@Override
		public void addOfficeSectionOutput(String name, Class<?> argumentType,
				boolean isEscalationOnly) {
			this.outputs.add(new OfficeSectionItem(name,
					(argumentType == null ? null : argumentType.getName()),
					isEscalationOnly));
		}

		@Override
		public void addOfficeSectionObject(String name, Class<?> objectType) {
			this.objects.add(new OfficeSectionItem(name, objectType.getName()));
		}

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
		 * Type.
		 */
		private final String type;

		/**
		 * Flag indicating if escalation only.
		 */
		private final boolean isEscalation;

		/**
		 * Initialise.
		 * 
		 * @param name
		 *            Name.
		 * @param type
		 *            Type.
		 * @param isEscalation
		 *            Flag indicating if escalation only.
		 */
		public OfficeSectionItem(String name, String type, boolean isEscalation) {
			this.name = name;
			this.type = type;
			this.isEscalation = isEscalation;
		}

		/**
		 * Initialise.
		 * 
		 * @param name
		 *            Name.
		 * @param type
		 *            Type.
		 */
		public OfficeSectionItem(String name, String type) {
			this(name, type, false);
		}

		/*
		 * ================ OfficeSectionInput ======================
		 */

		@Override
		public String getOfficeSectionInputName() {
			return this.name;
		}

		@Override
		public String getParameterType() {
			return this.type;
		}

		/*
		 * ================ OfficeSectionOutput ======================
		 */

		@Override
		public String getOfficeSectionOutputName() {
			return this.name;
		}

		@Override
		public String getArgumentType() {
			return this.type;
		}

		@Override
		public boolean isEscalationOnly() {
			return this.isEscalation;
		}

		/*
		 * ================ OfficeSectionObject ======================
		 */

		@Override
		public String getOfficeSectionObjectName() {
			return this.name;
		}

		@Override
		public String getObjectType() {
			return this.type;
		}
	}

}