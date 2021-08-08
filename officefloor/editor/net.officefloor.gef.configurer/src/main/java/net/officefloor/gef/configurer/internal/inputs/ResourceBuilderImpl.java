/*-
 * #%L
 * [bundle] OfficeFloor Configurer
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import net.officefloor.gef.configurer.ResourceBuilder;
import net.officefloor.gef.configurer.internal.AbstractBuilder;
import net.officefloor.gef.configurer.internal.ValueInput;
import net.officefloor.gef.configurer.internal.ValueInputContext;

/**
 * {@link ResourceBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ResourceBuilderImpl<M>
		extends AbstractBuilder<M, String, ResourceBuilderImpl.ResourceValueInput, ResourceBuilder<M>>
		implements ResourceBuilder<M> {

	/**
	 * {@link EnvironmentBridge}.
	 */
	private final EnvironmentBridge envBridge;

	/**
	 * Instantiate.
	 * 
	 * @param label     Label.
	 * @param envBridge {@link EnvironmentBridge}.
	 */
	public ResourceBuilderImpl(String label, EnvironmentBridge envBridge) {
		super(label);
		this.envBridge = envBridge;
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

			// Determine if have resource
			String resourcePath = value.getValue();
			if ((resourcePath == null) || (resourcePath.trim().length() == 0)) {
				return; // no resource path provided
			}

			// Determine if resource on the class path
			if (!this.envBridge.isResourceOnClassPath(resourcePath)) {
				ctx.setError("resource '" + resourcePath + "' not on project's class path");
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

	@Override
	protected Node createErrorFeedback(ResourceValueInput valueInput, Property<Throwable> errorProperty) {

		// Hook in to error property for issues working with Java model
		valueInput.errorProperty = errorProperty;

		// Provide error feedback
		return super.createErrorFeedback(valueInput, errorProperty);
	}

	/**
	 * Does the selection of a resource.
	 * 
	 * @param text       {@link Property} for the text.
	 * @param valueInput {@link ResourceValueInput}.
	 */
	private void doResourceSelection(Property<String> text, ResourceValueInput valueInput) {
		Consumer<Exception> errorHandler = (ex) -> valueInput.errorProperty
				.setValue(new Exception("Plugin Error: " + ex.getMessage(), ex));
		try {

			// Select resource from class path
			this.envBridge.selectClassPathResource(text.getValue(), new SelectionHandler() {

				@Override
				public void selected(String resourcePath) {
					if (resourcePath != null) {
						text.setValue(resourcePath);
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
		 * @param inputNode {@link ValueInput} {@link Node}.
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
