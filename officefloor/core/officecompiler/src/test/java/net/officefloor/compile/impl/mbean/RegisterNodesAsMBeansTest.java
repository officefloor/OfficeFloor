/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.impl.mbean;

import java.lang.management.ManagementFactory;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MXBean;
import javax.management.ObjectName;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.extension.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure able to register the various {@link Node} instances as {@link MXBean}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class RegisterNodesAsMBeansTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to register the {@link OfficeFloorSource} as an MBean.
	 */
	public void testRegisterOfficeFloorSource() throws Exception {

		// Obtain the object name
		ObjectName objectName = getObjectName(OfficeFloorSource.class, "OfficeFloor");

		// Compile and open the OfficeFloor
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.getOfficeFloorCompiler().setOfficeFloorSourceClass(TestOfficeFloorSource.class);
		compile.getOfficeFloorCompiler().setRegisterMBeans(true);
		OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor((deployer, context) -> {
		});

		// Ensure able to obtain OfficeFloorSource MBean
		TestOfficeFloorSourceMBean mbean = getMBean(objectName, TestOfficeFloorSourceMBean.class);
		assertEquals("Incorrect Mbean value", "OfficeFloor Test", mbean.getOfficeFloorSourceMBeanValue());

		// Close the OfficeFloor (unregistering the MBeans)
		officeFloor.closeOfficeFloor();
		assertMBeanUnregistered(objectName);
	}

	public static interface TestOfficeFloorSourceMBean {
		String getOfficeFloorSourceMBeanValue();
	}

	@TestSource
	public static class TestOfficeFloorSource extends AbstractOfficeFloorSource implements TestOfficeFloorSourceMBean {

		@Override
		public String getOfficeFloorSourceMBeanValue() {
			return "OfficeFloor Test";
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void specifyConfigurationProperties(RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {
		}
	}

	public void testRegisterManagedObjectSource() {
		fail("TODO implement");
	}

	public void testRegisterTeamSource() {
		fail("TODO implement");
	}

	public void testRegisterOfficeSource() {
		fail("TODO implement");
	}

	public void testRegisterAdministrationSource() {
		fail("TODO implement");
	}

	public void testRegisterGovernanceSource() {
		fail("TODO implement");
	}

	public void testRegisterSectionSource() {
		fail("TODO implement");
	}

	public void testRegisterManagedFunctionSource() {
		fail("TODO implement");
	}

	/**
	 * Obtains the MBean.
	 * 
	 * @param objectName
	 *            MBean {@link ObjectName}.
	 * @param proxyType
	 *            Proxy type for MBean.
	 * @return MBean.
	 * @throws Exception
	 *             If fails to obtain the MBean.
	 */
	private static <B> B getMBean(ObjectName objectName, Class<B> proxyType) throws Exception {
		MBeanServerConnection connection = ManagementFactory.getPlatformMBeanServer();
		return JMX.newMBeanProxy(connection, objectName, proxyType);
	}

	/**
	 * Ensures the MBean has been unregistered.
	 * 
	 * @param objectName
	 *            MBean {@link ObjectName}.
	 * @throws Exception
	 *             If fails to check MBean.
	 */
	private static void assertMBeanUnregistered(ObjectName objectName) throws Exception {
		MBeanServerConnection connection = ManagementFactory.getPlatformMBeanServer();
		assertFalse("MBean should be unregistered", connection.isRegistered(objectName));
	}

	/**
	 * Obtains the {@link ObjectName}.
	 * 
	 * @param type
	 *            Type of MBean.
	 * @param name
	 *            Name of MBean.
	 * @return {@link ObjectName}.
	 * @throws Exception
	 *             If fails to create {@link ObjectName}.
	 */
	private static ObjectName getObjectName(Class<?> type, String name) throws Exception {
		return new ObjectName("net.officefloor:type=" + type.getName() + ",name=" + name);
	}

}