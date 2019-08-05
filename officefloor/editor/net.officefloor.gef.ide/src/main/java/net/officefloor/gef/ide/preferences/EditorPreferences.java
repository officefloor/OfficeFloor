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
package net.officefloor.gef.ide.preferences;

/**
 * Preferences for the Editor.
 * 
 * @author Daniel Sagenschneider
 */
public interface EditorPreferences {

	/**
	 * Obtains the preference.
	 * 
	 * @param preferenceId Preference identifier.
	 * @return Preference value or <code>null</code> if no configured.
	 */
	String getPreference(String preferenceId);

	/**
	 * Adds a {@link PreferenceListener}.
	 * 
	 * @param listener {@link PreferenceListener}.
	 */
	void addPreferenceListener(PreferenceListener listener);

	/**
	 * Preference listener.
	 */
	@FunctionalInterface
	interface PreferenceListener {

		/**
		 * Handles the {@link PreferenceEvent}.
		 * 
		 * @param event {@link PreferenceEvent}.
		 */
		void changedPreference(PreferenceEvent event);
	}

	/**
	 * Preference event.
	 */
	class PreferenceEvent {

		/**
		 * Preference identifier.
		 */
		public final String preferenceId;

		/**
		 * Instantiate.
		 * 
		 * @param preferenceId Preference identifier.
		 */
		public PreferenceEvent(String preferenceId) {
			this.preferenceId = preferenceId;
		}
	}

}