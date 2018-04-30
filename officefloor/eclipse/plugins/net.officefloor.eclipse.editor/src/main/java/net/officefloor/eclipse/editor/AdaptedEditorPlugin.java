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
package net.officefloor.eclipse.editor;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import net.officefloor.eclipse.editor.internal.style.StyleRegistry;
import net.officefloor.eclipse.editor.internal.style.SystemStyleRegistry;

/**
 * {@link Plugin} for the {@link AdaptedEditorModule}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedEditorPlugin extends AbstractUIPlugin {

	/**
	 * Creates a new {@link StyleRegistry}.
	 * 
	 * @return New {@link StyleRegistry}.
	 */
	public static StyleRegistry createStyleRegistry() {

		// Determine if running within OSGi environment
		if (INSTANCE == null) {

			// Running outside OSGi environment
			return new SystemStyleRegistry();
		}

		// TODO implement OSGi service URL registration
		throw new UnsupportedOperationException("TODO implement style registry for OSGi environment");
	}

	/**
	 * Obtains the {@link AdaptedEditorPlugin} singleton.
	 * 
	 * @return {@link AdaptedEditorPlugin} singleton.
	 */
	public AdaptedEditorPlugin getDefault() {
		return INSTANCE;
	}

	/**
	 * Singleton.
	 */
	private static AdaptedEditorPlugin INSTANCE;

	/**
	 * Instantiate.
	 */
	public AdaptedEditorPlugin() {
		INSTANCE = this;
	}

}