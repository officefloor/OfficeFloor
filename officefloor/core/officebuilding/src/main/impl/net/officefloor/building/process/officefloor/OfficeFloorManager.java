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
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link ManagedProcess} for an {@link OfficeFloor} {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManager implements ManagedProcess {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(OfficeFloorManager.class.getName());

	/**
	 * {@link ObjectName} for the {@link OfficeFloorManager}.
	 */
	static ObjectName OFFICE_FLOOR_MANAGER_OBJECT_NAME;

	static {
		try {
			OFFICE_FLOOR_MANAGER_OBJECT_NAME = new ObjectName("officefloor", "type", "OfficeFloor");
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
	 * =================== ManagedProcess ============================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void init(ManagedProcessContext context) throws Throwable {

		synchronized (this) {
			this.context = context;

			// Create the OfficeFloor compiler
			OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

			// Add properties for the compiler
			for (String name : this.officeFloorProperties.stringPropertyNames()) {
				String value = this.officeFloorProperties.getProperty(name);
				compiler.addProperty(name, value);
			}
			compiler.addSystemProperties();
			compiler.addEnvProperties();
			compiler.setRegisterMBeans(true);
			compiler.setOfficeFloorLocation(this.officeFloorLocation);

			// Determine if override the default OfficeFloorSource
			if ((this.officeFloorSourceClassName != null) && (this.officeFloorSourceClassName.trim().length() > 0)) {

				// Load the OfficeFloorSource class
				Class<? extends OfficeFloorSource> officeFloorSourceClass = (Class<? extends OfficeFloorSource>) compiler
						.getClassLoader().loadClass(this.officeFloorSourceClassName);

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
			List<FunctionProcessState> functionProcessStates;
			synchronized (this) {
				context = this.context;
				officeFloor = this.officeFloor;

				// Open the OfficeFloor
				this.officeFloor.openOfficeFloor();

				// Obtain thread safe list
				functionProcessStates = new ArrayList<FunctionProcessState>(this.functionProcessStates);
			}

			// Ensure close OfficeFloor
			try {

				// Determine if initial function to run
				if (functionProcessStates.size() == 0) {
					// No function, so wait until triggered to stop
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
					// Invoke the functions and stop once they are complete
					for (FunctionProcessState functionProcessState : functionProcessStates) {
						functionProcessState.invoke(officeFloor);
					}

					// Wait for all functions to complete
					for (;;) {

						// Check if all functions complete
						boolean isAllFunctionsComplete = true;
						for (FunctionProcessState functionProcessState : functionProcessStates) {
							if (!functionProcessState.isComplete()) {
								isAllFunctionsComplete = false;
							}
						}

						// Exit processing if functions complete or flagged stop
						if (isAllFunctionsComplete || (!context.continueProcessing())) {
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
	private static class FunctionProcessState implements Serializable {

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
		 * Indicates if the {@link ManagedFunction} is completd.
		 */
		private transient volatile boolean isComplete = false;

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
				this.isComplete = true;
			});
		}

		/**
		 * Indicates if the invoked {@link ManagedFunction} is complete.
		 * 
		 * @return <code>true</code> if complete.
		 */
		public boolean isComplete() {
			return this.isComplete;
		}
	}

}