/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.maven.woof;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.function.Consumer;

import org.eclipse.gef.mvc.fx.domain.IDomain;

import com.google.inject.Inject;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
import net.officefloor.gef.bridge.ClassLoaderEnvironmentBridge;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.editor.internal.officefloorstyle.Handler;
import net.officefloor.gef.woof.WoofEditor;
import net.officefloor.woof.WoofLoaderExtensionService;

/**
 * Viewer of the configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class Viewer extends Application {

	/**
	 * Launches this application.
	 * 
	 * @param args Arguments.
	 * @throws Exception If fails to launch.
	 */
	public static void main(String[] args) throws Exception {

		// Obtain the arguments (ensuring path specified)
		String[] runArgs = (args.length == 0) ? new String[] { WoofLoaderExtensionService.APPLICATION_WOOF } : args;

		// Run the application (using the provided class loader)
		launch(runArgs);
	}

	/**
	 * IDomain
	 */
	@Inject
	private IDomain domain;

	/*
	 * ================== Application =========================
	 */

	@Override
	public void start(Stage stage) throws Exception {

		// Loads the error
		Consumer<Throwable> loadError = (error) -> {
			StringWriter buffer = new StringWriter();
			error.printStackTrace(new PrintWriter(buffer));
			Text text = new Text(buffer.toString());
			HBox.setHgrow(text, Priority.ALWAYS);
			stage.setScene(new Scene(new HBox(text)));
			stage.setWidth(800);
			stage.setHeight(600);
			stage.show();
		};

		// Ensure have configuration path
		String configurationItemPath = this.getParameters().getRaw().get(0);
		if (configurationItemPath == null) {
			loadError.accept(new IllegalStateException("No configuration path specified"));
			return;
		}

		// Obtain the class loader
		ClassLoader classLoader = Viewer.class.getClassLoader();

		// Load the configuration
		InputStream configurationInput = classLoader.getResourceAsStream(configurationItemPath);
		if (configurationInput == null) {
			loadError.accept(new FileNotFoundException("Failed to find " + configurationItemPath));
			return;
		}

		// Obtain the configuration item
		WritableConfigurationItem configurationItem;
		try {
			configurationItem = MemoryConfigurationContext.createWritableConfigurationItem(configurationItemPath);
			configurationItem.setConfiguration(configurationInput);
		} catch (IOException ex) {
			loadError.accept(new IllegalStateException("Failed to load configuration for " + configurationItemPath));
			return;
		}

		// Create the environment bridge
		EnvironmentBridge envBridge = new ClassLoaderEnvironmentBridge(classLoader);

		// Setup OfficeFloor style URL handling
		URL.setURLStreamHandlerFactory((protocol) -> {
			if (!"officefloorstyle".equals(protocol)) {
				return null;
			}
			return new Handler();
		});

		// Create and initialise the editor
		WoofEditor editor = new WoofEditor(envBridge);
		editor.init(null, (injector) -> {
			injector.injectMembers(this);
			return this.domain;
		});
		editor.setConfigurationItem(configurationItem);

		// Display the editor
		editor.loadView((view) -> stage.setScene(new Scene(view)));
		stage.setWidth(1600);
		stage.setHeight(1200);
		stage.show();
		
		// Activate
		this.domain.activate();
	}

}