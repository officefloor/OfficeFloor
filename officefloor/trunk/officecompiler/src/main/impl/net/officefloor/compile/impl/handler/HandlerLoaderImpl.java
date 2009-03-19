/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.handler;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.handler.HandlerLoader;
import net.officefloor.compile.spi.handler.HandlerType;
import net.officefloor.compile.spi.handler.source.HandlerSource;
import net.officefloor.compile.spi.handler.source.HandlerSourceProperty;
import net.officefloor.compile.spi.handler.source.HandlerSourceSpecification;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Handler;

/**
 * {@link HandlerLoader} implementation.
 * 
 * @author Daniel
 */
public class HandlerLoaderImpl implements HandlerLoader {

	/**
	 * {@link LocationType}.
	 */
	private LocationType locationType;

	/**
	 * Location.
	 */
	private final String location;

	/**
	 * Name of the {@link Handler}.
	 */
	private final String handlerName;

	/**
	 * Initiate.
	 * 
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Location.
	 * @param handlerName
	 *            Name of the {@link Handler}.
	 */
	public HandlerLoaderImpl(LocationType locationType, String location,
			String handlerName) {
		this.locationType = locationType;
		this.location = location;
		this.handlerName = handlerName;
	}

	/*
	 * ====================== HandlerLoader ==========================
	 */

	@Override
	public <HS extends HandlerSource> PropertyList loadSpecification(
			Class<HS> handlerSourceClass, CompilerIssues issues) {

		// Instantiate the handler source
		HandlerSource handlerSource = CompileUtil.newInstance(
				handlerSourceClass, HandlerSource.class, this.locationType,
				this.location, AssetType.HANDLER, this.handlerName, issues);
		if (handlerSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		HandlerSourceSpecification specification;
		try {
			specification = handlerSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ HandlerSourceSpecification.class.getSimpleName()
					+ " from " + handlerSourceClass.getName(), ex, issues);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No "
					+ HandlerSourceSpecification.class.getSimpleName()
					+ " returned from " + handlerSourceClass.getName(), issues);
			return null; // no specification obtained
		}

		// Obtain the properties
		HandlerSourceProperty[] handlerSourceProperties;
		try {
			handlerSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ HandlerSourceProperty.class.getSimpleName()
					+ " instances from "
					+ HandlerSourceSpecification.class.getSimpleName()
					+ " for " + handlerSourceClass.getName(), ex, issues);
			return null; // failed to obtain properties
		}

		// Load the handler source properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (handlerSourceProperties != null) {
			for (int i = 0; i < handlerSourceProperties.length; i++) {
				HandlerSourceProperty handlerProperty = handlerSourceProperties[i];

				// Ensure have the handler source property
				if (handlerProperty == null) {
					this.addIssue(HandlerSourceProperty.class.getSimpleName()
							+ " " + i + " is null from "
							+ HandlerSourceSpecification.class.getSimpleName()
							+ " for " + handlerSourceClass.getName(), issues);
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = handlerProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for "
							+ HandlerSourceProperty.class.getSimpleName() + " "
							+ i + " from "
							+ HandlerSourceSpecification.class.getSimpleName()
							+ " for " + handlerSourceClass.getName(), ex,
							issues);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(HandlerSourceProperty.class.getSimpleName()
							+ " " + i + " provided blank name from "
							+ HandlerSourceSpecification.class.getSimpleName()
							+ " for " + handlerSourceClass.getName(), issues);
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = handlerProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for "
							+ HandlerSourceProperty.class.getSimpleName() + " "
							+ i + " (" + name + ") from "
							+ HandlerSourceSpecification.class.getSimpleName()
							+ " for " + handlerSourceClass.getName(), ex,
							issues);
					return null; // must have complete property details
				}

				// Add to the properties
				propertyList.addProperty(name, label);
			}
		}

		// Return the property list
		return propertyList;
	}

	@Override
	public <F extends Enum<F>, HS extends HandlerSource> HandlerType<F> loadHandler(
			Class<HS> handlerSourceClass, PropertyList propertyList,
			ClassLoader classLoader, CompilerIssues issues) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement HandlerLoader.loadHandler");
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	private void addIssue(String issueDescription, CompilerIssues issues) {
		issues.addIssue(this.locationType, this.location, AssetType.HANDLER,
				this.handlerName, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	private void addIssue(String issueDescription, Throwable cause,
			CompilerIssues issues) {
		issues.addIssue(this.locationType, this.location, AssetType.HANDLER,
				this.handlerName, issueDescription, cause);
	}

}