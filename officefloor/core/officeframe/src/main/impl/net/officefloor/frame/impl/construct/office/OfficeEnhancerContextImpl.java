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
package net.officefloor.frame.impl.construct.office;

import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.construct.RawOfficeFloorMetaData;

/**
 * {@link OfficeEnhancerContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeEnhancerContextImpl implements OfficeEnhancerContext {

	/**
	 * Enhances the {@link OfficeConfiguration}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param officeConfiguration
	 *            {@link OfficeConfiguration}.
	 * @param issues
	 *            {@link OfficeFloorIssues} to provide issues in enhancing the
	 *            {@link OfficeConfiguration}.
	 */
	public static void enhanceOffice(String officeName, OfficeConfiguration officeConfiguration,
			OfficeFloorIssues issues) {

		// Create the context to enhance
		OfficeEnhancerContext context = new OfficeEnhancerContextImpl(officeConfiguration);

		// Enhance the office
		for (OfficeEnhancer enhancer : officeConfiguration.getOfficeEnhancers()) {
			try {
				enhancer.enhanceOffice(context);
			} catch (OfficeEnhancerError ex) {
				issues.addIssue(AssetType.OFFICE, officeName, ex.getMessage());
			} catch (Throwable ex) {
				issues.addIssue(AssetType.OFFICE, officeName, "Failure in enhancing office", ex);
			}
		}
	}

	/**
	 * {@link OfficeConfiguration}.
	 */
	private final OfficeConfiguration officeConfiguration;

	/**
	 * Initiate.
	 * 
	 * @param officeConfiguration
	 *            {@link OfficeConfiguration}.
	 * @param rawOfficeFloorMetaData
	 *            {@link RawOfficeFloorMetaData}.
	 */
	private OfficeEnhancerContextImpl(OfficeConfiguration officeConfiguration) {
		this.officeConfiguration = officeConfiguration;
	}

	/*
	 * =================== OfficeEnhancerContext ===========================
	 */

	@Override
	public FlowNodeBuilder<?> getFlowNodeBuilder(String functionName) {
		return this.getFlowNodeBuilder(null, functionName);
	}

	@Override
	public FlowNodeBuilder<?> getFlowNodeBuilder(String namespace, String functionName) {
		FlowNodeBuilder<?> flowBuilder = this.officeConfiguration.getFlowNodeBuilder(namespace, functionName);
		if (flowBuilder == null) {
			throw new OfficeEnhancerError(
					ManagedFunction.class.getSimpleName() + " '" + functionName + "' of namespace '"
							+ (namespace == null ? "[none]" : namespace) + "' not available for enhancement");
		}
		return flowBuilder;
	}

	/**
	 * Used to propagate errors in {@link OfficeEnhancer}.
	 */
	private static class OfficeEnhancerError extends Error {

		/**
		 * Initiate.
		 * 
		 * @param message
		 *            Message.
		 */
		public OfficeEnhancerError(String message) {
			super(message);
		}
	}

}