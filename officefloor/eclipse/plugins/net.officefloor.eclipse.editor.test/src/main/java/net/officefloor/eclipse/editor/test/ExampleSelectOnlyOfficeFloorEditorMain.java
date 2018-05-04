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
package net.officefloor.eclipse.editor.test;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;
import net.officefloor.eclipse.editor.ContentStyler;
import net.officefloor.eclipse.editor.SelectOnly;
import net.officefloor.model.Model;

/**
 * Example {@link SelectOnly} {@link ExampleOfficeFloorEditorMain}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleSelectOnlyOfficeFloorEditorMain extends ExampleOfficeFloorEditorMain {

	/**
	 * Main to run the editor.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	/**
	 * Configure {@link SelectOnly}.
	 */
	public ExampleSelectOnlyOfficeFloorEditorMain() {
		this.setSelectOnly(new SelectOnly() {

			@Override
			public void paletteIndicator(Property<String> style) {
				style.setValue(".palette-indicator { -fx-background-color: lawngreen }");
			}

			@Override
			public void palette(Property<String> style) {
				style.setValue(".palette { -fx-background-color: mediumspringgreen }");
			}

			private boolean isContentToggle = false;

			@Override
			public void content(ContentStyler contentStyler) {
				this.isContentToggle = !this.isContentToggle;
				contentStyler.setContentBackground(this.isContentToggle
						? new Background(new BackgroundFill(Paint.valueOf("lawngreen"), null, null))
						: null);
				contentStyler.getGridModel().setShowGrid(!this.isContentToggle);
			}

			@Override
			public void model(Model model) {
				System.out.println("Selected model " + model.getClass().getName());
			}
		});
	}
}