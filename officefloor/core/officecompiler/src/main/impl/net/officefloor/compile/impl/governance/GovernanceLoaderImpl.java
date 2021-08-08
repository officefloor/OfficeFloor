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

package net.officefloor.compile.impl.governance;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.governance.GovernanceEscalationType;
import net.officefloor.compile.governance.GovernanceFlowType;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceFlowMetaData;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.GovernanceSourceContext;
import net.officefloor.compile.spi.governance.source.GovernanceSourceMetaData;
import net.officefloor.compile.spi.governance.source.GovernanceSourceProperty;
import net.officefloor.compile.spi.governance.source.GovernanceSourceSpecification;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.api.source.IssueTarget;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link GovernanceLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceLoaderImpl implements GovernanceLoader, IssueTarget {

	/**
	 * {@link Node} requiring the {@link Governance}.
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
	 * Indicates using for loading type.
	 */
	private final boolean isLoadingType;

	/**
	 * Instantiate.
	 * 
	 * @param node          {@link Node} requiring the {@link Governance}.
	 * @param officeNode    {@link OfficeNode}. May be <code>null</code> if not
	 *                      loading within {@link OfficeNode}.
	 * @param nodeContext   {@link NodeContext}.
	 * @param isLoadingType Indicates using for loading type.
	 */
	public GovernanceLoaderImpl(Node node, OfficeNode officeNode, NodeContext nodeContext, boolean isLoadingType) {
		this.node = node;
		this.officeNode = officeNode;
		this.nodeContext = nodeContext;
		this.isLoadingType = isLoadingType;
	}

	/*
	 * ======================== GovernanceLoader =============================
	 */

	@Override
	public <I, F extends Enum<F>, GS extends GovernanceSource<I, F>> PropertyList loadSpecification(
			Class<GS> governanceSourceClass) {

		// Instantiate the governance source
		GS governanceSource = CompileUtil.newInstance(governanceSourceClass, GovernanceSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (governanceSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		GovernanceSourceSpecification specification;
		try {
			specification = governanceSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + GovernanceSourceSpecification.class.getSimpleName() + " from "
					+ governanceSourceClass.getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + GovernanceSourceSpecification.class.getSimpleName() + " returned from "
					+ governanceSourceClass.getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		GovernanceSourceProperty[] governanceSourceProperties;
		try {
			governanceSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + GovernanceSourceProperty.class.getSimpleName() + " instances from "
					+ GovernanceSourceSpecification.class.getSimpleName() + " for " + governanceSourceClass.getName(),
					ex);
			return null; // failed to obtain properties
		}

		// Load the governance source properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (governanceSourceProperties != null) {
			for (int i = 0; i < governanceSourceProperties.length; i++) {
				GovernanceSourceProperty mosProperty = governanceSourceProperties[i];

				// Ensure have the governance source property
				if (mosProperty == null) {
					this.addIssue(GovernanceSourceProperty.class.getSimpleName() + " " + i + " is null from "
							+ GovernanceSourceSpecification.class.getSimpleName() + " for "
							+ governanceSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = mosProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for " + GovernanceSourceProperty.class.getSimpleName() + " " + i
							+ " from " + GovernanceSourceSpecification.class.getSimpleName() + " for "
							+ governanceSourceClass.getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(GovernanceSourceProperty.class.getSimpleName() + " " + i
							+ " provided blank name from " + GovernanceSourceSpecification.class.getSimpleName()
							+ " for " + governanceSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = mosProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for " + GovernanceSourceProperty.class.getSimpleName() + " " + i
							+ " (" + name + ") from " + GovernanceSourceSpecification.class.getSimpleName() + " for "
							+ governanceSourceClass.getName(), ex);
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
	public <I, F extends Enum<F>, GS extends GovernanceSource<I, F>> GovernanceType<I, F> loadGovernanceType(
			Class<GS> governanceSourceClass, PropertyList properties) {

		// Instantiate the governance source
		GovernanceSource<I, F> governanceSource = CompileUtil.newInstance(governanceSourceClass, GovernanceSource.class,
				this.node, this.nodeContext.getCompilerIssues());
		if (governanceSource == null) {
			return null; // failed to instantiate

		}

		// Return the governance type
		return this.loadGovernanceType(governanceSource, properties);
	}

	@Override
	public <I, F extends Enum<F>, GS extends GovernanceSource<I, F>> GovernanceType<I, F> loadGovernanceType(
			GS governanceSource, PropertyList properties) {

		// Obtain qualified name
		String qualifiedName = this.node.getQualifiedName();

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.nodeContext.overrideProperties(this.node, qualifiedName,
				this.officeNode, properties);

		// Create the context for the governance source
		SourceContext sourceContext = this.nodeContext.getRootSourceContext();
		String[] additionalProfiles = this.nodeContext.additionalProfiles(this.officeNode);
		SourceProperties sourceProperties = new PropertyListSourceProperties(overriddenProperties);
		GovernanceSourceContextImpl context = new GovernanceSourceContextImpl(qualifiedName, this.isLoadingType,
				additionalProfiles, sourceContext, sourceProperties);

		// Initialise the governance source and obtain the meta-data
		GovernanceSourceMetaData<I, F> metaData;
		try {
			// Initialise the governance source
			metaData = governanceSource.init(context);

		} catch (AbstractSourceError ex) {
			ex.addIssue(this);
			return null; // can not carry on

		} catch (Throwable ex) {
			this.addIssue("Failed to initialise " + governanceSource.getClass().getName(), ex);
			return null; // can not carry on
		}

		// Ensure have meta-data
		if (metaData == null) {
			this.addIssue("Must provide meta-data");
			return null; // can not carry on
		}

		// Ensure handle any issue in interacting with meta-data
		GovernanceFactory<? extends I, F> governanceFactory;
		Class<I> extensionInterface;
		GovernanceFlowType<F>[] flowTypes;
		GovernanceEscalationType[] escalationTypes;
		try {

			// Ensure have governance factory
			governanceFactory = metaData.getGovernanceFactory();
			if (governanceFactory == null) {
				this.addIssue("No " + GovernanceFactory.class.getSimpleName() + " provided");
				return null; // can not carry on
			}

			// Ensure have extension interface
			extensionInterface = metaData.getExtensionInterface();
			if (extensionInterface == null) {
				this.addIssue("No extension interface type provided");
				return null; // can not carry on
			}

			// Obtain the flow types
			flowTypes = this.getGovernanceFlowTypes(metaData);
			if (flowTypes == null) {
				return null; // issue in getting flow types
			}

			// Obtain the escalation types
			escalationTypes = this.getGovernanceEscalationTypes(metaData);
			if (escalationTypes == null) {
				return null; // issue in getting escalation types
			}

		} catch (Throwable ex) {
			this.addIssue("Exception from " + governanceSource.getClass().getName(), ex);
			return null; // must be successful with meta-data
		}

		// Create the governance type
		GovernanceType<I, F> governanceType = new GovernanceTypeImpl<I, F>(governanceFactory, extensionInterface,
				flowTypes, escalationTypes);

		// Return the governance type
		return governanceType;
	}

	/**
	 * Obtains the {@link GovernanceFlowType} instances from the
	 * {@link GovernanceSourceMetaData}.
	 * 
	 * @param metaData {@link GovernanceSourceMetaData}.
	 * @return {@link GovernanceFlowType} instances.
	 */
	@SuppressWarnings("unchecked")
	private <F extends Enum<F>> GovernanceFlowType<F>[] getGovernanceFlowTypes(
			GovernanceSourceMetaData<?, F> metaData) {

		// Obtain the flow meta-data
		GovernanceFlowType<F>[] flowTypes;
		Class<?> flowKeys = null;
		GovernanceFlowMetaData<F>[] flowMetaDatas = metaData.getFlowMetaData();
		if (flowMetaDatas == null) {
			// No flows
			flowTypes = new GovernanceFlowType[0];

		} else {
			// Load the flows
			flowTypes = new GovernanceFlowType[flowMetaDatas.length];
			for (int i = 0; i < flowTypes.length; i++) {
				GovernanceFlowMetaData<F> flowMetaData = flowMetaDatas[i];

				// Ensure have flow meta-data
				if (flowMetaData == null) {
					this.addIssue("Null " + GovernanceFlowMetaData.class.getSimpleName() + " for flow " + i);
					return null; // missing meta-data
				}

				// Obtain details for flow
				String label = flowMetaData.getLabel();
				F key = flowMetaData.getKey();

				// Determine if the first flow
				if (i == 0) {
					// First flow, so load details
					flowKeys = (key == null ? null : key.getClass());
				} else {
					// Another flow that must adhere to previous
					boolean isIndexKeyMix;
					if (flowKeys == null) {
						// Dependencies expected to be indexed
						isIndexKeyMix = (key != null);

					} else {
						// Dependencies expected to be keyed
						isIndexKeyMix = (key == null);
						if (!isIndexKeyMix) {
							// Ensure the key is valid
							if (!flowKeys.isInstance(key)) {
								this.addIssue("Meta-data flows identified by different key types (" + flowKeys.getName()
										+ ", " + key.getClass().getName() + ")");
								return null; // mismatched keys
							}
						}
					}
					if (isIndexKeyMix) {
						this.addIssue("Meta-data flows mixing keys and indexes");
						return null; // can not mix indexing/keying
					}
				}

				// Obtain the argument type to the flow
				// (may be null for no argument)
				Class<?> type = flowMetaData.getArgumentType();

				// Determine the index for the flow
				int index = (key != null ? key.ordinal() : i);

				// Create and add the flow type
				flowTypes[i] = new GovernanceFlowTypeImpl<F>(index, type, key, label);
			}
		}

		// Validate have all the dependencies
		if (flowKeys == null) {
			// Determine if indexed or no dependencies
			flowKeys = (flowTypes.length == 0 ? None.class : Indexed.class);
		} else {
			// Ensure exactly one flow per key
			Set<?> keys = new HashSet<Object>(Arrays.asList(flowKeys.getEnumConstants()));
			for (GovernanceFlowType<F> flowType : flowTypes) {
				F key = flowType.getKey();
				if (!keys.contains(key)) {
					this.addIssue("Must have exactly one flow per key (key=" + key + ")");
					return null; // must be one flow per key
				}
				keys.remove(key);
			}
			if (keys.size() > 0) {
				StringBuilder msg = new StringBuilder();
				boolean isFirst = true;
				for (Object key : keys) {
					if (!isFirst) {
						msg.append(", ");
					}
					isFirst = false;
					msg.append(key.toString());
				}
				this.addIssue("Missing flow meta-data (keys=" + msg.toString() + ")");
				return null; // must have meta-data for each key
			}
		}

		// Ensure the flow types are in index order
		Arrays.sort(flowTypes, new Comparator<GovernanceFlowType<F>>() {
			@Override
			public int compare(GovernanceFlowType<F> a, GovernanceFlowType<F> b) {
				return a.getIndex() - b.getIndex();
			}
		});

		// Return the flow types
		return flowTypes;
	}

	/**
	 * Obtains the {@link GovernanceEscalationType} instances.
	 * 
	 * @param metaData {@link GovernanceSourceMetaData}.
	 * @return {@link GovernanceEscalationType} instances or <code>null</code> if
	 *         issue obtaining.
	 */
	private GovernanceEscalationType[] getGovernanceEscalationTypes(GovernanceSourceMetaData<?, ?> metaData) {

		// Obtain the governance escalations
		GovernanceEscalationType[] escalationTypes;
		Class<?>[] escalations = metaData.getEscalationTypes();
		if (escalations == null) {
			// No escalation types
			escalationTypes = new GovernanceEscalationType[0];

		} else {
			// Create the escalations types
			escalationTypes = new GovernanceEscalationType[escalations.length];
			for (int i = 0; i < escalationTypes.length; i++) {

				// Ensure have escalation type
				Class<?> escalation = escalations[i];
				if (escalation == null) {
					this.addIssue("Null escalation type for " + i);
					return null; // must have escalation
				}

				// Create the escalation type
				escalationTypes[i] = new GovernanceEscalationTypeImpl(escalation.getSimpleName(), escalation);
			}
		}

		// Return the escalation types
		return escalationTypes;
	}

	/*
	 * =================== IssueTarget =========================
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
	 * {@link GovernanceSourceContext} implementation.
	 */
	private class GovernanceSourceContextImpl extends SourceContextImpl implements GovernanceSourceContext {

		/**
		 * Initiate.
		 * 
		 * @param governanceName     Name of {@link Governance}.
		 * @param isLoadingType      Indicates if loading type.
		 * @param additionalProfiles Additional profiles.
		 * @param delegate           {@link SourceContext}.
		 * @param sourceProperties   {@link SourceProperties}.
		 */
		private GovernanceSourceContextImpl(String governanceName, boolean isLoadingType, String[] additionalProfiles,
				SourceContext delegate, SourceProperties sourceProperties) {
			super(governanceName, isLoadingType, additionalProfiles, delegate, sourceProperties);
		}
	}

}
