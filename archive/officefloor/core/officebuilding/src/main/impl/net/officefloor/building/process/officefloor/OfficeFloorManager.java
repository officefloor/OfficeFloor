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

import java.io.Serializable;
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
import net.officefloor.compile.mbean.OfficeFloorMBean;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link ManagedProcess} for an {@link OfficeFloor} {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManager implements ManagedProcess, OfficeFloorListener, OfficeFloorManagerMBean {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(OfficeFloorManager.class.getName());

	/**
	 * Obtains the {@link OfficeFloorManagerMBean} {@link ObjectName}.
	 * 
	 * @param officeFloorName
	 *            Name of the {@link OfficeFloor}.
	 * @return {@link OfficeFloorManagerMBean} {@link ObjectName}.
	 * @throws MalformedObjectNameException
	 *             If fails to construct the {@link ObjectName}.
	 */
	public static ObjectName getOfficeFloorManagerObjectName(String officeFloorName)
			throws MalformedObjectNameException {
		return new ObjectName(
				"net.officefloor:type=" + OfficeFloorManager.class.getName() + ",name=" + officeFloorName);
	}

	/**
	 * Obtains the {@link OfficeFloorMBean} {@link ObjectName}.
	 * 
	 * @param officeFloorName
	 *            Name of the {@link OfficeFloor}.
	 * @return {@link OfficeFloorMBean} {@link ObjectName}.
	 * @throws MalformedObjectNameException
	 *             If fails to construct the {@link ObjectName}.
	 */
	public static ObjectName getOfficeFloorObjectName(String officeFloorName) throws MalformedObjectNameException {
		return new ObjectName("net.officefloor:type=" + OfficeFloor.class.getName() + ",name=" + officeFloorName);
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
	 * Listing of the {@link FunctionProcessState}.
	 */
	private final List<FunctionProcessState> functionProcessStates = new LinkedList<FunctionProcessState>();

	/**
	 * {@link ManagedProcessContext}.
	 */
	private transient ManagedProcessContext context = null;

	/**
	 * {@link OfficeFloor} being managed within the {@link Process}.
	 */
	private transient OfficeFloor officeFloor = null;

	/**
	 * Indicates whether the {@link OfficeFloor} is open.
	 */
	private transient volatile boolean isOfficeFloorOpen = false;

	/**
	 * Indicates that the {@link OfficeFloor} has been closed.
	 */
	private transient volatile boolean isOfficeFloorClosed = false;

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
	public OfficeFloorManager(String officeFloorSourceClassName, String officeFloorLocation,
			Properties officeFloorProperties) {
		this.officeFloorSourceClassName = officeFloorSourceClassName;
		this.officeFloorLocation = officeFloorLocation;
		this.officeFloorProperties = officeFloorProperties;
	}

	/**
	 * Adds a {@link ManagedFunction} to be executed by the {@link OfficeFloor}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param functionName
	 *            Name of {@link ManagedFunction} within the {@link Office}.
	 * @param parameter
	 *            Parameter to the {@link ManagedFunction}.
	 */
	public void addExecuteFunction(String officeName, String functionName, Object parameter) {
		this.functionProcessStates.add(new FunctionProcessState(officeName, functionName, parameter));
	}

	/*
	 * ============== OfficeFloorManagerMBean ========================
	 */

	@Override
	public boolean isOfficeFloorOpen() {
		return this.isOfficeFloorOpen && (!this.isOfficeFloorClosed);
	}

	@Override
	public void closeOfficeFloor() {
		synchronized (this) {
			try {

				// Close the OfficeFloor
				this.officeFloor.closeOfficeFloor();

			} catch (Exception ex) {
				// Indicate failure
				LOGGER.log(Level.WARNING, "Failed to close OfficeFloor", ex);

			} finally {
				// Ensure flagged closed
				this.isOfficeFloorClosed = true;
			}
		}
	}

	/*
	 * ================ OfficeFloorListener ==========================
	 */

	@Override
	public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
		// Not flag open, as ensuring OfficeFloor open successfully
	}

	@Override
	public void officeFloorClosed(OfficeFloorEvent event) throws Exception {

		// Flag OfficeFloor closed
		this.isOfficeFloorClosed = true;

		// Notify immediately that closed
		synchronized (this) {
			this.notifyAll();
		}
	}

	/*
	 * =================== ManagedProcess ============================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void init(ManagedProcessContext context) throws Throwable {

		// Obtain the process name
		String processName = context.getProcessNamespace();

		// Lock as MBeanServer may also change state
		synchronized (this) {
			this.context = context;

			// Create the OfficeFloor compiler
			OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

			// Listen to state of OfficeFloor
			compiler.addOfficeFloorListener(this);

			// Register all MBeans with context
			compiler.setMBeanRegistrator((name, mbean) -> context.registerMBean(mbean, name));

			// Determine if override the default OfficeFloorSource
			if ((this.officeFloorSourceClassName != null) && (this.officeFloorSourceClassName.trim().length() > 0)) {

				// Load the OfficeFloorSource class
				Class<? extends OfficeFloorSource> officeFloorSourceClass = (Class<? extends OfficeFloorSource>) compiler
						.getClassLoader().loadClass(this.officeFloorSourceClassName);

				// Override the default OfficeFloorSource
				compiler.setOfficeFloorSourceClass(officeFloorSourceClass);
			}

			// Configure the location
			compiler.setOfficeFloorLocation(this.officeFloorLocation);

			// Add properties for the compiler
			compiler.addEnvProperties();
			compiler.addSystemProperties();
			for (String name : this.officeFloorProperties.stringPropertyNames()) {
				String value = this.officeFloorProperties.getProperty(name);
				compiler.addProperty(name, value);
			}

			// Compile the OfficeFloor
			this.officeFloor = compiler.compile(processName);
		}

		// Register as MBean
		context.registerMBean(this, getOfficeFloorManagerObjectName(processName));
	}

	@Override
	public void main() throws Throwable {

		// Ensure close the OfficeFloor
		OfficeFloor officeFloor = null;
		try {

			// Lock to ensure MBeanServer not close while undertaking operations
			synchronized (this) {
				officeFloor = this.officeFloor;

				// Open the OfficeFloor
				this.officeFloor.openOfficeFloor();

				// Flag OfficeFloor open
				this.isOfficeFloorOpen = true;

				// Ensure close OfficeFloor
				try {

					// Determine if initial function to run
					if (this.functionProcessStates.size() == 0) {
						// No function, so wait until triggered to stop
						while (!this.isOfficeFloorClosed) {

							// Determine if flagged to stop
							if (!context.continueProcessing()) {
								return; // triggered to stop
							}

							// Wait some time for trigger to stop
							this.wait(1000);
						}

					} else {
						// Invoke the functions and stop once they are complete
						for (FunctionProcessState functionProcessState : this.functionProcessStates) {
							functionProcessState.invoke(officeFloor);
						}

						// Wait for all functions to complete
						for (;;) {

							// Check if all functions complete
							boolean isAllFunctionsComplete = true;
							for (FunctionProcessState functionProcessState : this.functionProcessStates) {

								// Check if function complete
								if (functionProcessState.result == null) {
									// Function still executing
									isAllFunctionsComplete = false;

								} else if (functionProcessState.result.escalation != null) {
									// Propagate the escalation
									throw functionProcessState.result.escalation;
								}
							}

							// Exit if functions complete or flagged stop
							if (isAllFunctionsComplete || (!context.continueProcessing())) {
								return; // All functions complete
							}

							// Wait some time for function to complete
							this.wait(10000);
						}
					}

				} catch (Throwable ex) {
					// Indicate failure
					LOGGER.log(Level.WARNING, "Process had failure", ex);
				}
			}

		} finally {
			// Ensure trigger closing office
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Maintains state of {@link ManagedFunction} that is invoked on the
	 * {@link OfficeFloor}.
	 */
	private class FunctionProcessState implements Serializable {

		/**
		 * {@link Serializable} version.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Name of the {@link Office} containing the {@link ManagedFunction}.
		 */
		private final String officeName;

		/**
		 * Name of the {@link ManagedFunction}.
		 */
		private final String functionName;

		/**
		 * Parameter for the {@link ManagedFunction}.
		 */
		private final Object parameter;

		/**
		 * {@link FunctionResult}.
		 */
		private transient FunctionResult result = null;

		/**
		 * Initiate.
		 * 
		 * @param officeName
		 *            Name of the {@link Office} containing the
		 *            {@link ManagedFunction}.
		 * @param functionName
		 *            Name of the {@link ManagedFunction}.
		 * @param parameter
		 *            Parameter for the {@link ManagedFunction}.
		 */
		public FunctionProcessState(String officeName, String functionName, Object parameter) {
			this.officeName = officeName;
			this.functionName = functionName;
			this.parameter = parameter;
		}

		/**
		 * Invokes the {@link ManagedFunction}.
		 * 
		 * @param officeFloor
		 *            {@link OfficeFloor} to invoke the {@link ManagedFunction}
		 *            on.
		 * @throws Exception
		 *             If fails to invoke the {@link ManagedFunction}.
		 */
		public void invoke(OfficeFloor officeFloor) throws Exception {

			// Obtain the function manager
			FunctionManager functionManager = officeFloor.getOffice(this.officeName)
					.getFunctionManager(this.functionName);

			// Invoke the function (notifying when complete)
			functionManager.invokeProcess(this.parameter, (exception) -> {
				synchronized (OfficeFloorManager.this) {

					// Indicate function is complete
					this.result = new FunctionResult(exception);

					// Notify immediately that complete
					OfficeFloorManager.this.notifyAll();
				}
			});
		}
	}

	/**
	 * Result of the {@link FunctionProcessState}.
	 */
	private static class FunctionResult {

		/**
		 * {@link Escalation}. May be <code>null</code> if successful.s
		 */
		private final Throwable escalation;

		/**
		 * Instantiate.
		 * 
		 * @param escalation
		 *            {@link Escalation}. May be <code>null</code> if
		 *            successful.
		 */
		private FunctionResult(Throwable escalation) {
			this.escalation = escalation;
		}
	}

}