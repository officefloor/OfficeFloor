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
package net.officefloor.building.command.officefloor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ClassPathOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.FunctionNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.JvmOptionOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeFloorLocationOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeFloorNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeFloorSourceOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ParameterOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.PropertiesOfficeFloorCommandParameter;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.building.process.ProcessManager;
import net.officefloor.building.process.officefloor.OfficeFloorManager;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloorCommand} to open an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OpenOfficeFloorCommand implements OfficeFloorCommandFactory, OfficeFloorCommand {

	/**
	 * {@link OfficeFloorSource}.
	 */
	private final OfficeFloorSourceOfficeFloorCommandParameter officeFloorSource = new OfficeFloorSourceOfficeFloorCommandParameter();

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final OfficeFloorLocationOfficeFloorCommandParameter officeFloorLocation = new OfficeFloorLocationOfficeFloorCommandParameter();

	/**
	 * JVM options.
	 */
	private final JvmOptionOfficeFloorCommandParameter jvmOptions = new JvmOptionOfficeFloorCommandParameter();

	/**
	 * Addition to the class path.
	 */
	private final ClassPathOfficeFloorCommandParameter classpath = new ClassPathOfficeFloorCommandParameter();

	/**
	 * {@link OfficeFloor} name.
	 */
	private final OfficeFloorNameOfficeFloorCommandParameter officeFloorName = new OfficeFloorNameOfficeFloorCommandParameter();

	/**
	 * {@link Office} name.
	 */
	private final OfficeNameOfficeFloorCommandParameter officeName = new OfficeNameOfficeFloorCommandParameter();

	/**
	 * {@link ManagedFunction} name.
	 */
	private final FunctionNameOfficeFloorCommandParameter functionName = new FunctionNameOfficeFloorCommandParameter();

	/**
	 * Parameter for {@link ManagedFunction}.
	 */
	private final ParameterOfficeFloorCommandParameter parameter = new ParameterOfficeFloorCommandParameter();

	/**
	 * Properties for the {@link OfficeFloor}.
	 */
	private final PropertiesOfficeFloorCommandParameter properties = new PropertiesOfficeFloorCommandParameter();

	/**
	 * {@link OfficeFloorCommandParameter} instances for this
	 * {@link OfficeFloorCommand}.
	 */
	private final OfficeFloorCommandParameter[] parameters;

	/**
	 * Indicates if {@link OfficeFloor} is to be opened within a spawned
	 * {@link Process}.
	 */
	private final boolean isSpawn;

	/**
	 * Indicates if report on open/close of {@link OfficeFloor}.
	 */
	private final boolean isReportOpenClose;

	/**
	 * Initiate.
	 * 
	 * @param isSpawn
	 *            <code>true</code> if {@link OfficeFloor} is to be opened
	 *            within a spawned {@link Process}.
	 * @param isReportOpenClose
	 *            <code>true</code> to report on open/close of
	 *            {@link OfficeFloor}.
	 */
	public OpenOfficeFloorCommand(boolean isSpawn, boolean isReportOpenClose) {
		this.isSpawn = isSpawn;
		this.isReportOpenClose = isReportOpenClose;

		// Create the listing of parameters (max 10 parameters)
		List<OfficeFloorCommandParameter> parameters = new ArrayList<OfficeFloorCommandParameter>(10);
		parameters.addAll(Arrays.asList(
				new OfficeFloorCommandParameter[] { this.officeFloorSource, this.officeFloorLocation, this.classpath,
						this.officeFloorName, this.officeName, this.functionName, this.parameter, this.properties }));
		if (isSpawn) {
			// Spawning so include JVM options
			parameters.add(this.jvmOptions);
		}
		this.parameters = parameters.toArray(new OfficeFloorCommandParameter[parameters.size()]);
	}

	/*
	 * ================ OfficeFloorCommandFactory =====================
	 */

	@Override
	public String getCommandName() {
		return "open";
	}

	@Override
	public OfficeFloorCommand createCommand() {
		return new OpenOfficeFloorCommand(this.isSpawn, this.isReportOpenClose);
	}

	/*
	 * =================== OfficeFloorCommand =========================
	 */

	@Override
	public String getDescription() {
		return "Opens an OfficeFloor";
	}

	@Override
	public OfficeFloorCommandParameter[] getParameters() {
		return this.parameters;
	}

	@Override
	public void initialiseEnvironment(OfficeFloorCommandContext context) throws Exception {

		// Include the raw class paths
		for (String classPathEntry : this.classpath.getClassPathEntries()) {
			context.includeClassPathEntry(classPathEntry);
		}
	}

	@Override
	public ManagedProcess createManagedProcess(OfficeFloorCommandEnvironment environment) throws Exception {

		// Indicate if OfficeFloor in spawned process
		environment.setSpawnProcess(this.isSpawn);

		// Specify the OfficeFloor name
		String officeFloorName = this.officeFloorName.getOfficeFloorName();
		environment.setProcessName(officeFloorName);

		// Specify the JVM options
		for (String jvmOption : this.jvmOptions.getJvmOptions()) {
			environment.addJvmOption(jvmOption);
		}

		// Create the managed process to open the office floor
		String officeFloorSourceClassName = this.officeFloorSource.getOfficeFloorSourceClassName();
		String officeFloorLocation = this.officeFloorLocation.getOfficeFloorLocation();
		Properties officeFloorProperties = this.properties.getProperties();
		OfficeFloorManager officeFloorManager = new OfficeFloorManager(officeFloorSourceClassName, officeFloorLocation,
				officeFloorProperties);

		// Obtain details of the possible task to open
		String officeName = this.officeName.getOfficeName();
		String functionName = this.functionName.getFunctionName();
		String parameterValue = this.parameter.getParameterValue();

		// Determine if invoking task (by checking function name provided)
		if (functionName != null) {
			// Invoke the function
			officeFloorManager.addExecuteFunction(officeName, functionName, parameterValue);
		}

		// Report progress on running locally (not spawned in another process)
		ManagedProcess managedProcess;
		if (this.isReportOpenClose) {

			// Ensure have process name
			officeFloorName = (officeFloorName == null ? ProcessManager.DEFAULT_PROCESS_NAME : officeFloorName);

			// Obtain the open message
			StringBuilder openMessage = new StringBuilder();
			openMessage.append("Opening OfficeFloor '");
			openMessage.append(officeFloorName);
			openMessage.append("'");
			if (functionName != null) {
				openMessage.append(" for function (office=");
				openMessage.append(officeName);
				openMessage.append(", function=");
				openMessage.append(functionName);
				if (parameterValue != null) {
					openMessage.append(", parameter=");
					openMessage.append(parameterValue);
				}
				openMessage.append(")");
			}

			// Obtain the close message
			String successMessage = "OfficeFloor '" + officeFloorName + "' closed";

			// Wrap for reporting
			managedProcess = new ReportManagedProcess(officeFloorManager, openMessage.toString(), successMessage);

		} else {
			// No reporting so use directly
			managedProcess = officeFloorManager;
		}

		// Return the managed process
		return managedProcess;
	}

	/**
	 * {@link ManagedProcess} to provide output regarding progress of the
	 * {@link OfficeFloorManager} {@link ManagedProcess}.
	 */
	public static class ReportManagedProcess implements ManagedProcess {

		/**
		 * {@link Serializable} version.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Delegate {@link ManagedProcess}.
		 */
		private final ManagedProcess delegate;

		/**
		 * Message to output on starting the {@link ManagedProcess}.
		 */
		private final String startMessage;

		/**
		 * Message to output on successful completion of the
		 * {@link ManagedProcess}.
		 */
		private final String successMessage;

		/**
		 * Initiate.
		 * 
		 * @param delegate
		 *            Delegate {@link ManagedProcess}.
		 * @param startMessage
		 *            Message to output on starting the {@link ManagedProcess}.
		 * @param successMessage
		 *            Message to output on successful completion of the
		 *            {@link ManagedProcess}.
		 */
		public ReportManagedProcess(ManagedProcess delegate, String startMessage, String successMessage) {
			this.delegate = delegate;
			this.startMessage = startMessage;
			this.successMessage = successMessage;
		}

		/*
		 * ====================== ManagedProcess =========================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			// Initialise delegate
			this.delegate.init(context);
		}

		@Override
		public void main() throws Throwable {

			// Output the start message
			System.out.println(this.startMessage);

			// Run the delegate
			this.delegate.main();

			// As here completed successfully, so output success message
			System.out.println(this.successMessage);
		}
	}

}