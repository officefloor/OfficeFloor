package net.officefloor.compile.impl.executive;

import net.officefloor.compile.executive.ExecutionStrategyType;
import net.officefloor.compile.executive.ExecutiveLoader;
import net.officefloor.compile.executive.ExecutiveType;
import net.officefloor.compile.executive.TeamOversightType;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceProperty;
import net.officefloor.frame.api.executive.source.ExecutiveSourceSpecification;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.api.source.IssueTarget;
import net.officefloor.frame.impl.construct.executive.ExecutiveSourceContextImpl;
import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;

/**
 * {@link ExecutiveLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveLoaderImpl implements ExecutiveLoader, IssueTarget {

	/**
	 * {@link Node} requiring the {@link Executive}.
	 */
	private final Node node;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Instantiate.
	 * 
	 * @param node        {@link Node} requiring the {@link Executive}.
	 * @param nodeContext {@link NodeContext}.
	 */
	public ExecutiveLoaderImpl(Node node, NodeContext nodeContext) {
		this.node = node;
		this.nodeContext = nodeContext;
	}

	/*
	 * ================== ExecutiveLoader =========================
	 */

	@Override
	public <TS extends ExecutiveSource> PropertyList loadSpecification(Class<TS> executiveSourceClass) {

		// Instantiate the executive source
		ExecutiveSource executiveSource = CompileUtil.newInstance(executiveSourceClass, ExecutiveSource.class,
				this.node, this.nodeContext.getCompilerIssues());
		if (executiveSource == null) {
			return null; // failed to instantiate
		}

		// Return the specification
		return this.loadSpecification(executiveSource);
	}

	@Override
	public PropertyList loadSpecification(ExecutiveSource executiveSource) {

		// Obtain the specification
		ExecutiveSourceSpecification specification;
		try {
			specification = executiveSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + ExecutiveSourceSpecification.class.getSimpleName() + " from "
					+ executiveSource.getClass().getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + ExecutiveSourceSpecification.class.getSimpleName() + " returned from "
					+ executiveSource.getClass().getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		ExecutiveSourceProperty[] executiveSourceProperties;
		try {
			executiveSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + ExecutiveSourceProperty.class.getSimpleName() + " instances from "
					+ ExecutiveSourceSpecification.class.getSimpleName() + " for "
					+ executiveSource.getClass().getName(), ex);
			return null; // failed to obtain properties
		}

		// Load the executive source properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (executiveSourceProperties != null) {
			for (int i = 0; i < executiveSourceProperties.length; i++) {
				ExecutiveSourceProperty mosProperty = executiveSourceProperties[i];

				// Ensure have the executive source property
				if (mosProperty == null) {
					this.addIssue(ExecutiveSourceProperty.class.getSimpleName() + " " + i + " is null from "
							+ ExecutiveSourceSpecification.class.getSimpleName() + " for "
							+ executiveSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = mosProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for " + ExecutiveSourceProperty.class.getSimpleName() + " " + i
							+ " from " + ExecutiveSourceSpecification.class.getSimpleName() + " for "
							+ executiveSource.getClass().getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(ExecutiveSourceProperty.class.getSimpleName() + " " + i + " provided blank name from "
							+ ExecutiveSourceSpecification.class.getSimpleName() + " for "
							+ executiveSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = mosProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for " + ExecutiveSourceProperty.class.getSimpleName() + " " + i
							+ " (" + name + ") from " + ExecutiveSourceSpecification.class.getSimpleName() + " for "
							+ executiveSource.getClass().getName(), ex);
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
	public <TS extends ExecutiveSource> ExecutiveType loadExecutiveType(Class<TS> executiveSourceClass,
			PropertyList propertyList) {

		// Instantiate the executive source
		ExecutiveSource executiveSource = CompileUtil.newInstance(executiveSourceClass, ExecutiveSource.class,
				this.node, this.nodeContext.getCompilerIssues());
		if (executiveSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the executive type
		return this.loadExecutiveType(executiveSource, propertyList);
	}

	@Override
	public ExecutiveType loadExecutiveType(ExecutiveSource executiveSource, PropertyList propertyList) {

		// Obtain qualified name
		String qualifiedName = this.node.getQualifiedName(ExecutiveSourceContextImpl.EXECUTIVE_NAME);

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.nodeContext.overrideProperties(this.node, qualifiedName, propertyList);

		// Create thread factory manufacturer
		ThreadFactoryManufacturer threadFactoryManufacturer = new ThreadFactoryManufacturer(
				new ManagedExecutionFactoryImpl(new ThreadCompletionListener[0]), null);

		// Attempt to create the executive
		Executive executive;
		try {
			executive = executiveSource
					.createExecutive(new ExecutiveSourceContextImpl(true, this.nodeContext.getRootSourceContext(),
							new PropertyListSourceProperties(overriddenProperties), threadFactoryManufacturer));

		} catch (AbstractSourceError ex) {
			ex.addIssue(this);
			return null; // can not carry on

		} catch (Throwable ex) {
			this.addIssue("Failed to create the " + Executive.class.getSimpleName() + " from "
					+ executiveSource.getClass().getName(), ex);
			return null; // failed loading team
		}

		// Ensure have executive
		if (executive == null) {
			this.addIssue(
					"No " + Executive.class.getSimpleName() + " provided from " + executiveSource.getClass().getName());
			return null; // must have executive
		}

		// Load the type information
		ExecutionStrategyType[] strategyTypes;
		TeamOversightType[] oversightTypes;
		try {

			// Load the execution strategies
			ExecutionStrategy[] strategies = executive.getExcutionStrategies();
			if ((strategies == null) || (strategies.length == 0)) {
				this.addIssue(Executive.class.getSimpleName() + " must provide at least one "
						+ ExecutionStrategy.class.getSimpleName());
				return null; // must have at least one strategy
			}
			strategyTypes = new ExecutionStrategyType[strategies.length];
			for (int i = 0; i < strategyTypes.length; i++) {
				ExecutionStrategy strategy = strategies[i];

				// Ensure have strategy
				if (strategy == null) {
					this.addIssue("Null " + ExecutionStrategy.class.getSimpleName() + " provided for index " + i);
					return null;
				}

				// Ensure have strategy name
				String strategyName = strategy.getExecutionStrategyName();
				if (CompileUtil.isBlank(strategyName)) {
					this.addIssue("No name for " + ExecutionStrategy.class.getSimpleName() + " at index " + i);
					return null;
				}

				// Add the execution strategy type
				strategyTypes[i] = new ExecutionStrategyTypeImpl(strategyName);
			}

			// Load the team oversights
			TeamOversight[] oversights = executive.getTeamOversights();
			oversightTypes = new TeamOversightType[oversights == null ? 0 : oversights.length];
			for (int i = 0; i < oversightTypes.length; i++) {
				TeamOversight oversight = oversights[i];

				// Ensure have oversight
				if (oversight == null) {
					this.addIssue("Null " + TeamOversight.class.getSimpleName() + " provided for index " + i);
					return null;
				}

				// Ensure have oversight name
				String oversightName = oversight.getTeamOversightName();
				if (CompileUtil.isBlank(oversightName)) {
					this.addIssue("No name for " + TeamOversight.class.getSimpleName() + " at index " + i);
					return null;
				}

				// Add the team oversight type
				oversightTypes[i] = new TeamOversightTypeImpl(oversightName);
			}

		} catch (Throwable ex) {
			this.addIssue("Exception from " + Executive.class.getSimpleName() + " for "
					+ executiveSource.getClass().getName(), ex);
			return null; // must have type information
		}

		// Return the executive type
		return new ExecutiveTypeImpl(strategyTypes, oversightTypes);
	}

	/*
	 * ================== IssueTarget ==================
	 */

	@Override
	public void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription);
	}

	@Override
	public void addIssue(String issueDescription, Throwable cause) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription, cause);
	}

}