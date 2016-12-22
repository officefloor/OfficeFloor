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
package net.officefloor.plugin.jndi.work;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.xml.XmlUnmarshaller;

/**
 * <p>
 * Mock Facade.
 * <p>
 * To simplify using JNDI objects as {@link Work}, an facade can be optionally
 * used to simplify the JNDI object methods for configuring into
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockFacade {

	/**
	 * {@link XmlUnmarshaller} registry.
	 */
	private static final Map<Integer, XmlUnmarshaller> unmarshallerRegistry = new HashMap<Integer, XmlUnmarshaller>();

	/**
	 * Registers an {@link XmlUnmarshaller}.
	 * 
	 * @param identifier
	 *            Identifier for the {@link XmlUnmarshaller}.
	 * @param unmarshaller
	 *            {@link XmlUnmarshaller}.
	 */
	public static void registerXmlUnmarshaller(Integer identifier,
			XmlUnmarshaller unmarshaller) {
		unmarshallerRegistry.put(identifier, unmarshaller);
	}

	/**
	 * Resets for next test.
	 */
	public static void reset() {
		unmarshallerRegistry.clear();
	}

	/**
	 * Only methods with the JNDI Object type as input are included as
	 * {@link ManagedFunction} instances.
	 * 
	 * @param notJndiObject
	 *            Not {@link MockJndiObject} being used in tests.
	 */
	public void nonFacade(String notJndiObject) {
	}

	/**
	 * Simple facade {@link ManagedFunction}.
	 * 
	 * @param object
	 *            Required {@link MockJndiObject} parameter type to be included
	 *            as a {@link ManagedFunction}.
	 */
	public void simpleFacade(MockJndiObject object) {
		object.simpleTask();
	}

	/**
	 * Complex facade {@link ManagedFunction}.
	 * 
	 * @param xml
	 *            Test parameter.
	 * @param identifer
	 *            Identifier to the {@link XmlUnmarshaller}.
	 * @param object
	 *            Required {@link MockJndiObject} parameter type to be included
	 *            as a {@link ManagedFunction}.
	 * @return Test return value.
	 * @throws Exception
	 *             Test exception.
	 */
	public Date complexFacade(String xml, Integer identifer,
			MockJndiObject object) throws Exception {

		// Look up the XML unmarshaller
		XmlUnmarshaller unmarshaller = unmarshallerRegistry.get(identifer);

		// Invoke the complex task
		Long value = object.complexTask(xml, unmarshaller);

		// Return value as date
		return new Date(value.longValue());
	}

	/**
	 * Same {@link Method} name as
	 * {@link MockJndiObject#complexTask(String, XmlUnmarshaller)} to override
	 * as the {@link ManagedFunction}.
	 * 
	 * @param object
	 *            Required {@link MockJndiObject} parameter type to be included
	 *            as a {@link ManagedFunction}.
	 */
	public void complexTask(MockJndiObject object) {
		// Only example of overriding task
	}

}