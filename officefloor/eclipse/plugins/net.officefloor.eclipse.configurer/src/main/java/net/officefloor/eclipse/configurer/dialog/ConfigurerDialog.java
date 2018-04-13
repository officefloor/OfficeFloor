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
package net.officefloor.eclipse.configurer.dialog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Shell;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import net.officefloor.eclipse.configurer.Actioner;
import net.officefloor.eclipse.configurer.ChoiceBuilder;
import net.officefloor.eclipse.configurer.ClassBuilder;
import net.officefloor.eclipse.configurer.Configuration;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.Configurer;
import net.officefloor.eclipse.configurer.DefaultImages;
import net.officefloor.eclipse.configurer.ErrorListener;
import net.officefloor.eclipse.configurer.FlagBuilder;
import net.officefloor.eclipse.configurer.ListBuilder;
import net.officefloor.eclipse.configurer.MappingBuilder;
import net.officefloor.eclipse.configurer.MultipleBuilder;
import net.officefloor.eclipse.configurer.PropertiesBuilder;
import net.officefloor.eclipse.configurer.ResourceBuilder;
import net.officefloor.eclipse.configurer.TextBuilder;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.model.Model;

/**
 * {@link Dialog} for a {@link Configurer}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConfigurerDialog<M> extends Dialog<M> implements ConfigurationBuilder<M> {

	/**
	 * {@link Configurer}.
	 */
	private final Configurer<M> configurer = new Configurer<>();

	/**
	 * Apply label.
	 */
	private String applyLabel;

	/**
	 * Instantiate.
	 * 
	 * @param title
	 *            Title.
	 * @param headerText
	 *            Header.
	 */
	public ConfigurerDialog(String title, String headerText) {
		this.setTitle(title);
		this.setHeaderText(headerText);
		this.getDialogPane().setPrefSize(600, 400);
		this.setResizable(true);
	}

	/**
	 * Configures the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model}.
	 */
	public void configureModel(M model) {

		// Provide handler for result
		this.setResultConverter((buttonType) -> buttonType.getButtonData() == ButtonData.OK_DONE ? model : null);

		// Load the buttons
		ButtonType applyButtonType = new ButtonType(this.applyLabel, ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().setAll(applyButtonType,
				new ButtonType("Cancel", ButtonData.CANCEL_CLOSE));
		Node applyButton = this.getDialogPane().lookupButton(applyButtonType);

		// Configure the dialog
		Configuration configuration = this.configurer.loadConfiguration(model, this.getDialogPane().contentProperty(),
				new DialogErrorListener(applyButton));

		// Handle expanding (need to clear content to display correctly)
		Node content = this.getDialogPane().getContent();
		this.getDialogPane().expandedProperty().addListener((event) -> {
			if (this.getDialogPane().expandedProperty().get()) {
				// Clear content, so expanded content can be shown
				this.getDialogPane().setContent(null);
			} else {
				// Reset content, as not expended
				this.getDialogPane().setContent(content);
			}
		});

		// Configure the model (with ability to apply configuration)
		Actioner actioner = configuration.getActioner();
		this.showAndWait().ifPresent(response -> actioner.action());
	}

	/*
	 * =============== ConfigurationBuilder ====================
	 */

	@Override
	public void title(String title) {
		this.configurer.title(title);
	}

	@Override
	public ChoiceBuilder<M> choices(String label) {
		return this.configurer.choices(label);
	}

	@Override
	public <I> ListBuilder<M, I> list(String label, Class<I> itemType) {
		return this.configurer.list(label, itemType);
	}

	@Override
	public <I> MultipleBuilder<M, I> multiple(String label, Class<I> itemType) {
		return this.configurer.multiple(label, itemType);
	}

	@Override
	public PropertiesBuilder<M> properties(String label) {
		return this.configurer.properties(label);
	}

	@Override
	public MappingBuilder<M> map(String label, Function<M, ObservableList<String>> getSources,
			Function<M, ObservableList<String>> getTargets) {
		return this.configurer.map(label, getSources, getTargets);
	}

	@Override
	public ClassBuilder<M> clazz(String label, IJavaProject javaProject, Shell shell) {
		return this.configurer.clazz(label, javaProject, shell);
	}

	@Override
	public ResourceBuilder<M> resource(String label, IJavaProject javaProject, Shell shell) {
		return this.configurer.resource(label, javaProject, shell);
	}

	@Override
	public TextBuilder<M> text(String label) {
		return this.configurer.text(label);
	}

	@Override
	public FlagBuilder<M> flag(String label) {
		return this.configurer.flag(label);
	}

	@Override
	public void validate(ValueValidator<M> validator) {
		this.configurer.validate(validator);
	}

	@Override
	public void apply(String label, Consumer<M> applier) {
		this.applyLabel = label;
		this.configurer.apply(label, applier);
	}

	/**
	 * Dialog {@link ErrorListener}.
	 */
	private class DialogErrorListener implements ErrorListener {

		/**
		 * Apply button {@link Node}.
		 */
		private final Node applyButton;

		/**
		 * Instantiate.
		 * 
		 * @param applyButton
		 *            Apply button {@link Node}.
		 */
		private DialogErrorListener(Node applyButton) {
			this.applyButton = applyButton;
		}

		/*
		 * ================ ErrorListener ===================
		 */

		@Override
		public void error(String inputLabel, String message) {

			// Error label
			Label errorLabel = new Label("  " + (inputLabel == null ? "" : inputLabel + ": ") + message);
			errorLabel.getStyleClass().add("configurer-error-header");

			// Error image
			ImageView errorImage = new ImageView(new Image(DefaultImages.ERROR_IMAGE_PATH, 15, 15, true, true));
			Tooltip errorTooltip = new Tooltip(message);
			errorTooltip.getStyleClass().add("error-tooltip");
			Tooltip.install(errorImage, errorTooltip);

			// Header error panel
			GridPane panel = new GridPane();
			panel.getStyleClass().add("header-panel");
			panel.add(errorImage, 0, 0);
			panel.add(errorLabel, 1, 0);

			// Configure the error
			ConfigurerDialog.this.getDialogPane().setHeader(panel);
			ConfigurerDialog.this.getDialogPane().setExpandableContent(null);
			this.applyButton.setDisable(true);
		}

		@Override
		public void error(String inputLabel, Throwable error) {
			this.error(inputLabel, error.getMessage());

			// Obtain the stack trace of error
			StringWriter buffer = new StringWriter();
			error.printStackTrace(new PrintWriter(buffer));
			String exceptionText = buffer.toString();

			// Create text area for stack trace
			TextArea textArea = new TextArea(exceptionText);
			textArea.setEditable(false);
			textArea.setWrapText(true);
			textArea.setMaxWidth(Double.MAX_VALUE);
			textArea.setMaxHeight(Double.MAX_VALUE);
			GridPane.setVgrow(textArea, Priority.ALWAYS);
			GridPane.setHgrow(textArea, Priority.ALWAYS);

			// Provide the stack trace
			GridPane content = new GridPane();
			content.setMaxWidth(Double.MAX_VALUE);
			content.add(new Label("The exception stacktrace was:"), 0, 0);
			content.add(textArea, 0, 1);
			ConfigurerDialog.this.getDialogPane().setExpandableContent(content);
		}

		@Override
		public void valid() {
			ConfigurerDialog.this.getDialogPane().setHeader(null);
			ConfigurerDialog.this.getDialogPane().setExpandableContent(null);
			this.applyButton.setDisable(false);
		}
	}

}