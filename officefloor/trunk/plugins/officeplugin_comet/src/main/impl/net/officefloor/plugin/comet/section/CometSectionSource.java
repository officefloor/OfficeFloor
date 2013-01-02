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
package net.officefloor.plugin.comet.section;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.plugin.comet.internal.CometEvent;
import net.officefloor.plugin.comet.internal.CometPublicationService;
import net.officefloor.plugin.comet.internal.CometSubscriptionService;
import net.officefloor.plugin.comet.section.PublishWorkSource.Dependencies;
import net.officefloor.plugin.comet.spi.CometRequestServicer;
import net.officefloor.plugin.comet.spi.CometService;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.work.clazz.ClassWorkSource;

/**
 * {@link SectionSource} for servicing {@link CometSubscriptionService} and
 * {@link CometPublicationService}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometSectionSource extends AbstractSectionSource {

	/**
	 * Name of {@link SectionInput} for handling
	 * {@link CometSubscriptionService}.
	 */
	public static final String SUBSCRIBE_INPUT_NAME = "SUBSCRIBE";

	/**
	 * Name of {@link SectionInput} to handle {@link CometPublicationService}.
	 */
	public static final String PUBLISH_INPUT_NAME = "PUBLISH";

	/**
	 * Prefix on the {@link SectionOutput} name for manually handling
	 * {@link CometPublicationService}.
	 */
	public static final String PUBLISH_OUTPUT_PREFIX = "PUBLISH_";

	/**
	 * Property name prefix on the template URI to provide manual publishing.
	 */
	public static final String PROPERTY_MANUAL_PUBLISH_URI_PREFIX = "manual.publish.";

	/*
	 * ================== SectionSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	public void sourceSection(SectionDesigner designer,
			SectionSourceContext context) throws Exception {

		// Register the dependencies
		SectionObject connection = designer.addSectionObject(
				ServerGwtRpcConnection.class.getName(),
				ServerGwtRpcConnection.class.getName());
		SectionObject cometService = designer.addSectionObject(
				CometService.class.getName(), CometService.class.getName());
		SectionObject cometRequestServicer = designer.addSectionObject(
				CometRequestServicer.class.getName(),
				CometRequestServicer.class.getName());

		// Register the subscription work
		SectionWork subscribeWork = designer.addSectionWork("SUBSCRIBE",
				ClassWorkSource.class.getName());
		subscribeWork.addProperty(ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				SubscribeWork.class.getName());

		// Configure pre-subscription
		SectionTask preSubscription = subscribeWork.addSectionTask(
				"PRE_SUBSCRIBE", "preSubscribe");
		designer.link(preSubscription.getTaskObject(CometRequestServicer.class
				.getName()), cometRequestServicer);

		// Configure post-subscription (after pre-subscription)
		SectionTask postSubscription = subscribeWork.addSectionTask(
				"POST_SUBSCRIBE", "postSubscribe");
		designer.link(postSubscription.getTaskObject(CometRequestServicer.class
				.getName()), cometRequestServicer);
		designer.link(preSubscription, postSubscription);

		// Register input to service subscription
		designer.link(designer.addSectionInput(SUBSCRIBE_INPUT_NAME, null),
				preSubscription);

		// Register the publication task
		SectionWork publishWork = designer.addSectionWork("PUBLISH_WORK",
				PublishWorkSource.class.getName());
		SectionTask publishTask = publishWork.addSectionTask("PUBLISH",
				PublishWorkSource.TASK_NAME);
		designer.link(publishTask
				.getTaskObject(Dependencies.SERVER_GWT_RPC_CONNECTION.name()),
				connection);
		designer.link(
				publishTask.getTaskObject(Dependencies.COMET_SERVICE.name()),
				cometService);
		designer.link(designer.addSectionInput(PUBLISH_INPUT_NAME, null),
				publishTask);

		// Register the manual publish handling
		for (String name : context.getPropertyNames()) {
			if (name.startsWith(PROPERTY_MANUAL_PUBLISH_URI_PREFIX)) {

				// Obtain the Request URI Prefix (ensuring it has separators)
				String requestUriPrefix = context.getProperty(name).trim();

				// Obtain the output flow name
				String outputFlowName = name
						.substring(PROPERTY_MANUAL_PUBLISH_URI_PREFIX.length());

				// Register manual publish handling with work
				publishWork.addProperty(
						PublishWorkSource.PROPERTY_MANUAL_PUBLISH_URI_PREFIX
								+ outputFlowName, requestUriPrefix);

				// Link task manual handle flow to output flow
				designer.link(publishTask.getTaskFlow(outputFlowName), designer
						.addSectionOutput(PUBLISH_OUTPUT_PREFIX
								+ outputFlowName, CometEvent.class.getName(),
								false), FlowInstigationStrategyEnum.SEQUENTIAL);
			}
		}
	}

	/**
	 * {@link Work} to handle {@link CometSubscriptionService}.
	 */
	public static class SubscribeWork {

		/**
		 * Initially handles the {@link CometSubscriptionService}.
		 * 
		 * @param servicer
		 *            {@link CometRequestServicer}.
		 */
		public void preSubscribe(CometRequestServicer servicer) {
			// Service subscription (potentially triggers wait on event)
			servicer.service();
		}

		/**
		 * Invoked once a {@link CometEvent} is available for the
		 * {@link CometSubscriptionService} or timed out waiting on a
		 * {@link CometEvent}.
		 * 
		 * @param service
		 *            {@link CometService}.
		 */
		public void postSubscribe(CometRequestServicer servicer) {
			// Waits for CometEvent to be available for subscription
		}
	}

}