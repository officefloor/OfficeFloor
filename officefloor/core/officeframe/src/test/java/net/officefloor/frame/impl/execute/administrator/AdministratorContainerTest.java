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
package net.officefloor.frame.impl.execute.administrator;

import java.util.List;

import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyContext;
import net.officefloor.frame.spi.administration.DutyKey;
import net.officefloor.frame.spi.administration.GovernanceManager;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AdministratorContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorContainerTest<I, A extends Enum<A>, F extends Enum<F>, G extends Enum<G>>
		extends OfficeFrameTestCase {

	/**
	 * {@link AdministratorMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private final AdministratorMetaData<I, A> metaData = this
			.createMock(AdministratorMetaData.class);

	/**
	 * {@link AdministratorContainer}.
	 */
	private final AdministratorContainer<I, A> container = new AdministratorContainerImpl<I, A, F, G>(
			this.metaData);

	/**
	 * {@link TaskDutyAssociation}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskDutyAssociation<A> taskDuty = this
			.createMock(TaskDutyAssociation.class);

	/**
	 * Extension interfaces.
	 */
	@SuppressWarnings("unchecked")
	private final List<I> extensionInterfaces = this.createMock(List.class);

	/**
	 * {@link AdministratorContext}.
	 */
	private final AdministratorContext context = this
			.createMock(AdministratorContext.class);

	/**
	 * {@link AdministratorSource}.
	 */
	@SuppressWarnings("unchecked")
	private final AdministratorSource<I, A> administratorSource = this
			.createMock(AdministratorSource.class);

	/**
	 * {@link Administrator}.
	 */
	@SuppressWarnings("unchecked")
	private final Administrator<I, A> administrator = this
			.createMock(Administrator.class);

	/**
	 * {@link DutyKey}.
	 */
	@SuppressWarnings("unchecked")
	private final DutyKey<A> dutyKey = this.createMock(DutyKey.class);

	/**
	 * {@link DutyMetaData}.
	 */
	private final DutyMetaData dutyMetaData = this
			.createMock(DutyMetaData.class);

	/**
	 * {@link ContainerContext}.
	 */
	private final ContainerContext containerContext = this
			.createMock(ContainerContext.class);

	/**
	 * Ensure can obtain the extension interfaces.
	 */
	public void testExtensionInterfaces() throws Throwable {

		// Duty to validate context
		this.recordDoDuty(new Duty<I, F, G>() {
			@Override
			public void doDuty(DutyContext<I, F, G> context) throws Throwable {
				assertEquals("Incorrect extension interfaces",
						AdministratorContainerTest.this.extensionInterfaces,
						context.getExtensionInterfaces());
			}
		});

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to invoke {@link JobSequence}.
	 */
	public void testFlow() throws Throwable {

		final FlowMetaData<?> flowMetaData = this
				.createMock(FlowMetaData.class);

		// Duty to invoke flow
		this.recordDoDuty(new Duty<I, Flows, G>() {
			@Override
			public void doDuty(DutyContext<I, Flows, G> context)
					throws Throwable {
				context.doFlow(Flows.FLOW, "TEST");
			}
		});

		// Record invoking flow
		this.recordReturn(this.dutyMetaData,
				this.dutyMetaData.getFlow(Flows.FLOW.ordinal()), flowMetaData);
		this.context.doFlow(flowMetaData, "TEST");

		// Test
		this.doTest();
	}

	/**
	 * {@link JobSequence} keys for testing.
	 */
	private static enum Flows {
		FLOW
	}

	/**
	 * Ensure able to activate {@link Governance}.
	 */
	public void testGovernance_Activate() throws Throwable {

		// Record activating the governance
		GovernanceContainer<?, ?> governanceContainer = this
				.recordGovernance(new Duty<I, F, Governances>() {
					@Override
					public void doDuty(DutyContext<I, F, Governances> context)
							throws Throwable {
						GovernanceManager manager = context
								.getGovernance(Governances.GOVERNANCE);
						manager.activateGovernance();
					}
				});
		governanceContainer.activateGovernance(this.containerContext);

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to enforce {@link Governance}.
	 */
	public void testGovernance_Enforce() throws Throwable {

		// Record enforcing the governance
		GovernanceContainer<?, ?> governanceContainer = this
				.recordGovernance(new Duty<I, F, Governances>() {
					@Override
					public void doDuty(DutyContext<I, F, Governances> context)
							throws Throwable {
						GovernanceManager manager = context
								.getGovernance(Governances.GOVERNANCE);
						manager.enforceGovernance();
					}
				});
		governanceContainer.enforceGovernance(this.containerContext);

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to disregard {@link Governance}.
	 */
	public void testGovernance_Disregard() throws Throwable {

		// Record disregarding the governance
		GovernanceContainer<?, ?> governanceContainer = this
				.recordGovernance(new Duty<I, F, Governances>() {
					@Override
					public void doDuty(DutyContext<I, F, Governances> context)
							throws Throwable {
						GovernanceManager manager = context
								.getGovernance(Governances.GOVERNANCE);
						manager.disregardGovernance();
					}
				});
		governanceContainer.disregardGovernance(this.containerContext);

		// Test
		this.doTest();
	}

	/**
	 * {@link Governance} keys for testing.
	 */
	private static enum Governances {
		GOVERNANCE
	}

	/**
	 * Records setup of {@link Governance}.
	 * 
	 * @param duty
	 *            {@link Duty}.
	 * @return {@link GovernanceContainer}.
	 */
	private GovernanceContainer<?, ?> recordGovernance(
			Duty<?, ?, Governances> duty) throws Throwable {

		final int THREAD_INDEX = 3;
		final ThreadState threadState = this.createMock(ThreadState.class);
		final GovernanceContainer<?, ?> governanceContainer = this
				.createMock(GovernanceContainer.class);

		// Duty to validate context
		this.recordDoDuty(duty);

		// Record governance setup
		this.recordReturn(this.dutyMetaData, this.dutyMetaData
				.translateGovernanceIndexToThreadIndex(Governances.GOVERNANCE
						.ordinal()), THREAD_INDEX);
		this.recordReturn(this.context, this.context.getThreadState(),
				threadState);
		this.recordReturn(threadState,
				threadState.getGovernanceContainer(THREAD_INDEX),
				governanceContainer);

		// Return the governance container
		return governanceContainer;
	}

	/**
	 * Records undertaking the {@link Duty}.
	 * 
	 * @param duty
	 *            {@link Duty} to execute.
	 */
	private void recordDoDuty(Duty<?, ?, ?> duty) throws Throwable {

		// Record undertaking the duty
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorSource(),
				this.administratorSource);
		this.recordReturn(this.administratorSource,
				this.administratorSource.createAdministrator(),
				this.administrator);
		this.recordReturn(this.taskDuty, taskDuty.getDutyKey(), this.dutyKey);
		this.recordReturn(this.administrator,
				this.administrator.getDuty(this.dutyKey), duty);
		this.recordReturn(this.metaData,
				this.metaData.getDutyMetaData(this.dutyKey), this.dutyMetaData);
	}

	/**
	 * Undertakes the test.
	 */
	private void doTest() throws Throwable {
		// Test
		this.replayMockObjects();
		this.container.doDuty(this.taskDuty, this.extensionInterfaces,
				this.context, this.containerContext);
		this.verifyMockObjects();
	}

}