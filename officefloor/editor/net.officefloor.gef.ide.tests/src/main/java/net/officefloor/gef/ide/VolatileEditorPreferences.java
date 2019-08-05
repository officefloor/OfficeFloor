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

import net.officefloor.gef.ide.preferences.EditorPreferences;

/**
 * {@link EditorPreferences} that stores preferences only in memory.
 * 
 * @author Daniel Sagenschneider
 */
public class VolatileEditorPreferences implements EditorPreferences {

	@Override
	public String getPreference(String preferenceId) {
		return null;
	}

	@Override
	public void addPreferenceListener(PreferenceListener listener) {
	}

}