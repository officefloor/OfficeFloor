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
package net.officefloor.building.process.officefloor;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.api.manage.WorkManager;

/**
 * {@link ManagedProcess} for an {@link OfficeFloor} {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManager implements ManagedProcess,
		OfficeFloorManagerMBean {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(OfficeFloorManager.class.getName());

	/**
	 * {@link ObjectName} for the {@link OfficeFloorManager}.
	 */
	static ObjectName OFFICE_FLOOR_MANAGER_OBJECT_NAME;

	static {
		try {
			OFFICE_FLOOR_MANAGER_OBJECT_NAME = new ObjectName("officefloor",
					"type", "OfficeFloor");
		} catch (MalformedObjectNameException ex) {
			// This should never be the case
		}
	}

	/**
	 * Obtains the {@link OfficeFloorManagerMBean} {@link ObjectName}.
	 * 
	 * @return {@link OfficeFloorManagerMBean} {@link ObjectName}.
	 */
	public static ObjectName getOfficeFloorManagerObjectName() {
		return OFFICE_FLOOR_MANAGER_OBJECT_NAME;
	}

	/**
	 * Class name of the {@link OfficeFloorSource}.
	 */
	private final String officeFloorSourceClassName;

	/**
	 * Location of the {@link OfficeFloor} configuration.
	 */
	private final String officeFloorLocation;

	/**
	 * Properties for the {@link OfficeFloor}.
	 */
	private final Properties officeFloorProperties;

	/**
	 * Listing of the {@link WorkState}.
	 */
	private final List<WorkState> workStates = new LinkedList<WorkState>();

	/**
	 * {@link ManagedProcessContext}.
	 */
	private transient ManagedProcessContext context = null;

	/**
	 * {@link OfficeFloor} being managed within the {@link Process}.
	 */
	private transient OfficeFloor officeFloor = null;

	/**
	 * Flag indicating if the {@link OfficeFloor} is open and available to have
	 * {@link Task} instances invoked.
	 */
	private boolean isOpen = false;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorSourceClassName
	 *            Class name of the {@link OfficeFloorSource}. May be
	 *            <code>null</code> to use the default {@link OfficeFloorSource}
	 *            of the {@link OfficeFloorCompiler}.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor} configuration.
	 * @param officeFloorProperties
	 *            Properties for the {@link OfficeFloor}.
	 */
	public OfficeFloorManager(String officeFloorSourceClassName,
			String officeFloorLocation, Properties officeFloorProperties) {
		this.officeFloorSourceClassName = officeFloorSourceClassName;
		this.officeFloorLocation = officeFloorLocation;
		this.officeFloorProperties = officeFloorProperties;
	}

	/*
	 * ============= OfficeFloorManagedProcessMBean ===================
	 */

	@Override
	public String getOfficeFloorLocation() {
		return this.officeFloorLocation;
	}

	@Override
	public String listTasks() {

		OfficeFloor officeFloor;
		synchronized (this) {
			officeFloor = this.officeFloor;

			// Ensure open
			if (!this.isOpen) {
				return "OfficeFloor not open";
			}
		}

		// Create the listing of tasks
		StringBuilder tasks = new StringBuilder();
		try {
			boolean isFirst = true;
			for (String officeName : officeFloor.getOfficeNames()) {

				// Separator
				if (!isFirst) {
					tasks.append("\n");
				}
				isFirst = false;

				// Listing offices
				tasks.append(officeName);

				// List work of the office
				Office office = officeFloor.getOffice(officeName);
				for (String workName : office.getWorkNames()) {
					tasks.append("\n\t" + workName);

					// List tasks of work
					WorkManager work = office.getWorkManager(workName);
					for (String taskName : work.getTaskNames()) {
						TaskManager task = work.getTaskManager(taskName);
						Class<?> parameterType = task.getParameterType();
						tasks.append("\n\t\t"
								+ taskName
								+ " ("
								+ (parameterType == null ? "" : parameterType
										.getSimpleName() + ")"));
					}
				}
			}
		} catch (Exception ex) {
			StringWriter trace = new StringWriter();
			ex.printStackTrace(new PrintWriter(trace));
			tasks.append("\n" + trace.toString());
		}

		// Return the listing of tasks
		return tasks.toString();
	}

	@Override
	public void invokeTask(String officeName, String workName, String taskName,
			String parameter) throws Exception {

		WorkState work = null;
		OfficeFloor officeFloor;
		synchronized (this) {
			officeFloor = this.officeFloor;

			// Determine if already opened the OfficeFloor
			if (this.isOpen) {
				// OfficeFloor running so invoke immediately
				work = new WorkState(officeName, workName, taskName, parameter);

			} else {
				// Register the work to be invoked inside the main
				this.workStates.add(new WorkState(officeName, workName,
						taskName, parameter));
			}
		}

		// Invoke work if have (outside locks)
		if (work != null) {
			work.invoke(officeFloor);
		}
	}

	/*
	 * =================== ManagedProcess ============================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void init(ManagedProcessContext context) throws Throwable {

		synchronized (this) {
			this.context = context;

			// Create the OfficeFloor compiler
			OfficeFloorCompiler compiler = OfficeFloorCompiler
					.newOfficeFloorCompiler(null);

			// Add properties for the compiler
			for (String name : this.officeFloorProperties.stringPropertyNames()) {
				String value = this.officeFloorProperties.getProperty(name);
				compiler.addProperty(name, value);
			}
			compiler.addSystemProperties();
			compiler.addEnvProperties();

			// Ensure fail if not compiles
			compiler.setCompilerIssues(new CompilerIssues() {
				@Override
				public void addIssue(LocationType locationType,
						String location, AssetType assetType, String assetName,
						String issueDescription) {
					this.addIssue(locationType, location, assetType, assetName,
							issueDescription, null);
				}

				@Override
				public void addIssue(LocationType locationType,
						String location, AssetType assetType, String assetName,
						String issueDescription, Throwable cause) {
					throw new OfficeFloorCompileException(issueDescription
							+ " [" + locationType + ":" + location + ", "
							+ assetType + ":" + assetName + "] - "
							+ (cause == null ? "" : cause.getMessage()), cause);
				}
			});

			// Determine if override the default OfficeFloorSource
			if ((this.officeFloorSourceClassName != null)
					&& (this.officeFloorSourceClassName.trim().length() > 0)) {

				// Load the OfficeFloorSource class
				Class<? extends OfficeFloorSource> officeFloorSourceClass = (Class<? extends OfficeFloorSource>) compiler
						.getClassLoader().loadClass(
								this.officeFloorSourceClassName);

				// Override the default OfficeFloorSource
				compiler.setOfficeFloorSourceClass(officeFloorSourceClass);
			}

			// Compile the OfficeFloor
			this.officeFloor = compiler.compile(this.officeFloorLocation);
		}

		// Register this MBean (outside lock)
		context.registerMBean(this, OFFICE_FLOOR_MANAGER_OBJECT_NAME);
	}

	@Override
	public void main() throws Throwable {

		ManagedProcessContext context;
		OfficeFloor officeFloor = null;
		try {
			List<WorkState> workStates;
			synchronized (this) {
				context = this.context;
				officeFloor = this.officeFloor;

				// Open the OfficeFloor
				this.officeFloor.openOfficeFloor();

				// OfficeFloor now open
				this.isOpen = true;

				// Obtain thread safe list
				workStates = new ArrayList<WorkState>(this.workStates);
			}

			// Ensure close OfficeFloor
			try {

				// Determine if initial work to run
				if (workStates.size() == 0) {
					// No work, so wait until triggered to stop
					for (;;) {

						// Determine if flagged to stop
						if (!context.continueProcessing()) {
							return; // triggered to stop
						}

						// Wait some time for trigger to stop
						synchronized (this) {
							this.wait(1000);
						}
					}

				} else {
					// Invoke the work and stop once the work is complete
					for (WorkState workState : workStates) {
						workState.invoke(officeFloor);
					}

					// Wait for all work to complete
					for (;;) {

						// Check if all work complete
						boolean isAllWorkComplete = true;
						for (WorkState workState : workStates) {
							if (!workState.isComplete()) {
								isAllWorkComplete = false;
							}
						}

						// Exit processing if work complete or flagged stop
						if (isAllWorkComplete
								|| (!context.continueProcessing())) {
							return; // All work complete
						}

						// Wait some time for work to complete
						synchronized (this) {
							this.wait(1000);
						}
					}
				}

			} catch (Throwable ex) {
				// Indicate failure
				LOGGER.log(Level.WARNING, "Process had failure", ex);

			} finally {
				synchronized (this) {
					// Closed
					this.isOpen = false;
				}
			}

		} finally {
			// Ensure trigger closing office
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Maintains state of {@link Work} that is invoked on the
	 * {@link OfficeFloor}.
	 */
	private static class WorkState implements Serializable {

		/**
		 * Name of the {@link Office} containing the {@link Work}.
		 */
		private final String officeName;

		/**
		 * Name of the {@link Work}.
		 */
		private final String workName;

		/**
		 * Name of the {@link Task}. May be <code>null</code> to invoke initial
		 * {@link Task} of {@link Work}.
		 */
		private final String taskName;

		/**
		 * Parameter for the initial {@link Task} of the {@link Work}.
		 */
		private final Object parameter;

		/**
		 * {@link ProcessFuture} for invoking the {@link Work}.
		 */
		private transient ProcessFuture invokedProcessFuture = null;

		/**
		 * Initiate.
		 * 
		 * @param officeName
		 *            Name of the {@link Office} containing the {@link Work}.
		 * @param workName
		 *            Name of the {@link Work}.
		 * @param taskName
		 *            Name of the {@link Task}. May be <code>null</code> to
		 *            invoke initial {@link Task} of {@link Work}.
		 * @param parameter
		 *            Parameter for the initial {@link Task}.
		 */
		public WorkState(String officeName, String workName, String taskName,
				Object parameter) {
			this.officeName = officeName;
			this.workName = workName;
			this.taskName = taskName;
			this.parameter = parameter;
		}

		/**
		 * Invokes the {@link Work}.
		 * 
		 * @param officeFloor
		 *            {@link OfficeFloor} to invoke the {@link Work} on.
		 * @throws Exception
		 *             If fails to invoke the {@link Work}.
		 */
		public void invoke(OfficeFloor officeFloor) throws Exception {

			// Obtain the work manager
			WorkManager workManager = officeFloor.getOffice(this.officeName)
					.getWorkManager(this.workName);
			ProcessFuture invokedProcessFuture;
			if (this.taskName == null) {
				// Invoke initial task of work
				invokedProcessFuture = workManager.invokeWork(this.parameter);
			} else {
				// Invoke the specific task
				invokedProcessFuture = workManager
						.getTaskManager(this.taskName).invokeTask(
								this.parameter);
			}

			// Set state
			synchronized (this) {
				this.invokedProcessFuture = invokedProcessFuture;
			}
		}

		/**
		 * Indicates if the invoked {@link Work} is complete.
		 * 
		 * @return <code>true</code> if complete.
		 */
		public synchronized boolean isComplete() {
			return this.invokedProcessFuture.isComplete();
		}
	}

}