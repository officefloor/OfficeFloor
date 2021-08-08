/*-
 * #%L
 * Maven OfficeFloor Plugin
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
