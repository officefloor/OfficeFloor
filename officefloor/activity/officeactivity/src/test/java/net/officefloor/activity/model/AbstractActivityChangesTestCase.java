/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.activity.model;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureEscalationType;
import net.officefloor.activity.procedure.ProcedureFlowType;
import net.officefloor.activity.procedure.ProcedureObjectType;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.ProcedureVariableType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.test.changes.AbstractChangesTestCase;
import net.officefloor.plugin.variable.Var;

/**
 * Abstract {@link ActivityChanges} {@link TestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractActivityChangesTestCase extends AbstractChangesTestCase<ActivityModel, ActivityChanges> {

	/**
	 * Initiate.
	 */
	public AbstractActivityChangesTestCase() {
	}

	/**
	 * Initiate.
	 * 
	 * @param isSpecificSetupFilePerTest Flags if there is a specific setup file per
	 *                                   test.
	 */
	public AbstractActivityChangesTestCase(boolean isSpecificSetupFilePerTest) {
		super(isSpecificSetupFilePerTest);
	}

	/*
	 * =================== AbstractOperationsTestCase ========================
	 */

	@Override
	protected String getModelFileExtension() {
		return ".activity.xml";
	}

	@Override
	protected ActivityModel retrieveModel(ConfigurationItem configurationItem) throws Exception {
		ActivityModel woof = new ActivityModel();
		new ActivityRepositoryImpl(new ModelRepositoryImpl()).retrieveActivity(woof, configurationItem);
		return woof;
	}

	@Override
	protected ActivityChanges createModelOperations(ActivityModel model) {
		return null; // new ActivityChangesImpl(model);
	}

	@Override
	protected void assertModels(ActivityModel expected, ActivityModel actual) throws Exception {

		// Determine if output XML of actual
		if (this.isPrintMessages()) {

			// Provide details of the model compare
			this.printMessage("=============== MODEL COMPARE ================");

			// Provide details of expected model
			this.printMessage("------------------ EXPECTED ------------------");
			WritableConfigurationItem expectedConfig = MemoryConfigurationContext
					.createWritableConfigurationItem("location");
			new ActivityRepositoryImpl(new ModelRepositoryImpl()).storeActivity(expected, expectedConfig);
			this.printMessage(expectedConfig.getReader());

			// Provide details of actual model
			this.printMessage("------------------- ACTUAL -------------------");
			WritableConfigurationItem actualConfig = MemoryConfigurationContext
					.createWritableConfigurationItem("location");
			new ActivityRepositoryImpl(new ModelRepositoryImpl()).storeActivity(actual, actualConfig);
			this.printMessage(actualConfig.getReader());
			this.printMessage("================ END COMPARE =================");
		}

		// Under take the compare
		super.assertModels(expected, actual);
	}

	/**
	 * Constructs an {@link SectionType} for testing.
	 * 
	 * @param constructor {@link SectionTypeConstructor}.
	 * @return {@link SectionType}.
	 */
	protected SectionType constructSectionType(SectionTypeConstructor constructor) {

		// Construct and return the office section
		SectionTypeContextImpl context = new SectionTypeContextImpl();
		if (constructor != null) {
			constructor.construct(context);
		}
		return context;
	}

	/**
	 * Constructor of an {@link SectionType}.
	 */
	@FunctionalInterface
	protected interface SectionTypeConstructor {

		/**
		 * Constructs the {@link SectionType}.
		 * 
		 * @param context {@link SectionType}.
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
		 * @param name          Name.
		 * @param parameterType Parameter type.
		 */
		void addSectionInput(String name, Class<?> parameterType);

		/**
		 * Adds an {@link SectionOutputType}.
		 * 
		 * @param name             Name.
		 * @param argumentType     Argument type.
		 * @param isEscalationOnly Flag indicating if escalation only.
		 */
		void addSectionOutput(String name, Class<?> argumentType, boolean isEscalationOnly);

		/**
		 * Adds an {@link SectionObjectType}.
		 * 
		 * @param name          Name.
		 * @param objectType    Object type.
		 * @param typeQualifier Type qualifier.
		 */
		void addSectionObject(String name, Class<?> objectType, String typeQualifier);
	}

	/**
	 * {@link SectionTypeContext} implementation.
	 */
	private class SectionTypeContextImpl implements SectionTypeContext, SectionType {

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
			this.inputs.add(new SectionTypeItem(name, (parameterType == null ? null : parameterType.getName())));
		}

		@Override
		public void addSectionOutput(String name, Class<?> argumentType, boolean isEscalationOnly) {
			this.outputs.add(new SectionTypeItem(name, (argumentType == null ? null : argumentType.getName()),
					isEscalationOnly));
		}

		@Override
		public void addSectionObject(String name, Class<?> objectType, String typeQualifier) {
			this.objects.add(new SectionTypeItem(name, objectType.getName(), typeQualifier));
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
	private class SectionTypeItem implements SectionInputType, SectionOutputType, SectionObjectType {

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
		 * @param name Name.
		 * @param type Type.
		 */
		private SectionTypeItem(String name, String type) {
			this(name, type, false);
		}

		/**
		 * Initialise for {@link SectionOutputType}.
		 * 
		 * @param name         Name.
		 * @param type         Type.
		 * @param isEscalation Flag indicating if escalation only.
		 */
		private SectionTypeItem(String name, String type, boolean isEscalation) {
			this.name = name;
			this.type = type;
			this.typeQualifier = null;
			this.isEscalation = isEscalation;
		}

		/**
		 * Initialise for {@link SectionObjectType}.
		 * 
		 * @param name          Name.
		 * @param type          Type.
		 * @param typeQualifier Type qualifier.
		 */
		private SectionTypeItem(String name, String type, String typeQualifier) {
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

		@Override
		public Object[] getAnnotations() {
			return new Object[0];
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

	/**
	 * Constructs the {@link ProcedureType} for testing.
	 * 
	 * @param procedureName {@link Procedure} name.
	 * @param parameterType Parameter type.
	 * @param constructor   {@link ProcedureTypeConstructor}.
	 * @return {@link ProcedureType}.
	 */
	protected ProcedureType constructProcedureType(String procedureName, Class<?> parameterType,
			ProcedureTypeConstructor constructor) {

		// Construct and return the procedure
		ProcedureTypeContextImpl context = new ProcedureTypeContextImpl(procedureName, parameterType);
		if (constructor != null) {
			constructor.construct(context);
		}
		return context;
	}

	/**
	 * Constructor of an {@link ProcedureType}.
	 */
	@FunctionalInterface
	protected interface ProcedureTypeConstructor {

		/**
		 * Constructs the {@link ProcedureType}.
		 * 
		 * @param context {@link ProcedureTypeContext}.
		 */
		void construct(ProcedureTypeContext context);
	}

	/**
	 * Context to construct the {@link ProcedureType}.
	 */
	protected interface ProcedureTypeContext {

		/**
		 * Adds a {@link ProcedureObjectType}.
		 * 
		 * @param objectName    {@link Object} name.
		 * @param objectType    {@link Class} of {@link Object}.
		 * @param typeQualifier Type qualifier.
		 */
		void addObject(String objectName, Class<?> objectType, String typeQualifier);

		/**
		 * Adds a {@link ProcedureVariableType}.
		 * 
		 * @param variableName Name of {@link Var}.
		 * @param variableType {@link Class} of {@link Var}.
		 */
		void addVariable(String variableName, Class<?> variableType);

		/**
		 * Adds a {@link ProcedureFlowType}.
		 * 
		 * @param flowName     Name of {@link Flow}.
		 * @param argumentType Argument {@link Class} for {@link Flow}.
		 */
		void addFlow(String flowName, Class<?> argumentType);

		/**
		 * Adds a {@link ProcedureEscalationType}.
		 * 
		 * @param escalationType {@link Class} of {@link Escalation}.
		 */
		void addEscalation(Class<? extends Throwable> escalationType);

		/**
		 * Specifies the next argument {@link Class}.
		 * 
		 * @param nextArgumentType Next argument {@link Class}.
		 */
		void setNextArgumentType(Class<?> nextArgumentType);
	}

	/**
	 * {@link ProcedureTypeContext} implementation.
	 */
	private class ProcedureTypeContextImpl implements ProcedureTypeContext, ProcedureType {

		/**
		 * {@link Procedure} name.
		 */
		private final String procedureName;

		/**
		 * Parameter {@link Class}.
		 */
		private final Class<?> parameterType;

		/**
		 * {@link ProcedureObjectType} instances.
		 */
		private final List<ProcedureObjectType> objects = new LinkedList<>();

		/**
		 * {@link ProcedureVariableType} instances.
		 */
		private final List<ProcedureVariableType> variables = new LinkedList<>();

		/**
		 * {@link ProcedureFlowType} instances.
		 */
		private final List<ProcedureFlowType> flows = new LinkedList<>();

		/**
		 * {@link ProcedureEscalationType} instances.
		 */
		private final List<ProcedureEscalationType> escalations = new LinkedList<>();

		/**
		 * Next argument {@link Class}.
		 */
		private Class<?> nextArgumentType = null;

		/**
		 * Instantiate.
		 * 
		 * @param procedureName {@link Procedure} name.
		 * @param parameterType Parameter {@link Class}.
		 */
		private ProcedureTypeContextImpl(String procedureName, Class<?> parameterType) {
			this.procedureName = procedureName;
			this.parameterType = parameterType;
		}

		/*
		 * ================= ProcedureTypeContext =======================
		 */

		@Override
		public void addObject(String objectName, Class<?> objectType, String typeQualifier) {
			this.objects.add(new ProcedureTypeItem(objectName, objectType, typeQualifier));
		}

		@Override
		public void addVariable(String variableName, Class<?> variableType) {
			this.variables.add(new ProcedureTypeItem(variableName, variableType, null));
		}

		@Override
		public void addFlow(String flowName, Class<?> argumentType) {
			this.flows.add(new ProcedureTypeItem(flowName, argumentType, null));
		}

		@Override
		public void addEscalation(Class<? extends Throwable> escalationType) {
			this.escalations.add(new ProcedureTypeItem(escalationType.getSimpleName(), escalationType, null));
		}

		@Override
		public void setNextArgumentType(Class<?> nextArgumentType) {
			this.nextArgumentType = nextArgumentType;
		}

		/*
		 * ====================== ProcedureType ==========================
		 */

		@Override
		public String getProcedureName() {
			return this.procedureName;
		}

		@Override
		public Class<?> getParameterType() {
			return this.parameterType;
		}

		@Override
		public ProcedureObjectType[] getObjectTypes() {
			return this.objects.toArray(new ProcedureObjectType[this.objects.size()]);
		}

		@Override
		public ProcedureVariableType[] getVariableTypes() {
			return this.variables.toArray(new ProcedureVariableType[this.variables.size()]);
		}

		@Override
		public ProcedureFlowType[] getFlowTypes() {
			return this.flows.toArray(new ProcedureFlowType[this.flows.size()]);
		}

		@Override
		public ProcedureEscalationType[] getEscalationTypes() {
			return this.escalations.toArray(new ProcedureEscalationType[this.escalations.size()]);
		}

		@Override
		public Class<?> getNextArgumentType() {
			return this.nextArgumentType;
		}
	}

	/**
	 * Item for {@link ProcedureObjectType}, {@link ProcedureVariableType},
	 * {@link ProcedureFlowType} and {@link ProcedureEscalationType}.
	 */
	private class ProcedureTypeItem
			implements ProcedureObjectType, ProcedureVariableType, ProcedureFlowType, ProcedureEscalationType {

		/**
		 * Name of item.
		 */
		private final String itemName;

		/**
		 * Type for item.
		 */
		private final Class<?> itemType;

		/**
		 * Qualifier for item type.
		 */
		private final String typeQualifier;

		/**
		 * Initiate.
		 * 
		 * @param itemName      Name of item.
		 * @param itemType      Type for item.
		 * @param typeQualifier Qualifier for item type.
		 */
		private ProcedureTypeItem(String itemName, Class<?> itemType, String typeQualifier) {
			this.itemName = itemName;
			this.itemType = itemType;
			this.typeQualifier = typeQualifier;
		}

		/*
		 * ================== Item common methods ====================
		 */

		@Override
		public String getTypeQualifier() {
			return this.typeQualifier;
		}

		/*
		 * ================== ProcedureEscalationType ====================
		 */

		@Override
		public String getEscalationName() {
			return this.itemName;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends Throwable> getEscalationType() {
			return (Class<? extends Throwable>) this.itemType;
		}

		/*
		 * ================== ProcedureFlowType ====================
		 */

		@Override
		public String getFlowName() {
			return this.itemName;
		}

		@Override
		public Class<?> getArgumentType() {
			return this.itemType;
		}

		/*
		 * ================== ProcedureVariableType ====================
		 */

		@Override
		public String getVariableName() {
			return this.itemName;
		}

		@Override
		public String getVariableType() {
			return this.itemType.getName();
		}

		/*
		 * ================== ProcedureObjectType ====================
		 */

		@Override
		public String getObjectName() {
			return this.itemName;
		}

		@Override
		public Class<?> getObjectType() {
			return this.itemType;
		}
	}

}