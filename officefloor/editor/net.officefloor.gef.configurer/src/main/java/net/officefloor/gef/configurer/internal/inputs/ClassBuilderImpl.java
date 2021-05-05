/*-
 * #%L
 * [bundle] OfficeFloor Configurer
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

package net.officefloor.gef.configurer.internal.inputs;

import java.util.function.Consumer;

import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.bridge.EnvironmentBridge.SelectionHandler;
import net.officefloor.gef.configurer.ClassBuilder;
import net.officefloor.gef.configurer.internal.AbstractBuilder;
import net.officefloor.gef.configurer.internal.ValueInput;
import net.officefloor.gef.configurer.internal.ValueInputContext;

/**
 * {@link ClassBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassBuilderImpl<M> extends AbstractBuilder<M, String, ClassBuilderImpl.ClassValueInput, ClassBuilder<M>>
		implements ClassBuilder<M> {

	/**
	 * {@link EnvironmentBridge}.
	 */
	private final EnvironmentBridge envBridge;

	/**
	 * Super type for required {@link Class}.
	 */
	private String superType = null;

	/**
	 * Instantiate.
	 * 
	 * @param label     Label.
	 * @param envBridge {@link EnvironmentBridge}.
	 */
	public ClassBuilderImpl(String label, EnvironmentBridge envBridge) {
		super(label);
		this.envBridge = envBridge;
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
	protected ClassValueInput createInput(ValueInputContext<M, String> context) {

		// Obtain the value
		Property<String> value = context.getInputValue();

		// Ensure run validation for super type
		context.addValidator((ctx) -> {

			// Determine if have class name
			String className = value.getValue();
			if ((className == null) || (className.trim().length() == 0)) {
				return; // no class provided
			}

			// Ensure class on class path
			if (!this.envBridge.isClassOnClassPath(className)) {
				ctx.setError("Class " + className + " not on project's class path");
				return;
			}

			// Ensure super type (if super type specified)
			if ((this.superType != null) && (!this.envBridge.isSuperType(className, this.superType))) {
				ctx.setError("Must be child of " + this.superType);
				return;
			}
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
		ClassValueInput valueInput = new ClassValueInput(pane);

		// Hook in handlers
		text.setOnKeyPressed((event) -> {
			if ((event.isControlDown()) && (event.getCode() == KeyCode.SPACE)) {
				this.doClassSelection(value, valueInput);
			}
		});
		suggestion.setOnAction((event) -> this.doClassSelection(value, valueInput));

		// Return the value input
		return valueInput;
	}

	@Override
	protected Node createErrorFeedback(ClassValueInput valueInput, Property<Throwable> errorProperty) {

		// Hook in to error property for issues working with Java model
		valueInput.errorProperty = errorProperty;

		// Provide error feedback
		return super.createErrorFeedback(valueInput, errorProperty);
	}

	/**
	 * Does the selection of a class.
	 * 
	 * @param text       {@link Property} for the text.
	 * @param valueInput {@link ClassValueInput}.
	 */
	private void doClassSelection(Property<String> text, ClassValueInput valueInput) {
		Consumer<Exception> errorHandler = (ex) -> valueInput.errorProperty
				.setValue(new Exception("Plugin Error: " + ex.getMessage(), ex));
		try {

			// Select the class
			this.envBridge.selectClass(text.getValue(), this.superType, new SelectionHandler() {

				@Override
				public void selected(String className) {
					if (className != null) {
						text.setValue(className);
					}
				}

				@Override
				public void error(Exception error) {
					errorHandler.accept(error);
				}

				@Override
				public void cancelled() {
					// No operation
				}
			});

		} catch (Exception ex) {
			errorHandler.accept(ex);
		}
	}

	/**
	 * {@link Class} {@link ValueInput}.
	 */
	public static class ClassValueInput implements ValueInput {

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
		 * @param inputNode {@link ValueInput} {@link Node}.
		 */
		private ClassValueInput(Node inputNode) {
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
