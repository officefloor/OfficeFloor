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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import net.officefloor.eclipse.configurer.ChoiceBuilder;
import net.officefloor.eclipse.configurer.ClassBuilder;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.Configurer;
import net.officefloor.eclipse.configurer.ErrorListener;
import net.officefloor.eclipse.configurer.FlagBuilder;
import net.officefloor.eclipse.configurer.ListBuilder;
import net.officefloor.eclipse.configurer.MappingBuilder;
import net.officefloor.eclipse.configurer.MultipleBuilder;
import net.officefloor.eclipse.configurer.PropertiesBuilder;
import net.officefloor.eclipse.configurer.ResourceBuilder;
import net.officefloor.eclipse.configurer.TextBuilder;
import net.officefloor.model.Model;

/**
 * {@link Dialog} for a {@link Configurer}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConfigurerDialog<M> extends Dialog<M> implements ConfigurationBuilder<M>, ErrorListener {

	/**
	 * {@link Configurer}.
	 */
	private final Configurer<M> configurer = new Configurer<>();

	/**
	 * Apply button.
	 */
	private final Node applyButton;

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
		this.setResizable(true);
		this.getDialogPane().setPrefSize(600, 400);

		// Handle errors
		this.configurer.setErrorListener(this);

		// Load the buttons
		ButtonType applyButtonType = new ButtonType("Add", ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().setAll(applyButtonType,
				new ButtonType("Cancel", ButtonData.CANCEL_CLOSE));
		this.applyButton = this.getDialogPane().lookupButton(applyButtonType);
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

		// Configure the dialog
		this.configurer.loadConfiguration(model, this.getDialogPane().contentProperty());

		// Handle expanding (need to clear content)
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

		// Configure the model
		this.showAndWait().ifPresent(response -> System.out.println("TODO test " + response));
	}

	/*
	 * =============== ConfigurationBuilder ====================
	 */

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
	public void apply(Consumer<M> applier) {
		this.configurer.apply(applier);
	}

	/*
	 * ================ ErrorListener ===================
	 */

	@Override
	public void error(String message) {
		Label error = new Label(message);
		error.getStyleClass().add("configurer-error-header");
		this.getDialogPane().setHeader(error);
		this.getDialogPane().setExpandableContent(null);
		this.applyButton.setDisable(true);
	}

	@Override
	public void error(Throwable error) {
		this.error(error.getMessage());

		// Obtain the stack trace of error
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		error.printStackTrace(pw);
		String exceptionText = sw.toString();

		// Create text area for stack trace
		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(new Label("The exception stacktrace was:"), 0, 0);
		expContent.add(textArea, 0, 1);

		// Provide the stack trace
		this.getDialogPane().setExpandableContent(expContent);
	}

	@Override
	public void valid() {
		this.getDialogPane().setHeader(null);
		this.applyButton.setDisable(false);
	}

}