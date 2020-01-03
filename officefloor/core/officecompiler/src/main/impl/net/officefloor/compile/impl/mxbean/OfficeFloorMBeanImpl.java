/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.mxbean;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.manage.UnknownOfficeException;

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

		switch (attribute) {
		case "OfficeNames":
			return this.officeFloor.getOfficeNames();
		}

		// As here, unknown attribute
		throw new AttributeNotFoundException("Unknown attribute: " + attribute);
	}

	@Override
	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		// Unable to change attributes
		throw new InvalidAttributeValueException("Unable to change attributes");
	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		AttributeList list = new AttributeList();
		for (String attribute : attributes) {
			try {
				list.add(this.getAttribute(attribute));
			} catch (Exception ex) {
				// Ignore attribute
			}
		}
		return list;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		// Unable to change attributes
		return new AttributeList();
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {

		switch (actionName) {
		case "getManagedFunctionNames":
			try {
				return this.officeFloor.getOffice((String) params[0]).getFunctionNames();
			} catch (UnknownOfficeException ex) {
				throw new MBeanException(ex);
			}

		case "getManagedFunctionParameterType":
			try {
				Class<?> parameterType = this.officeFloor.getOffice((String) params[0])
						.getFunctionManager((String) params[1]).getParameterType();
				return parameterType != null ? parameterType.getName() : null;
			} catch (UnknownOfficeException | UnknownFunctionException ex) {
				throw new MBeanException(ex);
			}

		case "invokeFunction":
			try {
				this.officeFloor.getOffice((String) params[0]).getFunctionManager((String) params[1])
						.invokeProcess(params[2], null);
				return null;
			} catch (UnknownOfficeException | UnknownFunctionException | InvalidParameterTypeException ex) {
				throw new MBeanException(ex);
			}

		case "closeOfficeFloor":
			try {
				this.officeFloor.closeOfficeFloor();
				return null;
			} catch (Exception ex) {
				throw new MBeanException(ex);
			}
		}

		// As here, unknown operation
		throw new MBeanException(new Exception("Unknown operation: " + actionName));
	}

	@Override
	public MBeanInfo getMBeanInfo() {

		// Create the attributes
		MBeanAttributeInfo officeNames = new MBeanAttributeInfo("OfficeNames", String[].class.getName(),
				"Names of the Offices within the OfficeFloor", true, false, false);

		// Create the operations
		MBeanOperationInfo getManagedFunctions = new MBeanOperationInfo("getManagedFunctionNames",
				"Obtains the function names within the Office",
				new MBeanParameterInfo[] {
						new MBeanParameterInfo("officeName", String.class.getName(), "Name of the Office") },
				String[].class.getName(), MBeanOperationInfo.INFO);
		MBeanOperationInfo getManagedFunctionParameterType = new MBeanOperationInfo("getManagedFunctionParameterType",
				"Obtains the parameter type for the function",
				new MBeanParameterInfo[] {
						new MBeanParameterInfo("officeName", String.class.getName(), "Name of the Office"),
						new MBeanParameterInfo("managedFunctionName", String.class.getName(),
								"Name of the managed function") },
				String.class.getName(), MBeanOperationInfo.INFO);
		MBeanOperationInfo invokeFunction = new MBeanOperationInfo("invokeFunction",
				"Invokes a function within the OfficeFloor",
				new MBeanParameterInfo[] {
						new MBeanParameterInfo("officeName", String.class.getName(), "Name of the Office"),
						new MBeanParameterInfo("functionName", String.class.getName(),
								"Name of the function to invoke within the Office"),
						new MBeanParameterInfo("parameter", Object.class.getName(), "Parameter to the function") },
				null, MBeanOperationInfo.ACTION);
		MBeanOperationInfo closeOfficeFloor = new MBeanOperationInfo("closeOfficeFloor", "Closes the OfficeFloor",
				new MBeanParameterInfo[] {}, null, MBeanOperationInfo.ACTION);

		// Return the MBean Info
		return new MBeanInfo(OfficeFloorMBeanImpl.class.getName(), "MBean to managed the OfficeFloor",
				new MBeanAttributeInfo[] { officeNames }, new MBeanConstructorInfo[0],
				new MBeanOperationInfo[] { getManagedFunctions, getManagedFunctionParameterType, invokeFunction, closeOfficeFloor },
				new MBeanNotificationInfo[0]);
	}

}
