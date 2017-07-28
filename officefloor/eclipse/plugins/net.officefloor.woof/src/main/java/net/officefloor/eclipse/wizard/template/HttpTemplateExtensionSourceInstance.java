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
package net.officefloor.eclipse.wizard.template;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.configuration.project.ProjectConfigurationContext;
import net.officefloor.eclipse.extension.template.WoofTemplateExtensionSourceExtension;
import net.officefloor.eclipse.extension.template.WoofTemplateExtensionSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;
import net.officefloor.model.woof.WoofChangeIssues;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionLoader;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionLoaderImpl;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSource;

/**
 * Instance of the {@link WoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateExtensionSourceInstance {

	/**
	 * Obtains the label from the {@link WoofTemplateExtensionSource} class
	 * name.
	 * 
	 * @param woofTemplateExtensionSourceClassName
	 *            Class name of the {@link WoofTemplateExtensionSource}.
	 * @return Label.
	 */
	public static String getLabelFromWoofTemplateExtensionClassName(String woofTemplateExtensionSourceClassName) {
		String label = woofTemplateExtensionSourceClassName;
		int index = label.lastIndexOf('.');
		if (index > 0) {
			// Strip off package name
			label = label.substring(index + ".".length());
		}
		label = label.replace(WoofTemplateExtensionSource.class.getSimpleName(), "");
		return label;

	}

	/**
	 * Class name of the {@link WoofTemplateExtensionSource}.
	 */
	private final String woofTemplateExtensionSourceClassName;

	/**
	 * {@link WoofTemplateExtensionSourceExtension} for the
	 * {@link WoofTemplateExtensionSource}.
	 */
	private final WoofTemplateExtensionSourceExtension<?> extension;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * Initiate.
	 * 
	 * @param woofTemplateExtensionSourceClassName
	 *            {@link WoofTemplateExtensionSource} class name.
	 * @param extension
	 *            {@link WoofTemplateExtensionSourceExtension}. May be
	 *            <code>null</code>.
	 * @param project
	 *            {@link IProject}.
	 */
	public HttpTemplateExtensionSourceInstance(String woofTemplateExtensionSourceClassName,
			WoofTemplateExtensionSourceExtension<?> extension, IProject project) {
		this.woofTemplateExtensionSourceClassName = woofTemplateExtensionSourceClassName;
		this.extension = extension;
		this.project = project;
	}

	/**
	 * Obtains the {@link WoofTemplateExtensionSource} class name.
	 * 
	 * @return {@link WoofTemplateExtensionSource} class name.
	 */
	public String getWoofTemplateExtensionSourceClassName() {
		return this.woofTemplateExtensionSourceClassName;
	}

	/**
	 * Obtains the {@link WoofTemplateExtensionSource} label.
	 * 
	 * @return {@link WoofTemplateExtensionSource} label.
	 */
	public String getWoofTemplateExtensionLabel() {

		// Determine if have extension label
		if (this.extension != null) {
			String label = this.extension.getWoofTemplateExtensionSourceLabel();
			if (!(EclipseUtil.isBlank(label))) {
				return label;
			}
		}

		// No extension label, so determine label from class name
		return getLabelFromWoofTemplateExtensionClassName(this.woofTemplateExtensionSourceClassName);
	}

	/**
	 * Creates the {@link PropertyList} for the specification.
	 * 
	 * @param issues
	 *            {@link CompilerIssues} to report issues.
	 * @return {@link PropertyList} for the specification.
	 */
	public PropertyList createSpecification(CompilerIssues issues) {

		// Create the class loader
		ClassLoader classLoader = ProjectClassLoader.create(this.project,
				Thread.currentThread().getContextClassLoader());

		// Load the specification
		WoofTemplateExtensionLoader loader = new WoofTemplateExtensionLoaderImpl();
		PropertyList properties = loader.loadSpecification(this.woofTemplateExtensionSourceClassName, classLoader,
				issues);

		// Return the specification
		return properties;
	}

	/**
	 * Creates the {@link Control} instances for the populating the
	 * {@link PropertyList} for the {@link WoofTemplateExtensionSource}.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 * @param context
	 *            {@link WoofTemplateExtensionSourceExtensionContext}.
	 */
	public void createControl(Composite page, final WoofTemplateExtensionSourceExtensionContext context) {

		// Determine if have extension
		if (this.extension != null) {
			// Load configuration via extension
			this.extension.createControl(page, context);

		} else {
			// Provide default editing of the extension properties
			page.setLayout(new GridLayout());
			PropertyListInput input = new PropertyListInput(context.getPropertyList());
			new InputHandler<PropertyList>(page, input, new InputAdapter() {
				@Override
				public void notifyValueChanged(Object value) {
					context.notifyPropertiesChanged();
				}
			});
		}
	}

	/**
	 * Validate the change to configuration for the
	 * {@link WoofTemplateExtensionSource}.
	 * 
	 * @param oldUri
	 *            Old URI.
	 * @param oldProperties
	 *            Old {@link PropertyList}.
	 * @param newUri
	 *            New URI.
	 * @param newProperties
	 *            New {@link PropertyList}.
	 * @param resourceSources
	 *            {@link ResourceSource} instances.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param node
	 *            {@link Node} to report issues against.
	 */
	public void validateChange(String oldUri, PropertyList oldProperties, String newUri, PropertyList newProperties,
			ResourceSource[] resourceSources, final CompilerIssues issues, final Node node) {

		// Create the configuration context
		ConfigurationContext configurationContext = new ProjectConfigurationContext(this.project);

		// Create the class loader
		ClassLoader classLoader = ProjectClassLoader.create(this.project,
				Thread.currentThread().getContextClassLoader());

		// Create the source context
		SourceContext sourceContext = new SourceContextImpl(true, classLoader, resourceSources);

		// Create the property lists
		SourceProperties oldSourceProperties = null;
		if (oldProperties != null) {
			oldSourceProperties = new PropertyListSourceProperties(oldProperties);
		}
		SourceProperties newSourceProperties = null;
		if (newProperties != null) {
			newSourceProperties = new PropertyListSourceProperties(newProperties);
		}

		// Report issues regarding compilation
		WoofChangeIssues changeIssues = new WoofChangeIssues() {

			@Override
			public void addIssue(String message, Throwable cause) {
				issues.addIssue(node, message, cause);
			}

			@Override
			public void addIssue(String message) {
				issues.addIssue(node, message);
			}
		};

		// Load the possible change
		WoofTemplateExtensionLoader loader = new WoofTemplateExtensionLoaderImpl();
		Change<?> change = loader.refactorTemplateExtension(this.woofTemplateExtensionSourceClassName, oldUri,
				oldSourceProperties, newUri, newSourceProperties, configurationContext, sourceContext, changeIssues);

		// Determine if issue
		if (change != null) {

			// Report the conflict
			Conflict[] conflicts = change.getConflicts();
			if ((conflicts != null) && (conflicts.length > 0)) {
				// Report the first conflict
				issues.addIssue(node, conflicts[0].getConflictDescription());
			}
		}
	}

}