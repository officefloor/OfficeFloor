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
package net.officefloor.eclipse.woof.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javafx.application.Application;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
import net.officefloor.eclipse.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.eclipse.ide.editor.AbstractEditorApplication;
import net.officefloor.eclipse.woof.WoofEditorRefactor;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofModel.WoofEvent;

/**
 * {@link Application} to run {@link WoofEditorRefactor}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofEditorRefactorApplication extends AbstractEditorApplication<WoofModel, WoofEvent, WoofChanges> {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	protected WritableConfigurationItem getConfiguration() throws IOException {
		return new MemoryConfigurationContext().createConfigurationItem("test",
				new ByteArrayInputStream("<woof />".getBytes()));
	}

	@Override
	protected AbstractAdaptedIdeEditor<WoofModel, WoofEvent, WoofChanges> createEditor() {
		return new WoofEditorRefactor();
	}

}