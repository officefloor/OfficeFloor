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
package net.officefloor.eclipse.osgi;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
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

/**
 * Bridge for {@link IJavaProject} to {@link OfficeFloorCompiler} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorOsgiBridge {

	/**
	 * <p>
	 * Obtains an {@link OfficeFloorOsgiBridge} for the current {@link ClassLoader}.
	 * <p>
	 * This should NOT be used in the Eclipse OSGi environment, as it takes the
	 * {@link ClassLoader} from the current {@link Thread} or system (which is
	 * indeterminate within the OSGi environment).
	 * <p>
	 * This is useful for testing outside the OSGi environment.
	 * 
	 * @return {@link OfficeFloorOsgiBridge} for the current {@link ClassLoader}.
	 */
	public static OfficeFloorOsgiBridge getClassLoaderInstance() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null) {
			classLoader = ClassLoader.getSystemClassLoader();
		}
		return new OfficeFloorOsgiBridge(classLoader);
	}

	/**
	 * {@link IJavaProject}.
	 */
	private final IJavaProject javaProject;

	/**
	 * Cached {@link ClassLoader} for the {@link IJavaProject}.
	 */
	private ClassLoader classLoader = null;

	/**
	 * Cached {@link OfficeFloorCompiler}.
	 */
	private OfficeFloorCompiler compiler = null;

	/**
	 * Instantiate.
	 * 
	 * @param javaProject
	 *            {@link IJavaProject}.
	 */
	public OfficeFloorOsgiBridge(IJavaProject javaProject) {
		this.javaProject = javaProject;

		/*
		 * Listen to java changes to see if class path has become invalid.
		 * 
		 * Note: rather than determine if particular project is changed, easier to just
		 * reconstruct class path again on any change.
		 */
		JavaCore.addElementChangedListener((event) -> {
			this.classLoader = null;
			this.compiler = null;
		});
	}

	/**
	 * <p>
	 * Instantiate to use the specified {@link ClassLoader}.
	 * <p>
	 * Typically this is only used for testing. When using for plugins should bridge
	 * to the {@link IJavaProject} to get latest class path.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public OfficeFloorOsgiBridge(ClassLoader classLoader) {
		this.javaProject = null;
		this.classLoader = classLoader;
	}

	/**
	 * Obtains the {@link IJavaProject}.
	 * 
	 * @return {@link IJavaProject}.
	 */
	public IJavaProject getJavaProject() {
		if (this.javaProject == null) {
			throw new IllegalStateException("Instantiated to only " + ClassLoader.class.getSimpleName() + " so "
					+ IJavaProject.class.getSimpleName() + " not available");
		}
		return this.javaProject;
	}

	/**
	 * Obtains the {@link ClassLoader} for the {@link IJavaProject}.
	 * 
	 * @return {@link ClassLoader} for the {@link IJavaProject}.
	 * @throws Exception
	 *             If fails to extract class path from {@link IJavaProject}.
	 */
	public ClassLoader getClassLoader() throws Exception {

		// Lazy load
		if (this.classLoader == null) {

			// Obtain the class path for the project
			String[] classPathEntries = JavaRuntime.computeDefaultRuntimeClassPath(this.javaProject);
			URL[] urls = new URL[classPathEntries.length];
			for (int i = 0; i < classPathEntries.length; i++) {
				String path = classPathEntries[i];
				File file = new File(path);
				if (file.exists()) {
					if (file.isDirectory()) {
						urls[i] = new URL("file", null, path + "/");
					} else {
						urls[i] = new URL("file", null, path);
					}
				}
			}

			// Create the class loader
			this.classLoader = new URLClassLoader(urls);
		}

		// Return the class loader
		return this.classLoader;
	}

	/**
	 * Loads the {@link Class}.
	 * 
	 * @param <T>
	 *            {@link Class} type.
	 * @param className
	 *            Name of the {@link Class}.
	 * @param superType
	 *            Super type of the {@link Class}.
	 * @return {@link Class}.
	 * @throws Exception
	 *             If {@link Class} not found or fails to load the {@link Class}.
	 */
	@SuppressWarnings("unchecked")
	public <T> Class<? extends T> loadClass(String className, Class<T> superType) throws Exception {
		return (Class<? extends T>) this.getClassLoader().loadClass(className);
	}

	/**
	 * Indicates if the {@link Class} is on the class path.
	 * 
	 * @param className
	 *            Name of the {@link Class}.
	 * @return <code>true</code> if the {@link Class} is on the class path.
	 * @throws Exception
	 *             If fails to determine if on class path.
	 */
	public boolean isClassOnClassPath(String className) throws Exception {

		// Determine if have java project
		if (this.javaProject == null) {

			// Check on provided class path
			ClassLoader classLoader = this.getClassLoader();
			try {
				Class<?> clazz = classLoader.loadClass(className);
				return (clazz != null);
			} catch (ClassNotFoundException ex) {
				return false; // no on class path
			}

		} else {

			// Use java project to determine if on class path
			IType type = this.javaProject.findType(className, (IProgressMonitor) null);
			return (type != null);
		}
	}

	/**
	 * Checks whether the super type relationship.
	 * 
	 * @param className
	 *            Name of class to extend super type.
	 * @param superTypeName
	 *            Name of super type.
	 * @return <code>true</code> if class extends super type.
	 * @throws Exception
	 *             If fails determining if inheritance relationship.
	 */
	public boolean isSuperType(String className, String superTypeName) throws Exception {

		// Determine if have java project
		if (this.javaProject == null) {

			// Use the provided class path
			ClassLoader classLoader = this.getClassLoader();
			Class<?> child = classLoader.loadClass(className);
			Class<?> superType = classLoader.loadClass(superTypeName);
			return superType.isAssignableFrom(child);

		} else {
			// Use java project to determine if super/child inheritance

			// Obtain the class type
			IType type = this.javaProject.findType(className);
			if (type == null) {
				throw new ClassNotFoundException("Class " + className + " not on class path");
			}

			// Obtain the super type from the project
			IType superType = this.javaProject.findType(superTypeName);
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
	}

	/**
	 * Allows for selecting a {@link Class}.
	 * 
	 * @param className
	 *            Existing class name (or part of). Used to pre-filter the list of
	 *            {@link Class} names to select from. May be <code>null</code> for
	 *            no filtering.
	 * @param parentShell
	 *            Parent {@link Shell} (for SWT dialogs in selecting the
	 *            {@link Class}).
	 * @param superTypeName
	 *            Optional super type name. May be <code>null</code>.
	 * @return Selected {@link Class} name or <code>null</code> if no {@link Class}
	 *         selected.
	 * @throws Exception
	 *             If fails to select a {@link Class}.
	 */
	public String selectClass(String className, Shell parentShell, String superTypeName) throws Exception {
		return this.selectType(className, IJavaElementSearchConstants.CONSIDER_CLASSES, parentShell, superTypeName);
	}

	/**
	 * Allows for selecting a type.
	 * 
	 * @param typeName
	 *            Existing type name (or part of). Used to pre-filter the list of
	 *            type names to select from. May be <code>null</code> for no
	 *            filtering.
	 * @param consideredTypes
	 *            {@link IJavaSearchConstants} value.
	 * @param parentShell
	 *            Parent {@link Shell} (for SWT dialogs in selecting the type).
	 * @param superTypeName
	 *            Optional super type name. May be <code>null</code>.
	 * @return Selected type name or <code>null</code> if no type selected.
	 * @throws Exception
	 *             If fails to select a type.
	 */
	public String selectType(String typeName, int consideredTypes, Shell parentShell, String superTypeName)
			throws Exception {

		// Ensure have the java project
		IJavaProject javaProject = this.getJavaProject();

		// Obtain the search scope
		IJavaSearchScope scope = null;
		if (superTypeName != null) {
			// Obtain the super type from the project
			IType superType = javaProject.findType(superTypeName);
			if (superType != null) {
				// Search for sub type class
				scope = SearchEngine.createStrictHierarchyScope(javaProject, superType, true, true, null);
			}
		}
		if (scope == null) {
			// No hierarchy, so search for any class
			scope = SearchEngine.createJavaSearchScope(new IJavaProject[] { javaProject }, true);
		}

		// Search for any class
		SelectionDialog dialog = JavaUI.createTypeDialog(parentShell, new ProgressMonitorDialog(parentShell), scope,
				consideredTypes, false, typeName == null ? "" : typeName);
		dialog.setBlockOnOpen(true);
		dialog.open();
		Object[] results = dialog.getResult();
		if ((results == null) || (results.length != 1)) {
			return null; // cancel
		}

		// Obtain the selected item
		Object selectedItem = results[0];
		if (selectedItem instanceof IType) {
			// Obtain the selected class
			return ((IType) selectedItem).getFullyQualifiedName();

		} else {
			// Unknown type
			throw new IllegalStateException("Plugin Error: selected item is not of " + IType.class.getName() + " ["
					+ (selectedItem == null ? null : selectedItem.getClass().getName()) + "]");
		}
	}

	/**
	 * Indicates if the resource is on the class path.
	 * 
	 * @param resourcePath
	 *            Path for the resource.
	 * @return <code>true</code> if the resource is on the class path.
	 * @throws Exception
	 *             If fails to determine if resource on class path.
	 */
	public boolean isResourceOnClassPath(String resourcePath) throws Exception {

		// Check on provided class path
		ClassLoader classLoader = this.getClassLoader();
		Enumeration<URL> resources = classLoader.getResources(resourcePath);
		return resources.hasMoreElements();
	}

	/**
	 * Obtains the class path location for the {@link IFile}.
	 * 
	 * @param file
	 *            {@link IFile}.
	 * @return Class path location for the {@link IFile}.
	 */
	public String getClassPathLocation(IFile file) {

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

	/**
	 * Selects a resource on the class path.
	 * 
	 * @param resourcePath
	 *            Existing resource path (or part of). Used to pre-filter the list
	 *            of resources to select from. May be <code>null</code> for no
	 *            filtering.
	 * @param parentShell
	 *            Parent {@link Shell} (for SWT dialogs in selecting the type).
	 * @return Selected resource path or <code>null</code> if no resource selected.
	 * @throws Exception
	 *             If fails to select a resource.
	 */
	public String selectClassPathResource(String resourcePath, Shell parentShell) throws Exception {

		// Ensure have the java project
		IJavaProject javaProject = this.getJavaProject();

		// Strip filter down to just the simple name
		String filter = resourcePath == null ? "" : resourcePath;
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
				javaProject.getProject(), IResource.FILE);
		dialog.setInitialPattern(filter);
		dialog.setBlockOnOpen(true);
		dialog.open();
		Object[] results = dialog.getResult();
		if ((results == null) || (results.length != 1)) {
			return null; // cancel
		}

		// Obtain the selected item
		Object selectedItem = results[0];
		if (selectedItem instanceof IFile) {
			// Specify class path location for file
			IFile file = (IFile) selectedItem;
			return getClassPathLocation(file);
		} else {
			// Unknown type
			throw new IllegalStateException("Plugin Error: selected item is not of " + IFile.class.getName() + " ["
					+ (selectedItem == null ? null : selectedItem.getClass().getName()) + "]");
		}
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler}.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 * @throws Exception
	 *             If fails to extract class path from {@link IJavaProject}.
	 */
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

}