/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
import net.officefloor.configuration.WritableConfigurationItem;
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
	 * @param isSpecificSetupFilePerTest Flag if specific setup file to be used.
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
	protected void storeModel(OfficeModel model, WritableConfigurationItem configurationItem) throws Exception {
		new OfficeRepositoryImpl(new ModelRepositoryImpl()).storeOffice(model, configurationItem);
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
	 * @param constructor {@link OfficeSectionConstructor}.
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
		 * @param context {@link OfficeSection}.
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
		 * @param name          Name.
		 * @param parameterType Parameter type.
		 */
		void addOfficeSectionInput(String name, Class<?> parameterType);

		/**
		 * Adds an {@link OfficeSectionOutput}.
		 * 
		 * @param name             Name.
		 * @param argumentType     Argument type.
		 * @param isEscalationOnly Flag indicating if escalation only.
		 */
		void addOfficeSectionOutput(String name, Class<?> argumentType, boolean isEscalationOnly);

		/**
		 * Adds an {@link OfficeSectionObject}.
		 * 
		 * @param name       Name.
		 * @param objectType Object type.
		 * @param qualifier  Type qualifier.
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
			fail("Should not invoke getParentOfficeSubSectionType");
			return null;
		}

		@Override
		public OfficeSubSectionType[] getOfficeSubSectionTypes() {
			fail("Should not invoke getOfficeSubSectionTypes");
			return null;
		}

		@Override
		public OfficeFunctionType[] getOfficeFunctionTypes() {
			fail("Should not invoke getOfficeFunctionTypes");
			return null;
		}

		@Override
		public OfficeSectionManagedObjectType[] getOfficeSectionManagedObjectTypes() {
			fail("Should not invoke getOfficeSectionManagedObjectTypes");
			return null;
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
		 * @param name             Name.
		 * @param type             Type.
		 * @param isEscalationOnly Flag indicating only {@link Escalation}.
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
			fail("Should not invoke getTypeQualifier");
			return null;
		}

		@Override
		public Object[] getAnnotations() {
			fail("Should not invoke getAnnotations");
			return null;
		}
	}

}
