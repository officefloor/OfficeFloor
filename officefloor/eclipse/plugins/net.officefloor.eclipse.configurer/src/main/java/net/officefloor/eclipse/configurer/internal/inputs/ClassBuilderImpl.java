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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.Property;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import net.officefloor.eclipse.configurer.ClassBuilder;
import net.officefloor.eclipse.configurer.internal.AbstractBuilder;
import net.officefloor.eclipse.configurer.internal.ValueInput;
import net.officefloor.eclipse.configurer.internal.ValueInputContext;

/**
 * {@link ClassBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassBuilderImpl<M> extends AbstractBuilder<M, String, ValueInput, ClassBuilder<M>>
		implements ClassBuilder<M> {

	/**
	 * {@link Logger}
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassBuilderImpl.class);

	/**
	 * {@link IJavaProject}.
	 */
	private final IJavaProject javaProject;

	/**
	 * {@link Shell}.
	 */
	private final Shell shell;

	/**
	 * Super type for required {@link Class}.
	 */
	private String superType = null;

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
	public ClassBuilderImpl(String label, IJavaProject javaProject, Shell shell) {
		super(label);
		this.javaProject = javaProject;
		this.shell = shell;
	}

	/*
	 * ============= ClassBuilder =======================
	 */

	@Override
	public ClassBuilder<M> superType(Class<?> superType) {
		this.superType = superType.getName();
		return this;
	}

	/*
	 * ================= AbstractBuilder ===============
	 */

	@Override
	protected ValueInput createInput(ValueInputContext<M, String> context) {

		// Obtain the value
		Property<String> value = context.getInputValue();

		// Ensure run validation for super type
		if (this.superType != null) {
			context.addValidator((ctx) -> {

				// Determine if have class name
				String className = value.getValue();
				if ((className == null) || (className.trim().length() == 0)) {
					return; // no class provided
				}

				// Obtain the class type
				IType type = ClassBuilderImpl.this.javaProject.findType(className);
				if (type == null) {
					ctx.setError("Class " + className + " not on project's class path");
					return;
				}

				// Obtain the super type from the project
				String superTypeClassName = ClassBuilderImpl.this.superType;
				IType superType = ClassBuilderImpl.this.javaProject.findType(superTypeClassName);
				if (superType == null) {
					// Type not on project's class path
					ctx.setError("Please add " + superTypeClassName + " to the project's class path");
					return;
				}

				// Ensure child of super type
				ITypeHierarchy typeHierarchy = type.newTypeHierarchy(new NullProgressMonitor());
				List<IType> superTypes = Arrays.asList(typeHierarchy.getAllSupertypes(type));
				boolean isSuperType = superTypes.stream().anyMatch(supertype -> {
					return supertype.getFullyQualifiedName().equals(ClassBuilderImpl.this.superType);
				});
				if (!isSuperType) {
					ctx.setError("Must be child of " + ClassBuilderImpl.this.superType);
				}
			});
		}

		// Provide editable text box
		TextField text = new TextField();
		text.textProperty().bindBidirectional(value);
		text.getStyleClass().add("configurer-input-class");
		text.setOnKeyPressed((event) -> {
			if ((event.isControlDown()) && (event.getCode() == KeyCode.SPACE)) {
				this.doClassSelection(value);
			}
		});

		// Provide button for suggestions
		Button suggestion = new Button("...");
		suggestion.getStyleClass().add("configurer-input-suggestion");
		suggestion.setOnAction((event) -> this.doClassSelection(value));

		// Return value input
		HBox pane = new HBox();
		pane.getChildren().setAll(text, suggestion);
		return () -> pane;
	}

	/**
	 * Does the selection of a class.
	 * 
	 * @param text
	 *            {@link Property} for the text.
	 */
	private void doClassSelection(Property<String> text) {
		try {

			// Obtain the text
			String textValue = text.getValue();
			textValue = (textValue == null) ? "" : textValue;

			// Obtain the search scope
			IJavaSearchScope scope = null;
			if (this.superType != null) {
				// Obtain the super type from the project
				IType superType = this.javaProject.findType(this.superType);
				if (superType != null) {
					// Search for sub type class
					scope = SearchEngine.createStrictHierarchyScope(this.javaProject, superType, true, true, null);
				}
			}
			if (scope == null) {
				// No hierarchy, so search for any class
				scope = SearchEngine.createJavaSearchScope(new IJavaProject[] { this.javaProject }, true);
			}

			// Search for any class
			SelectionDialog dialog = JavaUI.createTypeDialog(this.shell, new ProgressMonitorDialog(this.shell), scope,
					IJavaElementSearchConstants.CONSIDER_CLASSES, false, textValue);
			dialog.setBlockOnOpen(true);
			dialog.open();
			Object[] results = dialog.getResult();
			if ((results == null) || (results.length != 1)) {
				return; // cancel
			}

			// Obtain the selected item
			Object selectedItem = results[0];
			if (selectedItem instanceof IType) {
				// Set text to the type
				textValue = ((IType) selectedItem).getFullyQualifiedName();
				text.setValue(textValue);

			} else {
				// Unknown type
				LOGGER.warn("Selected item is not a java type: " + selectedItem);
			}

		} catch (Exception ex) {
			LOGGER.error("Failed to select a java type", ex);
		}
	}

}