/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlUnmarshaller;

/**
 * <p>
 * Mock JNDI Object.
 * <p>
 * Typically this may be an EJB.
 * 
 * @author Daniel Sagenschneider
 */
public interface MockJndiObject {

	/**
	 * Simple {@link Task}.
	 */
	public void simpleTask();

	/**
	 * Complex {@link Task}.
	 * 
	 * @param xml
	 *            Test parameter.
	 * @param unmarshaller
	 *            {@link XmlUnmarshaller} for test {@link ManagedObject}.
	 * @return Test return value.
	 */
	public long complexTask(String xml, XmlUnmarshaller unmarshaller)
			throws XmlMarshallException;

}