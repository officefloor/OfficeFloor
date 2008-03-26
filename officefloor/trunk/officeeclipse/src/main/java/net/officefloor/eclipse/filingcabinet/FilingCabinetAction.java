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
package net.officefloor.eclipse.windowaction;

import net.officefloor.plugin.filingcabinet.FilingCabinetGenerator;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * {@link IWorkbenchWindowActionDelegate} for the {@link FilingCabinetGenerator}.
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		MessageDialog.openInformation(this.window.getShell(), "Office Floor",
				"TODO do Filing Cabinet generation");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
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

		// TODO remove
		MessageDialog.openInformation(this.window.getShell(), "Office Floor",
				"IPackageFragment selected");

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
		// Nothing to dispose
	}

}
