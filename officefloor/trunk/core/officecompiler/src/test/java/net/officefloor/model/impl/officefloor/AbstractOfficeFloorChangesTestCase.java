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
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.test.changes.AbstractChangesTestCase;

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

		/**
		 * Add {@link OfficeInputType}.
		 * 
		 * @param name
		 *            Name of {@link OfficeSection}.
		 * @param inputName
		 *            Name of {@link OfficeSectionInput}.
		 * @param parameterType
		 *            Parameter type.
		 */
		void addOfficeInput(String name, String inputName,
				Class<?> parameterType);

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
		void addOfficeManagedObject(String name, Class<?> objectType,
				String typeQualifier, Class<?>... extensionInterfaces);

		/**
		 * Add {@link OfficeTeamType}.
		 * 
		 * @param name
		 *            Name of {@link OfficeTeamType}.
		 */
		void addOfficeTeam(String name);
	}

	/**
	 * {@link OfficeTypeContext} implementation.
	 */
	private class OfficeTypeContextImpl implements OfficeTypeContext,
			OfficeType {

		/**
		 * {@link OfficeInputType} instances.
		 */
		private final List<OfficeInputType> inputs = new LinkedList<OfficeInputType>();

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
		public void addOfficeInput(String name, String inputName,
				Class<?> parameterType) {
			this.inputs.add(new OfficeTypeItem(name, inputName, parameterType
					.getName()));
		}

		@Override
		public void addOfficeManagedObject(String name, Class<?> objectType,
				String typeQualifier, Class<?>... extensionInterfaces) {
			this.objects.add(new OfficeTypeItem(name, objectType.getName(),
					typeQualifier, extensionInterfaces));
		}

		@Override
		public void addOfficeTeam(String name) {
			this.teams.add(new OfficeTypeItem(name));
		}

		/*
		 * ===================== OfficeType ================================
		 */

		@Override
		public OfficeInputType[] getOfficeInputTypes() {
			return this.inputs.toArray(new OfficeInputType[0]);
		}

		@Override
		public OfficeManagedObjectType[] getOfficeManagedObjectTypes() {
			return this.objects.toArray(new OfficeManagedObjectType[0]);
		}

		@Override
		public OfficeTeamType[] getOfficeTeamTypes() {
			return this.teams.toArray(new OfficeTeamType[0]);
		}
	}

	/**
	 * {@link OfficeType} item.
	 */
	private class OfficeTypeItem implements OfficeInputType,
			OfficeManagedObjectType, OfficeTeamType {

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
		 * Initialise for {@link OfficeInputType}.
		 * 
		 * @param name
		 *            Name.
		 * @param inputName
		 *            Input name.
		 * @param parameterType
		 *            Parameter type.
		 */
		public OfficeTypeItem(String name, String inputName,
				String parameterType) {
			this.name = name;
			this.inputName = inputName;
			this.type = parameterType;
			this.typeQualifier = null;
			this.extensionInterfaces = null;
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
		public OfficeTypeItem(String name, String objectType,
				String typeQualifier, Class<?>[] extensionInterfaces) {
			this.name = name;
			this.inputName = null;
			this.type = objectType;
			this.typeQualifier = typeQualifier;
			this.extensionInterfaces = new String[extensionInterfaces.length];
			for (int i = 0; i < this.extensionInterfaces.length; i++) {
				this.extensionInterfaces[i] = extensionInterfaces[i].getName();
			}
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
		}

		/*
		 * ================== OfficeInputType =========================
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
	}

}