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
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceFlowMetaData;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.GovernanceSourceContext;
import net.officefloor.compile.spi.governance.source.GovernanceSourceMetaData;
import net.officefloor.compile.spi.governance.source.GovernanceSourceProperty;
import net.officefloor.compile.spi.governance.source.GovernanceSourceSpecification;
import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.frame.spi.source.UnknownResourceError;

/**
 * {@link GovernanceLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceLoaderImpl implements GovernanceLoader {

	/**
	 * {@link Node} requiring the {@link Governance}.
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
	 *            {@link Node} requiring the {@link Governance}.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public GovernanceLoaderImpl(Node node, NodeContext nodeContext) {
		this.node = node;
		this.nodeContext = nodeContext;
	}

	/*
	 * ======================== GovernanceLoader =============================
	 */

	@Override
	public <I, F extends Enum<F>, GS extends GovernanceSource<I, F>> PropertyList loadSpecification(
			Class<GS> governanceSourceClass) {

		// Instantiate the governance source
		GS governanceSource = CompileUtil.newInstance(governanceSourceClass,
				GovernanceSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (governanceSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		GovernanceSourceSpecification specification;
		try {
			specification = governanceSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ GovernanceSourceSpecification.class.getSimpleName()
					+ " from " + governanceSourceClass.getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No "
					+ GovernanceSourceSpecification.class.getSimpleName()
					+ " returned from " + governanceSourceClass.getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		GovernanceSourceProperty[] governanceSourceProperties;
		try {
			governanceSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain "
							+ GovernanceSourceProperty.class.getSimpleName()
							+ " instances from "
							+ GovernanceSourceSpecification.class
									.getSimpleName() + " for "
							+ governanceSourceClass.getName(), ex);
			return null; // failed to obtain properties
		}

		// Load the governance source properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (governanceSourceProperties != null) {
			for (int i = 0; i < governanceSourceProperties.length; i++) {
				GovernanceSourceProperty mosProperty = governanceSourceProperties[i];

				// Ensure have the governance source property
				if (mosProperty == null) {
					this.addIssue(GovernanceSourceProperty.class
							.getSimpleName()
							+ " "
							+ i
							+ " is null from "
							+ GovernanceSourceSpecification.class
									.getSimpleName()
							+ " for "
							+ governanceSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = mosProperty.getName();
				} catch (Throwable ex) {
					this.addIssue(
							"Failed to get name for "
									+ GovernanceSourceProperty.class
											.getSimpleName()
									+ " "
									+ i
									+ " from "
									+ GovernanceSourceSpecification.class
											.getSimpleName() + " for "
									+ governanceSourceClass.getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(GovernanceSourceProperty.class
							.getSimpleName()
							+ " "
							+ i
							+ " provided blank name from "
							+ GovernanceSourceSpecification.class
									.getSimpleName()
							+ " for "
							+ governanceSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = mosProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue(
							"Failed to get label for "
									+ GovernanceSourceProperty.class
											.getSimpleName()
									+ " "
									+ i
									+ " ("
									+ name
									+ ") from "
									+ GovernanceSourceSpecification.class
											.getSimpleName() + " for "
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
		GovernanceSource<I, F> governanceSource = CompileUtil.newInstance(
				governanceSourceClass, GovernanceSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (governanceSource == null) {
			return null; // failed to instantiate
		}

		// Create the context for the governance source
		SourceContext sourceContext = this.nodeContext.getRootSourceContext();
		SourceProperties sourceProperties = new PropertyListSourceProperties(
				properties);
		GovernanceSourceContextImpl context = new GovernanceSourceContextImpl(
				true, sourceContext, sourceProperties);

		try {
			// Initialise the governance source
			governanceSource.init(context);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Property '" + ex.getUnknownPropertyName()
					+ "' must be specified");
			return null; // can not carry on

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName()
					+ "'");
			return null; // can not carry on

		} catch (UnknownResourceError ex) {
			this.addIssue("Can not obtain resource at location '"
					+ ex.getUnknownResourceLocation() + "'");
			return null; // can not carry on

		} catch (Throwable ex) {
			this.addIssue("Failed to initialise "
					+ governanceSource.getClass().getName(), ex);
			return null; // can not carry on
		}

		// Obtain the meta-data
		GovernanceSourceMetaData<I, F> metaData;
		try {
			metaData = governanceSource.getMetaData();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to get "
							+ GovernanceSourceMetaData.class.getSimpleName(),
					ex);
			return null; // must have meta-data
		}
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
				this.addIssue("No " + GovernanceFactory.class.getSimpleName()
						+ " provided");
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
			this.addIssue("Exception from "
					+ governanceSource.getClass().getName(), ex);
			return null; // must be successful with meta-data
		}

		// Create the governance type
		GovernanceType<I, F> governanceType = new GovernanceTypeImpl<I, F>(
				governanceFactory, extensionInterface, flowTypes,
				escalationTypes);

		// Return the governance type
		return governanceType;
	}

	/**
	 * Obtains the {@link GovernanceFlowType} instances from the
	 * {@link GovernanceSourceMetaData}.
	 * 
	 * @param metaData
	 *            {@link GovernanceSourceMetaData}.
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
					this.addIssue("Null "
							+ GovernanceFlowMetaData.class.getSimpleName()
							+ " for flow " + i);
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
								this.addIssue("Meta-data flows identified by different key types ("
										+ flowKeys.getName()
										+ ", "
										+ key.getClass().getName() + ")");
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
				flowTypes[i] = new GovernanceFlowTypeImpl<F>(index, type, key,
						label);
			}
		}

		// Validate have all the dependencies
		if (flowKeys == null) {
			// Determine if indexed or no dependencies
			flowKeys = (flowTypes.length == 0 ? None.class : Indexed.class);
		} else {
			// Ensure exactly one flow per key
			Set<?> keys = new HashSet<Object>(Arrays.asList(flowKeys
					.getEnumConstants()));
			for (GovernanceFlowType<F> flowType : flowTypes) {
				F key = flowType.getKey();
				if (!keys.contains(key)) {
					this.addIssue("Must have exactly one flow per key (key="
							+ key + ")");
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
				this.addIssue("Missing flow meta-data (keys=" + msg.toString()
						+ ")");
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
	 * @param metaData
	 *            {@link GovernanceSourceMetaData}.
	 * @return {@link GovernanceEscalationType} instances or <code>null</code>
	 *         if issue obtaining.
	 */
	private GovernanceEscalationType[] getGovernanceEscalationTypes(
			GovernanceSourceMetaData<?, ?> metaData) {

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
				escalationTypes[i] = new GovernanceEscalationTypeImpl(
						escalation.getSimpleName(), escalation);
			}
		}

		// Return the escalation types
		return escalationTypes;
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(this.node,
				issueDescription);
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
		this.nodeContext.getCompilerIssues().addIssue(this.node,
				issueDescription, cause);
	}

	/**
	 * {@link GovernanceSourceContext} implementation.
	 */
	private class GovernanceSourceContextImpl extends SourceContextImpl
			implements GovernanceSourceContext {

		/**
		 * Initiate.
		 * 
		 * @param delegate
		 *            {@link SourceContext}.
		 * @param sourceProperties
		 *            {@link SourceProperties}.
		 */
		public GovernanceSourceContextImpl(boolean isLoadingType,
				SourceContext delegate, SourceProperties sourceProperties) {
			super(isLoadingType, delegate, sourceProperties);
		}
	}

}