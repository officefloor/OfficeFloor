/*-
 * #%L
 * [bundle] OfficeFloor OSGi Bridge
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

package net.officefloor.gef.bridge;

import net.officefloor.compile.OfficeFloorCompiler;

/**
 * Bridges the editor to the environment.
 * 
 * @author Daniel Sagenschneider
 */
public interface EnvironmentBridge {

	/**
	 * Handles selection result.
	 */
	interface SelectionHandler {

		/**
		 * Handles selection.
		 * 
		 * @param classPathEntry {@link Class} path entry.
		 */
		void selected(String classPathEntry);

		/**
		 * Indicates selection cancelled.
		 */
		void cancelled();

		/**
		 * Handles error.
		 * 
		 * @param error {@link Exception}.
		 */
		void error(Exception error);
	}

	/**
	 * Indicates if {@link Class} on the {@link Class} path.
	 * 
	 * @param className Name of the {@link Class}.
	 * @return <code>true</code> if {@link Class} on the {@link Class} path.
	 * @throws Exception If fails to determine if on {@link Class} path.
	 */
	boolean isClassOnClassPath(String className) throws Exception;

	/**
	 * Indicates if super type.
	 * 
	 * @param className Name of {@link Class}.
	 * @param superType Super type {@link Class}.
	 * @return <code>true</code> if super type {@link Class}.
	 * @throws Exception If fails to determine if super type.
	 */
	boolean isSuperType(String className, String superType) throws Exception;

	/**
	 * Loads the {@link Class}.
	 * 
	 * @param <S>       Super type.
	 * @param className Name of {@link Class}.
	 * @param superType Super type required of {@link Class}.
	 * @return {@link Class}.
	 * @throws Exception If fails to load the {@link Class}.
	 */
	<S> Class<? extends S> loadClass(String className, Class<S> superType) throws Exception;

	/**
	 * Obtains the {@link ClassLoader}.
	 * 
	 * @return {@link ClassLoader}.
	 * @throws Exception If fails to obtain {@link ClassLoader}.
	 */
	ClassLoader getClassLoader() throws Exception;

	/**
	 * Obtains the {@link OfficeFloorCompiler}.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 * @throws Exception If fails to obtain {@link OfficeFloorCompiler}.
	 */
	OfficeFloorCompiler getOfficeFloorCompiler() throws Exception;

	/**
	 * Selects a {@link Class}.
	 * 
	 * @param searchText Search text to find the {@link Class}.
	 * @param superType  Super type of the {@link Class}.
	 * @param handler    {@link SelectionHandler}.
	 */
	void selectClass(String searchText, String superType, SelectionHandler handler);

	/**
	 * Indicates if the resource is on the {@link Class} path.
	 * 
	 * @param resourcePath Resource path.
	 * @return <code>true</code> if the resource is on the {@link Class} path.
	 * @throws Exception If fails to determine if on {@link Class} path.
	 */
	boolean isResourceOnClassPath(String resourcePath) throws Exception;

	/**
	 * Selects a resource from the {@link Class} path.
	 * 
	 * @param searchText Search text to find the {@link Class} path resource.
	 * @param handler    {@link SelectionHandler}.
	 */
	void selectClassPathResource(String searchText, SelectionHandler handler);

	/**
	 * Obtains the preference.
	 * 
	 * @param preferenceId Preference identifier.
	 * @return Preference value or <code>null</code> if no configured.
	 */
	String getPreference(String preferenceId);

	/**
	 * Specifies the preference value.
	 * 
	 * @param preferenceId Preference identifier.
	 * @param value        Value for the preference.
	 */
	void setPreference(String preferenceId, String value);

	/**
	 * Resets the preference to default value.
	 * 
	 * @param preferenceId Preference identifier.
	 */
	void resetPreference(String preferenceId);

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
