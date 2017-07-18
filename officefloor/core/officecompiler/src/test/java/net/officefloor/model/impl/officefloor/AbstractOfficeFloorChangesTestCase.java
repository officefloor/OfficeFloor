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
package net.officefloor.model.impl.officefloor;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.OfficeInput;
import net.officefloor.compile.spi.office.OfficeOutput;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.test.changes.AbstractChangesTestCase;

/**
 * Abstract functionality for testing the {@link OfficeFloorChanges}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeFloorChangesTestCase
		extends AbstractChangesTestCase<OfficeFloorModel, OfficeFloorChanges> {

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
	protected OfficeFloorModel retrieveModel(ConfigurationItem configurationItem) throws Exception {
		OfficeFloorModel officeFloor = new OfficeFloorModel();
		new OfficeFloorRepositoryImpl(new ModelRepositoryImpl()).retrieveOfficeFloor(officeFloor, configurationItem);
		return officeFloor;
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

		/**
		 * Add {@link OfficeAvailableSectionInputType}.
		 * 
		 * @param name
		 *            Name of {@link OfficeSection}.
		 * @param inputName
		 *            Name of {@link OfficeSectionInput}.
		 * @param parameterType
		 *            Parameter type.
		 */
		void addOfficeSectionInput(String sectionName, String inputName, Class<?> parameterType);

		/**
		 * Add {@link OfficeManagedObjectType}.
		 * 
		 * @param name
		 *            Name of {@link OfficeManagedObjectType}.
		 * @param objectType
		 *            Object type.
		 * @param typeQualifier
		 *            Type qualifier.
		 * @param extensionInterfaces
		 *            Extension interfaces.
		 */
		void addOfficeManagedObject(String name, Class<?> objectType, String typeQualifier,
				Class<?>... extensionInterfaces);

		/**
		 * Add {@link OfficeTeamType}.
		 * 
		 * @param name
		 *            Name of {@link OfficeTeamType}.
		 */
		void addOfficeTeam(String name);

		/**
		 * Add {@link OfficeInputType}.
		 * 
		 * @param inputName
		 *            Name of {@link OfficeInput}.
		 * @param parameterType
		 *            Parameter type.
		 * @param response
		 *            {@link OfficeOutputType}.
		 */
		void addOfficeInput(String inputName, Class<?> parameterType, OfficeOutputType response);

		/**
		 * Add {@link OfficeOutputType}.
		 * 
		 * @param outputName
		 *            Name of {@link OfficeOutput}.
		 * @param argumentType
		 *            Argument type.
		 * @param handler
		 *            {@link OfficeInputType}.
		 */
		void addOfficeOutput(String outputName, Class<?> argumentType, OfficeInputType handler);
	}

	/**
	 * {@link OfficeTypeContext} implementation.
	 */
	private class OfficeTypeContextImpl implements OfficeTypeContext, OfficeType {

		/**
		 * {@link OfficeInputType} instances.
		 */
		private final List<OfficeInputType> inputs = new LinkedList<OfficeInputType>();

		/**
		 * {@link OfficeOutputType} instances.
		 */
		private final List<OfficeOutputType> outputs = new LinkedList<OfficeOutputType>();

		/**
		 * {@link OfficeAvailableSectionInputType} instances.
		 */
		private final List<OfficeAvailableSectionInputType> sectionInputs = new LinkedList<OfficeAvailableSectionInputType>();

		/**
		 * {@link OfficeManagedObjectType} instances.
		 */
		private final List<OfficeManagedObjectType> objects = new LinkedList<OfficeManagedObjectType>();

		/**
		 * {@link OfficeTeamType} instances.
		 */
		private final List<OfficeTeamType> teams = new LinkedList<OfficeTeamType>();

		/*
		 * ===================== OfficeTypeContext ============================
		 */

		@Override
		public void addOfficeInput(String inputName, Class<?> parameterType, OfficeOutputType response) {
			this.inputs.add(new OfficeTypeItem(inputName, parameterType.getName(), response));
		}

		@Override
		public void addOfficeOutput(String outputName, Class<?> argumentType, OfficeInputType handler) {
			this.outputs.add(new OfficeTypeItem(outputName, argumentType.getName(), handler));
		}

		@Override
		public void addOfficeSectionInput(String name, String inputName, Class<?> parameterType) {
			this.sectionInputs.add(new OfficeTypeItem(name, inputName, parameterType.getName()));
		}

		@Override
		public void addOfficeManagedObject(String name, Class<?> objectType, String typeQualifier,
				Class<?>... extensionInterfaces) {
			this.objects.add(new OfficeTypeItem(name, objectType.getName(), typeQualifier, extensionInterfaces));
		}

		@Override
		public void addOfficeTeam(String name) {
			this.teams.add(new OfficeTypeItem(name));
		}

		/*
		 * ===================== OfficeType ================================
		 */

		@Override
		public OfficeAvailableSectionInputType[] getOfficeSectionInputTypes() {
			return this.sectionInputs.toArray(new OfficeAvailableSectionInputType[0]);
		}

		@Override
		public OfficeManagedObjectType[] getOfficeManagedObjectTypes() {
			return this.objects.toArray(new OfficeManagedObjectType[0]);
		}

		@Override
		public OfficeTeamType[] getOfficeTeamTypes() {
			return this.teams.toArray(new OfficeTeamType[0]);
		}

		@Override
		public OfficeInputType[] getOfficeInputTypes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public OfficeOutputType[] getOfficeOutputTypes() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	/**
	 * {@link OfficeType} item.
	 */
	private class OfficeTypeItem implements OfficeAvailableSectionInputType, OfficeManagedObjectType, OfficeTeamType,
			OfficeInputType, OfficeOutputType {

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
		 * Input name.
		 */
		private final String inputName;

		/**
		 * Extension interfaces.
		 */
		private final String[] extensionInterfaces;

		/**
		 * {@link OfficeInputType}.
		 */
		private final OfficeInputType inputType;

		/**
		 * {@link OfficeOutputType}.
		 */
		private final OfficeOutputType outputType;

		/**
		 * Initialise for {@link OfficeAvailableSectionInputType}.
		 * 
		 * @param name
		 *            Name.
		 * @param inputName
		 *            Input name.
		 * @param parameterType
		 *            Parameter type.
		 */
		public OfficeTypeItem(String name, String inputName, String parameterType) {
			this.name = name;
			this.inputName = inputName;
			this.type = parameterType;
			this.typeQualifier = null;
			this.extensionInterfaces = null;
			this.inputType = null;
			this.outputType = null;
		}

		/**
		 * Initialise for {@link OfficeInputType}.
		 * 
		 * @param name
		 *            Name.
		 * @param parameterType
		 *            Parameter type.
		 * @param response
		 *            {@link OfficeOutputType}.
		 */
		public OfficeTypeItem(String name, String parameterType, OfficeOutputType response) {
			this.name = name;
			this.inputName = null;
			this.type = parameterType;
			this.typeQualifier = null;
			this.extensionInterfaces = null;
			this.inputType = null;
			this.outputType = response;
		}

		/**
		 * Initialise for {@link OfficeOutputType}.
		 * 
		 * @param name
		 *            Name.
		 * @param argumentType
		 *            Argument type.
		 * @param handler
		 *            {@link OfficeInputType}.
		 */
		public OfficeTypeItem(String name, String argumentType, OfficeInputType handler) {
			this.name = name;
			this.inputName = null;
			this.type = argumentType;
			this.typeQualifier = null;
			this.extensionInterfaces = null;
			this.inputType = handler;
			this.outputType = null;
		}

		/**
		 * Initialise for {@link OfficeManagedObjectType}.
		 * 
		 * @param name
		 *            Name.
		 * @param objectType
		 *            Object type.
		 * @param typeQualifier
		 *            Type qualifier.
		 * @param extensionInterfaces
		 *            Extension interfaces.
		 */
		public OfficeTypeItem(String name, String objectType, String typeQualifier, Class<?>[] extensionInterfaces) {
			this.name = name;
			this.inputName = null;
			this.type = objectType;
			this.typeQualifier = typeQualifier;
			this.extensionInterfaces = new String[extensionInterfaces.length];
			for (int i = 0; i < this.extensionInterfaces.length; i++) {
				this.extensionInterfaces[i] = extensionInterfaces[i].getName();
			}
			this.inputType = null;
			this.outputType = null;
		}

		/**
		 * Initialise for {@link OfficeTeamType}.
		 * 
		 * @param name
		 *            Name.
		 */
		public OfficeTypeItem(String name) {
			this.name = name;
			this.inputName = null;
			this.type = null;
			this.typeQualifier = null;
			this.extensionInterfaces = null;
			this.inputType = null;
			this.outputType = null;
		}

		/*
		 * =========== OfficeSectionInputType / OfficeInputType ===========
		 */

		@Override
		public String getOfficeSectionName() {
			return this.name;
		}

		@Override
		public String getOfficeSectionInputName() {
			return this.inputName;
		}

		@Override
		public String getParameterType() {
			return this.type;
		}

		@Override
		public String getOfficeInputName() {
			return this.name;
		}

		@Override
		public OfficeOutputType getResponseOfficeOutputType() {
			return this.outputType;
		}

		/*
		 * ================== OfficeManagedObjectType ======================
		 */

		@Override
		public String getOfficeManagedObjectName() {
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

		@Override
		public String[] getExtensionInterfaces() {
			return this.extensionInterfaces;
		}

		/*
		 * ================== OfficeTeamType =================================
		 */

		@Override
		public String getOfficeTeamName() {
			return this.name;
		}

		@Override
		public TypeQualification[] getTypeQualification() {
			return new TypeQualification[0];
		}

		/*
		 * ================== OfficeOutputType =================================
		 */

		@Override
		public String getOfficeOutputName() {
			return this.name;
		}

		@Override
		public String getArgumentType() {
			return this.type;
		}

		@Override
		public OfficeInputType getHandlingOfficeInputType() {
			return this.inputType;
		}
	}

}