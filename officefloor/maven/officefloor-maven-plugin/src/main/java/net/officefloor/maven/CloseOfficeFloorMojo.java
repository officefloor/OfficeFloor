/*-
 * #%L
 * Maven OfficeFloor Plugin
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.maven;

import java.io.EOFException;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.UnmarshalException;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import net.officefloor.compile.mbean.OfficeFloorMBean;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Open {@link OfficeFloor} {@link Mojo}.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "close", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class CloseOfficeFloorMojo extends AbstractMojo {

	/**
	 * Obtains the {@link OfficeFloorMBean}.
	 * 
	 * @param port Port that JMX is running on for {@link OfficeFloor}.
	 * @return {@link OfficeFloorMBean}.
	 * @throws Exception If failure to connect and obtain {@link OfficeFloorMBean}.
	 */
	public static OfficeFloorMBean getOfficeFloorBean(int port) throws Exception {

		// Connect to JMX for OfficeFloor
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + port + "/jmxrmi");
		JMXConnector connector = JMXConnectorFactory.connect(url);
		MBeanServerConnection connection = connector.getMBeanServerConnection();

		// Obtain the OfficeFloor MBean
		ObjectName name = new ObjectName("net.officefloor:type=" + OfficeFloor.class.getName() + ",name=OfficeFloor");
		OfficeFloorMBean mbean = JMX.newMBeanProxy(connection, name, OfficeFloorMBean.class);

		// Return the OfficeFloor MBean
		return mbean;
	}

	/**
	 * JMX port.
	 */
	@Parameter(required = false, defaultValue = "" + OpenOfficeFloorMojo.DEFAULT_JMX_PORT)
	private int jmxPort = OpenOfficeFloorMojo.DEFAULT_JMX_PORT;

	/*
	 * =================== AbstractMojo =================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			// Obtain the OfficeFloor MBean
			OfficeFloorMBean mbean = getOfficeFloorBean(this.jmxPort);

			// Close OfficeFloor
			mbean.closeOfficeFloor();

		} catch (UndeclaredThrowableException ex) {
			// Handle possible connection loss (as likely closed quickly)
			Throwable cause = ex.getCause();
			if (cause instanceof UnmarshalException) {
				cause = cause.getCause();
				if (cause instanceof EOFException) {
					return; // ignore closing too quickly
				}
			}

			// Propagate failure
			this.propagateException(ex);

		} catch (Exception ex) {
			this.propagateException(ex);
		}
	}

	/**
	 * Propagates the {@link Throwable}.
	 * 
	 * @param ex Cause.
	 * @throws MojoExecutionException Propagated {@link MojoExecutionException}.
	 */
	private void propagateException(Throwable ex) throws MojoExecutionException {
		throw new MojoExecutionException("Failed to close " + OfficeFloor.class.getSimpleName(), ex);
	}

}
