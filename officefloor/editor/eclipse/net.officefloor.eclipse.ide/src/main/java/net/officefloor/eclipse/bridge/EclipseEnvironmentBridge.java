/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
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

package net.officefloor.eclipse.bridge;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.gef.bridge.EnvironmentBridge;

/**
 * Eclipse {@link EnvironmentBridge}.
 * 
 * @author Daniel Sagenschneider
 */
public class EclipseEnvironmentBridge implements EnvironmentBridge {

	/**
	 * {@link IJavaProject}.
	 */
	private final IJavaProject javaProject;

	/**
	 * Items to dispose.
	 */
	private final List<Closeable> disposeItems = new LinkedList<>();

	/**
	 * Parent {@link Shell}.
	 */
	private Shell parentShell;

	/**
	 * Cached {@link ClassLoader} for the {@link IJavaProject}.
	 */
	private ClassLoader classLoader = null;

	/**
	 * Cached {@link OfficeFloorCompiler}.
	 */
	private OfficeFloorCompiler compiler = null;

	/**
	 * Instantiate for {@link IJavaProject} specific bridging.
	 * 
	 * @param javaProject {@link IJavaProject}.
	 */
	public EclipseEnvironmentBridge(IJavaProject javaProject) {
		this.javaProject = javaProject;

		/*
		 * Listen to java changes to see if class path has become invalid.
		 * 
		 * Note: rather than determine if particular project is changed, easier to just
		 * reconstruct class path again on any change.
		 */
		IElementChangedListener listener = (event) -> {
			this.classLoader = null;
			this.compiler = null;
		};
		JavaCore.addElementChangedListener(listener);
		this.disposeItems.add(() -> JavaCore.removeElementChangedListener(listener));
	}

	/**
	 * Initialise for non {@link IJavaProject} specific bridging.
	 */
	public EclipseEnvironmentBridge() {
		this.javaProject = null;
	}

	/**
	 * Initialise.
	 * 
	 * @param parentShell Parent {@link Shell}.
	 */
	public void init(Shell parentShell) {
		this.parentShell = parentShell;
	}

	/**
	 * Disposes of resources.
	 * 
	 * @throws IOException If fails to dispose.
	 */
	public void dispose() throws IOException {
		for (Closeable item : this.disposeItems) {
			item.close();
		}
	}

	/**
	 * Ensures obtains the {@link IJavaProject}.
	 * 
	 * @return {@link IJavaProject}.
	 */
	private IJavaProject getJavaProject() {
		if (this.javaProject == null) {
			throw new IllegalStateException("Attempting " + IJavaProject.class.getSimpleName()
					+ " specific bridging outside " + IJavaProject.class.getSimpleName() + " context");
		}
		return this.javaProject;
	}

	/*
	 * ==================== EnvironmentBridge =====================
	 */

	@Override
	public boolean isClassOnClassPath(String className) throws Exception {

		// Use java project to determine if on class path
		IType type = this.getJavaProject().findType(className, (IProgressMonitor) null);
		return (type != null);
	}

	@Override
	public boolean isSuperType(String className, String superTypeName) throws Exception {

		// Obtain the class type
		IType type = this.getJavaProject().findType(className);
		if (type == null) {
			throw new ClassNotFoundException("Class " + className + " not on class path");
		}

		// Obtain the super type from the project
		IType superType = this.getJavaProject().findType(superTypeName);
		if (superType == null) {
			throw new ClassNotFoundException("Please add " + superTypeName + " to the class path");
		}

		// Ensure child of super type
		ITypeHierarchy typeHierarchy = type.newTypeHierarchy(new NullProgressMonitor());
		List<IType> superTypes = Arrays.asList(typeHierarchy.getAllSupertypes(type));
		return superTypes.stream().anyMatch(supertype -> {
			return supertype.getFullyQualifiedName().equals(superTypeName);
		});
	}

	@Override
	public void selectClass(String searchText, String superTypeName, SelectionHandler handler) {
		try {
			// Obtain the search scope
			IJavaSearchScope scope = null;
			if (superTypeName != null) {
				// Obtain the super type from the project
				IType superType = this.getJavaProject().findType(superTypeName);
				if (superType != null) {
					// Search for sub type class
					scope = SearchEngine.createStrictHierarchyScope(this.getJavaProject(), superType, true, true, null);
				}
			}
			if (scope == null) {
				// No hierarchy, so search for any class
				scope = SearchEngine.createJavaSearchScope(new IJavaProject[] { this.getJavaProject() }, true);
			}

			// Search for any class
			SelectionDialog dialog = JavaUI.createTypeDialog(this.parentShell,
					new ProgressMonitorDialog(this.parentShell), scope, IJavaElementSearchConstants.CONSIDER_CLASSES,
					false, searchText == null ? "" : searchText);
			dialog.setBlockOnOpen(true);
			dialog.open();
			Object[] results = dialog.getResult();
			if ((results == null) || (results.length != 1)) {
				handler.cancelled();
				return; // cancel
			}

			// Obtain the selected item
			Object selectedItem = results[0];
			if (selectedItem instanceof IType) {
				// Obtained the selected class
				handler.selected(((IType) selectedItem).getFullyQualifiedName());

			} else {
				// Unknown type
				handler.error(new IllegalStateException("Plugin Error: selected item is not of " + IType.class.getName()
						+ " [" + (selectedItem == null ? null : selectedItem.getClass().getName()) + "]"));
				return;
			}
		} catch (JavaModelException ex) {
			handler.error(ex);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Class<? extends T> loadClass(String className, Class<T> superType) throws Exception {
		return (Class<? extends T>) this.getClassLoader().loadClass(className);
	}

	@Override
	public ClassLoader getClassLoader() throws Exception {

		// Lazy load
		if (this.classLoader == null) {

			// Obtain the class path for the project
			String[] classPathEntries = JavaRuntime.computeDefaultRuntimeClassPath(this.getJavaProject());
			List<URL> urls = new ArrayList<>(classPathEntries.length);
			for (int i = 0; i < classPathEntries.length; i++) {
				String path = classPathEntries[i];
				File file = new File(path);
				if (file.exists()) {
					if (file.isDirectory()) {
						urls.add(new URL("file", null, path + "/"));
					} else {
						urls.add(new URL("file", null, path));
					}
				}
			}

			// Create the class loader
			this.classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
		}

		// Return the class loader
		return this.classLoader;
	}

	@Override
	public OfficeFloorCompiler getOfficeFloorCompiler() throws Exception {

		// Lazy load
		if (this.compiler == null) {

			// Obtain the class loader
			ClassLoader classLoader = this.getClassLoader();

			// Create the OfficeFloor compiler
			this.compiler = OfficeFloorCompiler.newOfficeFloorCompiler(classLoader);
		}

		// Return the compiler
		return this.compiler;
	}

	@Override
	public boolean isResourceOnClassPath(String resourcePath) throws Exception {

		// Check on provided class path
		ClassLoader classLoader = this.getClassLoader();
		Enumeration<URL> resources = classLoader.getResources(resourcePath);
		return resources.hasMoreElements();
	}

	@Override
	public void selectClassPathResource(String searchText, SelectionHandler handler) {

		// Strip filter down to just the simple name
		String filter = searchText == null ? "" : searchText;
		int index = filter.lastIndexOf('/');
		if (index >= 0) {
			filter = filter.substring(index + "/".length());
		}
		index = filter.indexOf('.');
		if (index >= 0) {
			filter = filter.substring(0, index);
		}

		// Obtain the selected file
		FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(parentShell, false,
				this.getJavaProject().getProject(), IResource.FILE);
		dialog.setInitialPattern(filter);
		dialog.setBlockOnOpen(true);
		dialog.open();
		Object[] results = dialog.getResult();
		if ((results == null) || (results.length != 1)) {
			handler.cancelled();
			return; // cancel
		}

		// Obtain the selected item
		Object selectedItem = results[0];
		if (selectedItem instanceof IFile) {
			// Specify class path location for file
			IFile file = (IFile) selectedItem;
			handler.selected(getClassPathLocation(file));
		} else {
			// Unknown type
			handler.error(new IllegalStateException("Plugin Error: selected item is not of " + IFile.class.getName()
					+ " [" + (selectedItem == null ? null : selectedItem.getClass().getName()) + "]"));
		}
	}

	/**
	 * Obtains the class path location for the {@link IFile}.
	 * 
	 * @param file {@link IFile}.
	 * @return Class path location for the {@link IFile}.
	 */
	private String getClassPathLocation(IFile file) {

		// Obtain the resource for the path
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IResource pathResource = workspaceRoot.findMember(file.getFullPath());
		IResource resource = pathResource;

		// Obtain the java element
		IJavaElement javaElement = null;
		do {
			// Ensure have the resource
			if (resource == null) {
				// Did not find java element for resource
				return null;
			}

			// Obtain the java element from the resource
			javaElement = JavaCore.create(resource);

			// Obtain the parent resource
			resource = resource.getParent();

		} while (javaElement == null);

		// Obtain the package fragment root for the java element
		IPackageFragmentRoot fragmentRoot = null;
		do {

			// Determine if package fragment root
			if (javaElement instanceof IPackageFragmentRoot) {
				fragmentRoot = (IPackageFragmentRoot) javaElement;
			}

			// Obtain the parent java element
			javaElement = javaElement.getParent();

		} while ((fragmentRoot == null) && (javaElement != null));

		// Determine if have fragment root
		if (fragmentRoot == null) {
			// Return path as is
			return file.getFullPath().toString();
		}

		// Obtain the fragment root full path
		String fragmentPath = fragmentRoot.getResource().getFullPath().toString() + "/";

		// Obtain the class path location (by removing fragment root path)
		String fullPath = file.getFullPath().toString();
		String location = fullPath.substring(fragmentPath.length());

		// Return the location
		return location;
	}

	@Override
	public String getPreference(String preferenceId) {
		return AdaptedIdePlugin.getDefault().getPreferenceStore().getString(preferenceId);
	}

	@Override
	public void setPreference(String preferenceId, String value) {
		AdaptedIdePlugin.getDefault().getPreferenceStore().setValue(preferenceId, value);
	}

	@Override
	public void resetPreference(String preferenceId) {
		AdaptedIdePlugin.getDefault().getPreferenceStore().setToDefault(preferenceId);
	}

	@Override
	public void addPreferenceListener(PreferenceListener listener) {
		Closeable cleanup = AdaptedIdePlugin.getDefault().addPreferenceListener(listener);
		this.disposeItems.add(cleanup);
	}

}
