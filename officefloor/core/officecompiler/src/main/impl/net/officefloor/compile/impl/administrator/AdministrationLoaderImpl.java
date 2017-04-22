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
package net.officefloor.compile.impl.administrator;

import net.officefloor.compile.administration.AdministrationEscalationType;
import net.officefloor.compile.administration.AdministrationFlowType;
import net.officefloor.compile.administration.AdministrationGovernanceType;
import net.officefloor.compile.administration.AdministrationLoader;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.AdministrationEscalationMetaData;
import net.officefloor.compile.spi.administration.source.AdministrationFlowMetaData;
import net.officefloor.compile.spi.administration.source.AdministrationGovernanceMetaData;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.AdministrationSourceContext;
import net.officefloor.compile.spi.administration.source.AdministrationSourceMetaData;
import net.officefloor.compile.spi.administration.source.AdministrationSourceProperty;
import net.officefloor.compile.spi.administration.source.AdministrationSourceSpecification;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link AdministrationLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationLoaderImpl implements AdministrationLoader {

	/**
	 * {@link Node} requiring the {@link Administration}.
	 */
	private final Node node;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Instantiate.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link Administration}.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public AdministrationLoaderImpl(Node node, NodeContext nodeContext) {
		this.node = node;
		this.nodeContext = nodeContext;
	}

	/*
	 * ===================== AdministrationLoader =============================
	 */

	@Override
	public <E, F extends Enum<F>, G extends Enum<G>, AS extends AdministrationSource<E, F, G>> PropertyList loadSpecification(
			Class<AS> administrationSourceClass) {

		// Instantiate the administration source
		AdministrationSource<E, F, G> administrationSource = CompileUtil.newInstance(administrationSourceClass,
				AdministrationSource.class, this.node, this.nodeContext.getCompilerIssues());
		if (administrationSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		AdministrationSourceSpecification specification;
		try {
			specification = administrationSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + AdministrationSourceSpecification.class.getSimpleName() + " from "
					+ administrationSourceClass.getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + AdministrationSourceSpecification.class.getSimpleName() + " returned from "
					+ administrationSourceClass.getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		AdministrationSourceProperty[] administratorSourceProperties;
		try {
			administratorSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + AdministrationSourceProperty.class.getSimpleName() + " instances from "
					+ AdministrationSourceSpecification.class.getSimpleName() + " for "
					+ administrationSourceClass.getName(), ex);
			return null; // failed to obtain properties
		}

		// Load the administrator source properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (administratorSourceProperties != null) {
			for (int i = 0; i < administratorSourceProperties.length; i++) {
				AdministrationSourceProperty adminProperty = administratorSourceProperties[i];

				// Ensure have the administrator source property
				if (adminProperty == null) {
					this.addIssue(AdministrationSourceProperty.class.getSimpleName() + " " + i + " is null from "
							+ AdministrationSourceSpecification.class.getSimpleName() + " for "
							+ administrationSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = adminProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for " + AdministrationSourceProperty.class.getSimpleName() + " "
							+ i + " from " + AdministrationSourceSpecification.class.getSimpleName() + " for "
							+ administrationSourceClass.getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(AdministrationSourceProperty.class.getSimpleName() + " " + i
							+ " provided blank name from " + AdministrationSourceSpecification.class.getSimpleName()
							+ " for " + administrationSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = adminProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for " + AdministrationSourceProperty.class.getSimpleName() + " "
							+ i + " (" + name + ") from " + AdministrationSourceSpecification.class.getSimpleName()
							+ " for " + administrationSourceClass.getName(), ex);
					return null; // must have complete property details
				}

				// Add to the properties
				propertyList.addProperty(name, label);
			}
		}

		// Return the property list
		return propertyList;
	}

	@Override
	public <E, F extends Enum<F>, G extends Enum<G>, AS extends AdministrationSource<E, F, G>> AdministrationType<E, F, G> loadAdministrationType(
			Class<AS> administratorSourceClass, PropertyList propertyList) {

		// Create an instance of the administrator source
		AS administratorSource = CompileUtil.newInstance(administratorSourceClass, AdministrationSource.class,
				this.node, this.nodeContext.getCompilerIssues());
		if (administratorSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the source context
		SourceContext sourceContext = this.nodeContext.getRootSourceContext();

		// Create the administrator source context
		SourceProperties properties = new PropertyListSourceProperties(propertyList);
		AdministrationSourceContext context = new AdministrationSourceContextImpl(true, properties, sourceContext);

		try {
			// Initialise the administrator source
			administratorSource.init(context);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName() + "'");
			return null; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName() + "'");
			return null; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue("Can not obtain resource at location '" + ex.getUnknownResourceLocation() + "'");
			return null; // must have resource

		} catch (Throwable ex) {
			this.addIssue("Failed to init", ex);
			return null; // must initialise
		}

		// Ensure handle issues obtaining meta-data
		AdministrationSourceMetaData<E, F, G> metaData;
		try {
			metaData = administratorSource.getMetaData();
		} catch (Throwable ex) {
			this.addIssue("Failed to get " + AdministrationSourceMetaData.class.getSimpleName(), ex);
			return null; // must successfully get meta-data
		}
		if (metaData == null) {
			this.addIssue("Returned null " + AdministrationSourceMetaData.class.getSimpleName());
			return null; // must have meta-data
		}

		// Ensure handle issues interacting with meta-data
		AdministrationFactory<E, F, G> administrationFactory;
		Class<E> extensionInterface;
		Class<F> flowKeyClass = null;
		AdministrationFlowType<F>[] flowTypes;
		AdministrationEscalationType[] escalationTypes;
		Class<G> governanceKeyClass = null;
		AdministrationGovernanceType<G>[] governanceTypes;
		try {

			// Obtain the administration factory
			administrationFactory = metaData.getAdministrationFactory();
			if (administrationFactory == null) {
				this.addIssue("No " + AdministrationFactory.class.getSimpleName() + " provided");
				return null; // must have administration factory
			}

			// Obtain the extension interface type
			extensionInterface = metaData.getExtensionInterface();
			if (extensionInterface == null) {
				this.addIssue("No extension interface provided");
				return null; // must have extension interface
			}

			// Obtain the flow details
			AdministrationFlowMetaData<F>[] flowMetaDatas = metaData.getFlowMetaData();
			flowTypes = new AdministrationFlowType[flowMetaDatas.length];
			for (int i = 0; i < flowMetaDatas.length; i++) {
				AdministrationFlowMetaData<F> flowMetaData = flowMetaDatas[i];

				// Obtain the flow key class
				F flowKey = flowMetaData.getKey();
				if (flowKey != null) {
					flowKeyClass = (Class<F>) flowKey.getClass();
				} else {
					flowKeyClass = (Class<F>) Indexed.class;
				}

				// Obtain the flow details
				Class<?> argumentType = flowMetaData.getArgumentType();
				int flowIndex = (flowKey != null ? flowKey.ordinal() : i);
				String flowName = flowMetaData.getLabel();
				if (flowName == null) {
					flowName = (flowKey != null ? flowKey.toString() : String.valueOf(flowIndex));
				}

				// Create the flow type
				flowTypes[i] = new AdministrationFlowTypeImpl<F>(flowName, argumentType, flowIndex, flowKey);
			}

			// Obtain the escalation details
			AdministrationEscalationMetaData[] escalationMetaDatas = metaData.getEscalationMetaData();
			escalationTypes = new AdministrationEscalationType[escalationMetaDatas.length];
			for (int i = 0; i < escalationMetaDatas.length; i++) {
				AdministrationEscalationMetaData escalationMetaData = escalationMetaDatas[i];

				// Obtain the escalation details
				Class<? extends Throwable> escalationType = escalationMetaData.getEscalationType();
				String escalationName = escalationMetaData.getLabel();
				if (escalationName == null) {
					escalationName = escalationType.getSimpleName();
				}

				// Create the escalation type
				escalationTypes[i] = new AdministrationEscalationTypeImpl(escalationName, escalationType);
			}

			// Obtain the governance details
			AdministrationGovernanceMetaData<G>[] governanceMetaDatas = metaData.getGovernanceMetaData();
			governanceTypes = new AdministrationGovernanceType[governanceMetaDatas.length];
			for (int i = 0; i < governanceMetaDatas.length; i++) {
				AdministrationGovernanceMetaData<G> governanceMetaData = governanceMetaDatas[i];

				// Obtain the governance key class
				G governanceKey = governanceMetaData.getKey();
				if (governanceKey != null) {
					governanceKeyClass = (Class<G>) governanceKey.getClass();
				} else {
					governanceKeyClass = (Class<G>) Indexed.class;
				}

				// Obtain the governance details
				int governanceIndex = (governanceKey != null ? governanceKey.ordinal() : i);
				String governanceName = governanceMetaData.getLabel();
				if (governanceName == null) {
					governanceName = (governanceKey != null ? governanceKey.toString()
							: String.valueOf(governanceIndex));
				}

				// Create the governance type
				governanceTypes[i] = new AdministrationGovernanceTypeImpl<G>(governanceName, governanceIndex,
						governanceKey);
			}

		} catch (Throwable ex) {
			this.addIssue("Exception from " + administratorSourceClass.getName(), ex);
			return null; // must be successful with meta-data
		}

		// Return the administrator type
		return new AdministrationTypeImpl<E, F, G>(administrationFactory, extensionInterface, flowKeyClass, flowTypes,
				escalationTypes, governanceKeyClass, governanceTypes);

	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	private void addIssue(String issueDescription, Throwable cause) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription, cause);
	}

	/**
	 * {@link AdministrationSourceContext} implementation.
	 */
	public class AdministrationSourceContextImpl extends SourceContextImpl implements AdministrationSourceContext {

		/**
		 * Initiate.
		 * 
		 * @param isLoadingType
		 *            Indicates if loading type.
		 * @param properties
		 *            {@link SourceProperties}.
		 * @param sourceContext
		 *            Delegate {@link SourceContext}.
		 */
		public AdministrationSourceContextImpl(boolean isLoadingType, SourceProperties properties,
				SourceContext sourceContext) {
			super(isLoadingType, sourceContext, properties);
		}
	}

}