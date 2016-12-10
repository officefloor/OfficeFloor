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
package net.officefloor.building.manager;

import java.io.File;
import java.io.Serializable;
import java.util.Properties;

import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.console.OfficeBuilding;

/**
 * {@link ManagedProcess} for spawning an {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingManagedProcess implements ManagedProcess {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Host name for the {@link OfficeBuilding}.
	 */
	private final String hostName;

	/**
	 * Port for the {@link OfficeBuilding}.
	 */
	private final int port;

	/**
	 * Location of the key store {@link File}.
	 */
	private final String keyStoreLocation;

	/**
	 * Password to the key store {@link File}.
	 */
	private final String keyStorePassword;

	/**
	 * User name to allow connections.
	 */
	private final String userName;

	/**
	 * Password to allow connections.
	 */
	private final String password;

	/**
	 * Workspace location.
	 */
	private final String workspaceLocation;

	/**
	 * Flag indicating to isolate {@link Process} instances.
	 */
	private final boolean isIsolateProcesses;

	/**
	 * Environment {@link Properties}.
	 */
	private final Properties environment;

	/**
	 * JVM options for the {@link Process}.
	 */
	private final String[] jvmOptions;

	/**
	 * Flag indicating if the {@link OfficeBuilding} will allow configured class
	 * path entries.
	 */
	private final boolean isAllowClassPathEntries;

	/**
	 * {@link ManagedProcessContext}.
	 */
	private ManagedProcessContext context;

	/**
	 * {@link OfficeBuildingManager}.
	 */
	private OfficeBuildingManagerMBean manager;

	/**
	 * Initiate.
	 * 
	 * @param hostName
	 *            Host name for the {@link OfficeBuilding}.
	 * @param port
	 *            Port for the {@link OfficeBuilding}.
	 * @param keyStore
	 *            Key store {@link File}.
	 * @param keyStorePassword
	 *            Password to the key store {@link File}.
	 * @param userName
	 *            User name to allow connections.
	 * @param password
	 *            Password to allow connections.
	 * @param workspaceLocation
	 *            Workspace location.
	 * @param isIsolateProcesses
	 *            Flag indicating to isolate {@link Process} instances.
	 * @param environment
	 *            Environment {@link Properties}.
	 * @param jvmOptions
	 *            JVM options for the {@link Process}.
	 * @param isAllowClassPathEntries
	 *            Flag indicating if the {@link OfficeBuilding} will allow
	 *            configured class path entries.
	 */
	public OfficeBuildingManagedProcess(String hostName, int port, File keyStore, String keyStorePassword,
			String userName, String password, File workspaceLocation, boolean isIsolateProcesses,
			Properties environment, String[] jvmOptions, boolean isAllowClassPathEntries) {
		this.hostName = hostName;
		this.port = port;
		this.keyStoreLocation = keyStore.getAbsolutePath();
		this.keyStorePassword = keyStorePassword;
		this.userName = userName;
		this.password = password;
		this.workspaceLocation = (workspaceLocation == null ? null : workspaceLocation.getAbsolutePath());
		this.isIsolateProcesses = isIsolateProcesses;
		this.environment = environment;
		this.jvmOptions = jvmOptions;
		this.isAllowClassPathEntries = isAllowClassPathEntries;
	}

	/*
	 * ====================== ManagedProcess ===========================
	 */

	@Override
	public void init(ManagedProcessContext context) throws Throwable {
		this.context = context;

		// Obtain the workspace
		File workspace = (this.workspaceLocation == null ? null : new File(this.workspaceLocation));

		// Start the OfficeBuilding
		this.manager = OfficeBuildingManager.startOfficeBuilding(this.hostName, this.port,
				new File(this.keyStoreLocation), this.keyStorePassword, this.userName, this.password, workspace,
				this.isIsolateProcesses, this.environment, null, this.jvmOptions, this.isAllowClassPathEntries);
	}

	@Override
	public void main() throws Throwable {

		// Wait until the OfficeBuilding is stopped (or no longer processing)
		while ((!this.manager.isOfficeBuildingStopped()) && (this.context.continueProcessing())) {

			// Wait some time before checking again
			synchronized (this.manager) {
				this.manager.wait(1000);
			}
		}
	}

}