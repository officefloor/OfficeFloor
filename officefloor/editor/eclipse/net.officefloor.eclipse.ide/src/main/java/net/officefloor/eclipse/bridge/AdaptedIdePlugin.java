/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
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

package net.officefloor.eclipse.bridge;

import java.io.Closeable;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import net.officefloor.gef.bridge.EnvironmentBridge.PreferenceEvent;
import net.officefloor.gef.bridge.EnvironmentBridge.PreferenceListener;

/**
 * Adapted IDE {@link AbstractUIPlugin}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedIdePlugin extends AbstractUIPlugin {

	/**
	 * Default instance.
	 */
	private static AdaptedIdePlugin INSTANCE;

	/**
	 * Obtains the default {@link AdaptedIdePlugin} instances.
	 * 
	 * @return Default {@link AdaptedIdePlugin} instances.
	 */
	public static AdaptedIdePlugin getDefault() {
		return INSTANCE;
	}

	/**
	 * Instantiate, capturing instance.
	 */
	public AdaptedIdePlugin() {
		INSTANCE = this;
	}

	/**
	 * Provide means to obtain {@link IPreferenceStore}.
	 * 
	 * @return {@link IPreferenceStore}.
	 */
	public IPreferenceStore getPreferenceStore() {
		return super.getPreferenceStore();
	}

	/**
	 * Adds a {@link PreferenceListener}.
	 * 
	 * @param listener {@link PreferenceListener}.
	 * @return {@link Closeable} to stop listening.
	 */
	public Closeable addPreferenceListener(PreferenceListener listener) {

		// Handle change listener
		IPropertyChangeListener changeListener;

		// Listen to preference changes
		IPreferenceStore preferences = AdaptedIdePlugin.getDefault().getPreferenceStore();
		if (preferences != null) {

			// Create and register listening to preference change
			changeListener = (event) -> listener.changedPreference(new PreferenceEvent(event.getProperty()));
			preferences.addPropertyChangeListener(changeListener);

		} else {
			changeListener = null;
		}

		// Provide means to remove listening
		return () -> {
			if (changeListener != null) {
				preferences.removePropertyChangeListener(changeListener);
			}
		};
	}

}
