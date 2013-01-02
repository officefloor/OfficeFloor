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
package net.officefloor.model.woof;

import java.util.LinkedList;
import java.util.List;

import org.easymock.AbstractMatcher;
import org.easymock.internal.AlwaysMatcher;

import junit.framework.TestCase;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.change.Change;
import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationItem;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.test.changes.AbstractChangesTestCase;
import net.officefloor.plugin.gwt.module.GwtChanges;

/**
 * Abstract {@link WoofChanges} {@link TestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWoofChangesTestCase extends
		AbstractChangesTestCase<WoofModel, WoofChanges> {

	/**
	 * Mock {@link GwtChanges}.
	 */
	private final GwtChanges gwtChanges = this.createMock(GwtChanges.class);

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

	/**
	 * Obtains the {@link GwtChanges}.
	 * 
	 * @return {@link GwtChanges}.
	 */
	protected GwtChanges getGwtChanges() {
		return this.gwtChanges;
	}

	/**
	 * Records the GWT Module path.
	 * 
	 * @param gwtModulePath
	 *            GWT Module path.
	 */
	protected void recordGwtModulePath(String gwtModulePath) {
		GwtChanges changes = this.getGwtChanges();
		this.recordReturn(changes, changes.createGwtModulePath(null),
				gwtModulePath, new AlwaysMatcher());
	}

	/**
	 * Record GWT update on the {@link GwtChanges}.
	 * 
	 * @param templateUri
	 *            Expected template URI.
	 * @param entryPointClassName
	 *            Expected EntryPoint class name.
	 * @param existingGwtModulePath
	 *            Expected existing GWT Module path.
	 * @return {@link Change}.
	 */
	@SuppressWarnings("unchecked")
	protected Change<GwtModuleModel> recordGwtUpdate(final String templateUri,
			final String entryPointClassName, final String existingGwtModulePath) {

		// Create the change
		final Change<GwtModuleModel> change = this.createMock(Change.class);

		// Record updating GWT Module
		GwtChanges changes = this.getGwtChanges();
		this.recordReturn(changes,
				changes.updateGwtModule(null, existingGwtModulePath), change,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						GwtModuleModel module = (GwtModuleModel) actual[0];
						assertEquals("Incorrect rename-to", templateUri,
								module.getRenameTo());
						assertEquals("Incorrect EntryPoint class name",
								entryPointClassName,
								module.getEntryPointClassName());
						assertEquals("Incorrect existing GWT Module Path",
								existingGwtModulePath, actual[1]);
						return true;
					}
				});

		// Return the change
		return change;
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
		return new WoofChangesImpl(model, this.getGwtChanges());
	}

	@Override
	protected void assertModels(WoofModel expected, WoofModel actual)
			throws Exception {

		// Determine if output XML of actual
		if (this.isPrintMessages()) {

			// Provide details of the model compare
			this.printMessage("=============== MODEL COMPARE ================");

			// Provide details of expected model
			this.printMessage("------------------ EXPECTED ------------------");
			ConfigurationItem expectedConfig = new MemoryConfigurationItem();
			new WoofRepositoryImpl(new ModelRepositoryImpl()).storeWoOF(
					expected, expectedConfig);
			this.printMessage(expectedConfig.getConfiguration());

			// Provide details of actual model
			this.printMessage("------------------- ACTUAL -------------------");
			ConfigurationItem actualConfig = new MemoryConfigurationItem();
			new WoofRepositoryImpl(new ModelRepositoryImpl()).storeWoOF(actual,
					actualConfig);
			this.printMessage(actualConfig.getConfiguration());
			this.printMessage("================ END COMPARE =================");
		}

		// Under take the compare
		super.assertModels(expected, actual);
	}

	/**
	 * Constructs an {@link SectionType} for testing.
	 * 
	 * @param constructor
	 *            {@link SectionTypeConstructor}.
	 * @return {@link SectionType}.
	 */
	protected SectionType constructSectionType(
			SectionTypeConstructor constructor) {

		// Construct and return the office section
		SectionTypeContextImpl context = new SectionTypeContextImpl();
		constructor.construct(context);
		return context;
	}

	/**
	 * Constructor of an {@link SectionType}.
	 */
	protected interface SectionTypeConstructor {

		/**
		 * Constructs the {@link SectionType}.
		 * 
		 * @param context
		 *            {@link SectionType}.
		 */
		void construct(SectionTypeContext context);
	}

	/**
	 * Context to construct the {@link SectionType}.
	 */
	protected interface SectionTypeContext {

		/**
		 * Adds an {@link SectionInputType}.
		 * 
		 * @param name
		 *            Name.
		 * @param parameterType
		 *            Parameter type.
		 */
		void addSectionInput(String name, Class<?> parameterType);

		/**
		 * Adds an {@link SectionOutputType}.
		 * 
		 * @param name
		 *            Name.
		 * @param argumentType
		 *            Argument type.
		 * @param isEscalationOnly
		 *            Flag indicating if escalation only.
		 */
		void addSectionOutput(String name, Class<?> argumentType,
				boolean isEscalationOnly);

		/**
		 * Adds an {@link SectionObjectType}.
		 * 
		 * @param name
		 *            Name.
		 * @param objectType
		 *            Object type.
		 * @param typeQualifier
		 *            Type qualifier.
		 */
		void addSectionObject(String name, Class<?> objectType,
				String typeQualifier);
	}

	/**
	 * {@link SectionTypeContext} implementation.
	 */
	private class SectionTypeContextImpl implements SectionTypeContext,
			SectionType {

		/**
		 * {@link SectionInputType} instances.
		 */
		private final List<SectionInputType> inputs = new LinkedList<SectionInputType>();

		/**
		 * {@link SectionOutputType} instances.
		 */
		private final List<SectionOutputType> outputs = new LinkedList<SectionOutputType>();

		/**
		 * {@link SectionObjectType} instances.
		 */
		private final List<SectionObjectType> objects = new LinkedList<SectionObjectType>();

		/*
		 * ===================== SectionTypeContext =====================
		 */

		@Override
		public void addSectionInput(String name, Class<?> parameterType) {
			this.inputs.add(new SectionTypeItem(name,
					(parameterType == null ? null : parameterType.getName())));
		}

		@Override
		public void addSectionOutput(String name, Class<?> argumentType,
				boolean isEscalationOnly) {
			this.outputs.add(new SectionTypeItem(name,
					(argumentType == null ? null : argumentType.getName()),
					isEscalationOnly));
		}

		@Override
		public void addSectionObject(String name, Class<?> objectType,
				String typeQualifier) {
			this.objects.add(new SectionTypeItem(name, objectType.getName(),
					typeQualifier));
		}

		/*
		 * ===================== SectionType ===========================
		 */

		@Override
		public SectionInputType[] getSectionInputTypes() {
			return this.inputs.toArray(new SectionInputType[0]);
		}

		@Override
		public SectionOutputType[] getSectionOutputTypes() {
			return this.outputs.toArray(new SectionOutputType[0]);
		}

		@Override
		public SectionObjectType[] getSectionObjectTypes() {
			return this.objects.toArray(new SectionObjectType[0]);
		}
	}

	/**
	 * Item from {@link SectionType}.
	 */
	private class SectionTypeItem implements SectionInputType,
			SectionOutputType, SectionObjectType {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Type.
		 */
		private final String type;

		/**
		 * Type qualifier.
		 */
		private final String typeQualifier;

		/**
		 * Flag indicating if escalation only.
		 */
		private final boolean isEscalation;

		/**
		 * Initialise for {@link SectionInputType}.
		 * 
		 * @param name
		 *            Name.
		 * @param type
		 *            Type.
		 */
		public SectionTypeItem(String name, String type) {
			this(name, type, false);
		}

		/**
		 * Initialise for {@link SectionOutputType}.
		 * 
		 * @param name
		 *            Name.
		 * @param type
		 *            Type.
		 * @param isEscalation
		 *            Flag indicating if escalation only.
		 */
		public SectionTypeItem(String name, String type, boolean isEscalation) {
			this.name = name;
			this.type = type;
			this.typeQualifier = null;
			this.isEscalation = isEscalation;
		}

		/**
		 * Initialise for {@link SectionObjectType}.
		 * 
		 * @param name
		 *            Name.
		 * @param type
		 *            Type.
		 * @param typeQualifier
		 *            Type qualifier.
		 */
		public SectionTypeItem(String name, String type, String typeQualifier) {
			this.name = name;
			this.type = type;
			this.typeQualifier = typeQualifier;
			this.isEscalation = false;
		}

		/*
		 * ================ SectionInputType ======================
		 */

		@Override
		public String getSectionInputName() {
			return this.name;
		}

		@Override
		public String getParameterType() {
			return this.type;
		}

		/*
		 * ================ SectionOutputType ======================
		 */

		@Override
		public String getSectionOutputName() {
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
		 * ================ SectionObjectType ======================
		 */

		@Override
		public String getSectionObjectName() {
			return this.name;
		}

		@Override
		public String getObjectType() {
			return this.type;
		}

		@Override
		public String getTypeQualifier() {
			return this.typeQualifier;
		}
	}

}