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

import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.OfficeFunctionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.test.changes.AbstractChangesTestCase;

/**
 * Abstract functionality for testing the {@link OfficeChanges}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeChangesTestCase extends AbstractChangesTestCase<OfficeModel, OfficeChanges> {

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
	protected OfficeModel retrieveModel(ConfigurationItem configurationItem) throws Exception {
		OfficeModel office = new OfficeModel();
		new OfficeRepositoryImpl(new ModelRepositoryImpl()).retrieveOffice(office, configurationItem);
		return office;
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
	protected OfficeSectionType constructOfficeSectionType(OfficeSectionConstructor constructor) {

		// Construct and return the office section type
		OfficeSectionContextImpl context = new OfficeSectionContextImpl();
		if (constructor != null) {
			constructor.construct(context);
		}
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
		void addOfficeSectionOutput(String name, Class<?> argumentType, boolean isEscalationOnly);

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
		void addOfficeSectionObject(String name, Class<?> objectType, String qualifier);
	}

	/**
	 * {@link OfficeSectionContext} implementation.
	 */
	private class OfficeSectionContextImpl implements OfficeSectionContext, OfficeSectionType {

		/**
		 * {@link OfficeSection} name.
		 */
		private String sectionName;

		/**
		 * {@link OfficeSectionInputType} instances.
		 */
		private Map<String, OfficeSectionInputType> inputs = new HashMap<String, OfficeSectionInputType>();

		/**
		 * {@link OfficeSectionOutputType} instances.
		 */
		private Map<String, OfficeSectionOutputType> outputs = new HashMap<String, OfficeSectionOutputType>();

		/**
		 * {@link OfficeSectionObjectType} instances.
		 */
		private Map<String, OfficeSectionObjectType> objects = new HashMap<String, OfficeSectionObjectType>();

		/*
		 * ===================== OfficeSectionContext =====================
		 */

		@Override
		public void addOfficeSectionInput(String name, Class<?> parameterType) {
			this.inputs.put(name, new OfficeSectionItem(name, parameterType, false));
		}

		@Override
		public void addOfficeSectionOutput(String name, Class<?> argumentType, boolean isEscalationOnly) {
			this.outputs.put(name, new OfficeSectionItem(name, argumentType, isEscalationOnly));
		}

		@Override
		public void addOfficeSectionObject(String name, Class<?> objectType, String qualifier) {
			this.objects.put(name, new OfficeSectionItem(name, objectType, false));
		}

		/*
		 * ===================== OfficeSectionType ===========================
		 */

		@Override
		public String getOfficeSectionName() {
			return this.sectionName;
		}

		@Override
		public OfficeSectionInputType[] getOfficeSectionInputTypes() {
			return this.inputs.values().stream().toArray(OfficeSectionInputType[]::new);
		}

		@Override
		public OfficeSectionOutputType[] getOfficeSectionOutputTypes() {
			return this.outputs.values().stream().toArray(OfficeSectionOutputType[]::new);
		}

		@Override
		public OfficeSectionObjectType[] getOfficeSectionObjectTypes() {
			return this.objects.values().stream().toArray(OfficeSectionObjectType[]::new);
		}

		@Override
		public OfficeSubSectionType getParentOfficeSubSectionType() {
			// TODO implement OfficeSubSectionType.getParentOfficeSubSectionType
			throw new UnsupportedOperationException(
					"TODO implement OfficeSubSectionType.getParentOfficeSubSectionType");

		}

		@Override
		public OfficeSubSectionType[] getOfficeSubSectionTypes() {
			// TODO implement OfficeSubSectionType.getOfficeSubSectionTypes
			throw new UnsupportedOperationException("TODO implement OfficeSubSectionType.getOfficeSubSectionTypes");

		}

		@Override
		public OfficeFunctionType[] getOfficeFunctionTypes() {
			// TODO implement OfficeSubSectionType.getOfficeTaskTypes
			throw new UnsupportedOperationException("TODO implement OfficeSubSectionType.getOfficeTaskTypes");

		}

		@Override
		public OfficeSectionManagedObjectType[] getOfficeSectionManagedObjectTypes() {
			// TODO implement
			// OfficeSubSectionType.getOfficeSectionManagedObjectSourceTypes
			throw new UnsupportedOperationException(
					"TODO implement OfficeSubSectionType.getOfficeSectionManagedObjectSourceTypes");

		}
	}

	/**
	 * Item from {@link OfficeSectionType}.
	 */
	private class OfficeSectionItem
			implements OfficeSectionInputType, OfficeSectionOutputType, OfficeSectionObjectType {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Type.
		 */
		private final String type;

		/**
		 * Flag indicating only {@link Escalation}.
		 */
		private final boolean isEscalationOnly;

		/**
		 * Initialise.
		 * 
		 * @param name
		 *            Name.
		 * @param type
		 *            Type.
		 * @param isEscalationOnly
		 *            Flag indicating only {@link Escalation}.
		 */
		public OfficeSectionItem(String name, Class<?> type, boolean isEscalationOnly) {
			this.name = name;
			this.type = type.getName();
			this.isEscalationOnly = isEscalationOnly;
		}

		/*
		 * ================ OfficeSectionInput ======================
		 */

		@Override
		public String getOfficeSectionInputName() {
			return this.name;
		}

		@Override
		public String getArgumentType() {
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
		public String getParameterType() {
			return this.type;
		}

		@Override
		public boolean isEscalationOnly() {
			return this.isEscalationOnly;
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

		@Override
		public String getTypeQualifier() {
			// TODO implement OfficeSectionObjectType.getTypeQualifier
			throw new UnsupportedOperationException("TODO implement OfficeSectionObjectType.getTypeQualifier");
		}
	}

}