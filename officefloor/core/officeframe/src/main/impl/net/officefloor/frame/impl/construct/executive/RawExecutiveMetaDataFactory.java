/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.construct.executive;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.OfficeFloorIssueTarget;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;
import net.officefloor.frame.internal.configuration.ExecutiveConfiguration;

/**
 * Factory for the construction of the {@link RawExecutiveMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawExecutiveMetaDataFactory {

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext;

	/**
	 * {@link ThreadFactoryManufacturer}.
	 */
	private final ThreadFactoryManufacturer threadFactoryManufacturer;

	/**
	 * Instantiate.
	 * 
	 * @param sourceContext             {@link SourceContext}.
	 * @param threadFactoryManufacturer {@link ThreadFactoryManufacturer}.
	 */
	public RawExecutiveMetaDataFactory(SourceContext sourceContext,
			ThreadFactoryManufacturer threadFactoryManufacturer) {
		this.sourceContext = sourceContext;
		this.threadFactoryManufacturer = threadFactoryManufacturer;
	}

	/**
	 * Creates the {@link RawExecutiveMetaData}.
	 *
	 * @param <XS>            {@link ExecutiveSource} type.
	 * @param configuration   {@link ExecutiveConfiguration}.
	 * @param officeFloorName Name of the {@link OfficeFloor}.
	 * @param issues          {@link OfficeFloorIssues}.
	 * @return {@link RawExecutiveMetaData} or <code>null</code> if fails to
	 *         construct.
	 */
	public <XS extends ExecutiveSource> RawExecutiveMetaData constructRawExecutiveMetaData(
			ExecutiveConfiguration<XS> configuration, String officeFloorName, OfficeFloorIssues issues) {

		// Obtain the executive name
		final String EXECUTIVE_NAME = ExecutiveSourceContextImpl.EXECUTIVE_NAME;

		// Obtain the executive source
		XS executiveSource = configuration.getExecutiveSource();
		if (executiveSource == null) {
			Class<XS> executiveSourceClass = configuration.getExecutiveSourceClass();
			if (executiveSourceClass == null) {
				issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME, "No ExecutiveSource class provided");
				return null; // can not carry on
			}

			// Instantiate the executive source
			executiveSource = ConstructUtil.newInstance(executiveSourceClass, ExecutiveSource.class, "Executive Source",
					AssetType.EXECUTIVE, EXECUTIVE_NAME, issues);
			if (executiveSource == null) {
				return null; // can not carry on
			}
		}

		Executive executive;
		Map<String, ThreadFactory[]> strategies = new HashMap<>();
		TeamOversight teamOversight;
		try {
			// Create the team source context
			SourceProperties properties = configuration.getProperties();
			ExecutiveSourceContextImpl context = new ExecutiveSourceContextImpl(false, this.sourceContext, properties,
					this.threadFactoryManufacturer);

			// Create the executive
			executive = executiveSource.createExecutive(context);
			if (executive == null) {
				// Indicate failed to provide executive
				issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME, ExecutiveSource.class.getSimpleName()
						+ " failed to provide " + Executive.class.getSimpleName());
				return null; // can not carry on
			}

			// Obtain the execution strategies
			ExecutionStrategy[] executionStrategies = executive.getExcutionStrategies();
			if ((executionStrategies == null) || (executionStrategies.length == 0)) {
				// Must have at least one execution strategy
				issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
						"Must have at least one " + ExecutionStrategy.class.getSimpleName());
				return null; // can not carry on
			}

			// Load the execution strategies
			for (int i = 0; i < executionStrategies.length; i++) {
				ExecutionStrategy executionStrategy = executionStrategies[i];

				// Ensure have execution strategy
				if (executionStrategy == null) {
					issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
							"Null " + ExecutionStrategy.class.getSimpleName() + " provided for index " + i);
					return null; // can not carry on
				}

				// Ensure have name
				String executionStrategyName = executionStrategy.getExecutionStrategyName();
				if ((executionStrategyName == null) || (executionStrategyName.trim().length() == 0)) {
					// Must have name
					issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
							ExecutionStrategy.class.getSimpleName() + " for index " + i + " did not provide a name");
					return null; // can not carry on
				}

				// Ensure not duplicate name
				if (strategies.containsKey(executionStrategyName)) {
					// Duplicate name
					issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
							"One or more " + ExecutionStrategy.class.getSimpleName()
									+ " instances provided by the same name '" + executionStrategyName + "'");
					return null; // can not carry on
				}

				// Obtain the thread factories for the execution strategy
				ThreadFactory[] threadFactories = executionStrategy.getThreadFactories();
				if ((threadFactories == null) || (threadFactories.length == 0)) {
					// Must have at least one thread factory
					issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
							ExecutionStrategy.class.getSimpleName() + " '" + executionStrategyName
									+ "' must provide at least one " + ThreadFactory.class.getSimpleName());
					return null; // can not carry on
				}

				// Map in the execution strategy
				strategies.put(executionStrategyName, threadFactories);
			}

			// Obtain the team oversight
			teamOversight = executive.getTeamOversight();

		} catch (AbstractSourceError ex) {
			ex.addIssue(new OfficeFloorIssueTarget(issues, AssetType.EXECUTIVE, EXECUTIVE_NAME));
			return null; // can not carry on

		} catch (Throwable ex) {
			// Indicate failure to initialise
			issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME, "Failed to create " + Executive.class.getSimpleName(),
					ex);
			return null; // can not carry on
		}

		// Return the executive meta-data
		return new RawExecutiveMetaData(executive, strategies, teamOversight);
	}

}
