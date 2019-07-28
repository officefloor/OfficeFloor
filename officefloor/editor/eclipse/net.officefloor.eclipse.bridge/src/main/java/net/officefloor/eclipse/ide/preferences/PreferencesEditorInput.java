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
package net.officefloor.eclipse.ide.preferences;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import net.officefloor.eclipse.bridge.AbstractIdeEclipseEditor;
import net.officefloor.model.Model;

/**
 * {@link IEditorInput} to load {@link AbstractIdeEclipseEditor} for preference
 * configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class PreferencesEditorInput implements IEditorInput {

	/**
	 * Name of the {@link AbstractIdeEclipseEditor}.
	 */
	private final String editorName;

	/**
	 * Root {@link Model} to configure the {@link AbstractIdeEclipseEditor}.
	 */
	private final Model rootModel;

	/**
	 * Instantiate.
	 * 
	 * @param editorName
	 *            Name of the {@link AbstractIdeEclipseEditor}.
	 * @param rootModel
	 *            Root {@link Model} to configure the {@link AbstractIdeEclipseEditor}.
	 */
	public PreferencesEditorInput(String editorName, Model rootModel) {
		this.editorName = editorName;
		this.rootModel = rootModel;
	}

	/**
	 * Obtains the root {@link Model}.
	 * 
	 * @return Root model.
	 */
	public Model getRootModel() {
		return this.rootModel;
	}

	/*
	 * =============== IEditorInput ===================
	 */

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return this.editorName;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return null;
	}

}