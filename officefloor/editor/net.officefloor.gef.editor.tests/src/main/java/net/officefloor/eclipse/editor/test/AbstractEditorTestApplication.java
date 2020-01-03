/*-
 * #%L
 * net.officefloor.gef.editor.tests
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

package net.officefloor.eclipse.editor.test;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.officefloor.gef.editor.AdaptedBuilderContext;
import net.officefloor.gef.editor.AdaptedEditorModule;
import net.officefloor.gef.editor.AdaptedEditorPlugin;
import net.officefloor.gef.editor.AdaptedModel;
import net.officefloor.gef.editor.SelectOnly;
import net.officefloor.model.Model;

/**
 * Provides means to test editor configurations without loading the Eclipse
 * platform.
 */
public abstract class AbstractEditorTestApplication<R extends Model> extends Application {

	/**
	 * {@link SelectOnly}.
	 */
	private SelectOnly selectOnly = null;

	/**
	 * Builds the {@link AdaptedModel} instances.
	 * 
	 * @param context {@link AdaptedBuilderContext}.
	 */
	protected abstract void buildModels(AdaptedBuilderContext context);

	/**
	 * Creates the root {@link Model}.
	 * 
	 * @return Root {@link Model}.
	 */
	protected abstract R createRootModel();

	/**
	 * Creates the replacement root {@link Model}.
	 * 
	 * @return Replacement root {@link Model}.
	 */
	protected abstract R createRootReplacement();

	/**
	 * <p>
	 * Creates the {@link AdaptedEditorModule}.
	 * <p>
	 * Allows overriding the {@link AdaptedEditorModule}.
	 * 
	 * @return {@link AdaptedEditorModule}.
	 */
	protected AdaptedEditorModule createModule() {
		return new AdaptedEditorModule();
	}

	/**
	 * Allows specifying that {@link SelectOnly}.
	 * 
	 * @param selectOnly {@link SelectOnly}.
	 */
	protected void setSelectOnly(SelectOnly selectOnly) {
		this.selectOnly = selectOnly;
	}

	/*
	 * ==================== Application ==========================
	 */

	@Override
	public void start(Stage stage) throws Exception {

		// Create the module
		AdaptedEditorPlugin.initNonOsgiEnvironment();
		AdaptedEditorModule module = this.createModule();

		// Configure select only (if specified)
		if (this.selectOnly != null) {
			module.setSelectOnly(this.selectOnly);
		}

		// Provide means to replace model
		VBox container = new VBox();

		// Provide button to replace root model
		Button replaceButton = new Button("Change Root Model");
		VBox.setVgrow(replaceButton, Priority.NEVER);
		container.getChildren().add(replaceButton);

		// Create the parent
		Parent parent = module.createParent((context) -> this.buildModels(context));
		VBox.setVgrow(parent, Priority.ALWAYS);
		container.getChildren().add(parent);

		// Setup visuals
		Scene scene = new Scene(container);
		stage.setScene(scene);
		stage.setResizable(true);
		stage.setWidth(1200);
		stage.setHeight(800);
		stage.setTitle(this.getClass().getSimpleName());

		// Activate the domain
		R rootModel = this.createRootModel();
		Property<R> rootModelProperty = module.activateDomain(rootModel);

		// Allow button to replace root model
		replaceButton.setOnAction((event) -> rootModelProperty.setValue(this.createRootReplacement()));

		// Show
		stage.show();
	}

}
