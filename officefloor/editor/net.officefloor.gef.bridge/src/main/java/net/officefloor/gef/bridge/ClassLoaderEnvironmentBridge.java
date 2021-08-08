/*-
 * #%L
 * [bundle] OfficeFloor OSGi Bridge
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.gef.bridge;

import java.net.URL;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;
import net.officefloor.compile.OfficeFloorCompiler;

/**
 * {@link ClassLoader} {@link EnvironmentBridge}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassLoaderEnvironmentBridge implements EnvironmentBridge {

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Preferences.
	 */
	private final ObservableMap<String, String> preferences = FXCollections.observableHashMap();

	/**
	 * Instantiate with default {@link ClassLoader}.
	 */
	public ClassLoaderEnvironmentBridge() {
		this.classLoader = this.getClass().getClassLoader();
	}

	/**
	 * Instantiate.
	 * 
	 * @param classLoader {@link ClassLoader}.
	 */
	public ClassLoaderEnvironmentBridge(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Attempts to load the {@link Class}.
	 * 
	 * @param className Name of {@link Class}.
	 * @return {@link Class} or <code>null</code>.
	 */
	private Class<?> loadClass(String className) {
		try {
			return this.classLoader.loadClass(className);
		} catch (ClassNotFoundException ex) {
			return null;
		}
	}

	/**
	 * Attempts to load the resource.
	 * 
	 * @param resourcePath Path to resource.
	 * @return {@link URL}.
	 */
	private URL loadResource(String resourcePath) {
		return this.classLoader.getResource(resourcePath);
	}

	/*
	 * ================= EnvironmentBridge =====================
	 */

	@Override
	public boolean isClassOnClassPath(String className) {
		return (this.loadClass(className) != null);
	}

	@Override
	public boolean isSuperType(String className, String superType) {

		// Ensure have class
		Class<?> superClass = this.loadClass(superType);
		if (superClass == null) {
			return false;
		}

		// Ensure have class
		Class<?> clazz = this.loadClass(className);
		if (clazz == null) {
			return false;
		}

		// Return whether super type
		return superClass.isAssignableFrom(clazz);
	}

	@Override
	public void selectClass(String searchText, String superType, SelectionHandler handler) {

		// Ensure have class
		Class<?> superClass = this.loadClass(superType);
		if (superClass == null) {
			handler.error(new ClassNotFoundException("Can not find super type " + superType));
			return;
		}

		// Not deriving, so just return search text
		handler.selected(searchText);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S> Class<? extends S> loadClass(String className, Class<S> superType) throws Exception {
		return (Class<? extends S>) this.classLoader.loadClass(className);
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	@Override
	public OfficeFloorCompiler getOfficeFloorCompiler() {
		return OfficeFloorCompiler.newOfficeFloorCompiler(this.classLoader);
	}

	@Override
	public boolean isResourceOnClassPath(String resourcePath) {
		return (this.loadResource(resourcePath) != null);
	}

	@Override
	public void selectClassPathResource(String searchText, SelectionHandler handler) {

		// Not deriving, so just return search text
		handler.selected(searchText);
	}

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
