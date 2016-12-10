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
package net.officefloor.building.process;

import java.util.LinkedList;
import java.util.List;

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
public final class ProcessConfiguration {

	/**
	 * Name to identify the {@link Process}.
	 */
	private String processName = null;

	/**
	 * Additional class path.
	 */
	private String additionalClassPath = null;

	/**
	 * JVM options for the {@link Process}.
	 */
	private List<String> jvmOptions = new LinkedList<String>();

	/**
	 * {@link ProcessStartListener}.
	 */
	private ProcessStartListener startListener = null;

	/**
	 * {@link ProcessCompletionListener}.
	 */
	private ProcessCompletionListener completionListener = null;

	/**
	 * {@link ProcessOutputStreamFactory}.
	 */
	private ProcessOutputStreamFactory outputStreamFactory = null;

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
	 * @param processName
	 *            Name to identify the {@link Process}.
	 */
	public void setProcessName(String processName) {
		this.processName = processName;
	}

	/**
	 * Obtains the additional class path.
	 * 
	 * @return Additional class path.
	 */
	public String getAdditionalClassPath() {
		return this.additionalClassPath;
	}

	/**
	 * Specifies the additional class path.
	 * 
	 * @param additionalClassPath
	 *            Additional class path.
	 */
	public void setAdditionalClassPath(String additionalClassPath) {
		this.additionalClassPath = additionalClassPath;
	}

	/**
	 * Obtains the JVM options for the {@link Process}.
	 * 
	 * @return JVM options for the {@link Process}.
	 */
	public String[] getJvmOptions() {
		return this.jvmOptions.toArray(new String[this.jvmOptions.size()]);
	}

	/**
	 * Adds a JVM options for the {@link Process}.
	 * 
	 * @param jvmOption
	 *            JVM option for the {@link Process}.
	 */
	public void addJvmOption(String jvmOption) {
		this.jvmOptions.add(jvmOption);
	}

	/**
	 * Obtains the {@link ProcessStartListener}.
	 * 
	 * @return {@link ProcessStartListener}.
	 */
	public ProcessStartListener getProcessStartListener() {
		return this.startListener;
	}

	/**
	 * Specifies the {@link ProcessStartListener}.
	 * 
	 * @param startListener
	 *            {@link ProcessStartListener}.
	 */
	public void setProcessStartListener(ProcessStartListener startListener) {
		this.startListener = startListener;
	}

	/**
	 * Obtains the {@link ProcessCompletionListener}.
	 * 
	 * @return {@link ProcessCompletionListener}.
	 */
	public ProcessCompletionListener getProcessCompletionListener() {
		return this.completionListener;
	}

	/**
	 * Specifies the {@link ProcessCompletionListener}.
	 * 
	 * @param completionListener
	 *            {@link ProcessCompletionListener}.
	 */
	public void setProcessCompletionListener(
			ProcessCompletionListener completionListener) {
		this.completionListener = completionListener;
	}

	/**
	 * Obtains the {@link ProcessOutputStreamFactory}.
	 * 
	 * @return {@link ProcessOutputStreamFactory}.
	 */
	public ProcessOutputStreamFactory getProcessOutputStreamFactory() {
		return this.outputStreamFactory;
	}

	/**
	 * Specifies the {@link ProcessOutputStreamFactory}.
	 * 
	 * @param outputStreamFactory
	 *            {@link ProcessOutputStreamFactory}.
	 */
	public void setProcessOutputStreamFactory(
			ProcessOutputStreamFactory outputStreamFactory) {
		this.outputStreamFactory = outputStreamFactory;
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