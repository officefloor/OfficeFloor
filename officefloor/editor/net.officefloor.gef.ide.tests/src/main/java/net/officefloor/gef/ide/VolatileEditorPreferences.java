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
package net.officefloor.gef.ide;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;
import net.officefloor.gef.ide.preferences.EditorPreferences;

/**
 * {@link EditorPreferences} that stores preferences only in memory.
 * 
 * @author Daniel Sagenschneider
 */
public class VolatileEditorPreferences implements EditorPreferences {

	/**
	 * Preferences.
	 */
	private final ObservableMap<String, String> preferences = FXCollections.observableHashMap();

	@Override
	public String getPreference(String preferenceId) {
		return this.preferences.get(preferenceId);
	}

	@Override
	public void setPreference(String preferenceId, String value) {
		this.preferences.put(preferenceId, value);
	}

	@Override
	public void resetPreference(String preferenceId) {
		this.preferences.remove(preferenceId);
	}

	@Override
	public void addPreferenceListener(PreferenceListener listener) {
		this.preferences.addListener((Change<? extends String, ? extends String> event) -> {
			listener.changedPreference(new PreferenceEvent(event.getKey()));
		});
	}

}