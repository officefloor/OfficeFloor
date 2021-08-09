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

package net.officefloor.gef.configurer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.officefloor.gef.editor.AdaptedEditorPlugin;

/**
 * Provides means to test editor configurations without loading the Eclipse
 * platform.
 */
public abstract class AbstractConfigurerApplication extends Application {

	/**
	 * Loads the configuration.
	 * 
	 * @param pane {@link Pane} to load in the configuration.
	 */
	protected abstract void loadConfiguration(Pane pane);

	/*
	 * ==================== Application ==========================
	 */

	@Override
	public void start(Stage stage) throws Exception {

		// Setup visuals
		Pane parent = new Pane();
		Scene scene = new Scene(parent);
		stage.setScene(scene);
		stage.setResizable(true);
		stage.setWidth(640);
		stage.setHeight(480);
		stage.setTitle(this.getClass().getSimpleName());
		
		// Initialise for Non-Osgi
		AdaptedEditorPlugin.initNonOsgiEnvironment();

		// Load configuration
		this.loadConfiguration(parent);

		// Show
		stage.show();
	}

}
