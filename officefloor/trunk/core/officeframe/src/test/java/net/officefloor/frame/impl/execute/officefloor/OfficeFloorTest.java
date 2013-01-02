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
package net.officefloor.frame.impl.execute.officefloor;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.NameAwareWorkFactory;
import net.officefloor.frame.api.build.OfficeAwareWorkFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteContextFactory;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupTask;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.internal.AlwaysMatcher;

/**
 * Tests the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link OfficeFloorMetaData}.
	 */
	private final OfficeFloorMetaData officeFloorMetaData = this
			.createMock(OfficeFloorMetaData.class);

	/**
	 * {@link OfficeFloor} to test.
	 */
	private final OfficeFloor officeFloor = new OfficeFloorImpl(
			this.officeFloorMetaData);

	/**
	 * Mock {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData = this
			.createMock(OfficeMetaData.class);

	/**
	 * Mock {@link OfficeManager}.
	 */
	private final OfficeManager officeManager = this
			.createMock(OfficeManager.class);

	/**
	 * Mock {@link Team}.
	 */
	private final Team team = this.createMock(Team.class);

	/**
	 * Ensure correctly opens the {@link OfficeFloor}.
	 */
	public void testOpenOfficeFloor() throws Exception {

		// Record open
		this.recordOpeningOfficeFloor();

		// Test
		this.replayMockObjects();
		this.officeFloor.openOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure correct closes the {@link OfficeFloor}.
	 */
	public void testCloseOfficeFloor() throws Exception {

		final ManagedObjectSourceInstance<?> mosInstance = this
				.createMock(ManagedObjectSourceInstance.class);
		final ManagedObjectSource<?, ?> mos = this
				.createMock(ManagedObjectSource.class);
		final TeamManagement teamManagement = this
				.createMock(TeamManagement.class);

		// Record open (to ensure in appropriate state for close)
		this.recordOpeningOfficeFloor();

		// Record stopping the managed object sources
		this.recordReturn(this.officeFloorMetaData,
				this.officeFloorMetaData.getManagedObjectSourceInstances(),
				new ManagedObjectSourceInstance[] { mosInstance });
		this.recordReturn(mosInstance, mosInstance.getManagedObjectSource(),
				mos);
		mos.stop();

		// No processes run, so should carry on to close

		// Record closing
		this.recordReturn(this.officeFloorMetaData,
				this.officeFloorMetaData.getTeams(),
				new TeamManagement[] { teamManagement });
		this.recordReturn(teamManagement, teamManagement.getTeam(), this.team);
		this.team.stopWorking();
		this.recordReturn(this.officeFloorMetaData,
				this.officeFloorMetaData.getOfficeMetaData(),
				new OfficeMetaData[] { this.officeMetaData });
		this.recordReturn(this.officeMetaData,
				this.officeMetaData.getOfficeManager(), this.officeManager);
		this.officeManager.stopManaging();

		// Test
		this.replayMockObjects();
		this.officeFloor.openOfficeFloor();
		this.officeFloor.closeOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Records opening the {@link OfficeFloor}.
	 */
	@SuppressWarnings("unchecked")
	private void recordOpeningOfficeFloor() throws Exception {

		final WorkMetaData<?> workMetaData = this
				.createMock(WorkMetaData.class);
		final MockWorkFactory workFactory = this
				.createMock(MockWorkFactory.class);
		final ManagedObjectSourceInstance<Indexed> mosInstance = this
				.createMock(ManagedObjectSourceInstance.class);
		final ManagedObjectSource<?, Indexed> mos = this
				.createMock(ManagedObjectSource.class);
		final ManagedObjectExecuteContextFactory<Indexed> executeContextFactory = this
				.createMock(ManagedObjectExecuteContextFactory.class);
		final ManagedObjectExecuteContext<Indexed> executeContext = this
				.createMock(ManagedObjectExecuteContext.class);
		final ManagedObjectPool managedObjectPool = this
				.createMock(ManagedObjectPool.class);
		final TeamManagement teamManagement = this
				.createMock(TeamManagement.class);
		final OfficeStartupTask startupTask = this
				.createMock(OfficeStartupTask.class);
		final FlowMetaData<?> flowMetaData = this
				.createMock(FlowMetaData.class);
		final Object parameter = new Object();
		final JobNode jobNode = this.createMock(JobNode.class);

		// Record opening the OfficeFloor
		this.recordReturn(this.officeFloorMetaData,
				this.officeFloorMetaData.getOfficeMetaData(),
				new OfficeMetaData[] { this.officeMetaData });
		this.recordReturn(this.officeMetaData,
				this.officeMetaData.getOfficeName(), "OFFICE");
		this.recordReturn(this.officeMetaData,
				this.officeMetaData.getWorkMetaData(),
				new WorkMetaData<?>[] { workMetaData });
		this.recordReturn(workMetaData, workMetaData.getWorkFactory(),
				workFactory);
		this.recordReturn(workMetaData, workMetaData.getWorkName(), "WORK");
		workFactory.setBoundWorkName("WORK");
		workFactory.setOffice(null);
		this.control(workFactory).setMatcher(new AlwaysMatcher());
		this.recordReturn(this.officeFloorMetaData,
				this.officeFloorMetaData.getManagedObjectSourceInstances(),
				new ManagedObjectSourceInstance[] { mosInstance });
		this.recordReturn(mosInstance, mosInstance.getManagedObjectSource(),
				mos);
		this.recordReturn(mosInstance,
				mosInstance.getManagedObjectExecuteContextFactory(),
				executeContextFactory);
		this.recordReturn(executeContextFactory, executeContextFactory
				.createManagedObjectExecuteContext(null, null), executeContext,
				new AlwaysMatcher());
		mos.start(executeContext);
		this.recordReturn(mosInstance, mosInstance.getManagedObjectPool(),
				managedObjectPool);
		managedObjectPool.init(null);
		this.control(managedObjectPool).setMatcher(new AlwaysMatcher());
		this.recordReturn(this.officeMetaData,
				this.officeMetaData.getOfficeManager(), this.officeManager);
		this.officeManager.startManaging();
		this.recordReturn(this.officeFloorMetaData,
				this.officeFloorMetaData.getTeams(),
				new TeamManagement[] { teamManagement });
		this.recordReturn(teamManagement, teamManagement.getTeam(), this.team);
		this.team.startWorking();
		this.recordReturn(this.officeMetaData,
				this.officeMetaData.getStartupTasks(),
				new OfficeStartupTask[] { startupTask });
		this.recordReturn(startupTask, startupTask.getFlowMetaData(),
				flowMetaData);
		this.recordReturn(startupTask, startupTask.getParameter(), parameter);
		this.recordReturn(this.officeMetaData,
				this.officeMetaData.createProcess(flowMetaData, parameter),
				jobNode);
		jobNode.activateJob(OfficeFloorImpl.STARTUP_TEAM);
	}

	/**
	 * {@link WorkFactory} for testing ensure appropriate notification at open.
	 */
	private static interface MockWorkFactory extends
			NameAwareWorkFactory<Work>, OfficeAwareWorkFactory<Work> {
	}

}