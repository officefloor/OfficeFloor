package net.officefloor.mocks;

import java.util.HashMap;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.TeamOverloadException;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.impl.execute.asset.MonitorClockImpl;
import net.officefloor.frame.impl.execute.asset.OfficeManagerHirerImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.impl.execute.job.FunctionLoopImpl;
import net.officefloor.frame.impl.execute.office.OfficeMetaDataImpl;
import net.officefloor.frame.impl.execute.process.ProcessMetaDataImpl;
import net.officefloor.frame.impl.execute.process.ProcessStateImpl;
import net.officefloor.frame.impl.execute.thread.ThreadMetaDataImpl;
import net.officefloor.frame.internal.structure.AssetManagerHirer;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupFunction;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadMetaData;

/**
 * Provides creation of mock objects.
 * 
 * @author Daniel Sagenschneider
 */
public class MockUtil {

	/**
	 * {@link ProcessIdentifier}.
	 */
	public static final ProcessIdentifier PROCESS_IDENTIFIER = new ProcessIdentifier() {
	};

	/**
	 * {@link ThreadMetaData}.
	 */
	private static final ThreadMetaData THREAD_META_DATA = new ThreadMetaDataImpl(new ManagedObjectMetaData[0],
			new GovernanceMetaData[0], 1000, new ThreadSynchroniserFactory[0], new EscalationProcedureImpl(), null);

	/**
	 * {@link ProcessMetaData}.
	 */
	private static final ProcessMetaData PROCESS_META_DATA = new ProcessMetaDataImpl(new ManagedObjectMetaData[0],
			THREAD_META_DATA);

	/**
	 * Default {@link Team}.
	 */
	private static final Team TEAM = new Team() {

		@Override
		public void startWorking() {
			// Nothing to start
		}

		@Override
		public void assignJob(Job job) throws TeamOverloadException, Exception {
			// Run immediately
			job.run();
		}

		@Override
		public void stopWorking() {
			// Nothing to stop
		}
	};

	/**
	 * Default {@link TeamManagement}
	 */
	private static TeamManagement TEAM_MANAGEMENT = new TeamManagement() {

		@Override
		public Object getIdentifier() {
			return PROCESS_IDENTIFIER;
		}

		@Override
		public Team getTeam() {
			return TEAM;
		}
	};

	/**
	 * Creates the {@link OfficeMetaData}.
	 * 
	 * @param executive {@link Executive}.
	 * @return Mock {@link OfficeMetaData}.
	 */
	public static OfficeMetaData createOfficeMetaData(Executive executive) {
		MonitorClockImpl monitorClock = new MonitorClockImpl();
		FunctionLoop functionLoop = new FunctionLoopImpl(TEAM_MANAGEMENT);
		OfficeManagerHirerImpl officeManagerHirer = new OfficeManagerHirerImpl(monitorClock, 1000L, functionLoop);
		officeManagerHirer.setAssetManagerHirers(new AssetManagerHirer[0]);
		ManagedExecutionFactory managedExecutionFactory = new ManagedExecutionFactoryImpl(
				new ThreadCompletionListener[0]);
		ManagedFunctionLocator functionLocator = (functionName) -> null;
		return new OfficeMetaDataImpl("MOCK", officeManagerHirer, monitorClock, functionLoop, null, executive,
				managedExecutionFactory, new ManagedFunctionMetaData[0], functionLocator, PROCESS_META_DATA, null,
				new HashMap<>(), new OfficeStartupFunction[0], null);
	}

	/**
	 * Creates mock {@link ProcessState}.
	 * 
	 * @return Mock {@link ProcessState}.
	 */
	public static ProcessState createProcessState(Executive executive) {
		return new ProcessStateImpl(PROCESS_META_DATA, createOfficeMetaData(executive), null, null, null, null, null);
	}

	/**
	 * Creates mock {@link ProcessState}.
	 * 
	 * @return Mock {@link ProcessState}.
	 */
	public static ProcessState createProcessState() {
		return createProcessState(new DefaultExecutive());
	}

	/**
	 * All access via static methods.
	 */
	private MockUtil() {
	}

}