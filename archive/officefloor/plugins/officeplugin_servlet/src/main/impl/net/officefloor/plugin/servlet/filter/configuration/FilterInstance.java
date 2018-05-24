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
package net.officefloor.plugin.servlet.filter.configuration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.plugin.servlet.context.source.OfficeServletContextManagedObjectSource;

/**
 * Configuration of a {@link Filter} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterInstance {

	/**
	 * Loads the {@link FilterInstance} instances from the {@link PropertyList}.
	 * 
	 * @param properties
	 *            {@link PropertyList}.
	 * @return {@link FilterInstance} instances.
	 */
	public static FilterInstance[] loadFilterInstances(PropertyList properties) {

		// Load filter instances
		List<FilterInstance> instances = new LinkedList<FilterInstance>();
		for (String propertyName : properties.getPropertyNames()) {
			if (propertyName
					.startsWith(OfficeServletContextManagedObjectSource.PROPERTY_FILTER_INSTANCE_NAME_PREFIX)) {

				// Obtain the filter instance name
				String name = propertyName
						.substring(OfficeServletContextManagedObjectSource.PROPERTY_FILTER_INSTANCE_NAME_PREFIX
								.length());

				// Create the filter instance
				FilterInstance instance = new FilterInstance(name);
				instance.inputProperties(properties);

				// Add the filter instance
				instances.add(instance);
			}
		}

		// Return the filter instances
		return instances.toArray(new FilterInstance[0]);
	}

	/**
	 * Name of the {@link Filter}.
	 */
	private String name;

	/**
	 * {@link Filter} class name.
	 */
	private String className;

	/**
	 * Init parameters for the {@link Filter}.
	 */
	private final List<InitParam> initParameters = new LinkedList<InitParam>();

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name of the {@link Filter}.
	 */
	public FilterInstance(String name) {
		this.name = name;
	}

	/**
	 * Convenience constructor to fully initialise.
	 * 
	 * @param name
	 *            Name of the {@link Filter}.
	 * @param className
	 *            {@link Filter} class name.
	 * @param initParameterNameValues
	 *            Init parameters for the {@link Filter}.
	 */
	public FilterInstance(String name, String className,
			String... initParameterNameValues) {
		this.name = name;
		this.className = className;
		for (int i = 0; i < initParameterNameValues.length; i += 2) {
			this.initParameters.add(new InitParam(initParameterNameValues[i],
					initParameterNameValues[i + 1]));
		}
	}

	/**
	 * Obtains the name of the {@link Filter}.
	 * 
	 * @return Name of the {@link Filter}.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Specifies the name of the {@link Filter}.
	 * 
	 * @param name
	 *            Name of the {@link Filter}.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Obtains the {@link Class} name of the {@link Filter}.
	 * 
	 * @return {@link Class} name of the {@link Filter}.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Specifies the {@link Class} name of the {@link Filter}.
	 * 
	 * @param className
	 *            {@link Class} name of the {@link Filter}.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Adds an init parameter.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public void addInitParameter(String name, String value) {
		this.initParameters.add(new InitParam(name, value));
	}

	/**
	 * Obtains the init parameters for the {@link Filter}.
	 * 
	 * @return Init parameters for the {@link Filter}.
	 */
	public Map<String, String> getInitParameters() {
		Map<String, String> params = new HashMap<String, String>();
		for (InitParam initParam : this.initParameters) {
			params.put(initParam.name, initParam.value);
		}
		return params;
	}

	/**
	 * Output the {@link Property} instances.
	 * 
	 * @param properties
	 *            {@link PropertyList} to output {@link Property} instances.
	 */
	public void outputProperties(PropertyList properties) {

		// Write out the filter name and class name
		String propertyName = OfficeServletContextManagedObjectSource.PROPERTY_FILTER_INSTANCE_NAME_PREFIX
				+ this.name;
		properties.addProperty(propertyName).setValue(this.className);

		// Write out the filter init parameters
		String initParameterPrefix = OfficeServletContextManagedObjectSource.PROPERTY_FILTER_INSTANCE_INIT_PREFIX
				+ this.name + ".";
		for (InitParam initParam : this.initParameters) {
			properties.addProperty(initParameterPrefix + initParam.name)
					.setValue(initParam.value);
		}
	}

	/**
	 * Input the {@link Property} instances.
	 * 
	 * @param properties
	 *            {@link PropertyList} to input {@link Property} instances.
	 */
	public void inputProperties(PropertyList properties) {

		// Obtain the class name
		String propertyName = OfficeServletContextManagedObjectSource.PROPERTY_FILTER_INSTANCE_NAME_PREFIX
				+ this.name;
		this.className = properties.getPropertyValue(propertyName, null);

		// Load the init parameters
		String initParameterPrefix = OfficeServletContextManagedObjectSource.PROPERTY_FILTER_INSTANCE_INIT_PREFIX
				+ this.name + ".";
		for (Property property : properties) {

			// Determine if init parameter for this Filter instance
			String name = property.getName();
			if (!name.startsWith(initParameterPrefix)) {
				continue; // not init parameter for this Filter instance
			}

			// Obtain the init parameter name and value
			name = name.substring(initParameterPrefix.length());
			String value = property.getValue();

			// Configure in the init parameter
			this.initParameters.add(new InitParam(name, value));
		}
	}

	/**
	 * Init-param for the {@link Filter}.
	 */
	private static class InitParam {

		/**
		 * Name.
		 */
		public final String name;

		/**
		 * Value.
		 */
		public final String value;

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Name.
		 * @param value
		 *            Value.
		 */
		public InitParam(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}

}