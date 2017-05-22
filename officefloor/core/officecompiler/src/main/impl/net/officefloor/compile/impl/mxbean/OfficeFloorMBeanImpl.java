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
package net.officefloor.compile.impl.mxbean;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link DynamicMBean} for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMBeanImpl implements DynamicMBean {

	/**
	 * {@link OfficeFloor}.
	 */
	private final OfficeFloor officeFloor;

	/**
	 * Instantiate.
	 * 
	 * @param officeFloor
	 *            {@link OfficeFloor}.
	 */
	public OfficeFloorMBeanImpl(OfficeFloor officeFloor) {
		this.officeFloor = officeFloor;
	}

	/*
	 * ================== DynamicMBean =========================
	 */

	@Override
	public Object getAttribute(String attribute)
			throws AttributeNotFoundException, MBeanException, ReflectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		// TODO Auto-generated method stub

	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
