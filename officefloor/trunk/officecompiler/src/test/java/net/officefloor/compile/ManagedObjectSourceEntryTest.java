/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.BuilderFactory;
import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.officefloor.FlowTaskToOfficeTaskModel;
import net.officefloor.model.officefloor.LinkProcessToOfficeTaskModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerInstanceModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerLinkProcessModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerModel;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.ManagedObjectSourceToOfficeFloorOfficeModel;
import net.officefloor.model.officefloor.ManagedObjectTaskFlowModel;
import net.officefloor.model.officefloor.ManagedObjectTaskModel;
import net.officefloor.model.officefloor.ManagedObjectTeamModel;
import net.officefloor.model.officefloor.ManagedObjectTeamToTeamModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeTaskModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.officefloor.TeamModel;

import org.easymock.internal.AlwaysMatcher;

/**
 * Tests the {@link ManagedObjectSourceEntry}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceEntryTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link BuilderFactory}.
	 */
	private BuilderFactory builderFactory = this
			.createMock(BuilderFactory.class);

	/**
	 * Mock {@link ManagedObjectBuilder}.
	 */
	@SuppressWarnings("unchecked")
	private ManagedObjectBuilder<Indexed> managedObjectBuilder = this
			.createMock(ManagedObjectBuilder.class);

	/**
	 * Mock {@link OfficeBuilder}.
	 */
	private OfficeBuilder officeBuilder = this.createMock(OfficeBuilder.class);

	/**
	 * Mock {@link OfficeFloorBuilder}.
	 */
	private OfficeFloorBuilder officeFloorBuilder = this
			.createMock(OfficeFloorBuilder.class);

	/**
	 * Mock {@link OfficeEnhancerContext}.
	 */
	private OfficeEnhancerContext officeEnhancerContext = this
			.createMock(OfficeEnhancerContext.class);

	/**
	 * Mock {@link ManagedObjectHandlerBuilder}.
	 */
	@SuppressWarnings("unchecked")
	private ManagedObjectHandlerBuilder<MockHandlerKey> managedObjectHandlerBuilder = this
			.createMock(ManagedObjectHandlerBuilder.class);

	/**
	 * Mock {@link HandlerBuilder}.
	 */
	@SuppressWarnings("unchecked")
	private HandlerBuilder<Indexed> handlerBuilder = this
			.createMock(HandlerBuilder.class);

	/**
	 * Mock {@link FlowNodeBuilder}.
	 */
	private FlowNodeBuilder<?> flowNodeBuilder = this
			.createMock(FlowNodeBuilder.class);

	/**
	 * {@link OfficeFloorEntry}.
	 */
	private OfficeFloorEntry officeFloorEntry;

	/**
	 * Managing {@link OfficeEntry}.
	 */
	private OfficeEntry managingOfficeEntry;

	/**
	 * {@link ManagedObjectSourceModel}.
	 */
	private ManagedObjectSourceModel managedObjectSourceModel;

	/**
	 * {@link LoaderContext}.
	 */
	private LoaderContext loaderContext;

	/**
	 * {@link OfficeFloorCompilerContext}.
	 */
	private OfficeFloorCompilerContext compilerContext;

	/**
	 * Default timeout.
	 */
	private long defaultTimeout = 0;

	/**
	 * {@link PropertyModel} instances.
	 */
	private List<PropertyModel> properties = new LinkedList<PropertyModel>();

	/**
	 * {@link ManagedObjectTeamModel} instances.
	 */
	private List<ManagedObjectTeamModel> teams = new LinkedList<ManagedObjectTeamModel>();

	/**
	 * {@link ManagedObjectHandlerModel} instances.
	 */
	private List<ManagedObjectHandlerModel> handlers = new LinkedList<ManagedObjectHandlerModel>();

	/**
	 * {@link ManagedObjectTaskModel} instances.
	 */
	private List<ManagedObjectTaskModel> tasks = new LinkedList<ManagedObjectTaskModel>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {

		// Create the office floor entry
		this.officeFloorEntry = new OfficeFloorEntry("OFFICE_FLOOR",
				this.officeFloorBuilder, null);

		// Create the managing office
		OfficeFloorOfficeModel managingOfficeFloorOffice = new OfficeFloorOfficeModel(
				"OFFICE_ID", "OFFICE", null, null, null, null);
		OfficeModel managingOffice = new OfficeModel();
		this.managingOfficeEntry = new OfficeEntry("OFFICE_ID",
				this.officeBuilder, managingOffice, this.officeFloorEntry);
		this.officeFloorEntry.officeMap.put(managingOfficeFloorOffice,
				this.managingOfficeEntry);

		// Create the managed object source model
		this.managedObjectSourceModel = new ManagedObjectSourceModel();
		this.managedObjectSourceModel.setId("MANAGED_OBJECT_SOURCE_ID");
		this.managedObjectSourceModel.setSource(MockManagedObjectSource.class
				.getName());
		this.managedObjectSourceModel
				.setManagingOffice(new ManagedObjectSourceToOfficeFloorOfficeModel(
						"OFFICE", this.managedObjectSourceModel,
						managingOfficeFloorOffice));

		// Create the office floor compiler context
		this.loaderContext = new LoaderContext(this.getClass().getClassLoader());
		this.compilerContext = new OfficeFloorCompilerContext(null, null,
				this.builderFactory, this.loaderContext);
	}

	/**
	 * Ensures able to build a simple {@link ManagedObjectSource}.
	 */
	public void testSimpleBuild() throws Exception {
		this.doTest();
	}

	/**
	 * Ensures loads default timeout.
	 */
	public void testDefaultTimeoutBuild() throws Exception {
		this.setDefaultTimeout(10);
		this.doTest();
	}

	/**
	 * Ensures load properties.
	 */
	public void testPropertiesBuild() throws Exception {
		this.addProperty("NAME", "VALUE");
		this.doTest();
	}

	/**
	 * Ensures load teams.
	 */
	public void testTeamsBuild() throws Exception {
		this.addTeam("TEAM_NAME", "TEAM_ID");
		this.doTest();
	}

	/**
	 * Ensures load handlers.
	 */
	public void testHandlersBuild() throws Exception {
		this.addHandler(MockHandlerKey.KEY_ONE, "0", "WORK", "TASK");
		this.doTest();
	}

	/**
	 * Ensures load tasks.
	 */
	public void testTasksBuild() throws Exception {
		this.addTask("MO_WORK", "MO_TASK", "0", "OT_WORK", "OT_TASK");
		this.doTest();
	}

	/**
	 * Ensures able to build a {@link ManagedObjectSource} that has all details.
	 */
	public void testComplexBuild() throws Exception {
		this.setDefaultTimeout(10);
		this.addProperty("NAME", "VALUE");
		this.addTeam("TEAM_NAME", "TEAM_ID");
		this.addHandler(MockHandlerKey.KEY_ONE, "0", "WORK", "TASK");
		this.addTask("MO_WORK", "MO_TASK", "0", "OT_WORK", "OT_TASK");
		this.doTest();
	}

	/**
	 * Specifies the default timeout.
	 * 
	 * @param defaultTimeout
	 *            Default timeout.
	 */
	private void setDefaultTimeout(long defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
	}

	/**
	 * Adds a {@link PropertyModel}.
	 * 
	 * @param name
	 *            Name of {@link PropertyModel}.
	 * @param value
	 *            Value of {@link PropertyModel}.
	 */
	private void addProperty(String name, String value) {
		this.properties.add(new PropertyModel(name, value));
	}

	/**
	 * Adds a {@link ManagedObjectTeamModel}.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @param teamId
	 *            Id of the {@link Team}.
	 */
	private void addTeam(String teamName, String teamId) {
		// Create the configuration
		TeamModel team = new TeamModel();
		team.setId(teamId);
		ManagedObjectTeamToTeamModel conn = new ManagedObjectTeamToTeamModel();
		conn.setTeam(team);
		ManagedObjectTeamModel moTeam = new ManagedObjectTeamModel();
		moTeam.setTeamName(teamName);
		moTeam.setTeam(conn);

		// Add for testing
		this.teams.add(moTeam);
	}

	/**
	 * Adds a {@link ManagedObjectHandlerModel}.
	 * 
	 * @param handlerKey
	 *            Key for the {@link ManagedObjectHandlerModel}.
	 * @param linkProcessId
	 *            Link process Id. May be <code>null</code>.
	 * @param workName
	 *            Name of the linked {@link Work}.
	 * @param taskName
	 *            Name of the linked {@link Task}.
	 */
	public void addHandler(MockHandlerKey handlerKey, String linkProcessId,
			String workName, String taskName) {
		// Create the configuration
		ManagedObjectHandlerModel handler = new ManagedObjectHandlerModel();
		handler.setHandlerKey(handlerKey.name());
		handler.setHandlerKeyClass(MockHandlerKey.class.getName());
		if (linkProcessId != null) {
			ManagedObjectHandlerInstanceModel instance = new ManagedObjectHandlerInstanceModel();
			handler.setHandlerInstance(instance);
			ManagedObjectHandlerLinkProcessModel linkProcess = new ManagedObjectHandlerLinkProcessModel();
			instance.addLinkProcess(linkProcess);
			linkProcess.setLinkProcessId(linkProcessId);
			LinkProcessToOfficeTaskModel conn = new LinkProcessToOfficeTaskModel();
			linkProcess.setOfficeTask(conn);
			OfficeTaskModel officeTask = new OfficeTaskModel();
			conn.setOfficeTask(officeTask);
			officeTask.setWorkName(workName);
			officeTask.setTaskName(taskName);
		}

		// Add for testing
		this.handlers.add(handler);
	}

	/**
	 * Adds a {@link ManagedObjectTaskModel}.
	 * 
	 * @param managedObjectWorkName
	 *            {@link ManagedObjectTaskModel} work name.
	 * @param managedObjectTaskName
	 *            {@link ManagedObjectTaskModel} task name.
	 * @param flowId
	 *            Flow Id.
	 * @param officeTaskWorkName
	 *            {@link OfficeTaskModel} work name.
	 * @param officeTaskTaskName
	 *            {@link OfficeTaskModel} task name.
	 */
	private void addTask(String managedObjectWorkName,
			String managedObjectTaskName, String flowId,
			String officeTaskWorkName, String officeTaskTaskName) {
		// Create the configuration
		ManagedObjectTaskModel task = new ManagedObjectTaskModel();
		task.setWorkName(managedObjectWorkName);
		task.setTaskName(managedObjectTaskName);
		ManagedObjectTaskFlowModel flow = new ManagedObjectTaskFlowModel();
		task.addFlow(flow);
		FlowTaskToOfficeTaskModel link = new FlowTaskToOfficeTaskModel();
		flow.setOfficeTask(link);
		flow.setFlowId(flowId);
		OfficeTaskModel officeTask = new OfficeTaskModel();
		link.setOfficeTask(officeTask);
		officeTask.setWorkName(officeTaskWorkName);
		officeTask.setTaskName(officeTaskTaskName);

		// Add for testing
		this.tasks.add(task);
	}

	/**
	 * Does the test.
	 */
	private void doTest() throws Exception {

		// Record managed objects
		this.builderFactory
				.createManagedObjectBuilder(MockManagedObjectSource.class);
		this.control(this.builderFactory).setReturnValue(
				this.managedObjectBuilder);
		this.managedObjectBuilder.setManagingOffice("OFFICE");

		// Handle default timeout
		if (defaultTimeout > 0) {
			// Adjust model and record resulting actions
			this.managedObjectSourceModel.setDefaultTimeout(String
					.valueOf(defaultTimeout));
			this.managedObjectBuilder.setDefaultTimeout(defaultTimeout);
		}

		// Handle properties
		for (PropertyModel property : this.properties) {
			this.managedObjectSourceModel.addProperty(property);
			this.managedObjectBuilder.addProperty(property.getName(), property
					.getValue());
		}

		// Handle teams
		for (ManagedObjectTeamModel team : teams) {
			this.managedObjectSourceModel.addTeam(team);
			this.officeBuilder.registerTeam("MANAGED_OBJECT_SOURCE_ID."
					+ team.getTeamName(), team.getTeam().getTeam().getId());
		}

		// Enhance the office (capture the enhancer)
		final OfficeEnhancer[] officeEnhancer = new OfficeEnhancer[1];
		this.officeBuilder.addOfficeEnhancer(null);
		this.control(this.officeBuilder).setMatcher(new AlwaysMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				officeEnhancer[0] = (OfficeEnhancer) actual[0];
				return true;
			}
		});

		// Add the managed object to the office floor
		this.officeFloorBuilder.addManagedObject("MANAGED_OBJECT_SOURCE_ID",
				this.managedObjectBuilder);

		// Interaction with office enhancement (occurs later)

		// Add the handlers via office enhancement
		for (ManagedObjectHandlerModel handler : this.handlers) {
			this.managedObjectSourceModel.addHandler(handler);
			this.officeEnhancerContext.getManagedObjectHandlerBuilder(
					"MANAGED_OBJECT_SOURCE_ID", MockHandlerKey.class);
			this.control(this.officeEnhancerContext).setReturnValue(
					this.managedObjectHandlerBuilder);
			MockHandlerKey handlerKey = MockHandlerKey.valueOf(handler
					.getHandlerKey());
			this.managedObjectHandlerBuilder.registerHandler(handlerKey);
			this.control(this.managedObjectHandlerBuilder).setReturnValue(
					this.handlerBuilder);
			ManagedObjectHandlerInstanceModel instance = handler
					.getHandlerInstance();
			if (instance != null) {
				ManagedObjectHandlerLinkProcessModel linkProcess = instance
						.getLinkProcesses().get(0);
				int processIndex = Integer.parseInt(linkProcess
						.getLinkProcessId());
				OfficeTaskModel officeTask = linkProcess.getOfficeTask()
						.getOfficeTask();
				this.handlerBuilder.linkProcess(processIndex, officeTask
						.getWorkName(), officeTask.getTaskName());
			}
		}

		// Add the teams via office enhancement
		for (ManagedObjectTaskModel task : this.tasks) {
			this.managedObjectSourceModel.addTask(task);
			this.officeEnhancerContext.getFlowNodeBuilder(
					"MANAGED_OBJECT_SOURCE_ID", task.getWorkName(), task
							.getTaskName());
			this.control(this.officeEnhancerContext).setReturnValue(
					this.flowNodeBuilder);
			ManagedObjectTaskFlowModel flow = task.getFlows().get(0);
			int flowIndex = Integer.parseInt(flow.getFlowId());
			OfficeTaskModel officeTask = flow.getOfficeTask().getOfficeTask();
			this.flowNodeBuilder.linkFlow(flowIndex, officeTask.getWorkName(),
					officeTask.getTaskName(),
					FlowInstigationStrategyEnum.SEQUENTIAL);
		}

		// Replay mock objects
		this.replayMockObjects();

		// Load and build the managed object source entry
		ManagedObjectSourceEntry mosEntry = ManagedObjectSourceEntry
				.loadManagedObjectSource(this.managedObjectSourceModel,
						this.officeFloorEntry, this.compilerContext);
		mosEntry.build(this.loaderContext);

		// Enhance the office
		officeEnhancer[0].enhanceOffice(this.officeEnhancerContext);

		// Verify functionality
		this.verifyMockObjects();
	}

}
