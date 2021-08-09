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

package net.officefloor.compile.impl.administrator;

import java.util.HashSet;
import java.util.Set;

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
import net.officefloor.compile.internal.structure.OfficeNode;
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
import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.api.source.IssueTarget;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link AdministrationLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationLoaderImpl implements AdministrationLoader, IssueTarget {

	/**
	 * {@link Node} requiring the {@link Administration}.
	 */
	private final Node node;

	/**
	 * {@link OfficeNode}.
	 */
	private final OfficeNode officeNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Indicates if using to load type.
	 */
	private final boolean isLoadingType;

	/**
	 * Instantiate.
	 * 
	 * @param node          {@link Node} requiring the {@link Administration}.
	 * @param officeNode    {@link OfficeNode}. May be <code>null</code> if not
	 *                      loading within {@link OfficeNode}.
	 * @param nodeContext   {@link NodeContext}.
	 * @param isLoadingType Indicates if using to load type.
	 */
	public AdministrationLoaderImpl(Node node, OfficeNode officeNode, NodeContext nodeContext, boolean isLoadingType) {
		this.node = node;
		this.officeNode = officeNode;
		this.nodeContext = nodeContext;
		this.isLoadingType = isLoadingType;
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

		// Load and return specification
		return this.loadSpecification(administrationSource);
	}

	@Override
	public <E, F extends Enum<F>, G extends Enum<G>> PropertyList loadSpecification(
			AdministrationSource<E, F, G> administrationSource) {

		// Obtain the specification
		AdministrationSourceSpecification specification;
		try {
			specification = administrationSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + AdministrationSourceSpecification.class.getSimpleName() + " from "
					+ administrationSource.getClass().getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + AdministrationSourceSpecification.class.getSimpleName() + " returned from "
					+ administrationSource.getClass().getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		AdministrationSourceProperty[] administratorSourceProperties;
		try {
			administratorSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + AdministrationSourceProperty.class.getSimpleName() + " instances from "
					+ AdministrationSourceSpecification.class.getSimpleName() + " for "
					+ administrationSource.getClass().getName(), ex);
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
							+ administrationSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = adminProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for " + AdministrationSourceProperty.class.getSimpleName() + " "
							+ i + " from " + AdministrationSourceSpecification.class.getSimpleName() + " for "
							+ administrationSource.getClass().getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(AdministrationSourceProperty.class.getSimpleName() + " " + i
							+ " provided blank name from " + AdministrationSourceSpecification.class.getSimpleName()
							+ " for " + administrationSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = adminProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for " + AdministrationSourceProperty.class.getSimpleName() + " "
							+ i + " (" + name + ") from " + AdministrationSourceSpecification.class.getSimpleName()
							+ " for " + administrationSource.getClass().getName(), ex);
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

		// Load and return type
		return this.loadAdministrationType(administratorSource, propertyList);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, F extends Enum<F>, G extends Enum<G>> AdministrationType<E, F, G> loadAdministrationType(
			AdministrationSource<E, F, G> administratorSource, PropertyList propertyList) {

		// Obtain qualified name
		String qualifiedName = this.node.getQualifiedName();

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.nodeContext.overrideProperties(this.node, qualifiedName,
				this.officeNode, propertyList);

		// Obtain the source context
		SourceContext sourceContext = this.nodeContext.getRootSourceContext();

		// Create the administrator source context
		String[] additionalProfiles = this.nodeContext.additionalProfiles(this.officeNode);
		SourceProperties properties = new PropertyListSourceProperties(overriddenProperties);
		AdministrationSourceContext context = new AdministrationSourceContextImpl(qualifiedName, this.isLoadingType,
				additionalProfiles, properties, sourceContext);

		// Initialise the administration source and obtain the meta-data
		AdministrationSourceMetaData<E, F, G> metaData;
		try {
			// Initialise the administrator source
			metaData = administratorSource.init(context);

		} catch (AbstractSourceError ex) {
			ex.addIssue(this);
			return null; // can not carry on

		} catch (Throwable ex) {
			this.addIssue("Failed to init", ex);
			return null; // must initialise
		}

		// Ensure have meta-data
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
			if (flowMetaDatas == null) {
				this.addIssue("Must provide flow meta-data");
				return null; // must have flows
			}
			Set<F> flowKeys = new HashSet<>();
			flowTypes = new AdministrationFlowType[flowMetaDatas.length];
			for (int i = 0; i < flowMetaDatas.length; i++) {
				AdministrationFlowMetaData<F> flowMetaData = flowMetaDatas[i];

				// Ensure have flow meta-data
				if (flowMetaData == null) {
					this.addIssue("Null meta-data for flow " + i);
					return null; // must have flow meta-data
				}

				// Obtain the flow key class
				F flowKey = flowMetaData.getKey();
				Class<?> keyClass;
				if (flowKey != null) {
					if (flowKeys.contains(flowKey)) {
						this.addIssue("Duplicate flow key " + flowKey.name() + " on flow meta-data");
						return null; // can not have duplicate key
					}
					flowKeys.add(flowKey);
					keyClass = flowKey.getClass();
				} else {
					keyClass = Indexed.class;
				}
				if (flowKeyClass == null) {
					flowKeyClass = (Class<F>) keyClass;
				} else if (!flowKeyClass.equals(keyClass)) {
					this.addIssue("May only use one enum type to define flow keys (" + flowKeyClass.getName() + ", "
							+ keyClass.getName() + ")");
					return null; // invalid flow key
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
			if (escalationMetaDatas == null) {
				this.addIssue("Must provide escalation meta-data");
				return null; // must have escalations
			}
			Set<Class<? extends Throwable>> uniqueEscalations = new HashSet<>();
			escalationTypes = new AdministrationEscalationType[escalationMetaDatas.length];
			for (int i = 0; i < escalationMetaDatas.length; i++) {
				AdministrationEscalationMetaData escalationMetaData = escalationMetaDatas[i];

				// Ensure have escalation meta-data
				if (escalationMetaData == null) {
					this.addIssue("Null meta-data for escalation " + i);
					return null; // must have escalation meta-data
				}

				// Obtain the escalation type
				Class<? extends Throwable> escalationType = escalationMetaData.getEscalationType();
				if (escalationType == null) {
					this.addIssue("Null escalation type from escalation " + i);
					return null; // must have escalation type
				}
				if (uniqueEscalations.contains(escalationType)) {
					this.addIssue("Escalation listed twice (" + escalationType.getName() + ")");
					return null; // all escalations must be unique
				}
				uniqueEscalations.add(escalationType);

				// Obtain the escalation details
				String escalationName = escalationMetaData.getLabel();
				if (escalationName == null) {
					escalationName = escalationType.getName();
				}

				// Create the escalation type
				escalationTypes[i] = new AdministrationEscalationTypeImpl(escalationName, escalationType);
			}

			// Obtain the governance details
			AdministrationGovernanceMetaData<G>[] governanceMetaDatas = metaData.getGovernanceMetaData();
			if (governanceMetaDatas == null) {
				this.addIssue("Must provide governance meta-data");
				return null; // must have governance
			}
			Set<G> governanceKeys = new HashSet<>();
			governanceTypes = new AdministrationGovernanceType[governanceMetaDatas.length];
			for (int i = 0; i < governanceMetaDatas.length; i++) {
				AdministrationGovernanceMetaData<G> governanceMetaData = governanceMetaDatas[i];

				// Ensure have governance meta-data
				if (governanceMetaData == null) {
					this.addIssue("Null meta-data for governance " + i);
					return null; // must have governance meta-data
				}

				// Obtain the governance key class
				G governanceKey = governanceMetaData.getKey();
				Class<?> keyClass;
				if (governanceKey != null) {
					if (governanceKeys.contains(governanceKey)) {
						this.addIssue("Duplicate governance key " + governanceKey.name() + " on governance meta-data");
						return null; // duplicate key
					}
					governanceKeys.add(governanceKey);
					keyClass = governanceKey.getClass();
				} else {
					keyClass = Indexed.class;
				}
				if (governanceKeyClass == null) {
					governanceKeyClass = (Class<G>) keyClass;
				} else if (!governanceKeyClass.equals(keyClass)) {
					this.addIssue("May only use one enum type to define governance keys ("
							+ governanceKeyClass.getName() + ", " + keyClass.getName() + ")");
					return null; // invalid governance key
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
			this.addIssue("Exception from " + administratorSource.getClass().getName(), ex);
			return null; // must be successful with meta-data
		}

		// Return the administrator type
		return new AdministrationTypeImpl<E, F, G>(administrationFactory, extensionInterface, flowKeyClass, flowTypes,
				escalationTypes, governanceKeyClass, governanceTypes);
	}

	/*
	 * =================== IssueTarget =======================
	 */

	@Override
	public void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription);
	}

	@Override
	public void addIssue(String issueDescription, Throwable cause) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription, cause);
	}

	/**
	 * {@link AdministrationSourceContext} implementation.
	 */
	private class AdministrationSourceContextImpl extends SourceContextImpl implements AdministrationSourceContext {

		/**
		 * Initiate.
		 * 
		 * @param administrationSourceName Name of {@link AdministrationSource}.
		 * @param isLoadingType            Indicates if loading type.
		 * @param additionalProfiles       Additional profiles.
		 * @param properties               {@link SourceProperties}.
		 * @param sourceContext            Delegate {@link SourceContext}.
		 */
		private AdministrationSourceContextImpl(String administrationSourceName, boolean isLoadingType,
				String[] additionalProfiles, SourceProperties properties, SourceContext sourceContext) {
			super(administrationSourceName, isLoadingType, additionalProfiles, sourceContext, properties);
		}
	}

}
