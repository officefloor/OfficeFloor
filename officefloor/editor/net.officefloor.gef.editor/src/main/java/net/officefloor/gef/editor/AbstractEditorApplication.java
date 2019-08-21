/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.gef.editor;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.officefloor.model.Model;

/**
 * Provides means to test editor configurations without loading the Eclipse
 * platform.
 */
public abstract class AbstractEditorApplication extends Application {

	/**
	 * {@link SelectOnly}.
	 */
	private SelectOnly selectOnly = null;

	/**
	 * Builds the {@link AdaptedModel} instances.
	 * 
	 * @param context
	 *            {@link AdaptedBuilderContext}.
	 */
	protected abstract void buildModels(AdaptedBuilderContext context);

	/**
	 * Creates the root {@link Model}.
	 * 
	 * @return Root {@link Model}.
	 */
	protected abstract Model createRootModel();

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
	 * @param selectOnly
	 *            {@link SelectOnly}.
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
		AdaptedEditorModule module = this.createModule();

		// Configure select only (if specified)
		if (this.selectOnly != null) {
			module.setSelectOnly(this.selectOnly);
		}

		// Create the parent
		Parent parent = module.createParent((context) -> this.buildModels(context));

		// Setup visuals
		Scene scene = new Scene(parent);
		stage.setScene(scene);
		stage.setResizable(true);
		stage.setWidth(640);
		stage.setHeight(480);
		stage.setTitle(this.getClass().getSimpleName());

		// Activate the domain
		Model rootModel = this.createRootModel();
		module.activateDomain(rootModel);

		// Show
		stage.show();
	}

}