/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.building.process;

import javax.management.MBeanServer;

/**
 * <p>
 * Provides optional configuration for the {@link Process}.
 * <p>
 * As the configuration is optional, any of the methods may return
 * <code>null</code> or be set to a <code>null</code> value.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessConfiguration {

	/**
	 * Name to identify the {@link Process}.
	 */
	private String processName = null;

	/**
	 * {@link MBeanServer}.
	 */
	private MBeanServer mbeanServer = null;

	/**
	 * Obtains the name to identify the {@link Process}.
	 * 
	 * @return Name to identify the {@link Process}.
	 */
	public String getProcessName() {
		return this.processName;
	}

	/**
	 * Specifies the name to identify the {@link Process}.
	 * 
	 * @param domain
	 *            Name to identify the {@link Process}.
	 */
	public void setProcessName(String processName) {
		this.processName = processName;
	}

	/**
	 * Obtains the {@link MBeanServer}.
	 * 
	 * @return {@link MBeanServer}.
	 */
	public MBeanServer getMbeanServer() {
		return this.mbeanServer;
	}

	/**
	 * Specifies the {@link MBeanServer}.
	 * 
	 * @param mbeanServer
	 *            {@link MBeanServer}.
	 */
	public void setMbeanServer(MBeanServer mbeanServer) {
		this.mbeanServer = mbeanServer;
	}

}