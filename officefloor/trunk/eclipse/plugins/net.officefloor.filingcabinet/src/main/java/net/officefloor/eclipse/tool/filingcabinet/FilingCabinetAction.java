/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.tool.filingcabinet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.eclipse.bootstrap.Bootstrap;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.filter.AlwaysIncludeInputFilter;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathSelectionInput;
import net.officefloor.plugin.filingcabinet.FilingCabinetGenerator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * {@link IWorkbenchWindowActionDelegate} for the {@link FilingCabinetGenerator}
 * .
 * 
 * @author Daniel
 */
public class FilingCabinetAction implements IWorkbenchWindowActionDelegate {

	/**
	 * {@link IWorkbenchWindow}.
	 */
	private IWorkbenchWindow window;

	/**
	 * {@link IPackageFragment} to generate classes beneath.
	 */
	private IPackageFragment packageFragment = null;

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void run(IAction action) {
		try {
			// Populate the bean to generate the file
			FilingCabinetBean bean = new FilingCabinetBean();
			BeanDialog dialog = new BeanDialog(this.window.getShell(), bean);
			ClasspathSelectionInput selectionInput = new ClasspathSelectionInput();
			selectionInput.getClasspathFilter().addPackageFragmentFilter(
					new AlwaysIncludeInputFilter());
			dialog.registerPropertyInput("Location", selectionInput);
			if (!dialog.populate()) {
				// Cancelled
				return;
			}

			// Obtain the package fragment
			IResource resource = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(new Path(bean.getLocation()));
			IJavaElement javaElement = JavaCore.create(resource);
			IPackageFragment fragment = (IPackageFragment) javaElement;

			// Obtain the package name
			String packageName = fragment.getElementName();

			// Obtain the package fragment root
			IPackageFragmentRoot fragmentRoot = null;
			do {
				// Specify if fragment root
				if (javaElement instanceof IPackageFragmentRoot) {
					fragmentRoot = (IPackageFragmentRoot) javaElement;
				}

				// Search upwards
				javaElement = javaElement.getParent();

			} while (fragmentRoot == null);

			// Obtain the directory for the package fragment root
			File rootDir = fragmentRoot.getResource().getRawLocation().toFile();

			// Obtain the class loader for the particular project
			IProject project = fragmentRoot.getJavaProject().getProject();
			ClassLoader classLoader = ProjectClassLoader.create(project);

			// Create the arguments
			Map<String, String> arguments = new HashMap<String, String>();
			arguments.put("driver", bean.getDatabaseDriver());
			arguments.put("url", bean.getDatabaseUrl());
			arguments.put("username", bean.getDatabaseUserName());
			arguments.put("password", bean.getDatabasePassword());
			arguments.put("location", rootDir.getAbsolutePath());
			arguments.put("package", packageName);

			// Bootstrap the filing cabinet
			Bootstrap.bootstrap(FilingCabinetBootable.class.getName(),
					arguments, classLoader);

			// Refresh the project (make generated files visible)
			project.refreshLocal(IResource.DEPTH_INFINITE, null);

		} catch (Throwable ex) {
			// Indicate error
			MessageDialog.openError(this.window.getShell(), "Filing Cabinet",
					ex.getMessage() + " [" + ex.getClass().getSimpleName()
							+ "]");
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// Only structured selection
		if (!(selection instanceof IStructuredSelection)) {
			this.changeSelection(action, null);
			return;
		}
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;

		// May only select one package fragment
		if (structuredSelection.size() != 1) {
			this.changeSelection(action, null);
			return;
		}

		// Obtain item selected
		Object selectedItem = structuredSelection.getFirstElement();
		if (!(selectedItem instanceof IPackageFragment)) {
			this.changeSelection(action, null);
			return;
		}

		// Package fragment selected
		this.packageFragment = (IPackageFragment) selectedItem;
		this.changeSelection(action, this.packageFragment);
	}

	/**
	 * Handles changing of selection.
	 * 
	 * @param action
	 *            {@link IAction}.
	 * @param packageFragment
	 *            {@link IPackageFragment}.
	 */
	private void changeSelection(IAction action,
			IPackageFragment packageFragment) {
		// Specify the package fragment
		this.packageFragment = packageFragment;
	}

	@Override
	public void dispose() {
		// Nothing to dispose
	}

}