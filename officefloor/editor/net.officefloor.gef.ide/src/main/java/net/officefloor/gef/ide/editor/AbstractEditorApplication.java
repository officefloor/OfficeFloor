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
package net.officefloor.gef.ide.editor;

import java.io.IOException;

import org.eclipse.gef.mvc.fx.domain.IDomain;

import com.google.inject.Inject;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.model.Model;

/**
 * Abstract IDE Editor {@link Application}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractEditorApplication<R extends Model, RE extends Enum<RE>, O> extends Application {

	@Inject
	private IDomain domain;

	/**
	 * Creates the {@link AbstractAdaptedIdeEditor}.
	 * 
	 * @param <R>  Root {@link Model}.
	 * @param <RE> Root event {@link Enum}.
	 * @param <O>  Operations.
	 * @return {@link AbstractAdaptedIdeEditor}.
	 */
	protected abstract AbstractAdaptedIdeEditor<R, RE, O> createEditor();

	/**
	 * Obtains path to the {@link WritableConfigurationItem}.
	 * 
	 * @return Path to the {@link WritableConfigurationItem}.
	 * @throws IOException If fails to obtain {@link WritableConfigurationItem}.
	 */
	protected abstract WritableConfigurationItem getConfiguration() throws IOException;

	/*
	 * =============== Application =============================
	 */

	@Override
	public void start(Stage stage) throws Exception {

		// Obtain the configuration item
		WritableConfigurationItem configurationItem = this.getConfiguration();

		// Load the editor
		AbstractAdaptedIdeEditor<R, RE, O> editor = this.createEditor();
		editor.init(null, (injector) -> {
			injector.injectMembers(this);
			return this.domain;
		});
		editor.setConfigurationItem(configurationItem);
		editor.loadView((view) -> stage.setScene(new Scene(view)));
		this.domain.activate();

		// Display stage
		stage.setWidth(800);
		stage.setHeight(600);
		stage.show();
	}

}