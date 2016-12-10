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
package net.officefloor.plugin.gwt.comet.section;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.gwt.comet.internal.CometEvent;
import net.officefloor.plugin.gwt.comet.internal.CometPublicationService;
import net.officefloor.plugin.gwt.comet.spi.CometService;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.socket.server.http.HttpRequest;

import com.google.gwt.user.server.rpc.RPCRequest;

/**
 * {@link WorkSource} to handle {@link CometPublicationService}.
 * 
 * @author Daniel Sagenschneider
 */
public class PublishWorkSource extends
		AbstractWorkSource<PublishWorkSource.PublishTask> {

	/**
	 * Property name prefix on the template URI to provide manual publishing.
	 */
	public static final String PROPERTY_MANUAL_PUBLISH_URI_PREFIX = "manual.publish.";

	/**
	 * Name of {@link Task} to handle the {@link CometPublicationService}.
	 */
	public static final String TASK_NAME = "PUBLISH";

	/**
	 * {@link PublishWorkSource} dependencies.
	 */
	public static enum Dependencies {
		SERVER_GWT_RPC_CONNECTION, COMET_SERVICE
	}

	/*
	 * ===================== WorkSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	public void sourceWork(WorkTypeBuilder<PublishTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Create the manual publish mappings
		List<String> flowLabels = new LinkedList<String>();
		Map<String, Integer> requestUriPrefixToFlowIndex = new HashMap<String, Integer>();
		for (String name : context.getPropertyNames()) {
			if (name.startsWith(PROPERTY_MANUAL_PUBLISH_URI_PREFIX)) {

				// Obtain the Request URI Prefix (ensuring it has separators)
				String requestUriPrefix = context.getProperty(name).trim();
				requestUriPrefix = requestUriPrefix.startsWith("/") ? requestUriPrefix
						: "/" + requestUriPrefix;
				requestUriPrefix = requestUriPrefix.endsWith("/") ? requestUriPrefix
						: requestUriPrefix + "/";

				// Include the manual publish handling
				String flowLabel = name
						.substring(PROPERTY_MANUAL_PUBLISH_URI_PREFIX.length());

				// Map the request URI prefix to the flow index
				requestUriPrefixToFlowIndex.put(requestUriPrefix,
						Integer.valueOf(flowLabels.size()));

				// Add the flow label
				flowLabels.add(flowLabel);
			}
		}

		// Create the factory
		PublishTask factory = new PublishTask(requestUriPrefixToFlowIndex);

		// Register the work
		workTypeBuilder.setWorkFactory(factory);

		// Register the task for work
		TaskTypeBuilder<Dependencies, Indexed> task = workTypeBuilder
				.addTaskType(TASK_NAME, factory, Dependencies.class,
						Indexed.class);

		// Register dependencies
		task.addObject(ServerGwtRpcConnection.class).setKey(
				Dependencies.SERVER_GWT_RPC_CONNECTION);
		task.addObject(CometService.class).setKey(Dependencies.COMET_SERVICE);

		// Register the flows
		for (String flowLabel : flowLabels) {
			TaskFlowTypeBuilder<Indexed> flow = task.addFlow();
			flow.setArgumentType(CometEvent.class);
			flow.setLabel(flowLabel);
		}
	}

	/**
	 * {@link Task} to handle {@link CometPublicationService}.
	 */
	public static class PublishTask extends
			AbstractSingleTask<PublishTask, Dependencies, Indexed> {

		/**
		 * Mapping of request URI prefix to flow index.
		 */
		private final Map<String, Integer> requestUriPrefixToFlowIndex;

		/**
		 * Initiate.
		 * 
		 * @param requestUriPrefixToFlowIndex
		 *            Mapping of request URI prefix to flow index.
		 */
		public PublishTask(Map<String, Integer> requestUriPrefixToFlowIndex) {
			this.requestUriPrefixToFlowIndex = requestUriPrefixToFlowIndex;
		}

		/*
		 * ================== Task ==================================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public Object doTask(
				TaskContext<PublishTask, Dependencies, Indexed> context) {

			// Obtain the RPC request
			ServerGwtRpcConnection<Long> connection = (ServerGwtRpcConnection<Long>) context
					.getObject(Dependencies.SERVER_GWT_RPC_CONNECTION);
			RPCRequest rpcRequest;
			try {
				rpcRequest = connection.getRpcRequest();
			} catch (Exception ex) {
				connection.onFailure(ex);
				return null; // failed to obtain request to service
			}

			// Obtain the Comet Event
			Object[] parameters = rpcRequest.getParameters();
			CometEvent event = (CometEvent) parameters[0];

			// Obtain the request URI prefix
			String requestUriPrefix;
			HttpRequest httpRequest = connection.getHttpRequest();
			String requestUri = httpRequest.getRequestURI();
			int separator = requestUri.indexOf('/', "/".length());
			if (separator < 0) {
				// No prefix
				requestUriPrefix = "";
			} else {
				// Extract the request URI prefix
				requestUriPrefix = requestUri.substring(0,
						separator + "/".length());
			}

			// Determine handling strategy
			Integer flowIndex = this.requestUriPrefixToFlowIndex
					.get(requestUriPrefix);
			if (flowIndex != null) {
				// Manually handle publish
				context.doFlow(flowIndex.intValue(), event);
				return null; // handled
			}

			// Not manually handled, so automatically handle
			CometService service = (CometService) context
					.getObject(Dependencies.COMET_SERVICE);
			long sequenceNumber = -1;
			try {

				// Publish the event
				sequenceNumber = service.publishEvent(
						event.getSequenceNumber(), event.getListenerTypeName(),
						event.getData(), event.getMatchKey());

			} catch (Exception ex) {
				// Failed to publish event
				connection.onFailure(ex);
				return null; // Not successful
			}

			// Notify successfully published
			connection.onSuccess(Long.valueOf(sequenceNumber));

			// Nothing to return
			return null;
		}
	}

}