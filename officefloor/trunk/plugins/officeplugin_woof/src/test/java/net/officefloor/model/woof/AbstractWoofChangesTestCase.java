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

import junit.framework.TestCase;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.test.changes.AbstractChangesTestCase;

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
		 */
		void addSectionObject(String name, Class<?> objectType);
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
		public void addSectionObject(String name, Class<?> objectType) {
			this.objects.add(new SectionTypeItem(name, objectType.getName()));
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
		public SectionTypeItem(String name, String type, boolean isEscalation) {
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
		public SectionTypeItem(String name, String type) {
			this(name, type, false);
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
	}

}