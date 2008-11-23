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
package net.officefloor.eclipse.common.dialog.input.impl;

import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.common.dialog.input.ClasspathFilter;
import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;
import net.officefloor.eclipse.common.dialog.input.filter.AlwaysIncludeInputFilter;
import net.officefloor.eclipse.common.persistence.ProjectConfigurationContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Abstract {@link Input} to obtain an item from the class path.
 * 
 * @author Daniel
 */
public class ClasspathSelectionInput implements Input<Tree> {

	/**
	 * {@link IProject} as root of {@link Tree}. If <code>null</code> includes
	 * all open {@link IProject} instances of the {@link IWorkspaceRoot}.
	 */
	private final IProject project;

	/**
	 * {@link ClasspathFilter}.
	 */
	private final ClasspathFilter filter;

	/**
	 * Initiate with {@link IWorkspaceRoot} as root.
	 */
	public ClasspathSelectionInput() {
		this(null);
	}

	/**
	 * Initiate with {@link IWorkspaceRoot} as root.
	 * 
	 * @param filter
	 *            {@link ClasspathFilter}. May be <code>null</code>.
	 */
	public ClasspathSelectionInput(ClasspathFilter filter) {
		this((IProject) null, filter);
	}

	/**
	 * Initiate with {@link IProject} as root.
	 * 
	 * @param project
	 *            {@link IProject} to be root.
	 * @param filter
	 *            {@link ClasspathFilter}. May be <code>null</code>.
	 */
	public ClasspathSelectionInput(IProject project, ClasspathFilter filter) {
		this.project = project;
		this.filter = (filter == null ? new ClasspathFilter(IFile.class,
				new AlwaysIncludeInputFilter()) : filter);
	}

	/**
	 * Initiate with the {@link IProject} of the {@link IEditorInput} of the
	 * input {@link IEditorPart}.
	 * 
	 * @param editor
	 *            {@link IEditorPart}.
	 * @param filter
	 *            {@link ClasspathFilter}. May be <code>null</code>.
	 */
	public ClasspathSelectionInput(IEditorPart editor, ClasspathFilter filter) {
		this(ProjectConfigurationContext.getProject(editor.getEditorInput()),
				filter);
	}

	/**
	 * Obtains the item that is the root of the tree.
	 * 
	 * @return Item to be the root of the tree.
	 */
	public Object getTreeRootItem() {
		// Obtain the root of the tree
		if (this.project == null) {
			// Use the workspace root
			return ResourcesPlugin.getWorkspace().getRoot();

		} else {
			// Use the specified project
			Object specificProject = ClasspathUtil
					.getSpecificProject(this.project);

			// Return the specific project
			return specificProject;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.dialog.input.Input#buildControl(net.
	 * officefloor.eclipse.common.dialog.input.InputContext)
	 */
	@Override
	public Tree buildControl(final InputContext context) {

		// Create the tree
		final Tree tree = new Tree(context.getParent(), SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 150; // hint on height
		gridData.widthHint = 200; // hint on width
		tree.setLayoutData(gridData);

		// Obtain the root of the tree
		Object treeInput = this.getTreeRootItem();

		// Create the Tree selection
		TreeViewer treeViewer = new TreeViewer(tree);
		treeViewer.setAutoExpandLevel(2);
		treeViewer.setContentProvider(new ClasspathWorkbenchContentProvider());
		treeViewer.setLabelProvider(WorkbenchLabelProvider
				.getDecoratingWorkbenchLabelProvider());
		treeViewer.setInput(treeInput);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// Notify of change
				context.notifyValueChanged(ClasspathSelectionInput.this
						.getValue(tree, context));
			}
		});

		// Return the tree
		return tree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.dialog.input.Input#getValue(org.eclipse
	 * .swt.widgets.Control,
	 * net.officefloor.eclipse.common.dialog.input.InputContext)
	 */
	@Override
	public Object getValue(Tree control, InputContext context) {

		// Obtain the selection from the tree
		TreeItem[] treeItems = control.getSelection();

		// Ensure only one tree item selected
		if (treeItems.length != 1) {
			return null;
		}

		// Obtain the data of selection
		TreeItem treeItem = treeItems[0];
		Object data = treeItem.getData();

		// Return based on specific type
		if (data instanceof IResource) {
			IResource resource = (IResource) data;
			return resource;

		} else if (data instanceof IJavaElement) {
			IJavaElement javaElement = (IJavaElement) data;
			return javaElement;

		} else {
			// Unknown type
			return null;
		}
	}

	/**
	 * Classpath {@link WorkbenchContentProvider}.
	 */
	private class ClasspathWorkbenchContentProvider extends
			WorkbenchContentProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ui.model.BaseWorkbenchContentProvider#getChildren(java
		 * .lang.Object)
		 */
		@Override
		public Object[] getChildren(Object o) {
			try {

				// Obtain the children
				Object[] children = ClasspathUtil.getChildren(o);

				// Filter based on descendants
				Object[] includedChildren = ClasspathSelectionInput.this.filter
						.descendantFilter(children);

				// Return the filtered children
				return includedChildren;

			} catch (Exception ex) {

				// TODO remove
				System.err.println("Failed to get children ["
						+ this.getClass().getName() + "]");
				ex.printStackTrace();

				// Failed to get children
				return new Object[0];
			}
		}
	}

}
