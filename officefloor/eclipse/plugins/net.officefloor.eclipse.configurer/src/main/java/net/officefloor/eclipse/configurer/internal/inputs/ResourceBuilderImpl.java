/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.configurer.internal.inputs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;

import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import net.officefloor.eclipse.configurer.ResourceBuilder;
import net.officefloor.eclipse.configurer.internal.AbstractBuilder;
import net.officefloor.eclipse.configurer.internal.MessageOnlyException;
import net.officefloor.eclipse.configurer.internal.ValueInput;
import net.officefloor.eclipse.configurer.internal.ValueInputContext;

/**
 * {@link ResourceBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ResourceBuilderImpl<M>
		extends AbstractBuilder<M, String, ResourceBuilderImpl.ResourceValueInput, ResourceBuilder<M>>
		implements ResourceBuilder<M> {

	/**
	 * {@link IJavaProject}.
	 */
	private final IJavaProject javaProject;

	/**
	 * {@link Shell}.
	 */
	private final Shell shell;

	/**
	 * Instantiate.
	 * 
	 * @param label
	 *            Label.
	 * @param javaProject
	 *            {@link IJavaProject}.
	 * @param shell
	 *            {@link Shell}.
	 */
	public ResourceBuilderImpl(String label, IJavaProject javaProject, Shell shell) {
		super(label);
		this.javaProject = javaProject;
		this.shell = shell;
	}

	/*
	 * ================ AbstractBuilder ==================
	 */

	@Override
	protected ResourceValueInput createInput(ValueInputContext<M, String> context) {

		// Obtain the value
		Property<String> value = context.getInputValue();

		// Add validation (to ensure resource existing in project)
		context.addValidator((ctx) -> {

			// Obtain the resource for the path
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

			// Obtains the path
			String path = value.getValue();

			// Determine if resource on the class path
			IClasspathEntry[] classPath = this.javaProject.getResolvedClasspath(true);
			for (IClasspathEntry entry : classPath) {

				// Obtain the class path resource
				IPath fullPath = entry.getPath().append(path);

				// Obtain the resource
				IResource resource = workspaceRoot.findMember(fullPath);
				if (resource == null) {
					continue;
				}

				// Determine if resource on class path
				if (this.javaProject.isOnClasspath(resource)) {
					return; // resource on class path
				}
			}

			// As here, resource not on class path
			ctx.setError("Resource not on project's class path");
		});

		// Provide editable text box
		TextField text = new TextField();
		text.textProperty().bindBidirectional(value);
		text.getStyleClass().add("configurer-input-class");
		HBox.setHgrow(text, Priority.ALWAYS);

		// Provide button for suggestions
		Button suggestion = new Button("...");
		suggestion.getStyleClass().add("configurer-input-suggestion");

		// Create the value input
		HBox pane = new HBox();
		pane.getChildren().setAll(text, suggestion);
		ResourceValueInput valueInput = new ResourceValueInput(pane);

		// Hook in handlers
		text.setOnKeyPressed((event) -> {
			if ((event.isControlDown()) && (event.getCode() == KeyCode.SPACE)) {
				this.doResourceSelection(value, valueInput);
			}
		});
		suggestion.setOnAction((event) -> this.doResourceSelection(value, valueInput));

		// Return the value input
		return valueInput;
	}

	/**
	 * Does the selection of a resource.
	 * 
	 * @param text
	 *            {@link Property} for the text.
	 * @param valueInput
	 *            {@link ResourceValueInput}.
	 */
	private void doResourceSelection(Property<String> text, ResourceValueInput valueInput) {
		try {

			// Strip filter down to just the simple name
			String filter = text.getValue();
			int index = filter.lastIndexOf('/');
			if (index >= 0) {
				filter = filter.substring(index + "/".length());
			}
			index = filter.indexOf('.');
			if (index >= 0) {
				filter = filter.substring(0, index);
			}

			// Obtain the selected file
			FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(this.shell, false,
					this.javaProject.getProject(), IResource.FILE);
			dialog.setInitialPattern(filter);
			dialog.setBlockOnOpen(true);
			dialog.open();
			Object[] results = dialog.getResult();
			if ((results == null) || (results.length != 1)) {
				return; // cancel
			}

			// Obtain the selected item
			Object selectedItem = results[0];
			if (selectedItem instanceof IFile) {
				// Specify class path location for file
				IFile file = (IFile) selectedItem;
				String filePath = getClassPathLocation(file);
				text.setValue(filePath);
			} else {
				// Unknown type
				valueInput.errorProperty.setValue(
						new MessageOnlyException("Plugin Error: selected item is not of " + IFile.class.getName() + " ["
								+ (selectedItem == null ? null : selectedItem.getClass().getName()) + "]"));
			}

		} catch (Exception ex) {
			valueInput.errorProperty.setValue(new Exception("Plugin Error: " + ex.getMessage(), ex));
		}
	}

	/**
	 * Obtains the class path location for the {@link IFile}.
	 * 
	 * @param file
	 *            {@link IFile}.
	 * @return Class path location for the {@link IFile}.
	 */
	public String getClassPathLocation(IFile file) {

		// Obtain the resource for the path
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IResource pathResource = workspaceRoot.findMember(file.getFullPath());
		IResource resource = pathResource;

		// Obtain the java element
		IJavaElement javaElement = null;
		do {
			// Ensure have the resource
			if (resource == null) {
				// Did not find java element for resource
				return null;
			}

			// Obtain the java element from the resource
			javaElement = JavaCore.create(resource);

			// Obtain the parent resource
			resource = resource.getParent();

		} while (javaElement == null);

		// Obtain the package fragment root for the java element
		IPackageFragmentRoot fragmentRoot = null;
		do {

			// Determine if package fragment root
			if (javaElement instanceof IPackageFragmentRoot) {
				fragmentRoot = (IPackageFragmentRoot) javaElement;
			}

			// Obtain the parent java element
			javaElement = javaElement.getParent();

		} while ((fragmentRoot == null) && (javaElement != null));

		// Determine if have fragment root
		if (fragmentRoot == null) {
			// Return path as is
			return file.getFullPath().toString();
		}

		// Obtain the fragment root full path
		String fragmentPath = fragmentRoot.getResource().getFullPath().toString() + "/";

		// Obtain the class path location (by removing fragment root path)
		String fullPath = file.getFullPath().toString();
		String location = fullPath.substring(fragmentPath.length());

		// Return the location
		return location;
	}

	/**
	 * Resource {@link ValueInput}.
	 */
	public static class ResourceValueInput implements ValueInput {

		/**
		 * {@link ValueInput} {@link Node}.
		 */
		private final Node inputNode;

		/**
		 * Error {@link Property}.
		 */
		private Property<Throwable> errorProperty;

		/**
		 * Instantiate.
		 * 
		 * @param inputNode
		 *            {@link ValueInput} {@link Node}.
		 */
		private ResourceValueInput(Node inputNode) {
			this.inputNode = inputNode;
		}

		/*
		 * ============== ValueInput
		 */

		@Override
		public Node getNode() {
			return this.inputNode;
		}
	}

}